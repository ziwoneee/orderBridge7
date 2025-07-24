package com.itwillbs.domain;

import java.util.Date;

import lombok.Data;

@Data
public class ClientVO {
    private String clientId;
    private String clientName;
    private String clientType1;
    private String clientType2;
    private String businessNumber;
    private String ceoName;
    private String clientTel;
    private String businessType;
    private String businessProduct;
    private String managerName;
    private String managerDept;
    private String managerTel;
    private String managerEmail;
    private String faxNumber;
    private Integer postCode;
    private String address;
    private String addressDetail;	    
    private Integer statusCode;
    private Date createdAt;
}
