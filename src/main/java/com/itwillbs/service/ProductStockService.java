package com.itwillbs.service;

import java.util.List;
import java.util.Map;

import com.itwillbs.domain.ProductStockTransactionVO;
import com.itwillbs.domain.ProductStockVO;
import com.itwillbs.domain.SearchCriteria;

public interface ProductStockService {
	//재고현황리스트
    List<ProductStockVO> getStockList(SearchCriteria cri);
    int getStockCount(SearchCriteria cri);
    
    //입출고리스트       
       List<ProductStockTransactionVO> getStockDetail(String productId, String lotNo);


}
