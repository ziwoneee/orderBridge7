package com.itwillbs.service;

import java.util.List;
import java.util.Map;

import com.itwillbs.domain.MaterialOutboundVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.WorkOrderVO;
import com.itwillbs.dto.MaterialOutboundDetailDTO;
import com.itwillbs.dto.MaterialOutboundSummaryDTO;

/**
 * 출고 관리 서비스 인터페이스
 * - Controller ↔ DAO 중간 계층
 * - 트랜잭션 관리, 비즈니스 로직 수행
 */
public interface MaterialOutboundService {
	
	List<?> getOutboundList(SearchCriteria cri) throws Exception;
    int getOutboundCount(SearchCriteria cri) throws Exception;

    List<WorkOrderVO> getWaitingOrders() throws Exception;
    Map<String,Object> getWorkOrderWithStockMap(String workOrderId) throws Exception;

    void registerOutbound(MaterialOutboundVO vo) throws Exception;

    Map<String,Object> getOutboundDetailMap(String outboundId) throws Exception;

    void processOutbound(String outboundId) throws Exception;
    
    List<Map<String,Object>> getLotsByMaterial(String materialId) throws Exception;
    
    int getOutboundCountByStatus(String status) throws Exception;
    
    
    
    /**
     * 작업지시서가 전 자재 충족 시 출고(미출고) 생성
     * @param workOrderId 작업지시서 ID
     * @return 결과 객체 (생성여부, 출고ID, 사유)
     */
    CreateOutboundResult createOutboundIfReady(String workOrderId) throws Exception;

    class CreateOutboundResult {
        public boolean created;     // 생성 여부
        public String outboundId;   // 생성된 OUT ID
        public String reason;       // 생성 안한 이유
    }
    
    

    /**
     * 특정 입고건의 가용 자재 목록 조회
     * @param inboundId 입고ID
     * @return 가용 자재 목록 (자재ID, 자재명, LOT번호, 가용수량 등)
     * @throws Exception
     */
    List<Map<String, Object>> getAvailableMaterialsByInbound(String inboundId) throws Exception;

    /**
     * 입고건 사용 상태 업데이트
     * @param inboundId 입고ID
     * @throws Exception
     */
    void updateInboundUsageStatus(String inboundId) throws Exception;
    
}
