package com.itwillbs.dto;

import lombok.Data;

/**
 * 출고 상세 내 자재 정보 DTO
 */
@Data
public class MaterialOutboundItemDTO {
	
	private String materialId;
    private String materialName;
    private int requiredQty;
    private int stockQty;

}
