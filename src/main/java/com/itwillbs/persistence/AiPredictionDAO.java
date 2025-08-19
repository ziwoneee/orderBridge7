package com.itwillbs.persistence;

import java.util.List;
import java.util.Map;

public interface AiPredictionDAO {
	
	Map<String, Object> getWorkOrderSummary(String workOrderId) throws Exception;
    List<Map<String, Object>> getShortageByMaterial(String workOrderId) throws Exception;
    List<Map<String, Object>> getLeadTimeStatsByMaterials(List<String> materialIds) throws Exception;

}
