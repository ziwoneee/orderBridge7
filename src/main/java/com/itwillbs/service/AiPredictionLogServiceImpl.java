package com.itwillbs.service;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.AiPredictionLogDTO;
import com.itwillbs.mapper.AiPredictionLogMapper;

@Service
public class AiPredictionLogServiceImpl implements AiPredictionLogService {
	
	@Inject
	private AiPredictionLogMapper mapper;
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override public void log(AiPredictionLogDTO dto){ 
		mapper.insert(dto);
	}
	
	// 페이징용 새 메서드들
	@Override
	public int getLogCount(String q, Date from, Date to) {
		return mapper.getLogCount(q, from, to);
	}
	
	@Override
	public List<AiPredictionLogDTO> searchWithPaging(String q, Date from, Date to, SearchCriteria cri) {
		return mapper.searchWithPaging(q, from, to, cri);
	}
}
