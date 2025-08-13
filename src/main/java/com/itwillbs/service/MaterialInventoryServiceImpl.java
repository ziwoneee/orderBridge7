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
	
	
	// 자재 재고 요약 목록 조회 (자재 1건당 1줄)
	@Override
	public List<MaterialInventoryVO> getInventorySummaryList(SearchCriteria cri) throws Exception {
	    // DAO를 통해 자재 요약 재고 목록 조회
	    return miDAO.selectInventorySummaryList(cri);
	}
	

	// 자재 재고 전체 건수 조회 (페이징용)
	@Override
	public int getInventoryCount(SearchCriteria cri) throws Exception {
		return miDAO.selectInventoryCount(cri);
	}


	// material_id로 LOT 목록 조회
	@Override
	public List<MaterialInventoryVO> getLotListByMaterialId(String materialId) throws Exception {
		return miDAO.selectLotListByMaterialId(materialId);
	}
	
	
	
	// 자재 기본 정보 조회 (새로 추가)
	@Override
	public MaterialInventoryVO getMaterialInfo(String materialId) throws Exception {
	    return miDAO.selectMaterialInfo(materialId);
	}
	
	
	
	
	
	
	
	
	
	
	
	
} // MaterialInventoryServiceImpl 끝















