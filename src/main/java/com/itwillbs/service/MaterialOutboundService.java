package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.MaterialOutboundVO;
import com.itwillbs.dto.MaterialOutboundSummaryDTO;

// 자재 출고 관리 서비스 인터패이스
public interface MaterialOutboundService {
	
	// 출고 목록 조회
	List<MaterialOutboundSummaryDTO> getOutboundList() throws Exception;

}
