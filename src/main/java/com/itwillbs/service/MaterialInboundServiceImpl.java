package com.itwillbs.service;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialInboundSummaryDTO;
import com.itwillbs.persistence.MaterialInboundDAO;

@Service
public class MaterialInboundServiceImpl implements MaterialInboundService{
	
	@Inject
	private MaterialInboundDAO miDAO;

	// 입고 목록 조회
	@Override
	public List<MaterialInboundSummaryDTO> getInboundList(SearchCriteria cri) throws Exception {
		
		return miDAO.getInboundList(cri);
	}
	
	// 목록 전체 수 조회
	@Override
    public int getInboundListCount(SearchCriteria cri) {
		
        return miDAO.getInboundListCount(cri);
    }
	
	
	

}
