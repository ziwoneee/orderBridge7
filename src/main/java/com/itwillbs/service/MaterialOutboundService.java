package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.MaterialOutboundVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.WorkOrderVO;
import com.itwillbs.dto.MaterialOutboundDetailDTO;
import com.itwillbs.dto.MaterialOutboundSummaryDTO;

// 자재 출고 관리 서비스 인터패이스
public interface MaterialOutboundService {
	
	// 1-1. 출고 목록 조회
	List<MaterialOutboundSummaryDTO> getOutboundList(SearchCriteria cri) throws Exception;

	// 1-2. 전체 출고 수 조회 (페이징 계산용)
	int getMaterialOutboundCount(SearchCriteria cri) throws Exception;
	
	// 출고 상세 조회
	MaterialOutboundDetailDTO getOutboundDetail(String outboundId) throws Exception;

	// 출고 처리 메서드 (실재고 확인 후 처리)
	boolean processOutbound(String outboundId) throws Exception;

	// 자재 재고 차감
	void updateOutboundItemStock(String outboundId, String materialId, int qty) throws Exception;
	
	// 작업지시서 목록 조회 서비스
	List<WorkOrderVO> getWaitingOrders() throws Exception;
	
	// 작업지시서 기반 출고 상세정보(자재 목록 포함) 조회
	MaterialOutboundDetailDTO getOutboundDetailByWorkOrder(String workOrderNo) throws Exception;

	// 출고 등록 서비스 메서드
	void registerOutbound(MaterialOutboundDetailDTO dto) throws Exception;



    
}
