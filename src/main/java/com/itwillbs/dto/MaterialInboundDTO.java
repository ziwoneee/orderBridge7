package com.itwillbs.dto;

import java.util.List;

import com.itwillbs.domain.MaterialInboundItemVO;
import com.itwillbs.domain.MaterialInboundVO;

import lombok.Data;

@Data
public class MaterialInboundDTO {
	
	private MaterialInboundVO inbound;
	private List<MaterialInboundItemVO> inboundItems;

}
