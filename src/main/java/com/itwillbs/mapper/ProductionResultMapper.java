package com.itwillbs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.itwillbs.domain.SearchCriteria;

@Mapper
public interface ProductionResultMapper {

    // --- 아름 세트(VO 반환) ---
    List<com.itwillbs.domain.ProductionResultVO> searchProductionResults(SearchCriteria cri);
    int countProductionResults(SearchCriteria cri);
    int insertResult(com.itwillbs.domain.ProductionResultVO vo);
    List<com.itwillbs.domain.ProductionResultVO> selectAllResults();

    // --- 태현 세트(DTO 반환) ---
    List<com.itwillbs.dto.ProductionResultDTO> selectResultList(SearchCriteria cri);
    int selectResultCount(SearchCriteria cri);
}
