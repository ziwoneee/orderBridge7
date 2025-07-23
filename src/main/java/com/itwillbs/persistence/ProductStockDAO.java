package com.itwillbs.persistence;


import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.itwillbs.domain.ProductStockTransactionVO;
import com.itwillbs.domain.ProductStockVO;
import com.itwillbs.domain.SearchCriteria;

public interface ProductStockDAO {
	//재고현황리스트
    List<ProductStockVO> getStockList(SearchCriteria cri);
    int getStockCount(SearchCriteria cri);
    
    //입출고 현황 업데이트
    void upsertStockQty(String productId, String lotNo, int qty);
    void decreaseStockQty(String productId, String lotNo, int qty);
    
    //입출고 모달창    
     List<ProductStockTransactionVO> getStockDetail(@Param("productId") String productId,
                                                   @Param("lotNo") String lotNo);


}
