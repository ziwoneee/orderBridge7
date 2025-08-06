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

	// 자재 목록 조회 (페이징 지원)
	@Override
	public List<MaterialInventoryVO> selectInventoryList(SearchCriteria cri) throws Exception {
		return sqlSession.selectList(NAMESPACE + "selectInventoryList", cri);
	}
	
	// 자재 재고 전체 건수 조회 (페이징용)
	@Override
	public int selectInventoryCount(SearchCriteria cri) throws Exception {
		return sqlSession.selectOne(NAMESPACE + "selectInventoryCount", cri);
	}
	
	

}
