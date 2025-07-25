package com.itwillbs.persistence;

import com.itwillbs.domain.ClientDeliveryVO;
import com.itwillbs.dto.LotStockDTO;
import com.itwillbs.dto.ShipmentPendingDTO;
import com.itwillbs.dto.ShipmentPendingGroupDTO;
import com.itwillbs.dto.ShipmentProductDTO;

import java.util.List;

public interface ClientDeliveryDAO {

    // 출하 대기 목록
    List<ShipmentPendingDTO> selectPendingShipmentList();
    List<ShipmentPendingGroupDTO> getPendingShipmentGroupedList();
    List<ShipmentProductDTO> selectPendingShipmentFlatList();

    // 수주 상세 항목 단건 조회
    ShipmentPendingDTO selectShipmentItem(Long orderDetailId);

    // 출하 등록
    void insertDelivery(ClientDeliveryVO vo);

    // LOT별 재고 차감 (새로 추가)
    void decreaseLotStock(String productId, String lotNo, int qty);

    // (기존 방식 - 필요시 사용)
    void decreaseStock(String productId, int qty);

    // 수주 상세 상태 업데이트
    void updateOrderDetailStatus(Long orderDetailId, String status);
    void updateClientOrderStatus(String clOrderId, String status);

    // 제품 LOT별 잔여재고 조회 (새로 추가)
    List<LotStockDTO> getAvailableLots(String productId);
   
   // 수주번호 기준으로 출하 대기 항목 조회
     List<ShipmentPendingDTO> selectItemsByOrderId(String clOrderId);
     
     int countUnshippedDetails(String clOrderId);
     void updateOrderStatus(String clOrderId, String status);


   
     
    

}
