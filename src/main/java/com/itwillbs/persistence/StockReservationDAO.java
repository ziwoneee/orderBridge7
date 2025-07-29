package com.itwillbs.persistence;

import com.itwillbs.domain.StockReservationVO;
import com.itwillbs.dto.LotStockDTO;

import java.util.List;
import java.util.Map;

public interface StockReservationDAO {
    void insertReservation(StockReservationVO vo);                         // 예약 등록
    void deleteReservation(String clOrderId, String productId);           // 예약 해제
    int getReservedQty(String clOrderId, String productId);               // 예약 수량 조회
    List<StockReservationVO> getReservationsByOrderId(String clOrderId);  // 수주번호 기준 전체 예약 조회
    
    void deleteByOrderId(String clOrderId);
    
    List<LotStockDTO> getAvailableLotsOrdered(String productId);
    
    int getReservedQtyByProductAndLot(Map<String, Object> param);
    
    List<String> getAllReservedOrderIds();

}