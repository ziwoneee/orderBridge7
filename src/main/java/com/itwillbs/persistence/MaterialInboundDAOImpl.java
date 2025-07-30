package com.itwillbs.persistence;

import java.util.List;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialInboundSummaryDTO;

@Repository
public class MaterialInboundDAOImpl implements MaterialInboundDAO {
	
	@Inject
	private SqlSession sqlSession;
	
	private static final String NAMESPACE = "com.itwillbs.mapper.MaterialInboundMapper.";

	
	// 입고 목록 조회
	@Override
	public List<MaterialInboundSummaryDTO> getInboundList(SearchCriteria cri) throws Exception {

		return sqlSession.selectList(NAMESPACE + "getInboundList", cri);
	}


	// 목록 전체 수 조회
	@Override
	public int getInboundListCount(SearchCriteria cri) {
		
		return sqlSession.selectOne(NAMESPACE + "getInboundListCount", cri);
	}
	
	
	
	

}
