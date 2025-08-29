package com.itwillbs.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.itwillbs.domain.AdminUserVO;
import com.itwillbs.domain.AlarmLogVO;
import com.itwillbs.service.AlarmService;

@RestController
@RequestMapping("/admin/alarm")
public class AlarmController {

	private static final Logger logger = LoggerFactory.getLogger(AlarmController.class);

    @Autowired
    private AlarmService alarmService;

    // 무한스크롤용 알림 목록 조회
    @GetMapping("/list")
    public List<AlarmLogVO> getAlarmList(HttpSession session,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int limit) {
        AdminUserVO loginAdmin = (AdminUserVO) session.getAttribute("loginAdmin");
        if (loginAdmin == null) {
            logger.warn("알림 목록 조회 실패: 로그인 정보 없음");
            return List.of();
        }

        logger.info(" 알림 목록 조회 요청 - adminId: {}, page: {}", loginAdmin.getAdminId(), page);
        return alarmService.getPagedAlarms(loginAdmin.getAdminId(), page, limit);
    }

    // 안읽은 알림 수 (뱃지용)
    @GetMapping("/unread-count")
    public int getUnreadCount(HttpSession session) {
        AdminUserVO loginAdmin = (AdminUserVO) session.getAttribute("loginAdmin");
        if (loginAdmin == null) {
            logger.warn("알림 개수 조회 실패: 로그인 정보 없음");
            return 0;
        }

        logger.info(" 안읽은 알림 개수 요청 - adminId: {}", loginAdmin.getAdminId());
        return alarmService.getUnreadCount(loginAdmin.getAdminId());
    }

    //  알림 읽음 처리
    @PostMapping("/read/{targetId}")
    public void markAsRead(@PathVariable int targetId) {
        logger.info(" 알림 읽음 처리 요청 - targetId: {}", targetId);
        alarmService.markAsRead(targetId);
    }

    //  알림 삭제 (완전 삭제)
    @DeleteMapping("/delete/{targetId}")
    public void deleteAlarm(@PathVariable int targetId) {
        logger.info(" 알림 삭제 요청 - targetId: {}", targetId);
        alarmService.deleteAlarm(targetId);
    }

    // 테스트용 알림 생성
    @PostMapping("/create")
    public void createTestAlarm(@RequestParam String type,
                                @RequestParam String message,
                                @RequestParam(required = false) String roleId,
                                @RequestParam(required = false) String adminId) {
    	logger.info(String.format(" 테스트 알림 생성 요청 - type: %s, message: %s, url: %s, roleId: %s, adminId: %s",
    	        type, message,roleId, adminId));
        alarmService.createAlarm(type, message, roleId, adminId);
    }
}
