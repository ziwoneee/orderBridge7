package com.itwillbs.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

import com.itwillbs.domain.ApprovalTokenVO;
import com.itwillbs.domain.MaterialOrderItemVO;
import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialOrderDTO;
import com.itwillbs.dto.PurchaseDraftRequest;
import com.itwillbs.dto.PurchaseDraftRequest.ShortageItem;
import com.itwillbs.dto.PurchaseDraftResult;
import com.itwillbs.dto.SupplierItemDTO;
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

    
 // 발주 등록
    @Override
    public void insertOrder(MaterialOrderDTO orderDTO) throws Exception {

        // 0) workOrderId 확보 (폼에서 order.workOrderId로 넘어오는 값)
        String workOrderId = orderDTO.getOrder().getWorkOrderId(); // 없으면 null 허용

        // 1) 납기일 유효성 검사 (시간오차 방지용으로 날짜만 비교 권장)
        Date today = new Date();
        Date expectedDate = orderDTO.getOrder().getExpectedArrivedDate();
        if (expectedDate != null && expectedDate.before(today)) {
            throw new IllegalArgumentException("납기일은 오늘 이후여야 합니다.");
        }

        // 2) 발주번호 생성
        String newOrderId = mOrderDAO.generateOrderId();
        orderDTO.getOrder().setOrderId(newOrderId);

        // 🔹 헤더에 work_order_id 주입
        if (workOrderId != null && !workOrderId.isEmpty()) {
            orderDTO.getOrder().setWorkOrderId(workOrderId);
        }

        // 3) order 테이블 insert (Mapper에 work_order_id 컬럼 이미 추가되어 있어야 함)
        mOrderDAO.insertOrder(orderDTO.getOrder());

        // 4) order_item 테이블 insert
        int index = 1;
        for (MaterialOrderItemVO item : orderDTO.getOrderItems()) {
            item.setOrderId(newOrderId); // FK

            // 🔹 아이템에도 동일 work_order_id 주입
            if (item.getWorkOrderId() == null || item.getWorkOrderId().isEmpty()) {
                item.setWorkOrderId(workOrderId); // 헤더와 통일
            }

            // order_item_id 생성
            item.setOrderItemId(newOrderId + "-" + index);
            index++;

            mOrderDAO.insertOrderItem(item); // Mapper에 work_order_id 포함되어 있어야 함
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

        // [1] 부족 자재 Map
        Map<String, Integer> shortageMap = request.getItems().stream()
            .collect(Collectors.toMap(
                PurchaseDraftRequest.ShortageItem::getMaterialId,
                PurchaseDraftRequest.ShortageItem::getLackQty,
                Integer::sum));

        List<String> materialIds = new ArrayList<>(shortageMap.keySet());

        // ✅ [1-1] 내부사용(N) 먼저 분리
        List<Map<String, Object>> nonPurchList = mOrderDAO.selectNonPurchasableFromList(materialIds);
        Set<String> internalIds = nonPurchList.stream()
            .map(m -> (String) m.get("materialId"))
            .collect(Collectors.toSet());

        // 구매대상(Y)만 남김
        List<String> purchOnlyIds = materialIds.stream()
            .filter(id -> !internalIds.contains(id))
            .collect(Collectors.toList());

        PurchaseDraftResult result = new PurchaseDraftResult();
        // (선택) 결과에 내부사용 사유로 별도 전달
        if (!internalIds.isEmpty() && result.getSkippedInternal() != null) {
            result.setSkippedInternal(new ArrayList<>(internalIds));
        }

        // ✅ 구매대상 아이디가 하나도 없으면 여기서 조용히 종료(예: 전부 내부사용일 때)
        if (purchOnlyIds.isEmpty()) {
            result.setUnmappedMaterials(Collections.emptyList());
            result.setOrderId(null); // 필요시 null 허용, 호출부에서 "생성할 항목 없음" 처리
            logger.info("구매대상(Y) 자재가 없어 발주 생성을 건너뜀. 내부사용 제외: {}", internalIds);
            return result;
        }

        // [2] 매핑 조회 (⚠️ 기존 materialIds -> purchOnlyIds 로 변경)
        List<Map<String, Object>> rawMappings = mOrderDAO.selectSupplierItemMappings(purchOnlyIds);
        logger.info("매핑 조회 결과: {}", rawMappings.size());

        // ⚠️ 전부 내부사용이 아니고, 구매대상인데 매핑이 0이면 그때만 오류/스킵 처리
        if (rawMappings.isEmpty()) {
            // unmapped = 구매대상인데 거래처 없는 자재 전체
            result.setUnmappedMaterials(new ArrayList<>(purchOnlyIds));
            // 필요 시 예외 대신 결과만 반환하고 프론트에서 안내
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

        // ✅ [4] unmapped 계산도 구매대상 집합만 기준
        List<String> unmapped = purchOnlyIds.stream()
            .filter(mid -> !chosenByMaterial.containsKey(mid) || chosenByMaterial.get(mid) == null)
            .collect(Collectors.toList());

        // [5] 거래처별 그룹핑
        Map<String, List<Map<String, Object>>> bySupplier = chosenByMaterial.values().stream()
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(m -> (String) m.get("supplierId")));

        // [6] 거래처별 헤더+아이템 생성 (기존 그대로)
        for (Map.Entry<String, List<Map<String, Object>>> entry : bySupplier.entrySet()) {
            String supplierId = entry.getKey();
            List<Map<String, Object>> supplierMappings = entry.getValue();

            try {
            	Map<String, Object> orderParams = new HashMap<>();
            	orderParams.put("supplierId", supplierId);
            	orderParams.put("orderStatus", "초안");
            	java.time.LocalDate eta = java.time.LocalDate.now().plusDays(7);
            	orderParams.put("expectedArrivedDate", java.sql.Date.valueOf(eta));

            	// ★ 담당자 ID 결정 (request DTO에 필드가 없으면 임시로 'admin' 등 ID 문자열 사용)
            	String requestedBy = (request.getRequestedBy() != null && !request.getRequestedBy().isEmpty())
            	        ? request.getRequestedBy() : "system";

    	        orderParams.put("handledBy",   requestedBy);
    	        
            	orderParams.put("note", "작업지시 " + request.getWorkOrderId() + " 부족분 자동 생성");
            	orderParams.put("workOrderId", request.getWorkOrderId()); // (선택) 헤더에도 연계

                mOrderDAO.insertOrderHeaderDraft(orderParams);
                String orderId = (String) orderParams.get("orderId");
                if (orderId == null || orderId.isEmpty()) throw new IllegalStateException("orderId 생성 실패");
                if (result.getOrderId() == null) result.setOrderId(orderId);

                List<Map<String, Object>> batch = new ArrayList<>();
                int idx = mOrderDAO.selectNextOrderItemIndex(orderId);

                for (Map<String, Object> m : supplierMappings) {
                    String materialId = (String) m.get("materialId");
                    Integer lack = shortageMap.get(materialId);
                    if (lack == null || lack <= 0) continue;

                    int unitPrice = ((Number) m.get("unitPrice")).intValue();
                    String warehouseCode = (String) m.get("warehouseCode");

                    Map<String, Object> item = new HashMap<>();
                    item.put("orderItemId", orderId + "-" + String.format("%03d", idx++));
                    item.put("orderId", orderId);
                    item.put("materialId", materialId);
                    item.put("orderQuantity", lack);
                    item.put("unitPrice", unitPrice);
                    item.put("totalPrice", unitPrice * lack);
                    item.put("warehouseCode", warehouseCode);
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
        return h;
    }
    
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
        String approvalLink = "http://localhost:8088/approval/confirm?token=" + tokenId;

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

    
}
    
    
    
    

