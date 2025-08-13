package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.ProductInboundVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.ProductionResultDTO;

public interface ProductInboundService {

    // 입고 등록
    void registerInbound(ProductInboundVO vo);

    // ✅ 기본 입고 내역   // ✅ 검색 + 정렬 + 날짜조회 + 페이징
    List<ProductInboundVO> searchProductionInboundList(SearchCriteria cri);

    // ✅ 검색 결과 총 개수 (페이징용)
    int countProductionInboundList(SearchCriteria cri);
    
    // 입고내역 DB저장
    void saveInboundFromProductionResults(SearchCriteria cri);
   
    // 자동 입고 처리
    void autoInboundFromExistingResults();
   
    // 실제 DB 검색
    List<ProductInboundVO> searchInboundList(SearchCriteria cri);
    int countInboundList(SearchCriteria cri);
    
    

}
