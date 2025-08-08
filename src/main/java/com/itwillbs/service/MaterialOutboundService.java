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
}
