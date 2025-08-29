package com.itwillbs.dto;

import lombok.Data;

@Data
public class InventoryShortageDTO {
	  private String id;        // material_id or product_id
	  private String name;      // 자재/제품명
	  private int available;    // 가용 수량
	  private int safetyStock;  // 안전재고(원자재는 material.safety_stock, 완제품은 파라미터 or p.safety_stock)
	  private String itemType;  // "MATERIAL" / "PRODUCT"

}
