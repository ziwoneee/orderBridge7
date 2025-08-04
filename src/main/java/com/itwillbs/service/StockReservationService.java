package com.itwillbs.service;

import com.itwillbs.domain.StockReservationVO;
import com.itwillbs.dto.LotStockDTO;

import java.util.List;

/**
 * ✅ 재고 예약 관련 서비스 인터페이스
 * - 수주번호 기반 예약/해지
 * - 예약 수량 및 LOT 기반 조회
 * - 예약 시 product_stock의 reservedQty 및 availableQty 반영 포함
 */
public interface StockReservationService {

    /**
     * 단일 예약 등록 (직접 호출용)
     */
    void reserveStock(StockReservationVO vo); // 예약 등록

    /**
     * 단일 예약 해제 (제품 단위)
     */
    void releaseStock(String clOrderId, String productId); // 예약 해제

    /**
     * 수주번호 + 제품 기준 예약 수량 조회
     */
    int getReservedQty(String clOrderId, String productId); // 예약 수량 조회

    /**
     * 수주번호 기준 전체 예약 목록 조회
     */
    List<StockReservationVO> getReservationsByOrderId(String clOrderId); // 수주 기준 전체 예약 목록

    /**
     * 수주번호 기준 전체 예약 삭제 + 재고 복원
     */
    void deleteReservation(String clOrderId); // 예약 해제 (전체) + 재고 복원

    /**
     * 수주번호 기준 재고 예약 수행 + 재고 차감
     * @return 
     */
    boolean reserveStockByOrderId(String clOrderId); // 예약 등록 (수주 전체) + 재고 차감

    /**
     * 제품 기준 유통기한 오름차순 LOT 가용 재고 목록 조회
     */
    List<LotStockDTO> getAvailableLotsOrdered(String productId); // LOT별 가용 수량 조회

    /**
     * 예약이 존재하는 수주번호 목록 (중복 제거)
     */
    List<String> getReservedOrderIds(); // 예약된 수주번호 목록

    /**
     * 해당 수주번호가 예약 상태인지 여부
     */
    boolean isReserved(String clOrderId); // 예약 여부 확인

}
