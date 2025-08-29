package com.itwillbs.persistence;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.StockReservationVO;
import com.itwillbs.dto.LotStockDTO;
import com.itwillbs.dto.ReservationDetailDTO;

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
    
    int countReservationsByOrderId(String clOrderId);

    
    //예약내역확인
    List<StockReservationVO> getFilteredReservationList(SearchCriteria cri);
   
    int countFilteredReservationList(SearchCriteria cri);
    
    //예약상세 모달
    // ✅ 모달 상세 (LOT + 수주 단건)
    ReservationDetailDTO selectReservationDetailForModal(String lotNo, String clOrderId);


}