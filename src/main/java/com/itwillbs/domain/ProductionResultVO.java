package com.itwillbs.domain;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class ProductionResultVO {

    private String resultId;        // 생산실적번호 (생산 실적 고유 ID)
    private String orderId;         // 작업지시번호
    private String lotNo;           // LOT 번호
    private Integer actualQty;          // 총 생산 수량 (실제 생산 수량)
    private Integer defectQty;          // 불량 수량 (총 불량 수량)
    private String productId;  
    private String workerName;      // 작업자명
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private Date startedAt;         // 작업 시작 시간
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private Date endedAt;           // 작업 종료 시간
    private Date createdAt;         // 등록일
    
    private String productName;


}
