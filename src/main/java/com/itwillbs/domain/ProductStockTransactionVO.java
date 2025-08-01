package com.itwillbs.domain;

import java.util.Date;

import lombok.Data;

@Data
public class ProductStockTransactionVO {

    private int transactionId;   // 이력 ID
    private String lotNo;        // LOT 번호
    private String type;         // 처리유형: 입고 / 출고
    private int qty;             // 수량

    private String clientId;     // 거래처 ID (출고 시)
    private String productId;    // 제품 ID

    private Date regDate;        // 처리일시
    private String manager;      // 담당자
    private String remark;       // 비고

    // 👇 조인 결과로 거래처명 표시용 (선택 사항)
    private String clientName;

   
}
