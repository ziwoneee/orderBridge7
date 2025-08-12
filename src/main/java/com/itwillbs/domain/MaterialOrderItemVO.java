package com.itwillbs.domain;

import lombok.Data;

@Data
public class MaterialOrderItemVO {

    private String orderItemId;           // 발주 상세 ID (발주 항목 고유 ID)
    private String orderId;               // 발주 ID (연결된 발주서 ID)
    private String materialId;            // 자재 ID (발주한 자재 ID)
    private String workOrderId;
    private Integer orderQuantity;            // 수량 (요청한 수량)
    private Integer unitPrice;         		  // 단가 (단가)
    private Integer totalPrice;        		   // 총 금액 (수량 * 단가)
    private String warehouseCode;         // 입고창고 (자재 납입 예정 창고)
    
    // 화면 표시용 추가 필드
    private String materialName;          // 자재명 (모달에서 사용)

}