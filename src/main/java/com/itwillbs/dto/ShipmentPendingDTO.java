package com.itwillbs.dto;

import lombok.Data;
import java.util.Date;



@Data
public class ShipmentPendingDTO {
    private Long orderDetailId;      // 주문 상세 ID
    private String clOrderId;        // 수주번호
    private String clientName;       // 거래처명
    private String productId;        // 제품 ID
    private String productName;      // 제품명
    private int orderQty;            // 수주 수량
    private int stockQty;            // 현재고 수량
    private String lotNo;           // LOT 번호

    // 아래는 출하 처리 시 필요할 수 있는 필드들 (ClientDeliveryVO에서 확장)
    private Long deliveryId;         // 출하 이력 고유 ID
    private Date deliveryDate;       // 출하일자
    private int deliveryQty;         // 출하 수량
    private String deliveryStatus;   // 출하 상태 (예: 배송준비, 배송완료)
    private int empId;               // 담당자 ID
    private String trackingNumber;   // 운송장번호
    private String memo;             // 비고
    private Date createdAt;          // 생성일시
    private Date updatedAt;          // 수정일시
    private String pdfFile;          // PDF 파일명
    private String isPdfGenerated;   // PDF 생성 여부 ('Y', 'N')
    private Long detailId;      // 수주 상세 ID
    
    // 출하 가능 여부 판단용
    public boolean isShippable() {
        return stockQty >= orderQty;
    }


  
}


