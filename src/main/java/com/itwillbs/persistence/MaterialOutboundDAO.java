package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.domain.MaterialOutboundVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialOutboundDetailDTO;
import com.itwillbs.dto.MaterialOutboundItemDTO;
import com.itwillbs.dto.MaterialOutboundSummaryDTO;

// 자재 출고 DAO 인터페이스 
public interface MaterialOutboundDAO {
	
	// 출고 목록 조회
	List<MaterialOutboundSummaryDTO> getOutboundList(SearchCriteria cri) throws Exception;

	// 전체 건수 조회
    int getMaterialOutboundCount(SearchCriteria cri) throws Exception;
    
    // 출고 상세 기본 정보 조회
    MaterialOutboundDetailDTO getOutboundDetail(String outboundId) throws Exception;

    // 출고 자재 항목 리스트 조회
    List<MaterialOutboundItemDTO> getOutboundItemList(String outboundId) throws Exception;

    
}
