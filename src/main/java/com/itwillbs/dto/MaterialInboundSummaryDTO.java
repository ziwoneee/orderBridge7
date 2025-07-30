package com.itwillbs.dto;

import java.util.Date;

import lombok.Data;

/**
 * 입고 목록 요약 DTO
 * - 대표 자재명, 총 수량, 진행 상태 등을 포함하여 리스트 테이블에서 사용
 */
@Data
public class MaterialInboundSummaryDTO {
	
    private String inboundId;           // 입고관리번호
    private String orderId;             // 발주관리번호
    private String representativeName;  // 대표 자재명 + 외 N건
    private int totalOrderQty;          // 발주 수량 총합
    private int totalInboundQty;        // 입고 수량 총합
    private String status;              // 진행현황 (입고완료 / 미입고 / 부분입고)
    private String handledBy;           // 입고 담당자
    private Date inboundDate;           // 입고일자
    private Date expectedArrivedDate;   // 예상 입고일

}
