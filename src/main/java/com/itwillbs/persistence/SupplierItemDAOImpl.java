package com.itwillbs.persistence;

import java.util.List;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.SupplierItemVO;

/**
 * 공급 품목 DAO 구현체
 */
@Repository
public class SupplierItemDAOImpl implements SupplierItemDAO {
	
	@Inject
    private SqlSession sqlSession;

    private static final String NAMESPACE = "com.itwillbs.mapper.SupplierItemMapper.";

    // 특정 거래처의 공급 품목 JSON 목록 반환
    public List<SupplierItemVO> getItemsBySupplier(String supplierId) throws Exception {
    	
        return sqlSession.selectList(NAMESPACE + "getItemsBySupplier", supplierId);
    }
    
    // 페이징
    @Override
	public int getItemCountBySupplier(String supplierId) throws Exception {
    	return sqlSession.selectOne(NAMESPACE + "getItemCountBySupplier", supplierId);
	}

    @Override
    public List<SupplierItemVO> getItemListBySupplierWithPaging(String supplierId, SearchCriteria cri) throws Exception {
        // MyBatis는 복수 파라미터를 전달할 때 Map 또는 DTO로 감싸야 함
        java.util.Map<String, Object> paramMap = new java.util.HashMap<>();
        paramMap.put("supplierId", supplierId);
        paramMap.put("perPageNum", cri.getPerPageNum());
        paramMap.put("pageStart", cri.getPageStart());
        
        // 🧪 로그 확인
        System.out.println("🔥 pageStart: " + cri.getPageStart());
        System.out.println("🔥 perPageNum: " + cri.getPerPageNum());

        return sqlSession.selectList(NAMESPACE + "getItemListBySupplierWithPaging", paramMap);
    }

	// 공급 품목 등록
    @Override
    public void insertItem(SupplierItemVO item) throws Exception {
        sqlSession.insert(NAMESPACE + "insertItem", item);
    }
    
    // 공급 품목 수정
    @Override
    public void updateItem(SupplierItemVO item) throws Exception {
        sqlSession.update(NAMESPACE + "updateItem", item);
    }
    
    // 공급 품목 단건 조회 (수정폼용)
    @Override
    public SupplierItemVO getItemById(String itemId) throws Exception {
        return sqlSession.selectOne(NAMESPACE + "getItemById", itemId);
    }



}
