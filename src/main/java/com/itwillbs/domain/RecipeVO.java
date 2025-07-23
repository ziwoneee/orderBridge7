package com.itwillbs.domain;

import java.util.Date;
import lombok.Data;

@Data
public class RecipeVO {

    private String recipeId;       // 레시피ID (레시피/BOM 고유 번호)
    private String recipeCode;     // 레시피코드 (레시피(BOM) 코드)
    private String productId;      // 제품ID (완제품(제품) 코드)
    private String recipeName;     // 레시피명 (레시피명/중량명)
    private String productName;    // 품목명 (완제품 명)
    private int recipeQty;         // 필요수량 (자재/원료 필요 수량)
    private Date recipeDate;       // 등록일자
    private String recipeEtc;      // 비고 (기타 메모)

}
