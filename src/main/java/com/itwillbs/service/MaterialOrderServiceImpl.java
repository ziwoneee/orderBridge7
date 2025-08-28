package com.itwillbs.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.itwillbs.domain.ApprovalTokenVO;
import com.itwillbs.domain.MaterialOrderItemVO;
import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialOrderDTO;
import com.itwillbs.dto.PurchaseDraftRequest;
import com.itwillbs.dto.PurchaseDraftResult;
import com.itwillbs.dto.SupplierItemDTO;
import com.itwillbs.mapper.AdminUserMapper;
import com.itwillbs.mapper.MaterialOutboundMapper;
import com.itwillbs.persistence.ApprovalTokenDAO;
import com.itwillbs.persistence.MaterialOrderDAO;
import com.itwillbs.persistence.SupplierDAO;

/**
 * 자재 발주 서비스 구현체
 * - DAO를 호출하여 비즈니스 로직 처리
 */
@Service
public class MaterialOrderServiceImpl implements MaterialOrderService {
	
	private static final Logger logger = LoggerFactory.getLogger(MaterialOrderServiceImpl.class);
	
	@Inject
	private MaterialOrderDAO mOrderDAO;
	
	// MaterialOrderServiceImpl.java 상단
	@Inject
	private MaterialOutboundMapper outboundMapper;
	
	
	@Inject
	private ApprovalTokenDAO approvalTokenDAO;

	@Inject
	private AdminUserMapper adminUserMapper;  

	@Inject
	private MailService mailService;
	
	@Autowired
	private MaterialOrderDAO materialOrderDAO;

	@Inject
	private SupplierDAO supplierDAO;




	// 발주 목록 조회
	@Override
    public List<MaterialOrderVO> getOrderList(SearchCriteria cri) throws Exception {
        return mOrderDAO.getOrderList(cri);
    }

	// 총 건수 조회 (페이징)
    @Override
    public int getTotalCount(SearchCriteria cri) throws Exception {
        return mOrderDAO.getTotalCount(cri);
    }

    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void insertOrder(MaterialOrderDTO orderDTO) throws Exception {

        // 0) workOrderId
        String workOrderId = orderDTO.getOrder().getWorkOrderId();

        // 1) 납기일 검증
        Date today = new Date();
        Date expectedDate = orderDTO.getOrder().getExpectedArrivedDate();
        if (expectedDate != null && expectedDate.before(today)) {
            throw new IllegalArgumentException("납기일은 오늘 이후여야 합니다.");
        }

        // 2) 발주번호 발급
        String newOrderId = mOrderDAO.generateOrderId();
        orderDTO.getOrder().setOrderId(newOrderId);
        if (StringUtils.hasText(workOrderId)) {
            orderDTO.getOrder().setWorkOrderId(workOrderId);
        }

        // 2.5) 아이템 바인딩 검증 로그
        List<MaterialOrderItemVO> items = orderDTO.getOrderItems();
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("발주 항목이 없습니다. (바인딩 실패 가능)");
        }
        logger.info("[insertOrder] items size={}", items.size());
        for (int i = 0; i < items.size(); i++) {
            MaterialOrderItemVO it = items.get(i);
            logger.info(" - [{}] mat={}, qty={}, price={}, total={}",
            	    new Object[]{ i, it.getMaterialId(), it.getOrderQuantity(), it.getUnitPrice(), it.getTotalPrice() });
        }

        // 3) 헤더 insert
        mOrderDAO.insertOrder(orderDTO.getOrder());

        // 4) 아이템 insert
        int index = 1;
        for (MaterialOrderItemVO item : items) {
            if (!StringUtils.hasText(item.getMaterialId()) || item.getOrderQuantity() <= 0) {
                continue; // 안전 필터링
            }

            item.setOrderId(newOrderId);
            if (!StringUtils.hasText(item.getWorkOrderId())) {
                item.setWorkOrderId(workOrderId);
            }
            item.setOrderItemId(newOrderId + "-" + index);
            index++;

            // 총액 보정
            if (item.getTotalPrice() <= 0) {
                long total = (long)item.getOrderQuantity() * (long)item.getUnitPrice();
                item.setTotalPrice(total > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) total);
            }

