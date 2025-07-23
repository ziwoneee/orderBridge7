package com.itwillbs.domain;

import java.util.Date;
import lombok.Data;

@Data
public class LotInfoVO {

    private String lotNo;           // LOT 번호
    private String orderId;         // 작업지시번호
    private String productId;       // 제품 ID
    private Date createdAt;         // 생산일자

}
