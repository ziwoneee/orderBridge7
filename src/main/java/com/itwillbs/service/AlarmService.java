package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.AlarmLogVO;

public interface AlarmService {

    // 알림 생성 (공통 알림 + 대상 등록)
    void createAlarm(String type, String message, String roleId, String adminId);

    // 알림 조회 (무한스크롤)
    List<AlarmLogVO> getPagedAlarms(String adminId, int page, int limit);

    // 안 읽은 알림 개수
    int getUnreadCount(String adminId);

    // 읽음 처리
    void markAsRead(int targetId);

    // 삭제 처리
    void deleteAlarm(int targetId);
    
    // 안 읽은 알림 목록 조회
    List<AlarmLogVO> getUnreadAlarmsByAdmin(String adminId);

}
