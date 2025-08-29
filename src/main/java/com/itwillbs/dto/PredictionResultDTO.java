package com.itwillbs.dto;

import java.util.List;

import lombok.Data;

/**
 * LLM 또는 휴리스틱 결과
 */
@Data
public class PredictionResultDTO {
	
	private String workOrderId;
    private Integer etaDays;       // 예측 소요일 수(오늘 기준)
    private String riskLevel;      // LOW/MEDIUM/HIGH
    private String reason;         // 요약 근거
    private List<String> actions;  // 권장 액션 리스트
    private String stage;
    

}
