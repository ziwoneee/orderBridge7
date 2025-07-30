package com.itwillbs.persistence;

import java.util.List;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialInboundSummaryDTO;
import com.itwillbs.dto.UnreceivedOrderDTO;

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
	
	// material_order 기준으로 아직 입고기록이 없는 발주건만 조회
	@Override
    public List<MaterialOrderVO> selectPendingInboundOrders() {
        return sqlSession.selectList(NAMESPACE + "selectPendingInboundOrders");
    }
	
	
	/**
     * 아직 입고되지 않은 발주건만 조회
     * - 입고항목 테이블에 존재하지 않는 발주항목만 필터링
     */
    @Override
    public List<UnreceivedOrderDTO> selectUnreceivedOrders() {
        return sqlSession.selectList(NAMESPACE + "selectUnreceivedOrders");
    }
	
	

}
