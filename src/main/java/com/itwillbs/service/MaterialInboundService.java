package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.MaterialOutboundVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialInboundDTO;
import com.itwillbs.dto.MaterialInboundItemDTO;
import com.itwillbs.dto.MaterialInboundSummaryDTO;
import com.itwillbs.dto.UnreceivedOrderDTO;

public interface MaterialInboundService {
	
	// 입고 목록 조회
	List<MaterialInboundSummaryDTO> getInboundList(SearchCriteria cri) throws Exception;

	// 목록 전체 수 조회
	int getInboundListCount(SearchCriteria cri) throws Exception;   

	// 미입고 발주건 리스트 조회
	List<MaterialOrderVO> getPendingInboundOrders() throws Exception;

	// 미입고 상태의 발주 목록 조회
	List<UnreceivedOrderDTO> getUnreceivedOrdersPaging(SearchCriteria cri) throws Exception;
	// 미입고 발주 전체 개수
	int getUnreceivedOrdersCount() throws Exception;


	// 미입고 발주건을 입고관리 테이블
	void insertUnreceivedOrders() throws Exception;

	// 선택된 발주 ID 목록의 미입고건만 입고 등록
	void insertSelectedUnreceivedOrders(String[] orderIds) throws Exception;
	
	// 입고 처리
	void processInbound(String inboundId) throws Exception;
	
	
	/**
	 * 개별 자재 입고 항목 처리
	 * - LOT/유통기한/입고수량/창고정보 기반 입고처리
	 */
	void processInboundItem(MaterialInboundItemDTO dto) throws Exception;
	
	
	// 입고 상세 조회 (입고ID 기준)
	MaterialInboundDTO getInboundDetail(String inboundId) throws Exception;;
	
	// 추가입고 생성
	void createAdditionalInbound(String orderItemId) throws Exception;
	
	// 특정 발주 항목의 누적 입고 수량 조회
	int getTotalInboundQuantity(String orderItemId) throws Exception;

	void touchUsageStatusAfterOutbound(String outboundId, List<String> inboundIds);

}
