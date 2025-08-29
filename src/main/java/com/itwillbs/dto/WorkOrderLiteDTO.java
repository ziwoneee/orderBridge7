package com.itwillbs.dto;

import lombok.Data;


@Data
public class WorkOrderLiteDTO {
	
    private String orderId;
    private String productId;
    private String lineId;
    private Integer orderQty;
    private String status;            // WAITING / READY / COMPLETED ...
    private String shortageStatus;    // NONE / DRAFTED / CHECKED ...
    private java.util.Date dueDate;     // was LocalDate
    private java.util.Date updatedAt;
    private String productName;

}
