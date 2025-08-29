package com.itwillbs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.itwillbs.domain.ProductionLineVO;
import com.itwillbs.dto.WorkOrderDTO;

public interface ProductionLineMapper {

    /**
     * 사용 가능한 전체 생산 라인 목록 조회
     * @return 생산 라인 목록
     */
    List<ProductionLineVO> selectAvailableLines();
    
    /**
     * 모든 생산라인 조회 (ACTIVE + INACTIVE)
     * @return 생산 라인 목록
     */
    List<ProductionLineVO> selectAllLines();

    /**
     * 상태별 개수 조회
     * @param status 상태
     * @return 개수
     */
    int selectCountByStatus(@Param("status") String status);

    /**
     * 생산라인 상세 조회
     * @param lineId 라인ID
     * @return 생산라인 정보
     */
    ProductionLineVO selectProductionLineDetail(@Param("lineId") String lineId);

    /**
     * 상태 변경
     * @param lineId 라인ID
     * @param status 상태
     * @return 변경 건수
     */
    int updateStatus(@Param("lineId") String lineId, @Param("status") String status);
    int updateStatusIfNoRunning(@Param("lineId") String lineId, @Param("status") String status);
    
    /**
     * 특정 라인의 진행 중 작업 1건
     */
    WorkOrderDTO selectCurrentWorkByLineId(@Param("lineId") String lineId);

    /**
     * 특정 라인의 대기 중 작업 목록
     */
    List<WorkOrderDTO> selectWaitingWorksByLineId(@Param("lineId") String lineId);
}
