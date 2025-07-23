package com.itwillbs.domain;

import java.util.Date;
import lombok.Data;

@Data
public class WorkOrderVO {

    private String orderId;         // 작업 지시 번호
    private String planId;          // 생산 계획 번호
    private String lineId;          // 생산 라인 ID
    private int orderQty;           // 지시 수량
    private Date orderDate;         // 지시일자
    private String status;          // 상태 (대기/진행중/완료)

}
