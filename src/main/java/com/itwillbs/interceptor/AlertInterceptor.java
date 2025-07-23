package com.itwillbs.interceptor;

import com.itwillbs.domain.AlarmLogVO;
import com.itwillbs.service.AlarmService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

public class AlertInterceptor implements HandlerInterceptor {

    private AlarmService alarmService;

    public void setAlarmService(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        return true; // ← 반드시 true
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loginAdminId") != null && modelAndView != null) {
            String adminId = (String) session.getAttribute("loginAdminId");

            List<AlarmLogVO> alertList = alarmService.getUnreadAlarmsByAdmin(adminId);

            modelAndView.addObject("alertList", alertList);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        // 필요 없으면 생략 가능
    }
}
