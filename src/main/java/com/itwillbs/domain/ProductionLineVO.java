package com.itwillbs.domain;

import lombok.Data;

@Data
public class ProductionLineVO {

    private String lineId;              // 생산라인번호 (라인 고유 ID)
    private String lineName;            // 생산라인명 (예: 라인1)
    private String availableProduct;    // 생산 가능 품목명

}
