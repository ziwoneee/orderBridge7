package com.itwillbs.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * LLM에 보낼 핵심 피처 묶음
 */
@Data
public class PredictionInputDTO {
	
	private String workOrderId;         // 작업지시서 ID
    private String productId;           // 제품 ID
    private String dueDate;             // 납기(yyyy-MM-dd)
    private Integer orderQty;           // 지시 수량

    // 자재별 부족 요약: material_id, required_qty, available_qty, shortage_qty
    private List<Map<String, Object>> shortages;

    // 리드타임 통계: material_id, avg_lead_days, max_lead_days, samples
    private List<Map<String, Object>> leadStats;

}
