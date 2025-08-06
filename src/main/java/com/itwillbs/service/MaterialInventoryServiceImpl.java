package com.itwillbs.service;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.itwillbs.domain.MaterialInventoryVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.persistence.MaterialInventoryDAO;

/**
 * 자재 재고관리 서비스 구현체
 */
@Service
public class MaterialInventoryServiceImpl implements MaterialInventoryService {
	
	
	// DAO 주입
	@Inject
	private MaterialInventoryDAO miDAO;
	
	

	// 자재 재고 목록 조회 (페이징 지원)
	@Override
	public List<MaterialInventoryVO> getInventoryList(SearchCriteria cri) throws Exception {
		return miDAO.selectInventoryList(cri);
	}

	// 자재 재고 전체 건수 조회 (페이징용)
	@Override
	public int getInventoryCount(SearchCriteria cri) throws Exception {
		return miDAO.selectInventoryCount(cri);
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
} // MaterialInventoryServiceImpl 끝















