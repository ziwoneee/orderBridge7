package com.itwillbs.domain;

import java.util.Date;
import lombok.Data;

@Data
public class AdminUserVO {

    private String adminId;       // 관리자번호 (로그인 ID)
    private String password;      // 비밀번호 (암호화 저장)
    private String name;          // 이름 (관리자 이름)
    private String phone;         // 연락처
    private String roleId;        // 권한 ID (권한 테이블 참조)
    private int failCount;        // 로그인 실패 횟수 (5회 시 잠금 등)
    private String status;        // 계정 상태 (ACTIVE / INACTIVE)
    private Date createdAt;       // 등록일시 (계정 생성일)
    private Date updatedAt;       // 수정일시 (정보 수정 시점)
    
    private Date lastLoginAt;   // 마지막 로그인 성공 시각
    private Date lastFailedAt;  // 마지막 로그인 실패 시각
    private Date lockedAt;      // 잠금 시각

}
