package com.itwillbs.dto;

import lombok.Data;

@Data
public class WorkOrderMaterialDTO {
    private String workOrderId;   // 작업지시 ID (FK)
    private String materialId;    // 자재 코드
    private double requiredQty;   // 총 소요량
}