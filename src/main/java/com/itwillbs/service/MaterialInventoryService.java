package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.MaterialInventoryVO;

/**
 * 자재 재고관리 서비스 인터페이스
 */
public interface MaterialInventoryService {
	
	// 자재 재고 리스트 조회
	List<MaterialInventoryVO> getInventoryList(String materialId, 
											   String materialName,
											   String materialType,
											   String sortColumn,
											   String sortDirection) throws Exception;

}
