package com.itwillbs.domain;

import java.util.Date;
import lombok.Data;

@Data
public class ProductStockTransactionVO {
    private Date regDate;
    private String type;
    private int qty;
    private String memo;
}
