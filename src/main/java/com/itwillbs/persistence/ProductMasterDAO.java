package com.itwillbs.persistence;

import com.itwillbs.domain.ProductVO;
import java.util.List;

public interface ProductMasterDAO {
	
	//제품 목록
    List<ProductVO> selectAllProducts();
    
    //제품 수정
    void updateProduct(ProductVO productVO);
    
    //제품 등록
    void insertProduct(ProductVO productVO);
    
    //제품코드 자동생성
    String selectLastProductId();
    
    //제품 소프트삭제
    void softDeleteProduct(String productId);


}
