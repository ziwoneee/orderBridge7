package com.itwillbs.domain;

import java.util.Date;
import lombok.Data;

@Data
public class DeliveryPredictionVO {

    private String predictionId;         // 납기예측 ID
    private String orderId;              // 주문 ID (연결된 주문 ID)
    private String materialId;           // 자재 ID (연결된 자재 ID)
    private Date requiredDate;           // 요청 납기일
    private Date predictedDeliveryDate;  // 예측 납기일 (GPT 예측 결과)
    private String riskLevel;            // 위험도 (low / medium / high)
    private String reason;               // 지연 사유 (GPT가 출력한 사유)
    private String generatedBy;          // 생성자 (자동 / 관리자)

}
