package com.itwillbs.service;

import com.itwillbs.domain.ProductionResultVO;

public interface ProductionResultService {

	// 생산결과 등록 메서드 (아름 시작)
	void insertResult(ProductionResultVO vo);

	void saveAllToInbound(); 
	
	// 생산결과 등록 메서드 (아름 끝)
}
