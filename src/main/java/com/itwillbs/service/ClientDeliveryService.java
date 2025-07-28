package com.itwillbs.service;

import com.itwillbs.domain.ClientDeliveryVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.ShipmentCompletedDTO;
import com.itwillbs.dto.ShipmentPendingDTO;
import com.itwillbs.dto.ShipmentPendingGroupDTO;

import java.util.List;

public interface ClientDeliveryService {
    List<ShipmentPendingDTO> getPendingShipmentList();
    
    List<ShipmentPendingGroupDTO> getPendingShipmentGroupedList();
    
    void processShipmentByOrderId(String clOrderId);
    
    void updateClientOrderStatus(String clOrderId, String status);

    //출고완료리스트
    List<ShipmentCompletedDTO> searchCompletedShipmentList(SearchCriteria cri);
    int countCompletedShipmentList(SearchCriteria cri);



}