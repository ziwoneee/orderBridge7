package com.itwillbs.service;

import com.itwillbs.domain.ClientDeliveryVO;
import com.itwillbs.domain.ProductOutboundVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.LotStockDTO;
import com.itwillbs.dto.ShipmentCompletedDTO;
import com.itwillbs.dto.ShipmentPendingDTO;
import com.itwillbs.dto.ShipmentPendingGroupDTO;
import com.itwillbs.persistence.ClientDeliveryDAO;
import com.itwillbs.persistence.ProductOutboundDAO;

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
    private ProductOutboundService outboundService; // ✅ 서비스로 변경 (기존 DAO 대체)

    @Override
    public List<ShipmentPendingDTO> getPendingShipmentList() {
        return deliveryDAO.selectPendingShipmentList();
    }

    @Override
    public List<ShipmentPendingGroupDTO> getPendingShipmentGroupedList() {
        return deliveryDAO.getPendingShipmentGroupedList();
    }

    /**
     * ✅ 수주번호 단위 출하 처리
     * - 제품별 LOT에서 차감
     * - 출하/출고 등록
     * - 같은 송장번호로 묶음
     */
    @Transactional
    @Override
    public void processShipmentByOrderId(String clOrderId) {
        List<ShipmentPendingDTO> itemList = deliveryDAO.selectItemsByOrderId(clOrderId);
        boolean allShipped = true;

        // ✅ 수주번호 단위로 송장번호 1개 생성 (전체에 공유)
        String trackingNumber = generateTrackingNumber();

        for (ShipmentPendingDTO item : itemList) {
            int remainingQty = item.getOrderQty();

            if (item.getStockQty() < remainingQty) {
                allShipped = false;
                continue;
            }

            List<LotStockDTO> lots = deliveryDAO.getAvailableLots(item.getProductId());

            for (LotStockDTO lot : lots) {
                if (remainingQty <= 0) break;

                int useQty = Math.min(lot.getRemainingQty(), remainingQty);

                // ✅ 출하 등록
                ClientDeliveryVO delivery = new ClientDeliveryVO();
                delivery.setClOrderId(item.getClOrderId());
                delivery.setProductId(item.getProductId());
                delivery.setLotNo(lot.getLotNo());
                delivery.setDeliveryQty(useQty);
                delivery.setDeliveryDate(new Date());
                delivery.setDeliveryStatus("배송준비");
                delivery.setClientName(item.getClientName());
                delivery.setClientId(item.getClientId());
                delivery.setTrackingNumber(trackingNumber); // ✅ 동일 송장번호 사용
                delivery.setCreatedAt(new Date());
                delivery.setUpdatedAt(new Date());

                deliveryDAO.insertDelivery(delivery);

                // ✅ 출고 등록
                ProductOutboundVO outbound = new ProductOutboundVO();
                outbound.setProductId(item.getProductId());
                outbound.setLotNo(lot.getLotNo());
                outbound.setOutboundQty(useQty);
                outbound.setOutboundDate(new Date());
                outbound.setClientId(item.getClientId());
                outbound.setTrackingNumber(trackingNumber); // ✅ 동일 송장번호 사용

                outboundService.registerOutbound(outbound); // 출고 ID 자동 생성 + 재고 차감

                remainingQty -= useQty;
            }

            if (remainingQty <= 0) {
                deliveryDAO.updateOrderDetailStatus(item.getDetailId(), "SHIPPED");
            } else {
                allShipped = false;
            }
        }

        if (allShipped) {
            deliveryDAO.updateClientOrderStatus(clOrderId, "SHIPPED");
        }
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
