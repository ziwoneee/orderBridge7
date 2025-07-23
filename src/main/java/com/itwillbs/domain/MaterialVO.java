package com.itwillbs.domain;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class MaterialVO {

    private String materialId;           // 자재 ID (원자재 고유 번호)
    private String materialName;         // 자재명 (예: 양지머리, 대파)
    private String materialType;         // 자재 유형 (예: 식육, 채소류, 포장재)
    private String unit;                 // 단위 (kg, g, 개 등 재고 수량이 어떤 단위로 관리되는지의 컬럼)
    private BigDecimal unitPrice;        // 단가 (기본 단가)
    private String storageMethod;        // 보관 방법 (냉동/냉장/상온 등)
    private String storageLocation;      // 보관 위치 (예: WH001)
    private int safety_stock;			 // 부족 여부 판단 기준
    private String lotFlag;              // LOT 관리 여부 (Y/N 관리 여부)
    private String supplyUnit;           // 입고 단위 (예: 10kg 망, 20kg 박스 등)

}