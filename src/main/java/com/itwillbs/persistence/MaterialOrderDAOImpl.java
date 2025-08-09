package com.itwillbs.persistence;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.MaterialOrderItemVO;
import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.SupplierItemDTO;

/**
 * 자재 발주 DAO 구현체
 * - MyBatis SqlSession을 통해 Mapper 쿼리 호출
 */
@Repository
public class MaterialOrderDAOImpl implements MaterialOrderDAO {
	
	@Inject
    private SqlSession sqlSession;

    private static final String NAMESPACE = "com.itwillbs.mapper.MaterialOrderMapper.";

    @Override
    public List<MaterialOrderVO> getOrderList(SearchCriteria cri) throws Exception {
        return sqlSession.selectList(NAMESPACE + "selectOrderList", cri);
    }

    @Override
    public int getTotalCount(SearchCriteria cri) throws Exception {
        return sqlSession.selectOne(NAMESPACE + "getTotalCount", cri);
    }

    @Override
    public String generateOrderId() throws Exception {
        return sqlSession.selectOne(NAMESPACE + "generateOrderId");
    }

    @Override
    public void insertOrder(MaterialOrderVO order) throws Exception {
        sqlSession.insert(NAMESPACE + "insertOrder", order);
    }

    @Override
    public void insertOrderItem(MaterialOrderItemVO item) throws Exception {
        sqlSession.insert(NAMESPACE + "insertOrderItem", item);
    }

    @Override
    public List<SupplierItemDTO> searchSuppliersByMaterial(String keyword) {
        return sqlSession.selectList(NAMESPACE + "searchSuppliersByMaterial", keyword);
    }

    // 부족분 발주 자동 생성 관련 메서드들
    @Override
    public int selectNextOrderItemIndex(String orderId) throws Exception {
        Integer result = sqlSession.selectOne(NAMESPACE + "selectNextOrderItemIndex", orderId);
        return result != null ? result : 1;
    }

    @Override
    public List<Map<String, Object>> selectSupplierItemMappings(List<String> materialIds) throws Exception {
        return sqlSession.selectList(NAMESPACE + "selectSupplierItemMappings", materialIds);
    }

    @Override
    public void insertOrderHeaderDraft(Map<String, Object> params) throws Exception {
        sqlSession.insert(NAMESPACE + "insertOrderHeaderDraft", params);
    }

    @Override
    public void insertOrderItemsBatch(List<Map<String, Object>> items) throws Exception {
        if (items != null && !items.isEmpty()) {
            sqlSession.insert(NAMESPACE + "insertOrderItemsBatch", items);
        }
    }

    @Override
    public int insertOrderItem(Map<String, Object> item) throws Exception {
        return sqlSession.insert(NAMESPACE + "insertOrderItemMap", item);
    }
    
    // 내부사용(N) 자재 조회
    @Override
    public List<Map<String, Object>> selectNonPurchasableFromList(List<String> materialIds) throws Exception {
        return sqlSession.selectList(NAMESPACE + "selectNonPurchasableFromList", materialIds);
    }

    
}