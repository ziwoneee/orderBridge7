package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.domain.MaterialInventoryVO;
import com.itwillbs.domain.SearchCriteria;

/**
 * 자재 재고관리 DAO 인터페이스
 */
public interface MaterialInventoryDAO {

	// 자재 재고 요약 목록 조회 (자재 ID별 1행)
	public List<MaterialInventoryVO> selectInventorySummaryList(SearchCriteria cri) throws Exception;
	
	// 자재 재고 전체 건수 조회 (페이징용)
	int selectInventoryCount(SearchCriteria cri) throws Exception;
	
	// material_id로 LOT 목록 조회
	List<MaterialInventoryVO> selectLotListByMaterialId(String materialId) throws Exception;

	//자재 재고 조회 (박스용)
	List<MaterialInventoryVO> selectAvailableLotsForMaterial(String materialId);
    
	//자재 재고 차감 (박스용)
	void decreaseLotQuantity(String inventoryId, int deductQty);
	
}
