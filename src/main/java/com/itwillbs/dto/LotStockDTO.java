package com.itwillbs.dto;

import lombok.Data;

// ✅ LOT 재고 정보 리스트를 담기 위한 내부 static 클래스
@Data
public class LotStockDTO {
    private String lotNo;
    private int remainingQty;
}


