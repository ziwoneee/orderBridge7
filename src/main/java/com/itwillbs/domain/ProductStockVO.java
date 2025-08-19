package com.itwillbs.domain;

import java.util.Date;
import java.util.List;

import lombok.Data;
import com.itwillbs.domain.ProductStockTransactionVO; 

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
    private int cancelQty;  //취소수량
    
    // ✅ MyBatis가 매핑할 실제 필드
    private Integer inboundQty;   // 입고 총합 (쿼리에서 별칭 inbound_qty)
    private Integer outboundQty;  // 출고 총합 (쿼리에서 별칭 outbound_qty)

  
    
 // ✅ 입출고 상세내역 포함
 // ✅ 리팩토링 후
    private List<ProductStockTransactionVO> transactions;

       
    public int getInboundQty() {
        if (transactions == null) return 0;
        return transactions.stream()
                .filter(t -> "입고".equals(t.getType()))
                .mapToInt(ProductStockTransactionVO::getQty)
                .sum();
    }

    public int getOutboundQty() {
        if (transactions == null) return 0;
        return transactions.stream()
                .filter(t -> "출고".equals(t.getType()))
                .mapToInt(ProductStockTransactionVO::getQty)
                .sum();
    }

    
    
    
}
