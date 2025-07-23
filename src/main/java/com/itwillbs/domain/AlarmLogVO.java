package com.itwillbs.domain;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class AlarmLogVO {
    private String alarmId;         // 알림 ID (UUID)
    private String alarmType;       // 알림 타입
    private String message;         // 메시지
    private Timestamp createdAt;    // 생성일시

    // JOIN용 alarm_target 테이블 정보 포함
    private Integer targetId;       // 타겟 PK
    private Integer isRead;         // 읽음 여부
    private Timestamp readAt;       // 읽은 시각
}