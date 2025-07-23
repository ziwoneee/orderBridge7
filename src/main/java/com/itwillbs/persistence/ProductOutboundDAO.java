package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.domain.ProductOutboundVO;
import com.itwillbs.domain.SearchCriteria;

public interface ProductOutboundDAO {
    void insertOutbound(ProductOutboundVO vo);
    
    List<ProductOutboundVO> searchOutboundList(SearchCriteria cri);
    int countOutboundList(SearchCriteria cri);
    
}