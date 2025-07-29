package com.itwillbs.dto;

import java.util.List;

import com.itwillbs.domain.MaterialOrderItemVO;
import com.itwillbs.domain.MaterialOrderVO;

import lombok.Data;

@Data
public class MaterialOrderDTO {
	
	private MaterialOrderVO order;                  // 발주 기본 정보
    private List<MaterialOrderItemVO> orderItems;   // 발주 항목 리스트

}
