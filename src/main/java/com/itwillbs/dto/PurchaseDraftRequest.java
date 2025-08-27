package com.itwillbs.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class PurchaseDraftRequest {
	
	private String workOrderId;              // 화면의 작업지시번호
    private List<ShortageItem> items;        // 부족 자재 목록
    
    private String requesterId;
    private String requestedBy;

    @Data
    public static class ShortageItem {
        private String materialId;
        private BigDecimal lackQty;                 // 부족 수량(= 발주 후보 수량)
    }

}
