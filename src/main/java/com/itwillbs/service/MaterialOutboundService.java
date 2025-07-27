package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.MaterialOutboundVO;
import com.itwillbs.domain.SearchCriteria;
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

	
}
