package com.itwillbs.service;

import com.itwillbs.domain.ProductVO;
import java.util.List;

public interface ProductMasterService {
    //제품 목록
	List<ProductVO> getAllProducts();
    
	//제품 수정
    void updateProduct(ProductVO productVO);

    //제품 등록
    void insertProduct(ProductVO productVO);
    
    //제품코드 자동생성
    String createNextProductId();

    //제품 소프트 삭제
    void softDeleteProduct(String productId);

}
