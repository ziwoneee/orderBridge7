package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.domain.MaterialInventoryVO;

/**
 * 자재 재고관리 DAO 인터페이스
 */
public interface MaterialInventoryDAO {

	// 자재 재고 목록 조회
	List<MaterialInventoryVO> selectInventoryList(String materialId,
												  String materialName,
												  String materialType,
												  String sortColumn,
												  String sortDirection) throws Exception;

}
