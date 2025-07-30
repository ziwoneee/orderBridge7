package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialInboundSummaryDTO;

public interface MaterialInboundDAO {
	
	// 입고 목록 조회
	List<MaterialInboundSummaryDTO> getInboundList(SearchCriteria cri) throws Exception;
	
	// 목록 전체 수 조회
	int getInboundListCount(SearchCriteria cri);  
	
	// 아직 입고되지 않은 발주건 리스트 조회
	List<MaterialOrderVO> selectPendingInboundOrders();
	
	// 미입고 발주건 목록 조회
    List<MaterialOrderVO> selectUnreceivedOrders();

}
