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
	
	// 출고 기본 정보
    private String outboundId;
    private String workOrderNo;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private Date workOrderDate;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private Date dueDate;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private Date outboundDate;
    
    private String handledBy;
    private String status;
    private String workOrderManager; // 작업지시 담당자
    private String materialName;

    // 출고 자재 상세 목록
    private List<MaterialOutboundItemDTO> materialList;

}
