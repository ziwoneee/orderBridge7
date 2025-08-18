package com.itwillbs.service;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
	
	
	@Override public List<AiPredictionLogDTO> search(String q, Date from, Date to, int limit){
	  return mapper.search(q, from, to, limit);
   }

}
