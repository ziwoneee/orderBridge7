package com.itwillbs.dto;

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


    public boolean isShippable() {
        return stockQty >= orderQty;
    }
}
