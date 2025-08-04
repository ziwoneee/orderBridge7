package com.itwillbs.domain;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ProductStockVO {
    private int productStockId;   // PK (Auto Increment)
    private String productId;     // 제품 ID
    private String productName;   // 제품명
    private String unit;          // 단위
    private int stockQty;         // 재고수량
    private int safeQty;          // 안전재고
    private String lotNo;         // LOT 번호
    private Date regDate;         // 등록일
    private Date expireDate;      // 유통기한
    private int reservedQty;   // 예약 수량
    private int availableQty;  // 가용 수량 = 현재고 - 예약수량

    
 // ✅ 입출고 상세내역 포함
    private List<StockTransaction> transactions;

    @Data
    public static class StockTransaction {
        private Date regDate;  // 등록일
        private String type;   // '입고' or '출고'
        private int qty;       // 수량
        private String memo;   // 비고
    }
    
    
}
