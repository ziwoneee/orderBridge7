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

    @Override
    public void reserveStock(StockReservationVO vo) {
        reservationDAO.insertReservation(vo);
    }

    @Override
    public void releaseStock(String clOrderId, String productId) {
        reservationDAO.deleteReservation(clOrderId, productId);
    }

    @Override
    public int getReservedQty(String clOrderId, String productId) {
        return reservationDAO.getReservedQty(clOrderId, productId);
    }

    @Override
    public List<StockReservationVO> getReservationsByOrderId(String clOrderId) {
        return reservationDAO.getReservationsByOrderId(clOrderId);
    }

    @Override
    public void deleteReservation(String clOrderId) {
        reservationDAO.deleteByOrderId(clOrderId);
    }

    @Override
    public void reserveStockByOrderId(String clOrderId) {
    	  // ✅ 기존 예약 삭제 (중복 방지)
        reservationDAO.deleteByOrderId(clOrderId);    	
    	
        // 1. 수주 상세 항목 조회
        List<ClientOrderDetailVO> orderDetails = orderDAO.getOrderDetailsByOrderId(clOrderId);

        for (ClientOrderDetailVO detail : orderDetails) {
            String productId = detail.getProductId();                    
            int requiredQty = detail.getOrderQty();

            // 🔽 여기 로그 추가
            System.out.println("▶ 수주번호: " + clOrderId);
            System.out.println("▶ 제품ID: " + productId + ", 수주 수량: " + requiredQty);

            // 2. LOT별 재고 조회 (유통기한 빠른 순)
            List<LotStockDTO> lotStocks = stockDAO.getAvailableLotsOrdered(productId);

            int totalAvailable = 0;
            for (LotStockDTO lot : lotStocks) {
                totalAvailable += lot.getAvailableQty();
                System.out.println("   - LOT: " + lot.getLotNo() + ", 가용 수량: " + lot.getAvailableQty());
            }

            System.out.println("▶ 전체 가용 수량 합계: " + totalAvailable);

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
                requiredQty -= allocQty;

                // 🔽 예약된 수량 출력
                System.out.println("   → 예약된 LOT: " + lot.getLotNo() + ", 예약 수량: " + allocQty + ", 남은 수량: " + requiredQty);
            }

            if (requiredQty > 0) {
                System.out.println("⚠ 재고 부족! 예약 실패 - 제품ID: " + productId + ", 부족 수량: " + requiredQty);
          }
        }
    }

   

    @Override
    public List<LotStockDTO> getAvailableLotsOrdered(String productId) {
        return reservationDAO.getAvailableLotsOrdered(productId);
    }

    
    @Override
    public List<String> getReservedOrderIds() {
        return reservationDAO.getAllReservedOrderIds(); // 중복 제거된 수주번호 목록
    }

    
}

