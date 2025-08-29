package com.itwillbs.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itwillbs.persistence.MaterialOrderDAO;
import com.itwillbs.persistence.MaterialOutboundDAO;
import com.itwillbs.persistence.MaterialReservationDAO;

/**
 * [자재 예약 Service 구현]
 * - 쉽게 읽히도록 로직을 단순 분해
 * - 모든 메서드는 트랜잭션으로 감싸 동시성/일관성 보장
 */
@Service
public class MaterialReservationServiceImpl implements MaterialReservationService {
	
	@Inject 
	private MaterialReservationDAO reservationDAO;
	
    @Inject
    private MaterialOrderDAO orderDAO;
    
    @Inject 
    private MaterialOutboundDAO outboundDAO;
    
    /** 시스템 기본 리드타임(일) — supplier_item/material에 값이 없을 때 사용 */
    private static final int DEFAULT_LEAD_DAYS = 3;

    /**
     * [도우미] WO의 필요자재 목록 조회 (materialId, requiredQty)
     */
    private List<Map<String,Object>> getWoMaterials(String workOrderId) throws Exception {
        return reservationDAO.selectWoMaterials(workOrderId);
    }

    /**
     * [도우미] 특정 자재의 "가용재고" 계산 = onhand - 총예약
     */
    private int getAvailable(String materialId) throws Exception {
        int onhand   = reservationDAO.selectOnhand(materialId);
        int reserved = reservationDAO.sumReservedByMaterial(materialId);
        return onhand - reserved;
    }

    /**
     * [도우미] 이번 WO가 아직 예약해야 할 잔여량 = required - (이미 예약된 양)
     */
    private int getStillNeed(String workOrderId, String materialId, int requiredQty) throws Exception {
        int woReserved = reservationDAO.selectWoReserved(workOrderId, materialId);
        int stillNeed  = requiredQty - woReserved;
        return Math.max(stillNeed, 0);
    }

    /**
     * [등록 버튼]
     * - 예약 먼저
     * - 전량 예약 충족 시: 출고 전표 DRAFT 생성
     *   (너의 DAO 메서드: nextOutboundId() + insertMaterialOutbound() + insertOutboundItemsFromWOM())
     */
    @Override
    @Transactional
    public String registerOrDraftOutbound(String workOrderId, String userId) throws Exception {
        List<Map<String,Object>> mats = getWoMaterials(workOrderId);

        boolean allOk = true;
        for (Map<String,Object> row : mats) {
            String matId = (String) row.get("materialId");
            int required = ((Number) row.get("requiredQty")).intValue();

            int stillNeed   = getStillNeed(workOrderId, matId, required);
            if (stillNeed <= 0) continue;

            int available   = Math.max(getAvailable(matId), 0);
            int willReserve = Math.min(stillNeed, available);

            if (willReserve > 0) {
                reservationDAO.upsertReservation(workOrderId, matId, willReserve);
                stillNeed -= willReserve;
            }
            if (stillNeed > 0) allOk = false;
        }

        if (!allOk) {
            // 부족 남았음 → 전표 만들지 않음
            return null;
        }

        // === 출고 전표 DRAFT 생성 (네 DAO 방식) ===
        String outboundId = outboundDAO.nextOutboundId();                 // 신규 ID
        outboundDAO.insertMaterialOutbound(outboundId, workOrderId);      // 헤더(기본값/DRAFT로 들어가도록 Mapper 설계되어 있다고 가정)
        outboundDAO.insertOutboundItemsFromWOM(outboundId, workOrderId);  // WOM에서 자재 목록 복사

        // 모든 자재 예약 충족 → RESOLVED 갱신 시도
        reservationDAO.resolveIfAllReserved(workOrderId);
        
        int upd = reservationDAO.transitionShortageStatus(workOrderId, "DRAFTED", "ALLOCATED", userId);
        if (upd == 0) {
            upd = reservationDAO.transitionShortageStatus(workOrderId, "CHECKED", "ALLOCATED", userId);
            if (upd == 0) {
                reservationDAO.transitionShortageStatus(workOrderId, "NONE", "ALLOCATED", userId);
            }
        }

        return outboundId;
    }

