package com.itwillbs.persistence;

import java.util.List;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.itwillbs.dto.MaterialInboundSummaryDTO;

@Repository
public class MaterialInboundDAOImpl implements MaterialInboundDAO {
	
	@Inject
	private SqlSession sqlSession;
	
	private static final String NAMESPACE = "com.itwillbs.mapper.MaterialInboundMapper.";

	
	// 입고 목록 조회
	@Override
	public List<MaterialInboundSummaryDTO> getInboundList() {

		return sqlSession.selectList(NAMESPACE + "getInboundList");
	}
	
	

}
