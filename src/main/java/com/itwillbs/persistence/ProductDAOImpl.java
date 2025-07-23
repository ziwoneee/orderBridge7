package com.itwillbs.persistence;

import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.ProductVO;

@Repository
public class ProductDAOImpl implements ProductDAO {

    @Autowired
    private SqlSession sqlSession;
    private static final String NAMESPACE = "com.itwillbs.mapper.ProductMapper";

    @Override
    public List<ProductVO> getAllProducts() {
        return sqlSession.selectList(NAMESPACE + ".getAllProducts");
    }
}
