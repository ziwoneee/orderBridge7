package com.itwillbs.dto;

import lombok.Data;

//LOT 선택 단위
@Data
public class LotPickDTO {
	
	private String lotNo;      // 선택 LOT
	private String inventoryId; // 선택 인벤토리ID(있으면 더 정확)
	private int qty;           // 선택 수량

}