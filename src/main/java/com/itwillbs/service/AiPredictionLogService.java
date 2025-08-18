package com.itwillbs.service;

import java.util.Date;
import java.util.List;

import com.itwillbs.dto.AiPredictionLogDTO;

public interface AiPredictionLogService {
	
	void log(AiPredictionLogDTO dto);
	List<AiPredictionLogDTO> search(String q, Date from, Date to, int limit);

}
