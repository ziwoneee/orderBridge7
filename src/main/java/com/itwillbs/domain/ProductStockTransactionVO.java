package com.itwillbs.domain;

import java.util.Date;
import lombok.Data;

@Data
public class ProductStockTransactionVO {
    private String type;          // 입고 or 출고
    private int qty;              // 수량
    private Date regDate;         // 입고일 or 출고일
    private String clientName;    // 출고일 경우 거래처명
    private String memo;          // 비고
}
