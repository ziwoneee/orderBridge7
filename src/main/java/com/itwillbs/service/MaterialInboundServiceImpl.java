package com.itwillbs.service;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.itwillbs.domain.MaterialOrderVO;
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
	
	// 입고되지 않은 발주건들만 조회 (inbound에 없는 order)
	@Override
    public List<MaterialOrderVO> getPendingInboundOrders() {
        return miDAO.selectPendingInboundOrders();
    }
	
	/**
     * 아직 입고되지 않은 발주건 목록 조회
     * - 발주 항목 중 한 번도 입고처리된 적 없는 건만 조회
     */
    @Override
    public List<MaterialOrderVO> getUnreceivedOrders() {
        return miDAO.selectUnreceivedOrders();
    }
    
	

}
