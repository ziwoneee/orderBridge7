package com.itwillbs.mapper;

import java.util.List;
import java.util.Map;

public interface AiPredictionMapper {
	
	// 작업지시서 메타 요약
    Map<String, Object> selectWorkOrderSummary(String workOrderId);

    // 자재별 부족 현황(필요-가용)
    List<Map<String, Object>> selectShortageByMaterial(String workOrderId);

    // 자재별 과거 리드타임 통계
    List<Map<String, Object>> selectLeadTimeStatsByMaterials(List<String> materialIds);

    // 상태로 작업지시서 목록 조회: 드롭다운/자동완성 등에 사용
    List<Map<String, Object>> selectWorkOrdersByStatus(String status);


}
