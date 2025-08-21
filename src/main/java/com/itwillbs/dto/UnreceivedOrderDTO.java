package com.itwillbs.dto;

import java.util.Date;

import lombok.Data;

/**
 * 미입고 발주건 표시용 DTO (자재 입주관리에 사용)
 */
@Data
public class UnreceivedOrderDTO {
	
	private String orderId;				 // 발주관리번호
	private String supplierId;        	 // 거래처
	private Date expectedArrivedDate;	 // 예상입고일
	private String handledBy;		     // 발주담당자 (handled_by)
    private String createdBy;			 // 작성자 (created_by) - 필요한 경우
    private String materialNames;  		 // 품명 (GROUP_CONCAT 결과)
    private String materialName;    	 // 단일 품명 (materialNames의 별칭)
    private int totalOrderQuantity;		 // 총 발주수량
    private int totalQuantity;      	 // 총 수량 (totalOrderQuantity의 별칭)
    private Date createdDate;         // 발주일자
    private int inboundQuantity;    	 // 입고된 수량 (기본 0)
    
    private String handledByName;
    private String createdByName;
    
}
