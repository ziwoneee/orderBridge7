package com.itwillbs.domain;

import lombok.Data;
import java.util.Date;

@Data
public class ProductVO {
    private String productId;      // 제품ID
    private String productName;    // 제품명
    private int unitPrice;         // 판매가
    private String unit;           // 단위
    private int minOrderQty;       // 최소주문수량
    private String storageMethod;  // 보관방법
    private String expirationType; // 유통기한구분
    private String recipeCode;          // 레시피코드
    private String productStatus;  // 제품상태
    private Date regDate;          // 등록일자
    private Date updDate;          // 수정일자
    
    private String deleteYn; // 소프트삭제

}
