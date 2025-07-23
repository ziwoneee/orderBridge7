package com.itwillbs.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itwillbs.domain.AlarmLogVO;
import com.itwillbs.domain.AlarmTargetVO;
import com.itwillbs.mapper.AlarmMapper;

@Service
public class AlarmServiceImpl implements AlarmService {

    @Autowired
    private AlarmMapper alarmMapper;

    @Override
    public void createAlarm(String type, String message, String url, String roleId, String adminId) {
        String alarmId = UUID.randomUUID().toString();

        // 1. 로그 테이블 저장
        AlarmLogVO log = new AlarmLogVO();
        log.setAlarmId(alarmId);
        log.setAlarmType(type);
        log.setMessage(message);
        alarmMapper.insertAlarmLog(log);

        // 2. 대상 등록
        AlarmTargetVO target = new AlarmTargetVO();
        target.setAlarmId(alarmId);
        target.setAdminId(adminId);
        target.setRoleId(roleId);
        target.setIsRead(0);
        alarmMapper.insertAlarmTarget(target);
    }

    @Override
    public List<AlarmLogVO> getPagedAlarms(String adminId, int page, int limit) {
        int offset = (page - 1) * limit;
        return alarmMapper.selectPagedAlarms(adminId, offset, limit);
    }

    @Override
    public int getUnreadCount(String adminId) {
        return alarmMapper.countUnreadAlarms(adminId);
    }

    @Override
    public void markAsRead(int targetId) {
        alarmMapper.markAsRead(targetId);
    }

    @Override
    public void deleteAlarm(int targetId) {
        alarmMapper.deleteTarget(targetId);
    }
    
    // 안 읽은 알림 목록 조회 
    @Override
    public List<AlarmLogVO> getUnreadAlarmsByAdmin(String adminId) {
        return alarmMapper.selectUnreadAlarmsByAdmin(adminId);  // MyBatis Mapper 연동
    }
}
