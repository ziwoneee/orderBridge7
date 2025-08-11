package com.itwillbs.mapper;

import java.util.List;
import java.util.Map;

public interface MaterialOutboundMapper {
	
	// 작업지시 기본 정보 1건
	Map<String,Object> selectWorkOrderHeader(String workOrderId);
	
	// 작업지시에 포함된 자재 목록
    List<Map<String,Object>> selectWorkOrderMaterials(String workOrderId);

    int updateWorkOrderShortageStatus(
            @org.apache.ibatis.annotations.Param("workOrderId") String workOrderId,
            @org.apache.ibatis.annotations.Param("status") String status
        );
    
    // 입고 모달 목록
    List<Map<String,Object>> getInboundDoneList();
    
    String findWorkOrderIdByInbound(String inboundId);
}
