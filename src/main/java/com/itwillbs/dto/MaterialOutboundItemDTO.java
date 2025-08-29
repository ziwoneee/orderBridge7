package com.itwillbs.dto;

import java.util.List;

import lombok.Data;

/**
 * 출고 상세 내 자재 정보 DTO
 */
@Data
public class MaterialOutboundItemDTO {
	
	  private String materialId;
	  private String materialName; // 전송은 선택
	  private int requiredQty;     // 검증용
	  private int stockQty;        // 조회용(전송 불필요)
	  private List<LotPickDTO> lots; // LOT 배분 목록

	  // 서버 INSERT 시 quantity/lot_no는 lots를 풀어서 저장
}
