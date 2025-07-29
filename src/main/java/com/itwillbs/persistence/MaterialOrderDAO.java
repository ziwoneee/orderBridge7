package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;

/**
 * 자재 발주 DAO 인터페이스
 * - DB와 직접 연결되는 쿼리 메서드 정의
 */
public interface MaterialOrderDAO {
	
	 // 발주 목록 조회
    List<MaterialOrderVO> getOrderList(SearchCriteria cri);

    // 전체 건수 조회
    int getTotalCount(SearchCriteria cri);

    // 등록 상태 건수 조회
    int getRegisteredCount(SearchCriteria cri);
    
}
