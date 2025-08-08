package com.itwillbs.dto;

import java.util.List;

import lombok.Data;

@Data
public class PurchaseDraftResult {
	
	private String orderId;                  // 생성된 초안 발주번호 (PO-RM-YYYYMMDD-###)
    private List<String> unmappedMaterials;  // 거래처/단가 매핑 못 찾은 자재ID 목록

}
