package com.itwillbs.persistence;

import com.itwillbs.domain.ClientOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.ProductionPlanDTO;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 생산 계획 관련 DAO
 * - CONFIRMED 상태 수주 목록 + 전체 수 조회 (페이징)
 */
@Repository
public class ProductionPlanDAO {

    private final SqlSession sqlSession;

    public ProductionPlanDAO(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    // CONFIRMED 상태 수주 목록 조회 (페이징 적용)
    public List<ClientOrderVO> getConfirmedOrderList(SearchCriteria cri) {
        return sqlSession.selectList("com.itwillbs.mapper.ProductionPlanMapper.getConfirmedOrderList", cri);
    }

    // CONFIRMED 상태 수주 총 개수 조회 (페이징용)
    public int getConfirmedOrderTotalCount(SearchCriteria cri) {
        return sqlSession.selectOne("com.itwillbs.mapper.ProductionPlanMapper.getConfirmedOrderTotalCount", cri);
    }
    

    // 생산계획 번호 자동 생성 (PL-20250723-001 형식)
    public String getNewPlanId() {
    		return sqlSession.selectOne("com.itwillbs.mapper.ProductionPlanMapper.getNewPlanId");
    }
    
    // [plan_id 자동생성용] 오늘 날짜 기준으로 가장 큰 plan_id 조회 (예: PL-20250723-007)
    public String getMaxPlanIdLike(String prefix) {
    	return sqlSession.selectOne("com.itwillbs.mapper.ProductionPlanMapper.getMaxPlanIdLike", prefix);
    }
    
    // 생산 계획 등록
    public void insertPlan(ProductionPlanDTO dto) {
    	sqlSession.insert("com.itwillbs.mapper.ProductionPlanMapper.insertPlan", dto);
    }
    
    // 생산 등록된 계획 목록 조회
    public List<ProductionPlanDTO> getPlanList(SearchCriteria cri) {
        return sqlSession.selectList("com.itwillbs.mapper.ProductionPlanMapper.getPlanList", cri);
    }

    // 생산 등록된 총 건수 조회
    public int getPlanListCount(SearchCriteria cri) {
        return sqlSession.selectOne("com.itwillbs.mapper.ProductionPlanMapper.getPlanListCount", cri);
    }
    
    // [중복 등록 체크] 동일한 수주번호 + 제품 ID로 등록된 생산 계획 존재 여부 확인
    public boolean isDuplicatePlan(ProductionPlanDTO dto) {
        Map<String, Object> param = new HashMap<>();
        param.put("clOrderId", dto.getClOrderId());
        param.put("productId", dto.getProductId());

        return sqlSession.selectOne("com.itwillbs.mapper.ProductionPlanMapper.isDuplicatePlan", param);
    }
    
}