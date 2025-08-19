package com.itwillbs.persistence;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class AiPredictionDAOImpl implements AiPredictionDAO {
	
    @Inject
    private SqlSession sqlSession;

    private static final String NAMESPACE = "com.itwillbs.mapper.AiPredictionMapper.";

    @Override
    public Map<String, Object> getWorkOrderSummary(String workOrderId) throws Exception {
        return sqlSession.selectOne(NAMESPACE + "selectWorkOrderSummary", workOrderId);
    }

    @Override
    public List<Map<String, Object>> getShortageByMaterial(String workOrderId) throws Exception {
        return sqlSession.selectList(NAMESPACE + "selectShortageByMaterial", workOrderId);
    }

    @Override
    public List<Map<String, Object>> getLeadTimeStatsByMaterials(List<String> materialIds) throws Exception {
        if (materialIds == null || materialIds.isEmpty()) return java.util.Collections.emptyList();
        return sqlSession.selectList(NAMESPACE + "selectLeadTimeStatsByMaterials", materialIds);
    }

}
