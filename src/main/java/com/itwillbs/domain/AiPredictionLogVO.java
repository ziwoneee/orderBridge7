package com.itwillbs.domain;

import java.sql.Timestamp;
import lombok.Data;

@Data
public class AiPredictionLogVO {

    private Long logId;                  // 로그 ID (GPT 예측 로그 고유 식별자)
    private String requestedBy;            // 요청자 (예측 요청 사용자 ID 또는 이름)
    private String requestType;            // 요청 유형 (예: 수요예측, 납기예측)
    private String inputDataJson;          // 입력 데이터 (요청 시 전달한 JSON 입력)
    private String predictionResultJson;   // 예측 결과 (GPT 예측 결과 데이터 JSON)
    private Timestamp createdAt;           // 요청 시각 (예측 요청 시각)

}
