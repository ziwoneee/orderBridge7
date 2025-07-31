package com.itwillbs.domain;

import java.util.Date;
import lombok.Data;

@Data
public class ClientDeliveryVO {

    private String deliveryId;          // 고객사출하ID (출하 이력 고유 ID)
    private String clOrderId;         // 수주ID (출하 대상 주문 식별자)
    private Date deliveryDate;        // 출하일자 (출하 처리 일시)
    private int deliveryQty;          // 출하수량 (실제 출하된 수량)
    private String lotNo;            // LOT 번호 (출하된 LOT 식별자)
    private String deliveryStatus;    // 출하상태 (예: 배송준비, 배송완료 등)
    private int empId;                // 담당자 (출하 처리 담당자)
    private String clientName;        // 거래처명
    private String clientId;
    private String trackingNumber;    // 운송장번호
    private String memo;              // 비고/메모
    private Date createdAt;           // 생성일시
    private Date updatedAt;           // 수정일시
    private String pdfFile;           // PDF파일명
    private boolean isPdfGenerated;    // PDF 생성 여부 ('Y', 'N')
    
    private String productId;

	

	
	
  

}
