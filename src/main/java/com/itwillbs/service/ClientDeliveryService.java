package com.itwillbs.service;

import com.itwillbs.domain.ClientDeliveryVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.ShipmentCompletedDTO;
import com.itwillbs.dto.ShipmentCompletedGroupDTO;
import com.itwillbs.dto.ShipmentPendingDTO;
import com.itwillbs.dto.ShipmentPendingGroupDTO;

import java.util.List;

public interface ClientDeliveryService {

	 // 출하대기 목록 (그룹형) - 검색 + 페이징 지원   
    List<ShipmentPendingGroupDTO> searchPendingGroupedList(SearchCriteria cri);

   // 출하대기 총 개수 조회 (SearchCriteria 기반, 그룹형)
    int countPendingGroupedList(SearchCriteria cri);

    // ✅ 출하 대기 목록 - 수주번호 기준 그룹형 전체 조회 (페이징 없이 모든 항목)
    List<ShipmentPendingGroupDTO> getPendingShipmentGroupedList();

    // ✅ 출하 처리 (수주번호 단위)
    void processShipmentByOrderId(String clOrderId);

    // ✅ 수주 상태 변경 (예: 출하 완료 등)
    void updateClientOrderStatus(String clOrderId, String status);

    // ✅ 출하 완료 목록 조회 (검색 + 페이징 포함)
    List<ShipmentCompletedDTO> searchCompletedShipmentList(SearchCriteria cri);

    // ✅ 출하 완료 총 개수 조회 (페이징용)
    int countCompletedShipmentList(SearchCriteria cri);

    // 출하 완료 목록 그룹 조회
    List<ShipmentCompletedGroupDTO> getCompletedGroupedList(SearchCriteria cri);


   
}
