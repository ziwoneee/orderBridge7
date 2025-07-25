package com.itwillbs.service;

import com.itwillbs.domain.ClientDeliveryVO;
import com.itwillbs.dto.ShipmentPendingDTO;
import com.itwillbs.dto.ShipmentPendingGroupDTO;

import java.util.List;

public interface ClientDeliveryService {
    List<ShipmentPendingDTO> getPendingShipmentList();
    
    List<ShipmentPendingGroupDTO> getPendingShipmentGroupedList();
    
    void processShipments(List<Long> orderDetailIds);
}