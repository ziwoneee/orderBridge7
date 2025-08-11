package com.itwillbs.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * [도우미] WO의 필요자재 목록 조회 (materialId, requiredQty)
     */
    private List<Map<String,Object>> getWoMaterials(String workOrderNo) throws Exception {
        return reservationDAO.selectWoMaterials(workOrderNo);
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
    private int getStillNeed(String workOrderNo, String materialId, int requiredQty) throws Exception {
        int woReserved = reservationDAO.selectWoReserved(workOrderNo, materialId);
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
    public String registerOrDraftOutbound(String workOrderNo, String userId) throws Exception {
        List<Map<String,Object>> mats = getWoMaterials(workOrderNo);

        boolean allOk = true;
        for (Map<String,Object> row : mats) {
            String matId = (String) row.get("materialId");
            int required = ((Number) row.get("requiredQty")).intValue();

            int stillNeed   = getStillNeed(workOrderNo, matId, required);
            if (stillNeed <= 0) continue;

            int available   = Math.max(getAvailable(matId), 0);
            int willReserve = Math.min(stillNeed, available);

            if (willReserve > 0) {
                reservationDAO.upsertReservation(workOrderNo, matId, willReserve);
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
        outboundDAO.insertMaterialOutbound(outboundId, workOrderNo);      // 헤더(기본값/DRAFT로 들어가도록 Mapper 설계되어 있다고 가정)
        outboundDAO.insertOutboundItemsFromWOM(outboundId, workOrderNo);  // WOM에서 자재 목록 복사

        // 모든 자재 예약 충족 → RESOLVED 갱신 시도
        reservationDAO.resolveIfAllReserved(workOrderNo);

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
    public String createShortageDraftPO(String workOrderNo, String userId) throws Exception {
        List<Map<String,Object>> mats = getWoMaterials(workOrderNo);
        List<Map<String,Object>> shortageItems = new ArrayList<>();

        for (Map<String,Object> row : mats) {
            String matId = (String) row.get("materialId");
            int required = ((Number) row.get("requiredQty")).intValue();

            int stillNeed   = getStillNeed(workOrderNo, matId, required);
            if (stillNeed <= 0) continue;

            int available   = Math.max(getAvailable(matId), 0);
            int willReserve = Math.min(stillNeed, available);

            if (willReserve > 0) {
                reservationDAO.upsertReservation(workOrderNo, matId, willReserve);
                stillNeed -= willReserve;
            }
            if (stillNeed > 0) {
                Map<String,Object> s = new HashMap<>();
                s.put("materialId",  matId);
                s.put("orderQty",    stillNeed); // ← 네 Mapper에서 쓰는 키 이름에 맞게 변경 가능
                shortageItems.add(s);
            }
        }

        if (shortageItems.isEmpty()) {
            // 부족 없음 → RESOLVED 체크만
            reservationDAO.resolveIfAllReserved(workOrderNo);
            return null;
        }

        // === 발주 초안 생성 (네 DAO 방식) ===
        String orderId = orderDAO.generateOrderId();

        // 헤더 파라미터(키 이름은 매퍼에서 받는대로 맞춰줘)
        Map<String,Object> header = new HashMap<>();
        header.put("orderId",          orderId);
        header.put("createdBy",        userId);
        header.put("orderStatus",      "DRAFT");
        header.put("linkedWorkOrder",  workOrderNo); // 매퍼에 없으면 빼도 됨

        orderDAO.insertOrderHeaderDraft(header);

        // 항목 배치 파라미터(키 이름은 매퍼에 맞게)
        // 예시 키: orderItemId, orderId, materialId, orderQuantity
        List<Map<String,Object>> batch = new ArrayList<>();
        int idx = orderDAO.selectNextOrderItemIndex(orderId); // 이미 있으면 이어서
        for (Map<String,Object> it : shortageItems) {
            String matId = (String) it.get("materialId");
            int qty      = ((Number) it.get("orderQty")).intValue();

            Map<String,Object> one = new HashMap<>();
            one.put("orderItemId",    orderId + "-" + (idx++));
            one.put("orderId",        orderId);
            one.put("materialId",     matId);
            one.put("orderQuantity",  qty);
            batch.add(one);
        }
        orderDAO.insertOrderItemsBatch(batch);

        return orderId;
    }

    /**
     * [출고 확정(ISSUED)]
     * - 기존 재고 차감/LOT 처리 후에 호출
     * - 예약 수량을 줄여준다(해제)
     */
    @Override
    @Transactional
    public void releaseReservationOnIssue(String workOrderNo, List<Map<String,Object>> items) throws Exception {
        for (Map<String,Object> it : items) {
            String materialId = (String) it.get("materialId");
            int qty           = ((Number) it.get("qty")).intValue();
            reservationDAO.releaseReservation(workOrderNo, materialId, qty);
        }
    }
    
    
    @Override
    @Transactional
    public boolean reserveOnlyForWo(String workOrderNo) throws Exception {
        List<Map<String,Object>> mats = reservationDAO.selectWoMaterials(workOrderNo);
        boolean allOk = true;

        for (Map<String,Object> row : mats) {
            String matId   = (String) row.get("materialId");
            int required   = ((Number) row.get("requiredQty")).intValue();
            int stillNeed  = getStillNeed(workOrderNo, matId, required);
            if (stillNeed <= 0) continue;

            int available   = Math.max(getAvailable(matId), 0);
            int willReserve = Math.min(stillNeed, available);

            if (willReserve > 0) {
                reservationDAO.upsertReservation(workOrderNo, matId, willReserve);
                stillNeed -= willReserve;
            }
            if (stillNeed > 0) allOk = false; // 부족 남음
        }

        if (allOk) reservationDAO.resolveIfAllReserved(workOrderNo); // 전량 예약되면 상태 갱신
        return allOk;
    }


}
