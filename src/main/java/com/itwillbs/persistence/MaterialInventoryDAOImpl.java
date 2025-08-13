package com.itwillbs.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.MaterialInventoryVO;
import com.itwillbs.domain.SearchCriteria;

@Repository
public class MaterialInventoryDAOImpl implements MaterialInventoryDAO {
	
	@Inject
	private SqlSession sqlSession;
	
	private static final String NAMESPACE = "com.itwillbs.mapper.MaterialInventoryMapper.";

	// 자재 재고 요약 목록 조회 (자재 ID별 1행)
	@Override
	public List<MaterialInventoryVO> selectInventorySummaryList(SearchCriteria cri) throws Exception {
		// MyBatis 매퍼 호출: selectInventorySummaryList
		return sqlSession.selectList(NAMESPACE + "selectInventorySummaryList", cri);
	}

	// 자재 재고 전체 건수 조회 (페이징용)
	@Override
	public int selectInventoryCount(SearchCriteria cri) throws Exception {
		return sqlSession.selectOne(NAMESPACE + "selectInventoryCount", cri);
	}
	
	// 상태별 카운트 조회
	@Override
    public Map<String, Object> selectStatusCounts() throws Exception {
        return sqlSession.selectOne(NAMESPACE + "selectStatusCounts");
    }

	
	// material_id로 LOT 목록 조회
	@Override
	public List<MaterialInventoryVO> selectLotListByMaterialId(String materialId) throws Exception {
		 return sqlSession.selectList(NAMESPACE + "selectLotListByMaterialId", materialId);
	}
	
	// 자재 재고 조회(박스용)
	@Override
    public List<MaterialInventoryVO> selectAvailableLotsForMaterial(String materialId) {
        return sqlSession.selectList(NAMESPACE + "selectAvailableLotsForMaterial", materialId);
    }
	
	// 자재 재고 차감(박스용)
    @Override
    public void decreaseLotQuantity(String inventoryId, int deductQty) {
        java.util.Map<String, Object> param = new java.util.HashMap<>();
        param.put("inventoryId", inventoryId);
        param.put("deductQty", deductQty);
        sqlSession.update(NAMESPACE + "decreaseLotQuantity", param);
    }
    
    
    // 자재 기본 정보 조회 (새로 추가)
    @Override
    public MaterialInventoryVO selectMaterialInfo(String materialId) throws Exception {
        return sqlSession.selectOne(NAMESPACE + "selectMaterialInfo", materialId);
    }
	

	
	
	

}
