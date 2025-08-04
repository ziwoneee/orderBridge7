package com.itwillbs.dto;

import lombok.Data;

/**
 * 개별 입고 항목 처리용 DTO
 * - 자재ID, LOT, 유통기한, 수량, 창고 정보 포함
 */
@Data
public class MaterialInboundItemDTO {
	
	private String inboundId;          // 입고관리번호 (IN-RM-20250804-001 등)
    private String materialId;         // 자재ID (RM-0001 등)
    private String lotNo;              // LOT 번호
    private String expirationDate;     // 유통기한 (yyyy-MM-dd)
    private int quantity;              // 입고 수량
    private String warehouseCode;      // 창고 코드 (WH001 등)

    private String materialName;
    private int orderQuantity;
    private String inboundStatus;
    
}