    /**
     * [부족분 발주 버튼]
     * - 예약 먼저(가용분만)
     * - 남은 부족분만 모아 발주 초안 생성
     *   (너의 DAO 메서드: generateOrderId() + insertOrderHeaderDraft(params) + insertOrderItemsBatch(items))
     */
    @Override
    @Transactional
    public String createShortageDraftPO(String workOrderId, String userId, Integer overrideLeadDays) throws Exception {
    	// 0) 기존 로직: 부족 자재 산출 (+ 물 제외)
        List<Map<String,Object>> mats = getWoMaterials(workOrderId);
        List<Map<String,Object>> shortages = new ArrayList<>();

        for (Map<String,Object> row : mats) {
            String mid = (String) row.get("materialId");
            int required = ((Number) row.get("requiredQty")).intValue();

            if ("RM-0015".equals(mid)) continue; // 물은 PO 제외

            int stillNeed = getStillNeed(workOrderId, mid, required);
            if (stillNeed <= 0) continue;

            int available = Math.max(getAvailable(mid), 0);
            int willReserve = Math.min(stillNeed, available);

            if (willReserve > 0) {
                reservationDAO.upsertReservation(workOrderId, mid, willReserve);
                stillNeed -= willReserve;
            }
            if (stillNeed > 0) {
                Map<String,Object> s = new HashMap<>();
                s.put("materialId", mid);
                s.put("orderQty",   stillNeed);
                shortages.add(s);
            }
        }

        if (shortages.isEmpty()) {
            reservationDAO.resolveIfAllReserved(workOrderId);
            return null;
        }
        
	     // === [A] 리드타임/자재납기일 계산 추가 ===
	
	     // 1) 작업지시 납기일 조회 (java.util.Date로 받음)
	     java.util.Date woDue = reservationDAO.selectWorkOrderDueDate(workOrderId);
	     if (woDue == null) {
	         throw new IllegalStateException("작업지시 납기일(due_date)을 찾을 수 없습니다: " + workOrderId);
	     }
	
	     // util.Date -> LocalDate 변환 (sql.Date면 그대로, 아니면 Instant 경유)
	     java.time.LocalDate dueDate =
	             (woDue instanceof java.sql.Date)
	             ? ((java.sql.Date) woDue).toLocalDate()
	             : woDue.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
	
	     // 2) 리드타임 결정: override > (자재/거래처 기준 max) > DEFAULT
	     Integer maxLead = reservationDAO.selectMaxLeadDaysForShortagePO(workOrderId, DEFAULT_LEAD_DAYS);
	     int leadDays = (overrideLeadDays != null) ? overrideLeadDays
	                  : (maxLead != null ? maxLead : DEFAULT_LEAD_DAYS);
	     if (leadDays < 0) leadDays = 0;
	
	     // 3) 자재 납기일 = 작업지시 납기일 - 리드타임
	     java.time.LocalDate expectedDate = dueDate.minusDays(leadDays);
	     // (선택) 오늘 이전이면 오늘로 보정
	     // if (expectedDate.isBefore(java.time.LocalDate.now())) expectedDate = java.time.LocalDate.now();
	
	     // DB insert/update에 쓸 java.sql.Date로 변환
	     java.sql.Date expectedArrivedDate = java.sql.Date.valueOf(expectedDate);


        
        
        // 1) 자재별 후보 협력사/단가/창고 조회
        List<String> mids = shortages.stream()
                .map(x -> (String)x.get("materialId"))
                .distinct()
                .collect(Collectors.toList());

        // 내부사용(N) 자재 사전 차단(선택)
        List<Map<String,Object>> nonPurch = orderDAO.selectNonPurchasableFromList(mids);
        if (nonPurch != null && !nonPurch.isEmpty()) {
            String bad = nonPurch.stream()
                    .map(m -> (String)m.get("materialId"))
                    .distinct().toString();
            throw new IllegalStateException("구매불가 자재 존재: " + bad);
        }

        List<Map<String,Object>> maps = orderDAO.selectSupplierItemMappings(mids);
        // materialId -> 후보 리스트
        Map<String, List<Map<String,Object>>> candByMat = new HashMap<>();
        for (Map<String,Object> m : maps) {
            String mid = (String) m.get("materialId");
            candByMat.computeIfAbsent(mid, k -> new ArrayList<>()).add(m);
        }

        // 1-1) 우선협력사 선정: unitPrice ASC (is_preferred 컬럼 없으므로 가격 기준)
        Map<String, Map<String,Object>> chosen = new HashMap<>();
        for (String mid : mids) {
        	List<Map<String,Object>> cands = candByMat.containsKey(mid)
        		    ? candByMat.get(mid)
        		    : java.util.Collections.<Map<String,Object>>emptyList();
            
            if (cands.isEmpty()) continue;
            cands.sort((a,b) -> {
                int au = ((Number)a.getOrDefault("unitPrice", 0)).intValue();
                int bu = ((Number)b.getOrDefault("unitPrice", 0)).intValue();
                return Integer.compare(au, bu);
            });
            chosen.put(mid, cands.get(0)); // 최저가 1건
        }
        // 협력사 미존재 자재 방지
        List<String> noSupp = mids.stream().filter(mid -> !chosen.containsKey(mid)).collect(Collectors.toList());
        if (!noSupp.isEmpty()) {
            throw new IllegalStateException("협력사 미지정 자재: " + String.join(", ", noSupp));
        }

        // 2) 협력사별 그룹핑
        Map<String, List<Map<String,Object>>> bySupplier = new HashMap<>();
        for (Map<String,Object> it : shortages) {
            String mid = (String) it.get("materialId");
            String sid = (String) chosen.get(mid).get("supplierId");
            bySupplier.computeIfAbsent(sid, k -> new ArrayList<>()).add(it);
        }

        // 3) 협력사별 헤더/아이템 생성
        String firstOrderId = null;

        for (Map.Entry<String, List<Map<String,Object>>> e : bySupplier.entrySet()) {
            String supplierId = e.getKey();
            List<Map<String,Object>> items = e.getValue();

            // 헤더 파라미터(Map) – selectKey가 orderId를 셋팅해 줌
            Map<String,Object> header = new HashMap<>();
            header.put("supplierId",  supplierId);      // ★ 반드시 세팅
            header.put("orderStatus", "초안");          // 혹은 "DRAFT" -> 매퍼에서 COALESCE 처리
            header.put("handledBy",   userId);
            header.put("workOrderId", workOrderId);
            // ★ 핵심: 자재 납기일 세팅
            header.put("expectedArrivedDate", expectedArrivedDate);
            header.put("note", "[자동] 부족분 발주 (리드타임 " + leadDays + "일, WO납기 " + dueDate + ")");

            orderDAO.insertOrderHeaderDraft(header); // 매퍼가 expected_arrived_date 받아서 넣도록 수정(아래 3) 참고)

            // 방금 생성된 orderId 회수 (selectKey BEFORE 덕분에 header에 값 들어있음)
            String orderId = (String) header.get("orderId");
            if (firstOrderId == null) firstOrderId = orderId;

            int idx = orderDAO.selectNextOrderItemIndex(orderId);
            List<Map<String,Object>> batch = new ArrayList<>();

            for (Map<String,Object> it2 : items) {
                String mid = (String) it2.get("materialId");

                Map<String,Object> chosenRow = chosen.get(mid);
                int unit = ((Number) chosenRow.getOrDefault("unitPrice", 0)).intValue();

                // stillNeed(기준단위, 예:g) → 정책 적용 수량(발주단위, 예:kg)으로 변환
                int stillNeedBase = ((Number) it2.get("orderQty")).intValue();
                BigDecimal needBase = new BigDecimal(stillNeedBase);

                BigDecimal conv     = getBD(chosenRow.get("convToBase"), BigDecimal.ONE);
                BigDecimal moq      = getBD(chosenRow.get("minOrderQty"), null);
                BigDecimal multiple = getBD(chosenRow.get("orderMultiple"), null);
                BigDecimal pack     = getBD(chosenRow.get("packQty"), null);

                BigDecimal poQtyUom = applyPurchasePolicy(needBase, conv, moq, multiple, pack);

                // 현재 VO/컬럼이 INTEGER이므로 정수 저장 (팩/배수라 대개 정수)
                int qty = poQtyUom.setScale(0, RoundingMode.CEILING).intValue();
                if (qty <= 0) continue; // 방어

                int total = unit * qty;
                String wh = (String) chosenRow.getOrDefault("warehouseCode", "WH001");

                Map<String,Object> row = new HashMap<>();
                row.put("orderItemId",   orderId + "-" + (idx++));
                row.put("orderId",       orderId);
                row.put("materialId",    mid);
                row.put("orderQuantity", qty);      // ✅ 정책 반영 수량
                row.put("unitPrice",     unit);
                row.put("totalPrice",    total);
                row.put("warehouseCode", wh);
                row.put("workOrderId",   workOrderId);

                batch.add(row);
            }

            orderDAO.insertOrderItemsBatch(batch);
            
        }
        
        int upd = reservationDAO.transitionShortageStatus(workOrderId, "NONE", "DRAFTED", userId);
        if (upd == 0) {
            reservationDAO.transitionShortageStatus(workOrderId, "CHECKED", "DRAFTED", userId);
        }

        return firstOrderId; // 필요하면 List<String>으로 바꿔서 전부 반환
    }
    
    
 // === [추가1] 배수 올림 ===
    private BigDecimal ceilToMultiple(BigDecimal x, BigDecimal multiple) {
        if (x == null) return BigDecimal.ZERO;
        if (multiple == null || multiple.compareTo(BigDecimal.ZERO) <= 0) return x;
        BigDecimal[] dr = x.divideAndRemainder(multiple);
        return (dr[1].compareTo(BigDecimal.ZERO) == 0) ? x : multiple.multiply(dr[0].add(BigDecimal.ONE));
    }

