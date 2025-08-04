package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialInboundSummaryDTO;
import com.itwillbs.dto.UnreceivedOrderDTO;

public interface MaterialInboundService {
	
	// 입고 목록 조회
	List<MaterialInboundSummaryDTO> getInboundList(SearchCriteria cri) throws Exception;

	// 목록 전체 수 조회
	int getInboundListCount(SearchCriteria cri);   

	// 미입고 발주건 리스트 조회
	List<MaterialOrderVO> getPendingInboundOrders();

	// 미입고 상태의 발주 목록 조회
	List<UnreceivedOrderDTO> getUnreceivedOrdersPaging(SearchCriteria cri);
	// 미입고 발주 전체 개수
	int getUnreceivedOrdersCount();


	// 미입고 발주건을 입고관리 테이블
	void insertUnreceivedOrders();

	// 선택된 발주 ID 목록의 미입고건만 입고 등록
	void insertSelectedUnreceivedOrders(String[] orderIds);

}
