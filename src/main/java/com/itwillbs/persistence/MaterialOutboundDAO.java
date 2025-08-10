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
	Date selectWorkOrderDueDate(String workOrderNo);
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
	
	

}
