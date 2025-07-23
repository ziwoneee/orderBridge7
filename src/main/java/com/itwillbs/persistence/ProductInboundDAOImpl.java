package com.itwillbs.persistence;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.ProductInboundVO;
import com.itwillbs.domain.ProductionResultVO;
import com.itwillbs.domain.SearchCriteria;

@Repository
public class ProductInboundDAOImpl implements ProductInboundDAO {

    @Autowired
    private SqlSession sqlSession;

    private static final String NAMESPACE = "com.itwillbs.mapper.ProductInboundMapper";

    // 입고 등록
    @Override
    public void insertInbound(ProductInboundVO vo) {
        sqlSession.insert(NAMESPACE + ".insertInbound", vo);
    }

    // 생산결과 기반 입고 리스트
    @Override
    public List<ProductionResultVO> selectProductionResultList() {
        return sqlSession.selectList(NAMESPACE + ".selectAllResults");
    }

    // 검색 + 정렬 + 날짜 + 페이징 리스트
    @Override
    public  List<ProductionResultVO> searchProductionResults(SearchCriteria cri) {
        return sqlSession.selectList(NAMESPACE + ".searchInboundList", cri);
    }

    // 전체 개수 (페이징용)
    @Override
    public int countProductionResults(SearchCriteria cri) {
        return sqlSession.selectOne(NAMESPACE + ".countInboundList", cri);
    }
    
   // 로트 중복확인
      @Override
      public boolean existsByLotNo(String lotNo) {
            return sqlSession.selectOne(NAMESPACE + ".existsByLotNo", lotNo);
        }
 
     
      // ✅ 입고 목록 조회
      @Override
      public List<ProductInboundVO> searchInboundList(SearchCriteria cri) {
          return sqlSession.selectList(NAMESPACE + ".searchInboundList", cri);
      }

      // ✅ 총 개수 조회
      @Override
      public int countInboundList(SearchCriteria cri) {
          return sqlSession.selectOne(NAMESPACE + ".countInboundList", cri);
      }
      
    
}
