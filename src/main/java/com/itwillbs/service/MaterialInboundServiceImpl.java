package com.itwillbs.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.itwillbs.domain.*;
import com.itwillbs.dto.*;
import com.itwillbs.persistence.MaterialInboundDAO;

@Service
public class MaterialInboundServiceImpl implements MaterialInboundService {

    @Inject
    private MaterialInboundDAO miDAO;

    private static final Logger logger = LoggerFactory.getLogger(MaterialInboundServiceImpl.class);

    /**
     * 입고 목록 조회 (조건 기반 페이징)
     */
    @Override
    public List<MaterialInboundSummaryDTO> getInboundList(SearchCriteria cri) throws Exception {
        return miDAO.getInboundList(cri);
    }

    /**
     * 입고 목록 전체 건수 조회
     */
    @Override
    public int getInboundListCount(SearchCriteria cri) throws Exception {
        return miDAO.getInboundListCount(cri);
    }

    /**
     * 아직 입고 테이블에 등록되지 않은 발주건들 조회
     */
    @Override
    public List<MaterialOrderVO> getPendingInboundOrders() throws Exception {
        return miDAO.selectPendingInboundOrders();
    }

    /**
     * 한 번도 입고된 적 없는 발주 항목 기준 - 미입고 목록 조회 (페이징 포함)
     */
    @Override
    public List<UnreceivedOrderDTO> getUnreceivedOrdersPaging(SearchCriteria cri) throws Exception {
        return miDAO.getUnreceivedOrdersPaging(cri);
    }

    /**
     * 미입고 발주건 목록 전체 건수
     */
    @Override
    public int getUnreceivedOrdersCount() throws Exception {
        return miDAO.getUnreceivedOrdersCount();
    }
    
    
    private String resolveUser(String user) {   // 이름도 user로 바꾸면 헷갈림↓ 줄어듦
        return (user != null && !user.trim().isEmpty()) ? user : "system";
    }
    

    /**
     * 전체 미입고 발주건을 입고 테이블(material_inbound / material_inbound_item)에 등록
     * - 발주ID별로 입고ID 자동 생성
     * - 항목은 0건 입고상태로 등록
     */
    @Override
    public void insertUnreceivedOrders(String handledBy) throws Exception {
        List<MaterialOrderItemVO> unreceivedItems = miDAO.getUnreceivedOrderItems();

        // 발주ID 기준으로 그룹핑
        Map<String, List<MaterialOrderItemVO>> grouped = unreceivedItems.stream()
            .collect(Collectors.groupingBy(MaterialOrderItemVO::getOrderId));

        for (String orderId : grouped.keySet()) {
            String inboundId = miDAO.generateInboundId();

            // 입고 마스터 등록
            MaterialInboundVO inbound = new MaterialInboundVO();
            inbound.setInboundId(inboundId);
            inbound.setOrderId(orderId);
            inbound.setInboundStatus("미입고");
            inbound.setInboundDate(null);
            inbound.setHandledBy(resolveUser(handledBy));
            miDAO.insertMaterialInbound(inbound);

            // 각 발주 항목을 입고 항목으로 등록 (초기 수량 0)
            for (MaterialOrderItemVO item : grouped.get(orderId)) {
                MaterialInboundItemVO itemVO = new MaterialInboundItemVO();
                itemVO.setInboundId(inboundId);
                itemVO.setOrderItemId(item.getOrderItemId());
                itemVO.setMaterialId(item.getMaterialId());
                itemVO.setOrderQuantity(item.getOrderQuantity());
                itemVO.setQuantity(0);
                miDAO.insertMaterialInboundItem(itemVO);
            }
        }
    }

