package com.itwillbs.dto;

import lombok.Data;

/**
 * 자재별 거래처 검색 결과를 담는 DTO 
 */
@Data
public class SupplierItemDTO {
	
	private String materialName;     // 자재명
    private String supplierId;       // 거래처 ID
    private String supplierName;     // 거래처명
    private int unitPrice;           // 공급 단가
    private String warehouseCode;    // 입고 창고명

}
