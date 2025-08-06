package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.MaterialInventoryVO;
import com.itwillbs.domain.SearchCriteria;

/**
 * 자재 재고관리 서비스 인터페이스
 */
public interface MaterialInventoryService {
	
	// 자재 재고 리스트 조회 (페이징 지원)
	List<MaterialInventoryVO> getInventoryList(SearchCriteria cri) throws Exception;
	
	// 자재 재고 전체 건수 조회 (페이징용)
	int getInventoryCount(SearchCriteria cri) throws Exception;

}
