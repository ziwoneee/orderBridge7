package com.itwillbs.persistence;


import com.itwillbs.domain.ProductVO;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ProductMasterDAOImpl implements ProductMasterDAO {

    private static final String NAMESPACE = "com.itwillbs.mapper.ProductMasterMapper";

    @Autowired
    private SqlSession sqlSession;
    //제품목록
    @Override
    public List<ProductVO> selectAllProducts() {
        return sqlSession.selectList(NAMESPACE + ".selectAllProducts");
    }
    //제품 등록
    @Override
    public void insertProduct(ProductVO productVO) {
        sqlSession.insert(NAMESPACE + ".insertProduct", productVO);
    }
    //제품 수정
    @Override
    public void updateProduct(ProductVO productVO) {
        sqlSession.update(NAMESPACE + ".updateProduct", productVO);
    }
    
    //제품 코드 자동생성
    @Override
    public String selectLastProductId() {
        return sqlSession.selectOne(NAMESPACE + ".selectLastProductId");
    }
    
    //제품 소프트 삭제
    @Override
    public void softDeleteProduct(String productId) {
        sqlSession.update(NAMESPACE + ".softDeleteProduct", productId);
    }


    
    
}
