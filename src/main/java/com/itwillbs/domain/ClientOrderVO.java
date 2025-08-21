package com.itwillbs.domain;

import lombok.Data;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

@Data
public class ClientOrderVO {

    private String clOrderId;         // 고객사 수주 ID (예: CLT20250713001)
    private String clientId;          // 거래처 ID (FK: client)
    private String adminId;           // 관리자 ID (FK: admin)
    private String clOrderNum;        // 수주 번호 (ORD-0001 형식)
    private Date clOrderDate;         // 수주 일자
    private String productId;         // 제품 ID (FK: product)
    private int clOrderQty;           // 수주 수량

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date clDeliveryDate;      // 고객 요청 납기일

    private int orderTotalPrice;      // 총 수주 금액
    private String clOrderStatus;     // 수주 상태 (REQUESTED, CONFIRMED, SHIPPED)
    private String deliveryAddress;   // 배송지 주소
    private String postCode;// 우편번호
    private String clOrderMemo;       // 고객 메모

    // 조인한 컬럼 추가
    private String clientName;
    private String productName;  
    
    // --- [고객사 상세 정보 추가] ---
    private String managerName;     // 담당자명
    private String managerTel;      // 담당자 전화번호
    private String address;
    private String addressDetail;
    
    private String adminName; //관리자 이름


   


}
