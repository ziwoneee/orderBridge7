package com.itwillbs.domain;

import lombok.Data;

@Data
public class AdminRoleVO {

    private String roleId;        // 권한 ID (예: ROLE_ADMIN)
    private String roleName;      // 권한명 (예: 최고관리자, 운영자 등)
    private String description;   // 설명 (권한 역할 설명)

}
