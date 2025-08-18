package com.itwillbs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.itwillbs.dto.WorkOrderLiteDTO;

public interface WorkOrderQueryMapper {
	
	List<WorkOrderLiteDTO> selectEligibleForEta(@Param("q") String q,
            @Param("limit") int limit);

	WorkOrderLiteDTO selectLiteById(@Param("orderId") String orderId);
	
	 String resolveAiStage(@Param("workOrderId") String workOrderId);  // PLANNED | PO_PLACED | CHECKED_ONLY | READY | IN_PROGRESS | COMPLETED

}
