package com.itwillbs.persistence;


import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.itwillbs.domain.ProductStockTransactionVO;
import com.itwillbs.domain.ProductStockVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.LotStockDTO;

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


     List<LotStockDTO> getAvailableLotsOrdered(String productId);

  // ✅ LOT 번호로 입출고 이력 조회
     List<ProductStockTransactionVO> getLotHistoryByLot(String lotNo);
     ProductStockVO getLotSummary(String lotNo);
     
     void insertTransaction(ProductStockTransactionVO tx);

   //예약시 수량 증감     
     void increaseReservedQty(String productId, String lotNo, int qty);

     void decreaseReservedQty(String productId, String lotNo, int qty);
	
     
     void insertTransaction(String type, String lotNo, int qty, String productId, String clientId, String manager);
	 
     void insertOrUpdateStock(Map<String, Object> param);
     
	void decreaseStockQty(Map<String, Object> stockParam);
	
	void increaseLotStock(String productId, String lotNo, int deliveryQty);


	


}
