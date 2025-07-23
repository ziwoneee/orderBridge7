package com.itwillbs.service;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.itwillbs.domain.MaterialInventoryVO;
import com.itwillbs.persistence.MaterialInventoryDAO;

/**
 * 자재 재고관리 서비스 구현체
 */
@Service
public class MaterialInventoryServiceImpl implements MaterialInventoryService {
	
	
	// DAO 주입
	@Inject
	private MaterialInventoryDAO miDAO;
	
	

	// 자재 재고 목록 조회
	@Override
	public List<MaterialInventoryVO> getInventoryList(String materialId,
													  String materialName,
													  String materialType,
													  String sortColumn,
													  String sortDirection) throws Exception {
		
		return miDAO.selectInventoryList(materialId, materialName, materialType, sortColumn, sortDirection);
	}
	
	

	
	
	
	
	
	
	
	
	
	
	
	
} // MaterialInventoryServiceImpl 끝















