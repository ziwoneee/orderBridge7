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

}
