package com.itwillbs.dto;

import lombok.Data;

/**
 * 제품별 BOM 기준 자재 소요량 정보를 담는 DTO
 * (작업지시 등록 시, 화면에서 자재 확인용으로 사용)
 */
@Data
public class BomItemDTO {
    private String materialId;     // 자재코드
    private String materialName;   // 자재명
    private String unit;           // 단위
    private String materialType;   // 자재 용도
    private Double qty;            // 1팩당 소요량
    private Double totalQty;       // 총 소요량 (계산값)
    
    public Double getTotalQty() {
        System.out.println("totalQty: " + totalQty);
        return totalQty;
    }
}