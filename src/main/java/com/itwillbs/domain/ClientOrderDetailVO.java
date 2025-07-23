package com.itwillbs.domain;

import lombok.Data;

@Data
public class ClientOrderDetailVO {

    private int detailId;            // 상세 PK
    private String clOrderId;        // 수주ID (FK: client_order)
    private String productId;        // 제품ID (FK: product)
    private int orderQty;            // 주문수량
    private int unitPrice;           // 단가
    private String detailMemo;       // 상세 메모 (요청사항 등)

    // 조인용
    private String productName;      // 제품명(조인)
    // 필요시 추가: 단위, 규격 등

}
