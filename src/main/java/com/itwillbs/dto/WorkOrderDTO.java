package com.itwillbs.dto;

import java.util.Date;
import lombok.Data;

/**
 * 작업지시 + 제품 + 라인 + 거래처 + 재고 정보를 담는 통합 DTO
 */
@Data
public class WorkOrderDTO {

    //작업 지시 기본 정보
    private String orderId;        // 작업 지시 번호 (PK)
    private String clOrderId;      // 연동된 수주 번호
    private Date dueDate;          // 납품 예정일
    private Date createdAt; 		// 작업 지시 등록일
    private int orderQty;          // 지시 수량
    private String priority;       // 우선순위 (EMERGENCY, HIGH, NORMAL, LOW)
    private String status;         // 상태 (WAITING, READY, IN_PROGRESS, DONE 등)

    //제품 정보
    private String productId;      // 제품 ID (FK)
    private String productName;    // 제품명
    private String unit;           // 제품 단위 (예: 개, 봉지)

    //생산 라인 정보
    private String lineId;         // 생산 라인 ID (FK)
    private String lineName;       // 생산 라인명

    //거래처(수주) 정보
    private String clientId;       // 거래처 ID
    private String clientName;     // 거래처명
    private Date clOrderDate;      // 수주일

    //재고 및 계산 정보
    private int stockQty;          // 현재 재고 수량
    private int requiredQty;       // 예상 필요 수량 (재고 - 수주 기준 계산)
}
