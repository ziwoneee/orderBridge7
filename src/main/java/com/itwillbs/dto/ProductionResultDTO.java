package com.itwillbs.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 생산실적 DTO (필요한 컬럼만 정리)
 */
@Data
public class ProductionResultDTO {

    // ====== DB 기본 컬럼 (production_result) ======
    private String  resultId;
    private String  orderId;
    private String  productId;
    private String  lotNo;
    private Integer actualQty;
    private Integer defectQty;
    private String  workerName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    // ====== JOIN 결과 컬럼 ======
    private String  productName;   // product.product_name
    private String  lineName;      // production_line.line_name
    private Integer orderQty;      // work_order.order_qty
    private String  orderManager;  // work_order.order_manager
    private String  status;        // work_order.status
    private String  priority;      // work_order.priority (필요시)

    // ====== 집계 컬럼 ======
    private Integer producedQty;    // 누적 양품
    private Integer defectQtyTotal; // 누적 불량
    private Integer remainingQty;   // 잔여수량
    private Double progressRate;      // 등록 시점 진행률
    private Integer cumulativeQty;    // 등록 시점 누적량
}
