// com.itwillbs.dto.ReservationDetailDTO
package com.itwillbs.dto;

import lombok.Data;

@Data
public class ReservationDetailDTO {
    private String lotNo;
    private String productName;
    private String clOrderId;
    private String clientName;
    private Integer reservedQty;
    private String reservedAt;           // ISO string
    private String reservedAtFormatted;  // yyyy-MM-dd HH:mm
    private String expireDate;           // yyyy-MM-dd
    private Integer currentStock;        // LOT 현재 재고
    private String deliveryDate;         // 납기요청일(yyyy-MM-dd)
}
