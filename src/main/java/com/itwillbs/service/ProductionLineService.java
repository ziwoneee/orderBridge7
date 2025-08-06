package com.itwillbs.service;

import java.util.List;
import com.itwillbs.domain.ProductionLineVO;

public interface ProductionLineService {

    /**
     * 사용 가능한 생산 라인 전체 조회
     * @return 생산 라인 목록
     */
    List<ProductionLineVO> getAvailableLines();
}