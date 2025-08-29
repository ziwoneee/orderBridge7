package com.itwillbs.dto;

import lombok.Data;
import java.util.List;

@Data
public class ShipmentPendingGroupDTO {
    private String clOrderId;
    private String clientName;
    private List<ShipmentProductDTO> productList;
}
