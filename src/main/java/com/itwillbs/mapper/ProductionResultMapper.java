package com.itwillbs.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.itwillbs.domain.SearchCriteria;

@Mapper
public interface ProductionResultMapper {
    
    // ==================== 아름 세트 (VO 반환) ====================
    List<com.itwillbs.domain.ProductionResultVO> searchProductionResults(SearchCriteria cri);
    int countProductionResults(SearchCriteria cri);
    int insertResult(com.itwillbs.domain.ProductionResultVO vo);
    List<com.itwillbs.domain.ProductionResultVO> selectAllResults();
    
    // ==================== 태현 세트 (DTO 반환) ====================
    List<com.itwillbs.dto.ProductionResultDTO> selectResultList(SearchCriteria cri);
    int selectResultCount(SearchCriteria cri);
    
    /**
     * 생산 실적 상세 조회
     */
    com.itwillbs.dto.ProductionResultDTO selectResultDetail(String resultId);
    
    // ==================== ID 발번 관련 (DB 기반) ====================
    
    /**
     * 오늘 날짜의 생산결과 ID 시퀀스 조회
     * @return "001", "002", ... 형태의 3자리 문자열
     */
    String selectTodayResultSeq();
    
    /**
     * 오늘 날짜의 해당 제품 LOT 시퀀스 조회 ⭐ 핵심!
     * @param productCode 제품 코드 (DG, SD, HW 등)
     * @param dateStr 날짜 문자열 (YYYYMMDD)
     * @return "001", "002", ... 형태의 3자리 문자열
     */
    String selectTodayLotSeq(@Param("productCode") String productCode, 
                            @Param("dateStr") String dateStr);
    
    // ==================== 작업지시 연동 ====================
    
    /**
     * 작업지시의 현재 진행 상황 조회 (참고용)
     */
    Map<String, Object> selectWorkOrderProgress(String orderId);
}