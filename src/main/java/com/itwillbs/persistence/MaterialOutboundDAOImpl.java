package com.itwillbs.persistence;

import java.util.List;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.MaterialOutboundVO;

// 자재 출고 DAO 구현채
@Repository
public class MaterialOutboundDAOImpl implements MaterialOutboundDAO {
	
	@Inject
	private SqlSession sqlSession;
	
	private static final String NAMESPACE = "com.itwillbs.mapper.MaterialOutboundMapper.";

	
	// 출고 목록 조회
	@Override
	public List<MaterialOutboundVO> getOutboundList() throws Exception {

		return sqlSession.selectList(NAMESPACE + "getOutboundList");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
