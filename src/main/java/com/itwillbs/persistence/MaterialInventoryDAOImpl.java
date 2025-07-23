package com.itwillbs.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.MaterialInventoryVO;

@Repository
public class MaterialInventoryDAOImpl implements MaterialInventoryDAO {
	
	@Inject
	private SqlSession sqlSession;
	
	private static final String NAMESPACE = "com.itwillbs.mapper.MaterialInventoryMapper.";

	// 자재 목록 조회
	@Override
	public List<MaterialInventoryVO> selectInventoryList(String materialId,
														 String materialName,
														 String materialType,
														 String sortColumn,
														 String sortDirection) throws Exception {
		
		// 검색 조건을 담을 Map 생성
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("materialId", materialId);
		paramMap.put("materialName", materialName);
		paramMap.put("materialType", materialType);
		
		// MyBatis 매퍼 호출
		return sqlSession.selectList(NAMESPACE + "selectInventoryList", paramMap);
	}
	
	

}
