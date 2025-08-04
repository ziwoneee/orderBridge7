package com.itwillbs.service;

import com.itwillbs.domain.ClientOrderDetailVO;
import com.itwillbs.domain.StockReservationVO;
import com.itwillbs.dto.LotStockDTO;
import com.itwillbs.persistence.ClientDeliveryDAO;
import com.itwillbs.persistence.ClientOrderDAO;
import com.itwillbs.persistence.ProductStockDAO;
import com.itwillbs.persistence.StockReservationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class StockReservationServiceImpl implements StockReservationService {

    @Autowired
    private StockReservationDAO reservationDAO;

    @Autowired
    private ClientDeliveryDAO deliveryDAO;

    @Autowired
    private ProductOutboundService outboundService;

    @Autowired
    private StockReservationService reservationService;

    @Autowired
    private ProductStockDAO stockDAO;

    @Autowired
    private ClientOrderDAO orderDAO;

    /**
     * 단일 예약 등록
     */
    @Override
    public void reserveStock(StockReservationVO vo) {
        reservationDAO.insertReservation(vo);
        stockDAO.increaseReservedQty(vo.getProductId(), vo.getLotNo(), vo.getReservedQty()); // ✅ lotNo 추가
    }

    /**
     * 단일 예약 해제 (제품 기준)
     */
    @Override
    public void releaseStock(String clOrderId, String productId) {
        // 수주번호 + 제품ID 기준 예약 목록 조회
        List<StockReservationVO> reservations = reservationDAO.getReservationsByOrderId(clOrderId);
        
        for (StockReservationVO r : reservations) {
            if (r.getProductId().equals(productId)) {
                // 예약 삭제
                reservationDAO.deleteReservation(clOrderId, productId);

                // 재고 복원 (LOT 단위)
                stockDAO.decreaseReservedQty(r.getProductId(), r.getLotNo(), r.getReservedQty());
            }
        }
    }


    /**
     * 예약 수량 조회 (수주번호 + 제품 기준)
     */
    @Override
    public int getReservedQty(String clOrderId, String productId) {
        return reservationDAO.getReservedQty(clOrderId, productId);
    }

    /**
     * 수주 전체 예약 목록 조회
     */
    @Override
    public List<StockReservationVO> getReservationsByOrderId(String clOrderId) {
        return reservationDAO.getReservationsByOrderId(clOrderId);
    }

    /**
     * 수주 전체 예약 등록 및 재고 반영
     */
    @Override
    public boolean reserveStockByOrderId(String clOrderId) {
        reservationDAO.deleteByOrderId(clOrderId);

        List<ClientOrderDetailVO> orderDetails = orderDAO.getOrderDetailsByOrderId(clOrderId);
        boolean success = true;

        for (ClientOrderDetailVO detail : orderDetails) {
            String productId = detail.getProductId();
            int requiredQty = detail.getOrderQty();

            List<LotStockDTO> lotStocks = stockDAO.getAvailableLotsOrdered(productId);
            boolean reserved = false;

            for (LotStockDTO lot : lotStocks) {
                if (requiredQty <= 0) break;

                int allocQty = Math.min(requiredQty, lot.getAvailableQty());
                if (allocQty <= 0) continue;

                StockReservationVO reservation = new StockReservationVO();
                reservation.setClOrderId(clOrderId);
                reservation.setDetailId(detail.getDetailId());
                reservation.setProductId(productId);
                reservation.setClientId(detail.getClientId());
                reservation.setLotNo(lot.getLotNo());
                reservation.setReservedQty(allocQty);
                reservation.setManager("SYSTEM");
                reservation.setCreatedAt(new Date());

                reservationDAO.insertReservation(reservation);
                stockDAO.increaseReservedQty(productId, lot.getLotNo(), allocQty);

                requiredQty -= allocQty;
                reserved = true;
            }

            if (!reserved || requiredQty > 0) {
                System.out.println("⚠ 예약 실패: 제품 " + productId + " - 부족 수량: " + requiredQty);
                success = false;
            }
        }

        return success; // ✅ boolean 반환 추가
    }


    /**
     * 수주 전체 예약 해제 및 재고 복원
     */
    @Override
    public void deleteReservation(String clOrderId) {
        // 예약 내역 조회
        List<StockReservationVO> reservations = reservationDAO.getReservationsByOrderId(clOrderId);

        for (StockReservationVO r : reservations) {
            // 예약 해제
            reservationDAO.deleteReservation(r.getClOrderId(), r.getProductId());

            // 예약 수량 만큼 재고에서 복원
            stockDAO.decreaseReservedQty(r.getProductId(), r.getLotNo(), r.getReservedQty());
        }
    }

    /**
     * 제품 기준 LOT별 가용 수량 조회
     */
    @Override
    public List<LotStockDTO> getAvailableLotsOrdered(String productId) {
        return reservationDAO.getAvailableLotsOrdered(productId);
    }

    /**
     * 예약 중인 수주번호 목록 조회
     */
    @Override
    public List<String> getReservedOrderIds() {
        return reservationDAO.getAllReservedOrderIds();
    }

    /**
     * 해당 수주번호가 예약 상태인지 여부 확인
     */
    @Override
    public boolean isReserved(String clOrderId) {
        return reservationDAO.countReservationsByOrderId(clOrderId) > 0;
    }
}
