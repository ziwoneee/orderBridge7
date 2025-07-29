package com.itwillbs.domain;

import lombok.Data;
import java.util.Date;

@Data
public class StockReservationVO {
    private Long reservationId;      // 예약 고유 ID (PK)
    private String clOrderId;        // 수주 번호
    private String productId;        // 제품 ID
    private String lotNo;            // LOT 번호
    private int reservedQty;         // 예약 수량
    private Date reservedAt;         // 예약 생성 시각
	public String clientId; // 고객ID
	public String manager; //담당자명
	public int detailId; //주문상세 ID
	public Date createdAt;
	
}