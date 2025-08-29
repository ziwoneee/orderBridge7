package com.itwillbs.dto;

import lombok.Data;
import java.util.Date;

@Data
public class ShipmentCompletedDTO {
    private String deliveryId;
    private String clOrderId;
    private String clientName;
    private String productId;
    private String productName;
    private String lotNo;
    private int deliveryQty;
    private Date deliveryDate;
    private String trackingNumber;
    private String deliveryStatus;
    private String clientId;
    
    private boolean cancelAvailable;
    private Date createdAt;


}
