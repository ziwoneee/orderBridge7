package com.itwillbs.domain;

import java.util.Date;
import lombok.Data;

@Data
public class MaterialInboundVO {

    private String inboundId;         // 입고 ID (입고 고유 식별자)
    private String orderId;           // 발주 ID (해당 입고가 연결된 발주서 ID)
    private Date inboundDate;         // 입고일 (입고 처리된 날짜)
    private String handledBy;         // 입고 담당자 (입고 처리자 이름 또는 ID)
    private String inboundStatus; 	  // 입고 상태
    private String note;              // 비고 (비고 또는 특이사항)
    private Date createdDate;
    private Date updatedDate;
    
    
    private Date expectedArrivedDate;  // 예상입고일
    private Date orderDate;            // 발주일자
    private String supplierId;         // 거래처 ID
    private String supplierName;

}