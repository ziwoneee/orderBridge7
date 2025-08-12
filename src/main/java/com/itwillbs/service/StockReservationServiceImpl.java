package com.itwillbs.service;

import com.itwillbs.domain.ClientOrderDetailVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.StockReservationVO;
import com.itwillbs.dto.LotStockDTO;
import com.itwillbs.dto.ReservationDetailDTO;
import com.itwillbs.persistence.ClientDeliveryDAO;
import com.itwillbs.persistence.ClientOrderDAO;
import com.itwillbs.persistence.ProductStockDAO;
import com.itwillbs.persistence.StockReservationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    @Autowired
    private ProductStockService productStockService; 

  

    /**
     * 단일 예약 등록
     */
    @Transactional
    @Override
    public void reserveStock(StockReservationVO vo) {
        // ✅ qty 검증 (primitive int이면 null 체크 불가)
        int reservedQty = vo.getReservedQty();
        if (reservedQty <= 0) {
            throw new IllegalArgumentException("예약 수량이 0 이하입니다.");
        }

        // ✅ productId 검증
        String productId = vo.getProductId();
        if (productId == null || productId.isEmpty()) {
            throw new IllegalArgumentException("productId가 필요합니다.");
        }

        // ✅ LOT 미지정 → 임박 LOT부터 자동 배정
        if (vo.getLotNo() == null || vo.getLotNo().isEmpty()) {   // ← '||' 사용
            int remaining = reservedQty;

            // 임박순 + 만료 제외 + FOR UPDATE (Mapper에 준비된 메서드로 교체)
            List<LotStockDTO> lots = stockDAO.getAvailableLotsOrdered(productId);
            if (lots == null || lots.isEmpty()) {
                throw new IllegalStateException("가용 LOT 없음: " + productId);
            }

            for (LotStockDTO lot : lots) {
                if (remaining <= 0) break;

                int alloc = Math.min(remaining, Math.max(lot.getAvailableQty(), 0));
                if (alloc <= 0) continue;

                StockReservationVO r = new StockReservationVO();
                r.setClOrderId(vo.getClOrderId());
                r.setDetailId(vo.getDetailId());
                r.setProductId(productId);
                r.setClientId(vo.getClientId());
                r.setLotNo(lot.getLotNo());
                r.setReservedQty(alloc);
                r.setManager(vo.getManager() == null ? "SYSTEM" : vo.getManager());
                r.setCreatedAt(new java.util.Date());

                reservationDAO.insertReservation(r);
                stockDAO.increaseReservedQty(productId, lot.getLotNo(), alloc);

                productStockService.insertTransaction(
                    "RESERVE", r.getLotNo(), alloc, productId,
                    r.getClientId(), r.getManager(), null, null, r.getClOrderId()
                );

                remaining -= alloc;
            }

            if (remaining > 0) {
                // 보상 롤백
                List<StockReservationVO> rollback = reservationDAO.getReservationsByOrderId(vo.getClOrderId());
                for (StockReservationVO r : rollback) {
                    if (!productId.equals(r.getProductId())) continue;
                    reservationDAO.deleteReservation(r.getClOrderId(), r.getProductId());
                    stockDAO.decreaseReservedQty(r.getProductId(), r.getLotNo(), r.getReservedQty());
                    productStockService.insertTransaction(
                        "CANCEL_RESERVE", r.getLotNo(), r.getReservedQty(), r.getProductId(),
                        r.getClientId(), r.getManager(), null, null, r.getClOrderId()
                    );
                }
                throw new IllegalStateException("자재 부족: " + productId + ", 부족=" + remaining);
            }
            return;
        }

        // ✅ LOT 지정된 경우: 기존 단일 LOT 예약
        reservationDAO.insertReservation(vo);
        stockDAO.increaseReservedQty(productId, vo.getLotNo(), reservedQty);
        productStockService.insertTransaction(
            "RESERVE", vo.getLotNo(), reservedQty, productId,
            vo.getClientId(), vo.getManager(), null, null, vo.getClOrderId()
        );
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
               
                // ✅ 예약 해제 이력 기록
                productStockService.insertTransaction(
                    "CANCEL_RESERVE",
                    r.getLotNo(),
                    r.getReservedQty(),
                    r.getProductId(),
                    r.getClientId(),
                    r.getManager(),
                    null,
                    null,
                    r.getClOrderId()
                );
            
            
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
    @Transactional
    @Override
    public boolean reserveStockByOrderId(String clOrderId) {
        // 기존 예약 삭제
        reservationDAO.deleteByOrderId(clOrderId);

        List<ClientOrderDetailVO> orderDetails = orderDAO.getOrderDetailsByOrderId(clOrderId);
        boolean success = true;

        // ✅ 임시 저장 리스트 (rollback 시 사용)
        List<StockReservationVO> tempReservations = new java.util.ArrayList<>();

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

                tempReservations.add(reservation); 
                
                productStockService.insertTransaction(
                        "RESERVE",
                        lot.getLotNo(),
                        allocQty,
                        productId,
                        detail.getClientId(),
                        "SYSTEM",         
                        null,
                        null,
                        clOrderId
                    );

                requiredQty -= allocQty;
                reserved = true;
            }

            if (!reserved || requiredQty > 0) {
                System.out.println("⚠ 예약 실패: 제품 " + productId + " - 부족 수량: " + requiredQty);
                success = false;
                break; // 더 이상 반복할 필요 없음
            }
        }

        // ✅ 실패 시 롤백 수행
        if (!success) {
            for (StockReservationVO r : tempReservations) {
                reservationDAO.deleteReservation(r.getClOrderId(), r.getProductId());
                stockDAO.decreaseReservedQty(r.getProductId(), r.getLotNo(), r.getReservedQty());
            }
        }

        return success;
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
        
         // ✅ 예약 전체 해제 이력 기록
            productStockService.insertTransaction(
                "CANCEL_RESERVE",
                r.getLotNo(),
                r.getReservedQty(),
                r.getProductId(),
                r.getClientId(),
                r.getManager(),
                null,
                null,
                r.getClOrderId()
            );
        
        
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
    
    //예약내역확인
    @Override
    public List<StockReservationVO> getFilteredReservationList(SearchCriteria cri) {
        return reservationDAO.getFilteredReservationList(cri);
    }

    @Override
    public int countFilteredReservationList(SearchCriteria cri) {
        return reservationDAO.countFilteredReservationList(cri);
    }
    
    //예약상세 모달
    @Override
    public ReservationDetailDTO getReservationDetail(String lotNo, String clOrderId) {
        return reservationDAO.selectReservationDetailForModal(lotNo, clOrderId);
    }

}
