package com.itwillbs.service;

import com.itwillbs.domain.ClientDeliveryVO;
import com.itwillbs.dto.LotStockDTO;
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

    /**
     * ✅ 평면 출하대기 목록 조회
     */
    @Override
    public List<ShipmentPendingDTO> getPendingShipmentList() {
        return deliveryDAO.selectPendingShipmentList();
    }

    /**
     * ✅ 수주번호 기준 그룹형 출하대기 목록 조회
     */
    @Override
    public List<ShipmentPendingGroupDTO> getPendingShipmentGroupedList() {
        return deliveryDAO.getPendingShipmentGroupedList();
    }

    /**
     * ✅ 출하 처리 - 수주번호 단위
     */
    @Transactional
    @Override
    public void processShipmentByOrderId(String clOrderId) {
        // 해당 수주번호의 상세 항목 리스트 조회
        List<ShipmentPendingDTO> itemList = deliveryDAO.selectItemsByOrderId(clOrderId);

        boolean allShipped = true;

        for (ShipmentPendingDTO item : itemList) {
            int remainingQty = item.getOrderQty();

            // 재고가 부족하면 해당 항목 스킵 (부분 출하 방지)
            if (item.getStockQty() < remainingQty) {
                allShipped = false;
                continue;
            }

            // LOT별 가용 재고 조회 (유통기한 빠른 순)
            List<LotStockDTO> lots = deliveryDAO.getAvailableLots(item.getProductId());

            for (LotStockDTO lot : lots) {
                if (remainingQty <= 0) break;

                int useQty = Math.min(lot.getRemainingQty(), remainingQty);

                // 출하 등록
                ClientDeliveryVO delivery = new ClientDeliveryVO();
                delivery.setClOrderId(item.getClOrderId());
                delivery.setProductId(item.getProductId());
                delivery.setLotNo(lot.getLotNo());
                delivery.setDeliveryQty(useQty);
                delivery.setDeliveryDate(new Date());
                delivery.setDeliveryStatus("배송준비");
                delivery.setClientName(item.getClientName());
                delivery.setTrackingNumber(generateTrackingNumber());
                delivery.setCreatedAt(new Date());
                delivery.setUpdatedAt(new Date());
               // delivery.setIsPdfGenerated(false);

                deliveryDAO.insertDelivery(delivery);

                // 재고 차감
                deliveryDAO.decreaseLotStock(item.getProductId(), lot.getLotNo(), useQty);

                remainingQty -= useQty;
            }

            // 출하 완료 처리
            if (remainingQty <= 0) {
                deliveryDAO.updateOrderDetailStatus(item.getDetailId(), "SHIPPED");
            } else {
                allShipped = false; // 한 항목이라도 미출하가 있으면 전체 미완료 처리
            }
        }

        // 모든 상세 항목이 출하 완료되었을 경우, 수주 마스터 상태도 업데이트
        if (allShipped) {
            deliveryDAO.updateClientOrderStatus(clOrderId, "SHIPPED");
        }
    }

    /**
     * ✅ 수동으로 client_order 상태 업데이트
     */
    @Override
    public void updateClientOrderStatus(String clOrderId, String status) {
        deliveryDAO.updateClientOrderStatus(clOrderId, status);
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
