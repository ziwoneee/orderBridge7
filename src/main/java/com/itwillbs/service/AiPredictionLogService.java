package com.itwillbs.service;

import java.util.Date;
import java.util.List;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.AiPredictionLogDTO;

public interface AiPredictionLogService {
	
	void log(AiPredictionLogDTO dto);
	
	// 페이징용 새 메서드들
	int getLogCount(String q, Date from, Date to);
	List<AiPredictionLogDTO> searchWithPaging(String q, Date from, Date to, SearchCriteria cri);

}
