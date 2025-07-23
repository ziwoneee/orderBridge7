package com.itwillbs.domain;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class SupplierVO {

    private String supplierId;           // 거래처 ID
    private String supplierName;         // 거래처명
    private String businessNumber;       // 사업자등록번호
    private String supplierType;         // 협력사 분류
    private String representativeName;   // 대표자명
    private String phone;                // 대표 전화번호
    private String email;                // 대표 이메일
    private String address;              // 주소
    private String addressDetail;		 // 상세주소
    private String zipcode;              // 우편번호
    private String status;               // 상태 (정상 / 계좌이체 등)
    private String settlementMethod;     // 정산 방식
    private String bankName;             // 은행명
    private String accountHolder;        // 예금주명
    private String accountNumber;        // 계좌번호
    private String contactName;          // 담당자 이름
    private String contactPhone;         // 담당자 연락처
    private String contactEmail;         // 담당자 이메일
    private String note;                 // 비고
    private Timestamp createdAt;		 // 최초 등록 시각
    private Timestamp updatedAt;		 // 마지막 수정 시각

}
