package com.itwillbs.service;

import com.itwillbs.domain.ClientDeliveryVO;
import com.itwillbs.domain.ProductOutboundVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.StockReservationVO;
import com.itwillbs.dto.LotStockDTO;
import com.itwillbs.dto.ShipmentCompletedDTO;
import com.itwillbs.dto.ShipmentPendingDTO;
import com.itwillbs.dto.ShipmentPendingGroupDTO;
import com.itwillbs.persistence.ClientDeliveryDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class ClientDeliveryServiceImpl implements ClientDeliveryService {

    @Autowired
    private ClientDeliveryDAO deliveryDAO;

    @Autowired
    private ProductOutboundService outboundService;

    @Autowired
    private StockReservationService reservationService;

    @Override
    public List<ShipmentPendingDTO> getPendingShipmentList() {
        return deliveryDAO.selectPendingShipmentList();
    }

    @Override
    public List<ShipmentPendingGroupDTO> getPendingShipmentGroupedList() {
        return deliveryDAO.getPendingShipmentGroupedList();
    }

    /**
     * ✅ 수주번호 단위 출하 처리 (예약 기반)
     */
    @Transactional
    @Override
    public void processShipmentByOrderId(String clOrderId) {
        List<StockReservationVO> reservations = reservationService.getReservationsByOrderId(clOrderId);

        if (reservations == null || reservations.isEmpty()) return;

        boolean allShipped = true;
        String trackingNumber = generateTrackingNumber();

        for (StockReservationVO r : reservations) {
            int reservedQty = r.getReservedQty();

            // ✅ 출하 등록
            ClientDeliveryVO delivery = new ClientDeliveryVO();
            delivery.setClOrderId(r.getClOrderId());
            delivery.setProductId(r.getProductId());
            delivery.setLotNo(r.getLotNo());
            delivery.setDeliveryQty(reservedQty);
            delivery.setDeliveryDate(new Date());
            delivery.setDeliveryStatus("배송준비");
            delivery.setClientId(r.getClientId());
            delivery.setTrackingNumber(trackingNumber);
            delivery.setCreatedAt(new Date());
            delivery.setUpdatedAt(new Date());

            deliveryDAO.insertDelivery(delivery);

            // ✅ 출고 등록
            ProductOutboundVO outbound = new ProductOutboundVO();
            outbound.setProductId(r.getProductId());
            outbound.setLotNo(r.getLotNo());
            outbound.setOutboundQty(reservedQty);
            outbound.setOutboundDate(new Date());
            outbound.setClientId(r.getClientId());
            outbound.setManager(r.getManager());
            outbound.setTrackingNumber(trackingNumber);

            outboundService.registerOutbound(outbound);

            // ✅ 상세 상태 SHIPPED 처리
            deliveryDAO.updateOrderDetailStatus(r.getDetailId(), "SHIPPED");
        }

        // ✅ 마스터 상태도 SHIPPED 처리
        deliveryDAO.updateClientOrderStatus(clOrderId, "SHIPPED");

        // ✅ 예약 해제
        reservationService.deleteReservation(clOrderId);
    }

    @Override
    public void updateClientOrderStatus(String clOrderId, String status) {
        deliveryDAO.updateClientOrderStatus(clOrderId, status);
    }

    @Override
    public List<ShipmentCompletedDTO> searchCompletedShipmentList(SearchCriteria cri) {
        return deliveryDAO.searchCompletedShipmentList(cri);
    }

    @Override
    public int countCompletedShipmentList(SearchCriteria cri) {
        return deliveryDAO.countCompletedShipmentList(cri);
    }

    /**
     * ✅ 송장번호 생성 메서드: 예) 20250725-123456
     */
    private String generateTrackingNumber() {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        int random = (int) (Math.random() * 1000000);
        return date + "-" + String.format("%06d", random);
    }
}
