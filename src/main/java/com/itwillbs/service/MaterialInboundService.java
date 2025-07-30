package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialInboundSummaryDTO;

public interface MaterialInboundService {
	
	// 입고 목록 조회
	List<MaterialInboundSummaryDTO> getInboundList(SearchCriteria cri) throws Exception;
	
	// 목록 전체 수 조회
	int getInboundListCount(SearchCriteria cri);   
	
	// 미입고 발주건 리스트 조회
	List<MaterialOrderVO> getPendingInboundOrders();
	
	// 미입고 상태의 발주 목록 조회
    List<MaterialOrderVO> getUnreceivedOrders();
}
