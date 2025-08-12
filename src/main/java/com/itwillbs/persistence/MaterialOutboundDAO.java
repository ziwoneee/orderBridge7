package com.itwillbs.persistence;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.itwillbs.domain.MaterialOutboundItemVO;
import com.itwillbs.domain.MaterialOutboundVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.WorkOrderVO;
import com.itwillbs.dto.MaterialOutboundDetailDTO;
import com.itwillbs.dto.MaterialOutboundItemDTO;
import com.itwillbs.dto.MaterialOutboundSummaryDTO;

/**
 * 출고 관리 DAO
 * - Mapper 호출 전용
 * - SQL Session 사용
 */
public interface MaterialOutboundDAO {
	
	List<?> selectOutboundList(SearchCriteria cri);
	int selectOutboundCount(SearchCriteria cri);
	
	List<WorkOrderVO> selectWaitingOrders();
	
	Map<String,Object> selectWorkOrderHeader(String workOrderId);
	List<Map<String,Object>> selectWorkOrderItemsWithStock(String workOrderId);
	
	void insertOutboundHeader(Map<String,Object> header);
	void insertOutboundItems(List<Map<String,Object>> items);
	
	Map<String,Object> selectOutboundHeader(String outboundId);
	List<Map<String,Object>> selectOutboundItems(String outboundId);
	
	int decreaseInventoryByOutbound(String outboundId);
	void updateOutboundCompleted(String outboundId);
	
	List<Map<String,Object>> getLotsByMaterial(String materialId);
	
	int countByStatus(String status);
	
	// MaterialOutboundDAO.java
	Date selectWorkOrderDueDate(String workOrderId);
	int updateWorkOrderShortageStatus(String workOrderId, String status);

	
	// 해당 작업지시서가 출고 가능한 상태인지 여부 (1=가능, 0=불가능)
    int isWorkOrderReady(String workOrderId) throws Exception;

    // 해당 작업지시서의 출고 레코드 존재 여부
    int existsOutboundByWorkOrder(String workOrderId) throws Exception;

    // 신규 출고 ID 생성
    String nextOutboundId() throws Exception;

    // 출고 마스터 INSERT
    void insertMaterialOutbound(String outboundId, String workOrderId) throws Exception;

    // 출고 아이템 INSERT (작업지시서 자재 목록 복사)
    void insertOutboundItemsFromWOM(String outboundId, String workOrderId) throws Exception;
    
    // 작업지시서 남은 필요수량 카운트
    int countRemainByWorkOrder(String workOrderId) throws Exception;
    
    // 부족해결 상태로 변경
    int updateWorkOrderShortageResolved(String workOrderId) throws Exception;
    
    // 전부 충족 시 지시 상태도 완료로 변경하고 싶으면 사용
    int updateWorkOrderIssuedCompleted(String workOrderId) throws Exception;
    
 // MaterialOutboundDAO.java에 추가할 메서드 선언들

    /**
     * 특정 입고건의 가용 자재 목록 조회
     * @param params 입고ID
     * @return 가용 자재 목록 (자재ID, 자재명, LOT번호, 가용수량, 필요수량 등)
     * @throws Exception
     */
    List<Map<String, Object>> getAvailableMaterialsByInbound(Map<String, Object> params) throws Exception;

    /**
     * 입고건 사용 상태 업데이트
     * @param inboundId 입고ID
     * @return 업데이트된 행 수
     * @throws Exception
     */
    int updateInboundUsageStatus(String inboundId) throws Exception;
	
	

}
