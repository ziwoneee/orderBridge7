package com.itwillbs.domain;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class ApprovalTokenVO {
    private String tokenId;
    private String orderId;
    private String supplierId;
    private String tokenType;
    private boolean isUsed;
    private Timestamp expiresAt;
    private Timestamp createdAt;
    private String email;

}
