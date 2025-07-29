package com.itwillbs.domain;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class SupplierItemVO {
	
	private int id;                     // 공급 품목 고유 ID (PK)
    private String supplierId;         // 거래처 ID (FK)
    private String materialId;         // 자재 ID (FK)
    
    private double unitPrice;          // 단가
    private String unit;                // 단위 (kg, 박스 등)
    private String supplyAvailable;    // 공급 가능 여부 (Y/N)
    private String note;                // 비고
    
    private Timestamp createdAt;       // 등록일
    private Timestamp updatedAt;       // 수정일
    
    private String materialName;   // 자재명 (조인된 필드)
    private String materialType;   // 자재유형 (조인된 필드)
    
    // 화면용 필드 (material 테이블의 입고창고 정보)
    private String storageLocation;  

}
