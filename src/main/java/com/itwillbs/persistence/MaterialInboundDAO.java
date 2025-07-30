package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.dto.MaterialInboundSummaryDTO;

public interface MaterialInboundDAO {
	
	// 입고 목록 조회
	List<MaterialInboundSummaryDTO> getInboundList();

}
