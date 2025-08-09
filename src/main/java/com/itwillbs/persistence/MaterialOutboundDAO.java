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
	
	String nextOutboundId();
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

	
	

}
