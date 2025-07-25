package com.itwillbs.dto;

import java.sql.Date;

import lombok.Data;

@Data
public class MaterialOutboundSummaryDTO {
	
    private String outboundId;
    private String workOrderNo;
    private Date workOrderDate;
    private Date dueDate;
    private Date outboundDate;
    private String status;
    private String handledBy;

    private String materialName;
    private String supplierName;
    private int requiredQty;
    private int stockQty;
    private String stockStatus;

}
