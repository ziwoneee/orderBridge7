package com.itwillbs.domain;

import lombok.Data;

@Data
public class ProductionLineVO {

    private String lineId;              // 생산라인번호 (라인 고유 ID)
    private String lineName;            // 생산라인명 (예: 라인1)
    private String status;              // 상태 (ACTIVE, INACTIVE)


    // 조인 결과 받기 위한 용도
    private String currentWorkOrder;    // 현재 진행 중인 작업지시번호
}
