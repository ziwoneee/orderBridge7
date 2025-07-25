package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.MaterialOutboundVO;

// 자재 출고 관리 서비스 인터패이스
public interface MaterialOutboundService {
	
	// 출고 목록 조회
	List<MaterialOutboundVO> getOutboundList() throws Exception;

}
