package com.itwillbs.domain;

import java.util.Date;
import lombok.Data;

@Data
public class MaterialOrderVO {

    private String orderId;                  // 발주 ID (발주 고유 식별자)
    private String supplierId;               // 거래처 ID (발주 대상 거래처 ID)
    private Date orderDate;                  // 발주일 (발주한 날짜)
    private String orderStatus;              // 발주 상태 (요청, 승인, 입고완료 등)
    private Date expectedArrivedDate;        // 예상 입고일 (입고 예정일)
    private String createdBy;                // 작성자 (발주서 작성자 ID 또는 이름)
    private String note;                     // 비고 (기타 비고사항)

}
