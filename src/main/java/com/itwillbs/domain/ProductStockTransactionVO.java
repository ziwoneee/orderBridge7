package com.itwillbs.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class ProductStockTransactionVO {

    private int transactionId;   // 이력 ID
    private String lotNo;        // LOT 번호
    private String type;         // 처리유형: 입고 / 출고 / 출하취소 등
    private int qty;             // 수량

    private String clientId;     // 거래처 ID (출고 시)
    private String productId;    // 제품 ID

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Date regDate;        // 처리일시

    private String manager;      // 담당자
    private String remark;       // 비고

    // 👇 조인 결과로 거래처명 표시용
    private String clientName;

    // ✅ 고유 식별자들 추가
    private String inboundId;    // 입고 ID (입고 이력 구분용)
    private String outboundId;   // 출고 ID (출고 이력 구분용)
    private String clOrderId;    // 수주 ID (출하취소 등에서 사용)
}
