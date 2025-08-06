package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.domain.MaterialInventoryVO;
import com.itwillbs.domain.SearchCriteria;

/**
 * 자재 재고관리 DAO 인터페이스
 */
public interface MaterialInventoryDAO {

	// 자재 재고 목록 조회 (페이징 지원)
	List<MaterialInventoryVO> selectInventoryList(SearchCriteria cri) throws Exception;
	
	// 자재 재고 전체 건수 조회 (페이징용)
	int selectInventoryCount(SearchCriteria cri) throws Exception;
	
}
