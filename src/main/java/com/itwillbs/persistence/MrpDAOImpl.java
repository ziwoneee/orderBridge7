package com.itwillbs.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

/**
 * MrpDAO 구현체.
 * - MyBatis SqlSession을 주입 받아 Mapper XML의 쿼리를 호출한다.
 * - 네임스페이스는 Mapper XML의 <mapper namespace="...">와 동일해야 한다.
 */
@Repository
public class MrpDAOImpl implements MrpDAO {
	
	private static final String NAMESPACE = "com.itwillbs.mapper.MrpMapper.";
	
    @Inject
    private SqlSession sqlSession;
    
    
    // 제품/수량을 입력 받아 자재별 "총소요량(gross requirement)"을 조회
    @Override
    public List<Map<String, Object>> selectGrossRequirements(String productId, double orderQty) {
        // 파라미터 맵 구성 (MyBatis는 Map의 key로 #{...} 바인딩)
        Map<String, Object> params = new HashMap<>();
        params.put("productId", productId);
        params.put("orderQty",  orderQty);

        // "NAMESPACE + .SQL_ID" 형태로 호출
        return sqlSession.selectList(NAMESPACE + "selectGrossRequirements", params);
    }
    
    
    // 제품/수량 기준으로 자재별 "가용/순소요"를 조회
    @Override
    public List<Map<String, Object>> selectNetting(String productId, double orderQty) {
        Map<String, Object> params = new HashMap<>();
        params.put("productId", productId);
        params.put("orderQty",  orderQty);
        return sqlSession.selectList(NAMESPACE + "selectNetting", params);
    }


}