    // === [추가2] 안전 BigDecimal 변환 ===
    private BigDecimal getBD(Object o, BigDecimal dflt) {
        if (o == null) return dflt;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        if (o instanceof Number) return new BigDecimal(((Number) o).toString());
        try { return new BigDecimal(String.valueOf(o)); } catch (Exception e) { return dflt; }
    }

    // === [추가3] 정책 적용: 기준단위 → 발주단위 환산 + MOQ/팩/배수 반영 ===
    private BigDecimal applyPurchasePolicy(BigDecimal needBase,
                                           BigDecimal convToBase,
                                           BigDecimal minOrderQty,
                                           BigDecimal orderMultiple,
                                           BigDecimal packQty) {
        if (needBase == null || needBase.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        if (convToBase == null || convToBase.compareTo(BigDecimal.ZERO) == 0) convToBase = BigDecimal.ONE;

        // 기준단위(예:g) → 발주단위(예:kg) (올림)
        BigDecimal needUom = needBase.divide(convToBase, 6, RoundingMode.UP);

        // MOQ
        if (minOrderQty != null && minOrderQty.compareTo(BigDecimal.ZERO) > 0) {
            needUom = needUom.max(minOrderQty);
        }
        // 포장단위 올림
        if (packQty != null && packQty.compareTo(BigDecimal.ZERO) > 0) {
            needUom = ceilToMultiple(needUom, packQty);
        }
        // 발주 배수 올림
        if (orderMultiple != null && orderMultiple.compareTo(BigDecimal.ZERO) > 0) {
            needUom = ceilToMultiple(needUom, orderMultiple);
        }
        return needUom;
    }


    /**
     * [출고 확정(ISSUED)]
     * - 기존 재고 차감/LOT 처리 후에 호출
     * - 예약 수량을 줄여준다(해제)
     */
    @Override
    @Transactional
    public void releaseReservationOnIssue(String workOrderId, List<Map<String,Object>> items, String userId) throws Exception {
        for (Map<String,Object> it : items) {
            String materialId = (String) it.get("materialId");
            int qty           = ((Number) it.get("qty")).intValue();
            reservationDAO.releaseReservation(workOrderId, materialId, qty);
        }
        
        // 재고/예약 해제 성공 후 상태 갱신
        // [STATE] ALLOCATED → RESOLVED (직접 발행 케이스 대비 DRAFTED도 허용)
        int upd = reservationDAO.transitionShortageStatus(workOrderId, "ALLOCATED", "RESOLVED", userId);
        if (upd == 0) {
            reservationDAO.transitionShortageStatus(workOrderId, "DRAFTED", "RESOLVED", userId);
        }
    }
    
    
    @Override
    @Transactional
    public boolean reserveOnlyForWo(String workOrderId) throws Exception {
        List<Map<String,Object>> mats = reservationDAO.selectWoMaterials(workOrderId);
        boolean allOk = true;

        for (Map<String,Object> row : mats) {
            String matId   = (String) row.get("materialId");
            int required   = ((Number) row.get("requiredQty")).intValue();
            int stillNeed  = getStillNeed(workOrderId, matId, required);
            if (stillNeed <= 0) continue;

            int available   = Math.max(getAvailable(matId), 0);
            int willReserve = Math.min(stillNeed, available);

            if (willReserve > 0) {
                reservationDAO.upsertReservation(workOrderId, matId, willReserve);
                stillNeed -= willReserve;
            }
            if (stillNeed > 0) allOk = false; // 부족 남음
        }

        return allOk;
    }
    
    
    @Override public int selectOnhand(String materialId) throws Exception {
        return reservationDAO.selectOnhand(materialId);
    }
    @Override public int sumReservedByMaterial(String materialId) throws Exception {
        return reservationDAO.sumReservedByMaterial(materialId);
    }
    @Override public int selectWoReserved(String workOrderId, String materialId) throws Exception {
        return reservationDAO.selectWoReserved(workOrderId, materialId);
    }
    

}
