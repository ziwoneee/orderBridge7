package com.itwillbs.dto;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 작업지시 자재 소요 DTO
 * - requiredQty는 BOM 계산 시 소수점이 나올 수 있어 double 유지
 *   (Service에서 INSERT 전 반올림하여 정수 저장 처리)
 */
@Data
public class WorkOrderMaterialDTO {
    private String workOrderId;   // 작업지시 ID (FK)
    private String materialId;    // 자재 코드
    private BigDecimal requiredQty;  // 총 소요량(소수점 포함 가능)
}
