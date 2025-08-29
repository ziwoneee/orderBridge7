package com.itwillbs.dto;

import java.util.List;

import com.itwillbs.domain.MaterialInboundItemVO;
import com.itwillbs.domain.MaterialInboundVO;

import lombok.Data;

@Data
public class MaterialInboundDTO {
	
	// 입고 마스터 정보 (inbound_id, order_id, 담당자 등)
	private MaterialInboundVO inbound;
	
	// 입고 자재 항목 리스트
	private List<MaterialInboundItemVO> inboundItems;

}
