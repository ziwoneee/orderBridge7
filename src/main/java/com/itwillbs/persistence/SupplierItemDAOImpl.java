package com.itwillbs.persistence;

import java.util.List;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.SupplierItemVO;

/**
 * 공급 품목 DAO 구현체
 */
@Repository
public class SupplierItemDAOImpl implements SupplierItemDAO {
	
	@Inject
    private SqlSession sqlSession;

    private static final String NAMESPACE = "com.itwillbs.mapper.SupplierItemMapper.";

    @Override
    public List<SupplierItemVO> selectSuppliedItemsBySupplierId(String supplierId) {
        return sqlSession.selectList(NAMESPACE + "selectSuppliedItemsBySupplierId", supplierId);
    }
    
    // 특정 거래처의 공급 품목 JSON 목록 반환
    public List<SupplierItemVO> getItemsBySupplier(String supplierId) throws Exception {
    	
        return sqlSession.selectList(NAMESPACE + "getItemsBySupplier", supplierId);
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



}
