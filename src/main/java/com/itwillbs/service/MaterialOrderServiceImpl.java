package com.itwillbs.service;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.persistence.MaterialOrderDAO;

/**
 * 자재 발주 서비스 구현체
 * - DAO를 호출하여 비즈니스 로직 처리
 */
@Service
public class MaterialOrderServiceImpl implements MaterialOrderService {
	
	@Inject
	private MaterialOrderDAO mOrderDAO;

	// 발주 목록 조회
	@Override
    public List<MaterialOrderVO> getOrderList(SearchCriteria cri) {
        return mOrderDAO.getOrderList(cri);
    }

	// 총 건수 조회 (페이징)
    @Override
    public int getTotalCount(SearchCriteria cri) {
        return mOrderDAO.getTotalCount(cri);
    }

    // '발주등록' 상태 건수
    @Override
    public int getRegisteredCount(SearchCriteria cri) {
        return mOrderDAO.getRegisteredCount(cri);
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