    /**
     * 선택한 발주ID 배열만 입고 데이터 등록
     */
    @Override
    public void insertSelectedUnreceivedOrders(String[] orderIds, String handledBy) throws Exception {
        Map<String, Boolean> uniqueOrderMap = new HashMap<>();
        for (String orderId : orderIds) {
            if (orderId != null && !orderId.trim().isEmpty()) {
                uniqueOrderMap.put(orderId.trim(), true);
            }
        }

        for (String orderId : uniqueOrderMap.keySet()) {
            try {
                List<MaterialOrderItemVO> orderItems = miDAO.getUnreceivedOrderItemsByOrderId(orderId);
                if (!orderItems.isEmpty()) {
                    String inboundId = miDAO.generateInboundId();

                    // 마스터 등록
                    MaterialInboundVO inbound = new MaterialInboundVO();
                    inbound.setInboundId(inboundId);
                    inbound.setOrderId(orderId);
                    inbound.setInboundStatus("미입고");
                    inbound.setInboundDate(null);
                    inbound.setHandledBy(resolveUser(handledBy));
                    miDAO.insertMaterialInbound(inbound);

                    // 항목 등록 (ID 자동 생성)
                    String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
                    String prefix = "IN-RM-ITEM-" + today + "-";
                    int seq = 1;

                    String latestId = miDAO.getLatestInboundItemId(prefix);
                    if (latestId != null) {
                        String[] parts = latestId.split("-");
                        seq = Integer.parseInt(parts[4]) + 1;
                    }

                    for (MaterialOrderItemVO item : orderItems) {
                        String inboundItemId = String.format("%s%03d", prefix, seq++);

                        MaterialInboundItemVO itemVO = new MaterialInboundItemVO();
                        itemVO.setInboundItemId(inboundItemId);
                        itemVO.setInboundId(inboundId);
                        itemVO.setOrderItemId(item.getOrderItemId());
                        itemVO.setMaterialId(item.getMaterialId());
                        itemVO.setOrderQuantity(item.getOrderQuantity());
                        itemVO.setQuantity(0);
                        itemVO.setInboundStatus("미입고");
                        itemVO.setUnitPrice(item.getUnitPrice());
                        itemVO.setTotalPrice(0);
                        itemVO.setCreatedDate(new Date());
                        itemVO.setUpdatedDate(new Date());

                        miDAO.insertMaterialInboundItem(itemVO);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("발주 ID " + orderId + " 처리 실패: " + e.getMessage());
            }
        }
    }
    
    /**
     * 단위 환산 유틸리티 메서드 - 더 포괄적인 단위 보정
     */
    private double calculateStockPerPack(double convToStock, double packQty, double convToBase, 
                                       String priceUnit, String stockUnit) {
        // 기본값 설정
        double stockPerPack = (convToStock > 0 ? convToStock
                            : (packQty > 0 ? packQty : Math.max(convToBase, 1d)));
        
        // 단위 정규화
        String normalizedPriceUnit = normalizePriceUnit(priceUnit);
        String normalizedStockUnit = normalizeStockUnit(stockUnit);
        
        // kg -> g 변환이 필요한 경우
        if ("KG".equals(normalizedPriceUnit) && "G".equals(normalizedStockUnit)) {
            // conv_to_stock 값이 1000보다 작으면 kg 기준값으로 보고 1000 곱하기
            if (stockPerPack < 1000d) {
                stockPerPack *= 1000d;
                logger.info("단위 보정 적용: kg->g, 보정 전: {}, 보정 후: {}", 
                           stockPerPack/1000, stockPerPack);
            }
        }
        // L -> ml 변환이 필요한 경우  
        else if ("L".equals(normalizedPriceUnit) && "ML".equals(normalizedStockUnit)) {
            if (stockPerPack < 1000d) {
                stockPerPack *= 1000d;
                logger.info("단위 보정 적용: L->ml, 보정 전: {}, 보정 후: {}", 
                           stockPerPack/1000, stockPerPack);
            }
        }
        // 같은 단위인데 값이 이상하게 작은 경우 (데이터 오류 가능성)
        else if (normalizedPriceUnit.equals(normalizedStockUnit) && stockPerPack < 0.1d) {
            stockPerPack = Math.max(convToBase, 1d);
            logger.warn("단위 보정: 동일 단위인데 환산값이 너무 작음. convToBase로 대체: {}", stockPerPack);
        }
        
        return stockPerPack;
    }

    /**
     * 가격 단위 정규화
     */
    private String normalizePriceUnit(String priceUnit) {
        if (priceUnit == null) return "EA";
        
        String unit = priceUnit.toUpperCase().trim();
        switch (unit) {
            case "KG": case "KILOGRAM": return "KG";
            case "G": case "GRAM": return "G";
            case "L": case "LITER": case "LITRE": return "L";
            case "ML": case "CC": case "MILLILITER": return "ML";
            case "EA": case "EACH": case "PCS": case "개": return "EA";
            case "PACK": case "팩": return "PACK";
            default: return unit;
        }
    }

    /**
     * 재고 단위 정규화
     */
    private String normalizeStockUnit(String stockUnit) {
        if (stockUnit == null) return "EA";
        
        String unit = stockUnit.toUpperCase().trim();
        switch (unit) {
            case "KG": case "KILOGRAM": return "KG";
            case "G": case "GRAM": return "G";
            case "L": case "LITER": case "LITRE": return "L";
            case "ML": case "CC": case "MILLILITER": return "ML";
            case "EA": case "EACH": case "PCS": case "개": return "EA";
            default: return unit;
        }
    }
    

    /**
     * 특정 입고건에 포함된 자재들 전체 입고 처리
     * - 수량 > 0 인 자재만 처리
     * - 재고 반영 (기존 있으면 update, 없으면 insert)
     * - 상태 변경, 입고일자 처리 포함
     */
    /**
     * 일괄 입고 처리 메서드도 동일하게 수정
     */
    @Override
    public void processInbound(String inboundId) throws Exception {
        List<MaterialInboundItemVO> itemList = miDAO.getInboundItemsByInboundId(inboundId);

        boolean anyProcessed = false;

        for (MaterialInboundItemVO item : itemList) {
            if (item.getQuantity() > 0) {
                int packs = item.getQuantity();

                // 메타 조회
                Map<String,Object> meta = miDAO.getPackMetaByOrderItemId(item.getOrderItemId());
                String priceUnit   = String.valueOf(meta.getOrDefault("priceUnit", "BASE")).toUpperCase();
                String stockUnit   = String.valueOf(meta.getOrDefault("stockUnit", "EA")).toUpperCase();
                double convToBase  = toDouble(meta.get("convToBase"),  1d);
                double convToStock = toDouble(meta.get("convToStock"), 1d);
                double packQty     = toDouble(meta.get("packQty"),    -1d);

                // ★ 개선된 재고 증가량 계산
                double stockPerPack = calculateStockPerPack(convToStock, packQty, convToBase, priceUnit, stockUnit);
                long stockQty = Math.round(packs * stockPerPack);
                

                boolean exists = miDAO.checkInventoryExists(item.getMaterialId(), item.getWarehouseCode());
                if (exists) {
                    miDAO.updateInventoryQuantity(item.getMaterialId(), item.getWarehouseCode(), (int) stockQty);
                } else {
                    miDAO.insertInventory(item.getMaterialId(), item.getWarehouseCode(), (int) stockQty);
                }

                miDAO.markItemAsReceived(item.getInboundItemId());
                anyProcessed = true;
            }
        }

        if (!anyProcessed) {
            throw new Exception("입고 가능한 수량이 있는 자재가 없습니다.");
        }

        miDAO.updateInboundMasterStatus(inboundId);
    }


    @Override
    public void processInboundItem(MaterialInboundItemDTO dto, String handledBy) throws Exception {

        // 0-0) 최초 처리자 보정
        if (handledBy != null && !handledBy.trim().isEmpty()) {
            miDAO.updateInboundHandledByIfBlank(dto.getInboundId(), handledBy.trim());
        }

        // [ADD] 창고 코드 자동 보정
        if (dto.getWarehouseCode() == null || dto.getWarehouseCode().isEmpty()) {
            String wh = resolveWarehouseCode(dto.getMaterialId(), dto.getOrderItemId());
            dto.setWarehouseCode(wh);
        }

        // 0) 기본 검증
        if (dto.getQuantity() <= 0) throw new Exception("입고 수량이 0 이하인 자재가 있습니다.");
        if (dto.getLotNo() == null || dto.getLotNo().isEmpty()) throw new Exception("LOT 번호가 누락되었습니다.");
        if (dto.getExpirationDate() == null) throw new Exception("유통기한이 누락되었습니다.");
        if (dto.getInboundId() == null || dto.getInboundId().isEmpty()) throw new Exception("inboundId 누락");
        if (dto.getOrderItemId() == null || dto.getOrderItemId().isEmpty()) throw new Exception("orderItemId 누락");
        if (dto.getMaterialId() == null || dto.getMaterialId().isEmpty()) throw new Exception("materialId 누락");
        if (dto.getWarehouseCode() == null || dto.getWarehouseCode().isEmpty()) throw new Exception("warehouseCode 누락");

        // 1) 발주 항목 조회
        MaterialOrderItemVO orderItem = miDAO.getOrderItemById(dto.getOrderItemId());
        if (orderItem == null) throw new RuntimeException("orderItem 조회 결과가 null입니다: " + dto.getOrderItemId());

        // null 안전
        Integer orderQtyObj       = orderItem.getOrderQuantity();
        Integer orderUnitPriceObj = orderItem.getUnitPrice();
        if (orderQtyObj == null || orderQtyObj <= 0) {
            throw new Exception("발주 수량 정보가 올바르지 않습니다. 발주항목: " + dto.getOrderItemId() + ", 수량: " + orderQtyObj);
        }
        final int orderQty  = (orderQtyObj == null) ? 0 : orderQtyObj;
        final int unitPrice = (orderUnitPriceObj == null) ? 0 : orderUnitPriceObj;

        // === (A) 단위/환산 메타 조회 ===
        Map<String,Object> meta = miDAO.getPackMetaByOrderItemId(dto.getOrderItemId());
        String priceUnit   = String.valueOf(meta.getOrDefault("priceUnit", "BASE")).toUpperCase();
        String stockUnit   = String.valueOf(meta.getOrDefault("stockUnit", "EA")).toUpperCase();
        double convToBase  = toDouble(meta.get("convToBase"),  1d);   // 1PACK이 과금단위(KG/L/EA 등)로 몇
        double convToStock = toDouble(meta.get("convToStock"), 1d);   // 1PACK이 재고단위로 몇
        double packQty     = toDouble(meta.get("packQty"),    -1d);   // 선택값(있으면 우선)

        // 2) 이번에 입고할 수량 (PACK)
        int packsThisTime = dto.getQuantity();

        // 3) 현재까지 누적 입고(=PACK) (null → 0)
        Integer totalReceivedObj = miDAO.getTotalReceivedQtyByOrderItemId(dto.getOrderItemId());
        int totalReceivedQty = (totalReceivedObj == null) ? 0 : totalReceivedObj;

        // 4) 누적 계산 및 초과 체크 (PACK 단위끼리 비교)
        int newTotalPacks = totalReceivedQty + packsThisTime;
        if (newTotalPacks > orderQty) {
            throw new Exception(String.format(
                    "입고 수량이 발주 수량을 초과합니다. (발주: %d, 기입고: %d, 입고요청: %d, 초과: %d)",
                    orderQty, totalReceivedQty, packsThisTime, newTotalPacks - orderQty
            ));
        }

        // 5) 상태
        String newStatus = (newTotalPacks >= orderQty) ? "입고완료" : "부분입고";

        // === (B) 금액 계산: 단가 단위에 맞춰 과금수량 산출 (×1000 같은 추가 변환 금지) ===
        double billedPerPack;
        if ("KG".equals(priceUnit) || "L".equals(priceUnit)) {
            billedPerPack = convToBase;                                     // 예: 1PACK = 20kg → 20
        } else if ("PACK".equals(priceUnit)) {
            billedPerPack = 1d;                                             // 팩 자체 과금
        } else {
            billedPerPack = (packQty > 0 ? packQty : Math.max(convToBase, 1d)); // EA/BUNDLE 등
        }
        double billedQtyThisTime  = packsThisTime * billedPerPack;
        long   totalPriceThisTime = Math.round(billedQtyThisTime * unitPrice);

        dto.setUnitPrice(unitPrice);
        dto.setTotalPrice((int) totalPriceThisTime);

        // 6) 입고 항목 업데이트 (DB에는 누적 PACK 수량으로 저장)
        dto.setQuantity(newTotalPacks);          // ★ PACK 누적
        dto.setInboundStatus(newStatus);
        miDAO.updateInboundItem(dto);

     // === (C) 재고 반영 — ★ 개선된 재고단위 보정 ★
        double stockPerPack = calculateStockPerPack(convToStock, packQty, convToBase, priceUnit, stockUnit);

        long stockQtyThisTime = Math.round(packsThisTime * stockPerPack);
        

        if (miDAO.checkInventoryExists(dto.getMaterialId(), dto.getWarehouseCode())) {
            miDAO.updateInventoryQuantity(dto.getMaterialId(), dto.getWarehouseCode(), (int) stockQtyThisTime);
        } else {
            MaterialInventoryVO vo = new MaterialInventoryVO();
            vo.setInventoryId(generateInventoryId());
            vo.setMaterialId(dto.getMaterialId());
            vo.setQuantity((int) stockQtyThisTime);   // ★ stock_unit 기준 수량
            vo.setLotNo(dto.getLotNo());
            vo.setExpirationDate(dto.getExpirationDate());
            vo.setReceivedDate(new Date());
            vo.setWarehouseCode(dto.getWarehouseCode());
            vo.setStatus("정상");
            vo.setInventoryStatus("보관중");
            miDAO.insertInventory(vo);
        }

        // 8) 마스터/발주 상태 갱신
        miDAO.updateInboundMasterStatus(dto.getInboundId());
        String orderId = miDAO.selectOrderIdByInboundId(dto.getInboundId());
        miDAO.updateOrderStatusToCompletedIfAllInboundDone(orderId);
    }



    /** null/타입 혼합 방지용 유틸 */
    private static double toDouble(Object v, double def) {
        if (v == null) return def;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception ignore) { return def; }
    }



    /**
     * 입고 마스터 및 자재 목록 상세 조회
     */
    @Override
    public MaterialInboundDTO getInboundDetail(String inboundId) throws Exception {
        MaterialInboundDTO dto = new MaterialInboundDTO();
        MaterialInboundVO inbound = miDAO.getInboundMaster(inboundId);
        if (inbound == null) return null;

        MaterialOrderVO order = miDAO.getOrderInfoByOrderId(inbound.getOrderId());
        if (order != null) {
            inbound.setExpectedArrivedDate(order.getExpectedArrivedDate());
            inbound.setOrderDate(order.getOrderDate());
            inbound.setSupplierId(order.getSupplierId());
        }

        dto.setInbound(inbound);
        List<MaterialInboundItemVO> items = miDAO.getInboundItemsByInboundId(inboundId);
        
        // [ADD] 항목별 창고코드 비어있으면 보정
        for (MaterialInboundItemVO it : items) {
            if (it.getWarehouseCode() == null || it.getWarehouseCode().isEmpty()) {
                String wh = resolveWarehouseCode(it.getMaterialId(), it.getOrderItemId());
                it.setWarehouseCode(wh);
            }
        }
        dto.setInboundItems(items);
        
        return dto;
    }

    /**
     * 부분입고 상태의 항목에 대해 추가입고건 생성
     * - 남은 수량만큼 새로운 입고ID + 항목 등록
     */
    @Override
    public void createAdditionalInbound(String orderItemId, String handledBy) throws Exception {
        MaterialOrderItemVO orderItem = miDAO.getOrderItemById(orderItemId);

        int orderedQty = orderItem.getOrderQuantity();
        int receivedQty = miDAO.getTotalReceivedQtyByOrderItemId(orderItemId);
        int remainQty = orderedQty - receivedQty;

        if (remainQty <= 0) throw new Exception("남은 입고 수량이 없습니다.");

        String inboundId = generateInboundId();

        MaterialInboundVO inbound = new MaterialInboundVO();
        inbound.setInboundId(inboundId);
        inbound.setOrderId(orderItem.getOrderId());
        inbound.setInboundStatus("미입고");
        inbound.setHandledBy(resolveUser(handledBy));
        inbound.setInboundDate(null);
        miDAO.insertMaterialInbound(inbound);

        MaterialInboundItemVO item = new MaterialInboundItemVO();
        item.setInboundId(inboundId);
        item.setMaterialId(orderItem.getMaterialId());
        item.setQuantity(remainQty);
        item.setInboundStatus("미입고");
        inbound.setHandledBy(resolveUser(handledBy));
        item.setOrderItemId(orderItemId);
        item.setWarehouseCode(orderItem.getWarehouseCode());

        String inboundItemId = generateInboundItemId();
        item.setInboundItemId(inboundItemId);

        miDAO.insertMaterialInboundItem(item);
    }

    /** 입고ID 자동 생성 (형식: IN-RM-YYYYMMDD-001) */
    private String generateInboundId() throws Exception {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String prefix = "IN-RM-" + date + "-";
        int seq = 1;

        String latestId = miDAO.generateInboundId();
        if (latestId != null && latestId.startsWith(prefix)) {
            String[] parts = latestId.split("-");
            seq = Integer.parseInt(parts[3]) + 1;
        }

        return String.format("%s%03d", prefix, seq);
    }

    /** 입고항목ID 자동 생성 (형식: IN-RM-ITEM-YYYYMMDD-001) */
    private String generateInboundItemId() throws Exception {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String prefix = "IN-RM-ITEM-" + date + "-";
        int seq = 1;

        String latestId = miDAO.getLatestInboundItemId(prefix);
        if (latestId != null && latestId.startsWith(prefix)) {
            String[] parts = latestId.split("-");
            seq = Integer.parseInt(parts[4]) + 1;
        }

        return String.format("%s%03d", prefix, seq);
    }

    /** 재고ID 자동 생성 (형식: INV-RM-YYYYMMDD-001) */
    private String generateInventoryId() throws Exception {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        int seq = miDAO.getTodayInventorySequence(date);
        return String.format("INV-RM-%s-%03d", date, seq + 1);
    }

    
    // 특정 발주 항목의 누적 입고 수량 조회
    @Override
    public int getTotalInboundQuantity(String orderItemId) throws Exception {
        return miDAO.getTotalInboundQuantity(orderItemId);
    }
    
    
    /** [ADD] 창고코드 자동결정: ORDER_ITEM > MATERIAL 기본창고 > 시스템 기본값(WH001) */
    private String resolveWarehouseCode(String materialId, String orderItemId) throws Exception {
        // 1) 발주항목 창고
        if (orderItemId != null && !orderItemId.trim().isEmpty()) {
            // getOrderItemById로도 가능 (이미 쓰고 있음)
            MaterialOrderItemVO oi = miDAO.getOrderItemById(orderItemId);
            if (oi != null && oi.getWarehouseCode() != null && !oi.getWarehouseCode().isEmpty()) {
                return oi.getWarehouseCode();
            }
            // 또는 전용 DAO 메서드가 있으면:
            // String w = miDAO.getOrderItemWarehouseCode(orderItemId);
            // if (w != null && !w.isEmpty()) return w;
        }
        // 2) 자재 기본 창고
        String w2 = miDAO.getDefaultWarehouseByMaterialId(materialId);
        if (w2 != null && !w2.isEmpty()) return w2;

        // 3) 시스템 기본
        return "WH001";
    }

    
    @Override
    @Transactional
    public void touchUsageStatusAfterOutbound(String outboundId, List<String> inboundIds) {
        // 1) 모달에서 여러 입고건을 골랐다면, 그 목록 기준으로 각각 재계산
        if (inboundIds != null && !inboundIds.isEmpty()) {
            for (String inb : inboundIds) {
                if (StringUtils.hasText(inb)) {
                    miDAO.recalcUsageStatusByInboundId(inb.trim());
                }
            }
            return;
        }

        // 2) 목록이 없으면 outboundId 기반으로 “이번 출고가 건드린 입고건 전체” 재계산
        if (StringUtils.hasText(outboundId)) {
            miDAO.recalcUsageStatusByOutboundId(outboundId.trim());
        }
    }
    
    
    /**
     * 입고 상태별 카운트 조회
     */
    public Map<String, Integer> getInboundStatusCounts() throws Exception {
        Map<String, Integer> statusCounts = new HashMap<>();
        
        // 각 상태별로 개수를 조회하여 Map에 저장
        statusCounts.put("미입고", miDAO.getInboundCountByStatus("미입고"));
        statusCounts.put("부분입고", miDAO.getInboundCountByStatus("부분입고"));
        statusCounts.put("입고완료", miDAO.getInboundCountByStatus("입고완료"));
        
        return statusCounts;
    }
    
    
    /* ▼ 하위호환: 기존 시그니처는 내부 위임만 남김 (중복 정의 금지!) */
    @Override public void insertUnreceivedOrders() throws Exception { insertUnreceivedOrders(null); }
    @Override public void insertSelectedUnreceivedOrders(String[] orderIds) throws Exception { insertSelectedUnreceivedOrders(orderIds, null); }
    @Override public void createAdditionalInbound(String orderItemId) throws Exception { createAdditionalInbound(orderItemId, null); }
    
    
    
}