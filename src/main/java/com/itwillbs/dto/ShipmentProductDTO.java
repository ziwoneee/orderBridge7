package com.itwillbs.dto;

import java.util.Date;

import lombok.Data;

@Data
public class ShipmentProductDTO {
    private String productId;
    private String productName;
    private int orderQty;
    private int stockQty;
    private String lotNum;
    private Long orderDetailId;
    private Long detailId;
    private Date clDeliveryDate; // 고객 요청 납기일


    public boolean isShippable() {
        return stockQty >= orderQty;
    }
    
 }
