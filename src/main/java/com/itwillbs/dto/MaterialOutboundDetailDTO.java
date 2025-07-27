package com.itwillbs.dto;

import java.sql.Date;
import java.util.List;

import lombok.Data;

/**
 * 출고 상세 DTO
 * - 출고 기본 정보 + 출고 자재 목록 포함
 */
@Data
public class MaterialOutboundDetailDTO {
	
	// 출고 기본 정보
    private String outboundId;
    private String workOrderNo;
    private Date workOrderDate;
    private Date dueDate;
    private Date outboundDate;
    private String handledBy;
    private String status;
    private String workOrderManager; // 작업지시 담당자
    private String materialName;

    // 출고 자재 상세 목록
    private List<MaterialOutboundItemDTO> materialList;

}
