
package com.itwillbs.dto;

import java.util.Date;
import lombok.Data;

@Data
public class DeliveryHistoryDTO {
    private String productId;
    private String productName;
    private int deliveryQty;
    private String lotNo;
    private Date deliveryDate;
    private String trackingNumber;
    private String deliveryStatus; //배송상태 확인
}
