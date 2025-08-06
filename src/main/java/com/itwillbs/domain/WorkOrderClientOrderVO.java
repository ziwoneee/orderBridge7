package com.itwillbs.domain;

import java.util.Date;
import lombok.Data;

@Data
public class WorkOrderClientOrderVO {
    private int id;                     // PK
    private String workOrderId;         // 작업지시 ID (FK)
    private String clOrderId;           // 수주 번호 (FK)
    private String productId;           // 제품 ID (FK)
    private int orderQty;               // 수주 수량
    private Date createdAt;             // 등록일시
}