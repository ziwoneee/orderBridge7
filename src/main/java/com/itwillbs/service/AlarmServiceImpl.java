package com.itwillbs.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itwillbs.domain.AlarmLogVO;
import com.itwillbs.domain.AlarmTargetVO;
import com.itwillbs.mapper.AlarmMapper;

@Service
public class AlarmServiceImpl implements AlarmService {

    @Autowired
    private AlarmMapper alarmMapper;

    @Override
    @Transactional
    public void createAlarm(String type, String message, String roleId, String adminId) {
        // 1) 로그 테이블 저장
        String alarmId = UUID.randomUUID().toString();

        AlarmLogVO log = new AlarmLogVO();
        log.setAlarmId(alarmId);
        log.setAlarmType(type);
        log.setMessage(message);
        alarmMapper.insertAlarmLog(log);

        // 2) 대상 분배
        if (adminId != null && !adminId.isBlank()) {
            // (A) 특정 사용자 1명에게
            AlarmTargetVO target = new AlarmTargetVO();
            target.setAlarmId(alarmId);
            target.setAdminId(adminId);
            target.setRoleId(roleId); // 기록용(옵션)
            target.setIsRead(0);
            alarmMapper.insertAlarmTarget(target);
            return;
        }

        if (roleId != null && !roleId.isBlank()) {
            // (B) 역할 전체에게 (콤마로 여러 개 가능: "PROD,MATERIAL")
            List<String> roles = Arrays.stream(roleId.split(","))
                                       .map(String::trim)
                                       .filter(s -> !s.isEmpty())
                                       .collect(Collectors.toList());
            if (!roles.isEmpty()) {
                alarmMapper.insertTargetsByRoles(alarmId, roles); // ✅ ACTIVE만 전파됨(XML 참고)
                return;
            }
        }

        // 대상 파라미터가 전혀 없으면 아무도 받지 않음 (필요시 로깅 추가)
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

    @Override
    public List<AlarmLogVO> getUnreadAlarmsByAdmin(String adminId) {
        return alarmMapper.selectUnreadAlarmsByAdmin(adminId);
    }
}
