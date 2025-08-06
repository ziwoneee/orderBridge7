package com.itwillbs.dto;

import lombok.Data;

/**
 * 병합된 수주 정보 DTO
 * - 작업지시 등록 시 여러 수주 정보를 담기 위한 DTO
 */
@Data
public class WorkOrderMergedDTO {
	private String workOrderId;
    private String clOrderId;     // 수주번호
    private String productId;     // 제품 ID
    private int orderQty;         // 수주 수량
}
