package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.domain.ProductVO;

public interface ProductDAO {
	
	// 제품목록(수주등록용)	
	    List<ProductVO> getAllProducts();
	
}
