package com.itwillbs.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 출고 상세 DTO
 * - 출고 기본 정보 + 출고 자재 목록 포함
 */
@Data
public class MaterialOutboundDetailDTO {
	
	private String outboundId; // 등록 시 생성
	private String workOrderId;
	private String productId;
	private String lineId;
	private Date dueDate;
	private String handledBy;
	private List<MaterialOutboundItemDTO> materialList;

	// 상세 응답 시 items(헤더+항목) 용도로 재사용 가능
}
