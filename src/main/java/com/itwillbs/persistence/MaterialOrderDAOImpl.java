package com.itwillbs.persistence;

import java.util.HashMap;
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
    
    
    /* 발주 초안에서 요청 */
    // 0) 초안 + 항목 로드 (검증용)
    @Override
    public List<Map<String, Object>> selectDraftWithItems(String orderId) {
        return sqlSession.selectList(NAMESPACE + "selectDraftWithItems", orderId);
    }
    
    // 0-1) 항목 총액 합계 (있으면 보여주기/로그용)
    @Override
    public Integer selectItemsTotal(String orderId) {
        return sqlSession.selectOne(NAMESPACE + "selectItemsTotal", orderId);
    }
    
    // 1) 상태 전이: 초안 -> 요청 (동시성 보호: 초안일 때만)
    @Override
    public int updateOrderToRequested(String orderId) {
        return sqlSession.update(NAMESPACE + "updateOrderToRequested", orderId);
    }

    
    /**
     * 발주 상세
     */
    // 주문 헤더
    @Override
    public Map<String, Object> selectOrderHeader(String orderId){
        return sqlSession.selectOne(NAMESPACE + "selectOrderHeader", orderId);
    }
    
    // 주문 아이템 목록
    @Override
    public List<Map<String, Object>> selectOrderItems(String orderId){
        return sqlSession.selectList(NAMESPACE + "selectOrderItems", orderId);
    }
    
    
    /**
     * 발주 상태별 탭기능 카운트
     */
    @Override
    public int getCountByStatus(String status) {
        return sqlSession.selectOne(NAMESPACE + "getCountByStatus", status);
    }
    
    @Override
    public Map<String, Integer> getStatusCounts() {
        // selectMap 대신 selectList 사용
        List<Map<String, Object>> result = sqlSession.selectList(NAMESPACE + "getStatusCounts");
        
        Map<String, Integer> statusMap = new HashMap<>();
        for(Map<String, Object> row : result) {
            String status = (String) row.get("status");
            Integer count = ((Number) row.get("count")).intValue(); // Number로 캐스팅 후 int 변환
            statusMap.put(status, count);
        }
        return statusMap;
    }
    
    //협력사 이메일
    @Override
    public MaterialOrderVO findById(String orderId) {
        return sqlSession.selectOne(NAMESPACE + "findById", orderId);
        
    } 
   
    // ✅ 협력사 발주 상태 업데이트
    @Override
    public void updateOrderStatus(MaterialOrderVO vo) {
        sqlSession.update(NAMESPACE + "updateOrderStatus", vo);
    }

        
    }
    
    
