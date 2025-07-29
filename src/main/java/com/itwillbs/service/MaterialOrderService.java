package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;

/**
 * 자재 발주 서비스 인터페이스
 * - 발주 목록 및 개수 조회 등의 비즈니스 로직 정의
 */
public interface MaterialOrderService {
	
	// 발주 목록 조회
    List<MaterialOrderVO> getOrderList(SearchCriteria cri);

    // 총 건수 조회 (페이징)
    int getTotalCount(SearchCriteria cri);

    // '발주등록' 상태 건수
    int getRegisteredCount(SearchCriteria cri);

}
