package com.itwillbs.service;

import java.util.List;
import java.util.Map;

/**
 * [자재 예약 Service]
 * - 버튼 액션(등록, 부족분 발주)에 맞춘 간단한 메서드 제공
 */
public interface MaterialReservationService {
	
	/**
     * [등록 버튼]
     * - 작업지시서 자재별로 "예약" 먼저 수행
     * - 모든 자재가 예약으로 충족되면 출고전표(DRAFT) 생성
     * - 전표가 생성되면 outboundId 반환, 아니면 null (= 부족분 있음)
     */
    String registerOrDraftOutbound(String workOrderNo, String userId) throws Exception;

    /**
     * [부족분 발주 버튼]
     * - 작업지시서 자재별로 "예약" 먼저 수행(가용분 만큼)
     * - 남은 부족분만 모아서 발주(PO) 초안 생성
     * - 생성된 orderId 반환 (부족분이 없으면 null)
     */
    String createShortageDraftPO(String workOrderNo, String userId) throws Exception;

    /**
     * [출고 확정(ISSUED)시 호출]
     * - 실제 재고 차감은 기존 로직대로 하고
     * - 예약 수량은 여기서 해제(차감)
     * - items: materialId, qty 리스트
     */
    void releaseReservationOnIssue(String workOrderNo, List<Map<String,Object>> items, String userId) throws Exception;
    
    // 등록 직전 "예약만" 선처리: 전표/PO 생성 안 함	
    boolean reserveOnlyForWo(String workOrderNo) throws Exception;
    
    int selectOnhand(String materialId) throws Exception;
    int sumReservedByMaterial(String materialId) throws Exception;
    int selectWoReserved(String workOrderNo, String materialId) throws Exception;

    
}
