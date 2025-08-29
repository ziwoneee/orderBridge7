package com.itwillbs.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * [자재 예약 DAO]
 * - Mapper XML을 sqlSession으로 호출하는 전통 DAO 방식
 */
public interface MaterialReservationDAO {
	
	// 예약 upsert: 같은 (WO, 자재)이면 수량 누적
    int upsertReservation(String workOrderId, String materialId, int qty) throws Exception;

    // 자재별 총 예약합(모든 작업지시서 합계)
    int sumReservedByMaterial(String materialId) throws Exception;

    // 해당 작업지시서가 이미 예약한 수량(없으면 0)
    int selectWoReserved(String workOrderId, String materialId) throws Exception;

    // 자재 onhand(고정창고 기준 합계)
    int selectOnhand(String materialId) throws Exception;

    // 작업지시서 필요자재 목록(materialId, requiredQty)
    List<Map<String,Object>> selectWoMaterials(String workOrderId) throws Exception;

    // 모든 자재 예약 충족 시 work_order.shortage_status = RESOLVED
    int resolveIfAllReserved(String workOrderId) throws Exception;

    // 출고 확정(ISSUED) 시 예약 수량 차감
    int releaseReservation(String workOrderId, String materialId, int qty) throws Exception;
    
    // 상태 단순 변경
    int markWorkOrderShortageStatus(String workOrderId, String status, String userId) throws Exception;
	// (옵션) 안전 전이
	int transitionShortageStatus(String workOrderId, String from, String to, String userId) throws Exception;

    // 이번 출고(outboundId)로 사용된 수량만큼 예약 차감
	void consumeReservationByOutbound(String outboundId) throws Exception;
	// 0이 된 예약행 정리
	void deleteZeroReservationsByOutbound(String outboundId) throws Exception;
	
	// 작업지시 납기일 조회
	java.util.Date selectWorkOrderDueDate(String workOrderId) throws Exception;

    // 자재/거래처 기준 리드타임 최대값(없으면 defaultLeadDays 반환)
    Integer selectMaxLeadDaysForShortagePO(String workOrderId, int defaultLeadDays) throws Exception;
	
    
    // ReservationDAO.java
    BigDecimal selectOnhandDecimal(String materialId);
    BigDecimal sumReservedByMaterialDecimal(String materialId);
    BigDecimal selectWoReservedDecimal(String workOrderId, String materialId);

}
