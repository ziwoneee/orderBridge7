package com.itwillbs.service;

import java.util.List;
import com.itwillbs.domain.ProductionLineVO;
import com.itwillbs.dto.WorkOrderDTO;

public interface ProductionLineService {
    
    /**
     * 사용 가능한 생산 라인 조회 (ACTIVE만) - 작업지시용
     * @return 생산 라인 목록
     */
    List<ProductionLineVO> getAvailableLines();
    
    /**
     * 모든 생산라인 조회 (ACTIVE + INACTIVE) - 관리용
     * @return 생산 라인 목록
     */
    List<ProductionLineVO> getAllLines();
    
    /**
     * 상태별 개수 조회
     * @param status 상태 (ACTIVE, INACTIVE)
     * @return 개수
     */
    int getCountByStatus(String status);
    
    /**
     * 생산라인 상세 조회
     * @param lineId 라인ID
     * @return 생산라인 정보
     */
    ProductionLineVO getProductionLineDetail(String lineId);
    
    /**
     * 상태 변경
     * @param lineId 라인ID
     * @param status 상태
     * @return 변경 건수
     */
    int updateStatus(String lineId, String status);
    
    /**
     * 특정 라인의 진행 중인 작업 (최신 1건)
     * @param lineId 라인ID
     * @return 진행중 작업 (없으면 null)
     */
    WorkOrderDTO getCurrentWorkByLine(String lineId);

    /**
     * 특정 라인의 대기 중인 작업 목록
     * @param lineId 라인ID
     * @return 대기 작업 목록
     */
    List<WorkOrderDTO> getWaitingWorksByLine(String lineId);
}
