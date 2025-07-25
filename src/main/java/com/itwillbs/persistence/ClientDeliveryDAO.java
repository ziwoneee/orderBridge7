package com.itwillbs.persistence;

import com.itwillbs.domain.ClientDeliveryVO;
import com.itwillbs.dto.ShipmentPendingDTO;
import com.itwillbs.dto.ShipmentPendingGroupDTO;
import com.itwillbs.dto.ShipmentProductDTO;

import java.util.List;

public interface ClientDeliveryDAO {

    List<ShipmentPendingDTO> selectPendingShipmentList();
    
    List<ShipmentPendingGroupDTO> getPendingShipmentGroupedList();
  
    ShipmentPendingDTO selectShipmentItem(Long orderDetailId);

    void insertDelivery(ClientDeliveryVO vo);

    void decreaseStock(String productId, int qty);

    void updateOrderDetailStatus(Long orderDetailId, String status);

   
    List<ShipmentProductDTO> selectPendingShipmentFlatList();
   
    




}
