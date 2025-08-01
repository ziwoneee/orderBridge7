package com.itwillbs.dto;

import java.util.Date;

import lombok.Data;

/**
 * 미입고 발주건 표시용 DTO (자재 입주관리에 사용)
 */
@Data
public class UnreceivedOrderDTO {
	
	private String orderId;
    private String createdBy;
    private Date expectedArrivedDate;

    private String materialName;     // 대표 품명 (ex. 사골곰탕 외 2건)
    private int totalQuantity;       // 발주 총 수량
    private int inboundQuantity;     // 입고된 수량 (기본 0)

    
}
