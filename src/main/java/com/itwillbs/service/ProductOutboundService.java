package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.ProductOutboundVO;
import com.itwillbs.domain.SearchCriteria;

public interface ProductOutboundService {
	 void registerOutbound(ProductOutboundVO vo);
	 
	 List<ProductOutboundVO> searchOutboundList(SearchCriteria cri);
	 int countOutboundList(SearchCriteria cri);
	 

}
