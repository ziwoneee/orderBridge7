package com.itwillbs.service;

import com.itwillbs.domain.ClientDeliveryVO;
import com.itwillbs.dto.ShipmentPendingDTO;
import com.itwillbs.dto.ShipmentPendingGroupDTO;

import com.itwillbs.persistence.ClientDeliveryDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ClientDeliveryServiceImpl implements ClientDeliveryService {

    @Autowired
    private ClientDeliveryDAO deliveryDAO;

    /**
     * 출하 대기 목록 - 평면 리스트
     */
    @Override
    public List<ShipmentPendingDTO> getPendingShipmentList() {
        return deliveryDAO.selectPendingShipmentList();
    }

    /**
     * 출하 대기 목록 - 수주번호 기준 그룹 리스트
     */
    @Override
    public List<ShipmentPendingGroupDTO> getPendingShipmentGroupedList() {
        return deliveryDAO.getPendingShipmentGroupedList();
    }

    /**
     * 출하 처리
     */
    @Transactional
    @Override
    public void processShipments(List<Long> orderDetailIds) {
        for (Long detailId : orderDetailIds) {
            ShipmentPendingDTO item = deliveryDAO.selectShipmentItem(detailId);

            // 재고가 부족하면 스킵
            if (item.getStockQty() < item.getOrderQty()) continue;

            // 1. 출하 등록
            ClientDeliveryVO delivery = new ClientDeliveryVO();
            delivery.setClOrderId(item.getClOrderId());
            delivery.setProductId(item.getProductId());
            delivery.setLotNo(item.getLotNo());
            delivery.setDeliveryQty(item.getOrderQty());
            delivery.setDeliveryDate(new Date());
            delivery.setDeliveryStatus("배송준비");

            deliveryDAO.insertDelivery(delivery);

            // 2. 재고 차감
            deliveryDAO.decreaseStock(item.getProductId(), item.getOrderQty());

            // 3. 수주상세 상태 업데이트
            deliveryDAO.updateOrderDetailStatus(detailId, "출하완료");
        }
    }
}
