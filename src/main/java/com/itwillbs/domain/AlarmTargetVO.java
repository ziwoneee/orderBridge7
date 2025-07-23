package com.itwillbs.domain;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class AlarmTargetVO {
    private int targetId;           // 타겟 ID (PK)
    private String alarmId;         // 알림 ID (FK)
    private String adminId;         // 관리자 ID (nullable)
    private String roleId;          // 권한 ID (nullable)
    private int isRead;             // 읽음 여부 (0/1)
    private Timestamp readAt;       // 읽은 시각
}