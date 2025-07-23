package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.ProductVO;

public interface ProductService {
	
	//제품목록(수주등록용)
	List<ProductVO> getAllProducts();
}
