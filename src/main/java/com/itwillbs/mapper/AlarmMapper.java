package com.itwillbs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.itwillbs.domain.AlarmLogVO;
import com.itwillbs.domain.AlarmTargetVO;

public interface AlarmMapper {

    // 알림 로그 등록
    void insertAlarmLog(AlarmLogVO vo);

    // 알림 수신 대상 등록
    void insertAlarmTarget(AlarmTargetVO vo);

    // 무한스크롤용 알림 목록 조회 (최신순 정렬)
    List<AlarmLogVO> selectPagedAlarms(
        @Param("adminId") String adminId,
        @Param("offset") int offset,
        @Param("limit") int limit
    );

    // 읽지 않은 알림 개수
    int countUnreadAlarms(@Param("adminId") String adminId);

    // 알림 읽음 처리
    void markAsRead(@Param("targetId") int targetId);

    // 알림 삭제 (완전 삭제)
    void deleteTarget(@Param("targetId") int targetId);
    
    // 안 읽은 알람 목록 조회
    List<AlarmLogVO> selectUnreadAlarmsByAdmin(@Param("adminId") String adminId);
    
    int insertTargetsByRoles(@Param("alarmId") String alarmId,
            @Param("roles") List<String> roles);
}
