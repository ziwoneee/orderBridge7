package com.itwillbs.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.Data;

@Data
public class SupplierItemVO {
	
	private int id;                     // 공급 품목 고유 ID (PK)
    private String supplierId;         // 거래처 ID (FK)
    private String materialId;         // 자재 ID (FK)
    
    // 화면/JS에서 필요
    private String materialName;   // 자재명 (조인된 필드)
    private String materialType;   // 자재유형 (조인된 필드)
    
    // 단위/환산
    private String orderUnit;    // 예: EA, BOX, BAG...
    private String priceUnit;    // 예: KG, PACK, EA...
    private Double convToStock;  // orderUnit 1개 ≈ convToStock stockUnit
    private String stockUnit;    // 재고단위 (KG, PACK, EA 등)
    
    // 금액/발주 제약
    private Double unitPrice;
    private Double minOrderQty;
    private Double orderMultiple;

    // 기타
    private String warehouseCode;
    private String note;
    private String supplyAvailable; // 'Y'/'N'
    
    

    
    private String unit;                // 단위 (kg, 박스 등)
    
    private Timestamp createdAt;       // 등록일
    private Timestamp updatedAt;       // 수정일
    private int leadDays;	
    
    
    
    
    // 발주 수정중
    private BigDecimal packQty;       // 포장단위(발주단위)
    private BigDecimal convToBase;    // 발주→기준 환산
    


}
