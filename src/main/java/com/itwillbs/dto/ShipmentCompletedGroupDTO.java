package com.itwillbs.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ShipmentCompletedGroupDTO {
    private String clOrderId;
    private String clientName;
    private Date deliveryDate;
    private List<ShipmentCompletedDTO> productList;
}
