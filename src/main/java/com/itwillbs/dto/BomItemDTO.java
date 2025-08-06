package com.itwillbs.dto;

import lombok.Data;

/**
 * 제품별 BOM 기준 자재 소요량 정보를 담는 DTO
 * (작업지시 등록 시, 화면에서 자재 확인용으로 사용)
 */
@Data
public class BomItemDTO {
    private String workOrderId;        // 작업지시번호 추가 (FK용)
    private String materialId;
    private String materialName;
    private String unit;
    private String materialType;
    private Double qty;            // 1팩당 소요량
    private Double totalQty;	   // 총 소요량
    
}