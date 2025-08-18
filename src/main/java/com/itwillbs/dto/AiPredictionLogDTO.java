package com.itwillbs.dto;

import java.util.Date;

import lombok.Data;

@Data
public class AiPredictionLogDTO {
	
	private Long   logId;
	private String requestedBy;          // 사용자 ID 또는 시스템
    private String requestType;          // 예: ETA_WORK_ORDER / ETA_FALLBACK
    private String inputDataJson;        // 요청 페이로드(JSON 문자열)
	private String predictionResultJson; // 결과(JSON 문자열)
	private Date   createdAt;            // DB default now

}
