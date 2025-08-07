package com.itwillbs.domain;

import java.util.Date;
import lombok.Data;

@Data
public class WorkOrderMaterialVO {
    private int id;                     // PK
    private String workOrderId;         // 작업지시 ID (FK)
    private String materialId;          // 자재 코드 (FK)
    private int requiredQty;            // 총 소요량
    private Date createdAt;             // 등록일시
}