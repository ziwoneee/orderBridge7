package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.domain.ProductInboundVO;
import com.itwillbs.domain.ProductionResultVO;
import com.itwillbs.domain.SearchCriteria;

public interface ProductInboundDAO {

    // 입고 등록
    void insertInbound(ProductInboundVO vo);

    // ✅ 생산 결과 기반 리스트
    List<ProductionResultVO> selectProductionResultList();

    // ✅ 검색 + 정렬 + 날짜조회 + 페이지네이션 리스트
    List<ProductionResultVO> searchProductionResults(SearchCriteria cri);

    // ✅ 검색 결과 총 개수 (페이징용)
    int countProductionResults(SearchCriteria cri);

   // 입고내역 로트중복확인    
    boolean existsByLotNo(String lotNo);
  
    // ✅ 검색용
    List<ProductInboundVO> searchInboundList(SearchCriteria cri);  
   
    // ✅ 페이징용 개수
    int countInboundList(SearchCriteria cri);                
}