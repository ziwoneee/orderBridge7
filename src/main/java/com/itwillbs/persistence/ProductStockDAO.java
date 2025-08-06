package com.itwillbs.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.itwillbs.domain.ProductStockTransactionVO;
import com.itwillbs.domain.ProductStockVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.LotStockDTO;

public interface ProductStockDAO {

    // ✅ 재고현황
    List<ProductStockVO> getStockList(SearchCriteria cri);
    int getStockCount(SearchCriteria cri);

    // ✅ 재고 입출고 업데이트
    void upsertStockQty(String productId, String lotNo, int qty);
    void decreaseStockQty(String productId, String lotNo, int qty);
    void decreaseStockQty(Map<String, Object> stockParam);
    void insertOrUpdateStock(Map<String, Object> param);

    // ✅ 재고 예약 처리
    void increaseReservedQty(String productId, String lotNo, int qty);
    void decreaseReservedQty(String productId, String lotNo, int qty);

    // ✅ 입출고 이력
    List<ProductStockTransactionVO> getStockDetail(@Param("productId") String productId,
                                                   @Param("lotNo") String lotNo);
    List<ProductStockTransactionVO> getLotHistoryByLot(String lotNo);
    ProductStockVO getLotSummary(String lotNo);

    // ✅ LOT 재고 조회
    List<LotStockDTO> getAvailableLotsOrdered(String productId);

    // ✅ 입출고 이력 저장 (VO 방식)
    void insertTransaction(ProductStockTransactionVO tx);

    // ✅ 중복 이력 확인
    boolean existsTransaction(Map<String, Object> param);

    // ✅ 출하 취소 재고 복원
    void increaseLotStock(String productId, String lotNo, int qty);
}
