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

    
    // 발주 목록 조회
    @Override
    public List<MaterialOrderVO> getOrderList(SearchCriteria cri) throws Exception {
        return sqlSession.selectList(NAMESPACE + "selectOrderList", cri);
    }

    // 전체 건수 조회
    @Override
    public int getTotalCount(SearchCriteria cri) throws Exception {
        return sqlSession.selectOne(NAMESPACE + "getTotalCount", cri);
    }

    // 발주 등록
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

    
    // 자재명으로 거래처 검색
    @Override
    public List<SupplierItemDTO> searchSuppliersByMaterial(String keyword) {
        return sqlSession.selectList(NAMESPACE + "searchSuppliersByMaterial", keyword);
    }

	
    // 부족분으로 발주 자동 생성
    @Override
    public List<Map<String, Object>> selectSupplierItemMappings(List<String> materialIds) throws Exception {
        return sqlSession.selectList(NAMESPACE + "selectSupplierItemMappings", materialIds);
    }

    @Override
    public int insertOrderHeaderDraft(Map<String, Object> header) throws Exception {
        return sqlSession.insert(NAMESPACE + "insertOrderHeaderDraft", header);
    }

    @Override
    public int insertOrderItem(Map<String, Object> item) throws Exception {
        return sqlSession.insert(NAMESPACE + "insertOrderItem", item);
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
