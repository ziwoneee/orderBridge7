package com.itwillbs.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.itwillbs.domain.ProductStockTransactionVO;
import com.itwillbs.domain.ProductStockVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.LotStockDTO;

public interface ProductStockService {
	//재고현황리스트
    List<ProductStockVO> getStockList(SearchCriteria cri);
    int getStockCount(SearchCriteria cri);
    
    //입출고리스트       
       List<ProductStockTransactionVO> getStockDetail(String productId, String lotNo);

       List<LotStockDTO> getAvailableLotsOrdered(String productId);

       
       //입출고 모달
       List<ProductStockTransactionVO> getStockDetailByLot(String lotNo);

      
       //
       ProductStockVO getLotSummary(String lotNo);
	
       //재고저장
       void insertTransaction(String type, String lotNo, int qty, String productId, String clientId, String manager,
			String inboundId, String outboundId, String clOrderId);
	
     
}