            mOrderDAO.insertOrderItem(item); // ← 매퍼/DAO 연결 id 확인 필수
        }
    }


	
    
    // 자재명으로 거래처 검색
    @Override
    public List<SupplierItemDTO> searchSuppliersByMaterial(String keyword) {
        return mOrderDAO.searchSuppliersByMaterial(keyword);
    }

	
	
	
    @Override
    public PurchaseDraftResult createDraftFromShortages(PurchaseDraftRequest request) throws Exception {
        logger.info("발주 초안 생성 시작 - workOrderId: {}", request.getWorkOrderId());

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("부족 자재 목록이 없습니다.");
        }

        // [1] 부족자재 맵 (kg/L/개, 소수 허용)
        Map<String, BigDecimal> shortageMap = request.getItems().stream()
            .collect(Collectors.toMap(
                PurchaseDraftRequest.ShortageItem::getMaterialId,
                it -> it.getLackQty() == null ? BigDecimal.ZERO : it.getLackQty(),
                BigDecimal::add
            ));

        List<String> materialIds = new ArrayList<>(shortageMap.keySet());

        // [1-1] 내부사용(N) 먼저 분리
        List<Map<String, Object>> nonPurchList = mOrderDAO.selectNonPurchasableFromList(materialIds);
        Set<String> internalIds = nonPurchList.stream()
            .map(m -> (String) m.get("materialId"))
            .collect(Collectors.toSet());

        // 구매대상(Y)만 남김
        List<String> purchOnlyIds = materialIds.stream()
            .filter(id -> !internalIds.contains(id))
            .collect(Collectors.toList());

        PurchaseDraftResult result = new PurchaseDraftResult();
        // (선택) 내부사용 제외 목록 결과에 전달
        if (!internalIds.isEmpty()) {
            result.setSkippedInternal(new ArrayList<>(internalIds));
        }

        // 구매대상이 하나도 없으면 종료
        if (purchOnlyIds.isEmpty()) {
            result.setUnmappedMaterials(Collections.emptyList());
            result.setOrderId(null);
            logger.info("구매대상(Y) 자재가 없어 발주 생성을 건너뜀. 내부사용 제외: {}", internalIds);
            return result;
        }

        // [2] 매핑 조회
        List<Map<String, Object>> rawMappings = mOrderDAO.selectSupplierItemMappings(purchOnlyIds);
        logger.info("매핑 조회 결과: {}", rawMappings.size());

        if (rawMappings.isEmpty()) {
            result.setUnmappedMaterials(new ArrayList<>(purchOnlyIds));
            return result;
        }

        // [3] 자재별 최저단가 선택
        Map<String, Map<String, Object>> chosenByMaterial = rawMappings.stream()
            .filter(m -> m.get("materialId") != null && m.get("supplierId") != null)
            .collect(Collectors.groupingBy(m -> (String) m.get("materialId")))
            .entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> {
                    Map<String, Object> best = e.getValue().stream()
                        .min(Comparator.comparingInt(m ->
                            ((Number) (m.get("unitPrice") != null ? m.get("unitPrice") : 0)).intValue()
                        ))
                        .orElse(null);
                    if (best == null) return null;
                    if (best.get("unitPrice") == null) best.put("unitPrice", 0);
                    if (best.get("warehouseCode") == null) best.put("warehouseCode", "WH001");
                    return best;
                }
            ));

        // [4] 매핑 안된 자재 정리(구매대상 기준)
        List<String> unmapped = purchOnlyIds.stream()
            .filter(mid -> !chosenByMaterial.containsKey(mid) || chosenByMaterial.get(mid) == null)
            .collect(Collectors.toList());

        // [5] 거래처별 그룹핑
        Map<String, List<Map<String, Object>>> bySupplier = chosenByMaterial.values().stream()
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(m -> (String) m.get("supplierId")));

        // [6] 거래처별 헤더+아이템 생성
        for (Map.Entry<String, List<Map<String, Object>>> entry : bySupplier.entrySet()) {
            String supplierId = entry.getKey();
            List<Map<String, Object>> supplierMappings = entry.getValue();

            try {
                Map<String, Object> orderParams = new HashMap<>();
                orderParams.put("supplierId", supplierId);
                orderParams.put("orderStatus", "초안");
                java.time.LocalDate eta = java.time.LocalDate.now().plusDays(7);
                orderParams.put("expectedArrivedDate", java.sql.Date.valueOf(eta));

                // 담당자 ID (세션/요청 필수)
                String handlerId = java.util.Optional.ofNullable(request.getRequestedBy())
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .orElseThrow(() -> new IllegalStateException("로그인 세션이 만료됐거나 권한이 없습니다. 다시 로그인 후 시도하세요."));
                orderParams.put("handledBy", handlerId);
                logger.info("발주 초안 handledBy = {}", handlerId);

                orderParams.put("note", "작업지시 " + request.getWorkOrderId() + " 부족분 자동 생성");
                orderParams.put("workOrderId", request.getWorkOrderId());

                mOrderDAO.insertOrderHeaderDraft(orderParams);
                String orderId = (String) orderParams.get("orderId");
                if (orderId == null || orderId.isEmpty()) throw new IllegalStateException("orderId 생성 실패");
                if (result.getOrderId() == null) result.setOrderId(orderId);

                List<Map<String, Object>> batch = new ArrayList<>();
                int idx = mOrderDAO.selectNextOrderItemIndex(orderId);

                for (Map<String, Object> m : supplierMappings) {
                    String materialId = (String) m.get("materialId");
                    BigDecimal lackQty = shortageMap.get(materialId);       // kg/L/개
                    if (lackQty == null || lackQty.compareTo(BigDecimal.ZERO) <= 0) continue;

                    // 단가/창고
                    BigDecimal unitPrice = asBD(m.get("unitPrice"), BigDecimal.ZERO);
                    String warehouseCode = (String) m.getOrDefault("warehouseCode", "WH001");

                 // ▲▲▲ 여기부터 교체 시작 ▲▲▲

                 // 1) DB 매핑에서 단위 확인(선택) — 안전 보정용
                 String stockUnit = String.valueOf(m.getOrDefault("stockUnit", "KG")).toUpperCase();

                 // 2) 1팩의 재고단위 수량(“팩→재고단위”) 우선순위:
//                     packQtyBase → convToStock(conv_to_stock) → (최후) 1
                 BigDecimal packQtyBase = asBD(m.get("packQtyBase"), null);
                 if (packQtyBase == null || packQtyBase.compareTo(BigDecimal.ZERO) <= 0) {
                     packQtyBase = asBD(m.get("convToStock"),
                                  asBD(m.get("conv_to_stock"), BigDecimal.ONE));
                 }
                 if (packQtyBase.compareTo(BigDecimal.ZERO) <= 0) {
                     packQtyBase = BigDecimal.ONE;
                 }

                 // 3) 혹시 g/ml 값이 매퍼에서 그대로 온 경우(예: 20000):
//                     이 메서드 *내부에서만* kg/L로 정규화 (수동발주엔 영향 없음)
                 if (("KG".equals(stockUnit) || "L".equals(stockUnit))
                     && packQtyBase.compareTo(new BigDecimal("500")) > 0) {
                     // 500kg/L 넘는 팩은 비정상으로 보고 g/ml → kg/L 보정
                     packQtyBase = packQtyBase.divide(new BigDecimal("1000"), 6, java.math.RoundingMode.HALF_UP);
                 }

                 // 4) 부족(kg/L/EA) → 팩수(상향 반올림)
                 int packs = lackQty
                     .divide(packQtyBase, 0, java.math.RoundingMode.CEILING)
                     .intValueExact();

                 // 5) MOQ/배수 적용 (※ DB가 기본단위로 줄 수도 있으니 팩으로 환산)
                 BigDecimal moqBaseBD = asBD(m.get("minOrderQty"), null);        // 예: 4000(g) or 2(kg) or 1(pack)
                 BigDecimal multBaseBD = asBD(m.get("orderMultiple"), null);
                 int moqPacks = 1;
                 int multPacks = 1;
                 if (moqBaseBD != null && moqBaseBD.compareTo(BigDecimal.ZERO) > 0) {
                     moqPacks = moqBaseBD
                         .divide(packQtyBase, 0, java.math.RoundingMode.CEILING) // 기본단위 → 팩
                         .intValueExact();
                 }
                 if (multBaseBD != null && multBaseBD.compareTo(BigDecimal.ZERO) > 0) {
                     multPacks = multBaseBD
                         .divide(packQtyBase, 0, java.math.RoundingMode.CEILING) // 기본단위 → 팩
                         .intValueExact();
                 }
                 if (packs < moqPacks) packs = moqPacks;
                 if (multPacks > 1) packs = ((packs + multPacks - 1) / multPacks) * multPacks;


                 // 6) 과금 수량(단가 단위 기준) 계산
//                     - priceUnit이 PACK이면 팩 개수 그대로
//                     - 그 외(KG/L/EA 등)면 "팩×(팩→과금단위 환산)" 사용
                 String priceUnit = String.valueOf(m.getOrDefault("priceUnit", "BASE")).toUpperCase();
                 BigDecimal convPerPackBilling = asBD(m.get("convPerPackBilling"), null);
                 if (convPerPackBilling == null || convPerPackBilling.compareTo(BigDecimal.ZERO) <= 0) {
                     // 별도 과금 환산이 없으면 convToStock으로 대체(대부분 KG/L/EA 과금 케이스)
                     convPerPackBilling = asBD(m.get("convToStock"),
                                         asBD(m.get("conv_to_stock"), BigDecimal.ONE));
                     if (convPerPackBilling.compareTo(BigDecimal.ZERO) <= 0) convPerPackBilling = BigDecimal.ONE;
                 }
                 BigDecimal billedQty = "PACK".equals(priceUnit)
                     ? new BigDecimal(packs)
                     : new BigDecimal(packs).multiply(convPerPackBilling);

                 // ▼▼▼ 여기까지 교체 끝 ▼▼▼

                        
                        

                    // 금액은 정수원(원)으로 반올림
                    BigDecimal totalPrice = billedQty.multiply(unitPrice).setScale(0, java.math.RoundingMode.HALF_UP);

                    Map<String, Object> item = new HashMap<>();
                    item.put("orderItemId", orderId + "-" + String.format("%03d", idx++));
                    item.put("orderId", orderId);
                    item.put("materialId", materialId);
                    item.put("orderQuantity", packs);                         // 저장은 팩 개수
                    item.put("unitPrice", unitPrice);                         // DECIMAL 컬럼 권장
                    item.put("totalPrice", totalPrice);                       // DECIMAL 또는 BIGINT 컬럼
                    item.put("warehouseCode", warehouseCode);
                    item.put("workOrderId", request.getWorkOrderId());
                    batch.add(item);
                }

                if (!batch.isEmpty()) {
                    mOrderDAO.insertOrderItemsBatch(batch);
                    logger.info("발주 초안 생성 완료 - orderId: {}, items: {}", orderId, batch.size());
                } else {
                    logger.warn("거래처 {} 에 대해 생성할 아이템이 없습니다.", supplierId);
                }

            } catch (Exception e) {
                logger.error("거래처 {} 발주 초안 생성 실패", supplierId, e);
                entry.getValue().forEach(m -> unmapped.add((String) m.get("materialId")));
            }
        }

        // [7] 결과 정리
        result.setUnmappedMaterials(unmapped);
        if (result.getOrderId() == null && unmapped.isEmpty() && internalIds.isEmpty()) {
            throw new RuntimeException("발주 초안 생성에 실패했습니다.");
        }

        outboundMapper.updateWorkOrderShortageStatus(request.getWorkOrderId(), "CHECKED");
        return result;
    }


    
    
    /* 발주 초안에서 요청 */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void submitOrderRequest(String orderId) throws Exception {
        // 1) 로드 & 기본 검증
        List<Map<String,Object>> rows = mOrderDAO.selectDraftWithItems(orderId);
        if (rows == null || rows.isEmpty()) {
            throw new IllegalStateException("발주가 존재하지 않거나 항목이 없습니다.");
        }
        String status = String.valueOf(rows.get(0).get("orderStatus"));
        if (!"초안".equals(status)) {
            throw new IllegalStateException("현재 상태(" + status + ")에서는 발주요청으로 전환할 수 없습니다.");
        }
        String supplierId = String.valueOf(rows.get(0).get("supplierId"));
        if (supplierId == null || supplierId.isEmpty()) {
            throw new IllegalStateException("공급처가 지정되어 있지 않습니다.");
        }

        // 항목 유효성 (수량/단가)
        for (Map<String,Object> r : rows) {
            int qty  = ((Number)r.get("orderQuantity")).intValue();
            int unit = ((Number)r.get("unitPrice")).intValue();
            if (qty <= 0)  throw new IllegalStateException("수량이 0 이하인 항목이 있습니다.");
            if (unit < 0)  throw new IllegalStateException("단가가 음수인 항목이 있습니다.");
        }

        // 2) (선택) 합계 조회해서 로깅/표시용
        mOrderDAO.selectItemsTotal(orderId); // 필요 시 반환값 사용

        // 3) 상태 전이
        int updated = mOrderDAO.updateOrderToRequested(orderId);
        if (updated == 0) {
            throw new IllegalStateException("상태가 변경되어 처리할 수 없습니다. 새로고침 후 다시 시도하세요.");
        }
    }
    
    
    /**
     * 발주 상세
     */
    // 주문 헤더
    @Override
    public Map<String, Object> getOrderHeader(String orderId) throws Exception {
        Map<String,Object> h = mOrderDAO.selectOrderHeader(orderId);
        if (h == null) throw new IllegalStateException("발주가 존재하지 않습니다.");

        String handlerName   = asStr(h.get("handlerName"));
        String handledBy     = asStr(h.get("handledBy"));
        String createdByName = asStr(h.get("createdByName"));
        String createdBy     = asStr(h.get("createdBy"));

        // 1) handlerName 비면 handledBy(ID)로 admin_user에서 보충
        if (isEmpty(handlerName) && !isEmpty(handledBy)) {
            var u = adminUserMapper.findByAdminId(handledBy);
            if (u != null && !isEmpty(u.getName())) handlerName = u.getName();
        }

        // 2) 그래도 비면 createdByName/ID로 보충
        if (isEmpty(handlerName)) {
            if (!isEmpty(createdByName)) handlerName = createdByName;
            else if (!isEmpty(createdBy)) {
                var u2 = adminUserMapper.findByAdminId(createdBy);
                if (u2 != null && !isEmpty(u2.getName())) handlerName = u2.getName();
            }
        }

        if (!isEmpty(handlerName)) {
            h.put("handlerName", handlerName);
            // 입고쪽 JS 폴백 키도 같이 채워두면 더 안전
            if (h.get("handledByName") == null) h.put("handledByName", handlerName);
        }

        return h;
    }
    
    // --- 아래 헬퍼 2개를 클래스 안에 추가 ---
    private static boolean isEmpty(String s) { return s == null || s.trim().isEmpty(); }
    private static String asStr(Object v) { return v == null ? null : String.valueOf(v); }
    
    // 주문 아이템 목록
    @Override
    public List<Map<String, Object>> getOrderItems(String orderId) throws Exception {
        return mOrderDAO.selectOrderItems(orderId);
    }


    /**
     * 발주 상태별 탭기능 카운트
     */
    @Override
    public int getCountByStatus(String status) {
        return mOrderDAO.getCountByStatus(status);
    }
    
    @Override
    public Map<String, Integer> getStatusCounts() {
        return mOrderDAO.getStatusCounts(); // DAO에서 이미 처리된 Map<String, Integer> 반환
    }    
    
    /**
     * 발주요청 시 거래처 승인 토큰 생성 + 이메일 전송
     */
    @Override
    public void sendApprovalRequest(String orderId) {
        // 1. 발주 정보 조회
    	MaterialOrderVO order = materialOrderDAO.findById(orderId);

        if (order == null) {
            throw new IllegalArgumentException("해당 발주 정보가 존재하지 않습니다.");
        }

        // 2. 거래처 이메일 조회
        String supplierEmail = supplierDAO.findEmailById(order.getSupplierId());
        if (supplierEmail == null || supplierEmail.isEmpty()) {
            throw new IllegalArgumentException("거래처 이메일을 찾을 수 없습니다.");
        }

        // 3. 토큰 생성
        String tokenId = UUID.randomUUID().toString();

        ApprovalTokenVO token = new ApprovalTokenVO();
        token.setTokenId(tokenId);
        token.setOrderId(order.getOrderId());
        token.setSupplierId(order.getSupplierId());
        token.setTokenType("ORDER_APPROVAL");
        token.setUsed(false);
        token.setExpiresAt(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));

        // 4. DB 저장
        approvalTokenDAO.insert(token);

        // 5. 승인 링크 생성
        String approvalLink = "http://c7d2503t1p2.itwillbs.com/approval/confirm?token=" + tokenId;

        // 6. 메일 전송
        String subject = "[OrderBridge] 발주 승인 요청";
        String body = "안녕하세요.\n\n다음 발주 요청을 승인 또는 거절해주세요.\n\n"
                    + "발주번호: " + order.getOrderId() + "\n"
                    + "납기일자: " + order.getExpectedArrivedDate() + "\n\n"
                    + "승인 링크: " + approvalLink + "\n\n"
                    + "유효기간: 24시간";

        mailService.sendMail(supplierEmail, subject, body);
    }
    //협력사 승인상태 변경
    @Override
    public MaterialOrderVO findByOrderId(String orderId) {
        return materialOrderDAO.findById(orderId);
    }
    
    
    private static BigDecimal asBD(Object v, BigDecimal def){
        if (v instanceof BigDecimal) return (BigDecimal) v;
        if (v instanceof Number) return new BigDecimal(((Number) v).toString());
        try { return v == null ? def : new BigDecimal(v.toString()); } catch (Exception e) { return def; }
    }
    private static int asInt(Object v, int def){
        if (v instanceof Number) return ((Number)v).intValue();
        try { return v == null ? def : Integer.parseInt(v.toString()); } catch(Exception e){ return def; }
    }


    
}
    
    
    
    

