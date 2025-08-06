package com.itwillbs.mapper;

import java.util.List;

import com.itwillbs.domain.ProductionLineVO;

public interface ProductionLineMapper {

    /**
     * 사용 가능한 전체 생산 라인 목록 조회
     * @return 생산 라인 목록
     */
    List<ProductionLineVO> selectAvailableLines();

}