package com.itwillbs.service;

import com.itwillbs.domain.StockReservationVO;
import com.itwillbs.dto.LotStockDTO;

import java.util.List;

public interface StockReservationService {
    void reserveStock(StockReservationVO vo);                             // 예약 등록
    void releaseStock(String clOrderId, String productId);               // 예약 해제
    int getReservedQty(String clOrderId, String productId);              // 예약 수량 조회
    List<StockReservationVO> getReservationsByOrderId(String clOrderId); // 수주 기준 전체 예약 목록
    
    void deleteReservation(String clOrderId);
    
    void reserveStockByOrderId(String clOrderId);
    
    List<LotStockDTO> getAvailableLotsOrdered(String productId);

    List<String> getReservedOrderIds();


}