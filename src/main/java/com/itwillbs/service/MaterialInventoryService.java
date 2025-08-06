package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.MaterialInventoryVO;
import com.itwillbs.domain.SearchCriteria;

/**
 * 자재 재고관리 서비스 인터페이스
 */
public interface MaterialInventoryService {
	
	// 자재 재고 요약 목록 조회 (자재 1건당 1줄)
	List<MaterialInventoryVO> getInventorySummaryList(SearchCriteria cri) throws Exception;
	
	// 자재 재고 전체 건수 조회 (페이징용)
	int getInventoryCount(SearchCriteria cri) throws Exception;
	

}
