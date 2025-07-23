package com.itwillbs.dto;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class ProductionPlanDTO {
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date dueDate;         // 납기일 (수주 납기)

    // DB insert용
    private String planId;        // 생산계획 ID (자동 생성)
    private String clOrderId;     // 수주 ID
    private String productId;     // 제품 ID
    private int orderQty;         // 수주 수량
    private String priority;      // 우선순위 (NORMAL, HIGH, EMERGENCY 등)
    private String status;        // 상태 (WAITING, IN_PROGRESS, DONE 등)
    private int plannedQty;   // 전체 생산 예정 수량 (불량 포함)
    
    // 조회용 (화면 표시용)
    private int requiredQty;      // 생산 필요 수량
    private String clientId;      // 거래처 ID
    private String clientName;    // 거래처명
    private Date clOrderDate;     // 수주일
    private String productName;   // 제품 이름
    private String unit;          // 단위
    private int stockQty;         // 현재 재고 수량
    private int reservedQty;      // 예약 수량
    private int availableQty;     // 가용 재고 = stockQty - reserved
}
