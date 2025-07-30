package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.domain.ProductOutboundVO;
import com.itwillbs.domain.SearchCriteria;

public interface ProductOutboundDAO {
    void insertOutbound(ProductOutboundVO vo);
    
    List<ProductOutboundVO> searchOutboundList(SearchCriteria cri);
    int countOutboundList(SearchCriteria cri);
    
    // ✅ 오늘 날짜 기준 최대 일련번호 조회    
    Integer getMaxOutboundSeqToday(String today);

    //상세보기
    ProductOutboundVO getOutboundDetail(String outboundId);
    
}