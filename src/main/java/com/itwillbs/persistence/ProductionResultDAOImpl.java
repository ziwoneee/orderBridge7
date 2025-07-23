package com.itwillbs.persistence;

import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.ProductionResultVO;
import com.itwillbs.domain.SearchCriteria;

@Repository
public class ProductionResultDAOImpl implements ProductionResultDAO {

    @Autowired
    private SqlSession sqlSession;

    private static final String NAMESPACE = "com.itwillbs.mapper.ProductionResultMapper";
    
    //완제품 입고(아름 시작)
    //내역
    @Override
    public List<ProductionResultVO> searchProductionResults(SearchCriteria cri) {
        return sqlSession.selectList(NAMESPACE + ".searchProductionResults", cri);
    }
    //카운트
    @Override
    public int countProductionResults(SearchCriteria cri) {
        return sqlSession.selectOne(NAMESPACE + ".countProductionResults", cri);
    }
    
    //완제품 입고자동업데이트
    @Override
    public void insertResult(ProductionResultVO vo) {
        sqlSession.insert(NAMESPACE + ".insertResult", vo);
    }
    
    @Override
    public List<ProductionResultVO> selectAllResults() {
        return sqlSession.selectList(NAMESPACE + ".selectAllResults");
    }
    
    //완제품 입고(아름 끝)
}
