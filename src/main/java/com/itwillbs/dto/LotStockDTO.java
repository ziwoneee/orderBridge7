package com.itwillbs.dto;

import java.util.Date;

import lombok.Data;


// ✅ LOT 재고 정보 리스트를 담기 위한 내부 static 클래스
@Data
public class LotStockDTO {
    private String lotNo;
    private String productId;
    private int inboundQty;
    private int totalOutboundQty;
    private int reservedQty;
    private int availableQty; 
    private Date expireDate;
}
