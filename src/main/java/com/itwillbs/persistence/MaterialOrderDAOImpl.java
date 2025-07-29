package com.itwillbs.persistence;

import java.util.List;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;

/**
 * 자재 발주 DAO 구현체
 * - MyBatis SqlSession을 통해 Mapper 쿼리 호출
 */
@Repository
public class MaterialOrderDAOImpl implements MaterialOrderDAO {
	
	@Inject
    private SqlSession sqlSession;

    private static final String NAMESPACE = "com.itwillbs.mapper.MaterialOrderMapper.";

    
    // 발주 목록 조회
    @Override
    public List<MaterialOrderVO> getOrderList(SearchCriteria cri) {
        return sqlSession.selectList(NAMESPACE + "selectOrderList", cri);
    }

    // 전체 건수 조회
    @Override
    public int getTotalCount(SearchCriteria cri) {
        return sqlSession.selectOne(NAMESPACE + "getTotalCount", cri);
    }

    // 등록 상태 건수 조회
    @Override
    public int getRegisteredCount(SearchCriteria cri) {
        return sqlSession.selectOne(NAMESPACE + "getRegisteredCount", cri);
    }
    
    
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
