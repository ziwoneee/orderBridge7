package com.itwillbs.domain;

import java.util.Date;
import lombok.Data;

@Data
public class ProductOrderVO {
    private String orderId;       // 발주ID
    private String productId;     // 제품ID
    private int orderQty;         // 발주수량
    private Date orderDate;       // 발주일자
    private String clientId;      // 발주처ID
    private String orderStatus;   // 상태 (예: 미처리/처리/취소 등)
    private String lotNo;         // LOT관리번호
    private String remark;        // 비고
    private Date regDate;         // 등록일
    private Date updDate;         // 수정일
}
