package com.itwillbs.persistence;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.ProductOutboundVO;
import com.itwillbs.domain.SearchCriteria;

@Repository
public class ProductOutboundDAOImpl implements ProductOutboundDAO {

    @Autowired
    private SqlSession sqlSession;

    private static final String NAMESPACE = "com.itwillbs.mapper.ProductOutboundMapper";

    @Override
    public void insertOutbound(ProductOutboundVO vo) {
        sqlSession.insert(NAMESPACE + ".insertOutbound", vo);
    }
    
    @Override
    public List<ProductOutboundVO> searchOutboundList(SearchCriteria cri) {
        return sqlSession.selectList(NAMESPACE + ".searchOutboundList", cri);
    }

    @Override
    public int countOutboundList(SearchCriteria cri) {
        return sqlSession.selectOne(NAMESPACE + ".countOutboundList", cri);
    }
    
    @Override
    public Integer getMaxOutboundSeqToday(String today) {
        return sqlSession.selectOne(NAMESPACE + ".getMaxOutboundSeqToday", today);
    }
    //상세보기
    @Override
    public ProductOutboundVO getOutboundDetail(String outboundId) {
        return sqlSession.selectOne(NAMESPACE + ".getOutboundDetail", outboundId);
    }
    
    //출하취소시 삭제
    @Override
    public void deleteOutboundByOrderId(String clOrderId) {
        sqlSession.delete("com.itwillbs.mapper.ProductOutboundMapper.deleteOutboundByOrderId", clOrderId);
    }


    
    
}