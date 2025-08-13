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

    
    // 3-1) 부족분 리스트 (net_req > 0)
    @Override
    public List<Map<String, Object>> selectShortages(String productId, double orderQty) {
        Map<String, Object> p = new HashMap<>();
        p.put("productId", productId);
        p.put("orderQty",  orderQty);
        return sqlSession.selectList(NAMESPACE + "selectShortages", p);
    }

    // 3-2) 발주 추천 초안
    @Override
    public List<Map<String, Object>> selectRecommendPO(String productId, double orderQty) {
        Map<String, Object> p = new HashMap<>();
        p.put("productId", productId);
        p.put("orderQty",  orderQty);
        return sqlSession.selectList(NAMESPACE + "selectRecommendPO", p);
    }
    
    
    // 4-1) 신규 PO 아이디 채번
    @Override
    public String selectNextPoId() {
        return sqlSession.selectOne(NAMESPACE + "selectNextPoId");
    }

    // 4-2) PO 헤더 INSERT
    @Override
    public int insertMaterialOrderHeader(String orderId, String supplierId, String expectedInbound) {
        Map<String, Object> p = new HashMap<>();
        p.put("orderId", orderId);
        p.put("supplierId", supplierId);
        p.put("expectedInbound", expectedInbound); // 헤더에 대표 예상입고(없으면 null)
        return sqlSession.insert(NAMESPACE + "insertMaterialOrderHeader", p);
    }

    // 4-3) PO 아이템 INSERT (배치)
    @Override
    public int insertMaterialOrderItems(String orderId, List<Map<String, Object>> items) {
        Map<String, Object> p = new HashMap<>();
        p.put("orderId", orderId);
        p.put("items", items);
        return sqlSession.insert(NAMESPACE + "insertMaterialOrderItems", p);
    }


}
