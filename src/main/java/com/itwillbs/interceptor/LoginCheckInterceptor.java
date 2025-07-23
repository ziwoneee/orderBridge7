package com.itwillbs.interceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.itwillbs.domain.AdminUserVO;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        
        HttpSession session = request.getSession();
        AdminUserVO loginUser = (AdminUserVO) session.getAttribute("loginAdmin");

        if (loginUser == null) {
            // AJAX 요청이면 401 응답으로 처리
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED); // 401
            } else {
                // 일반 요청은 로그인 페이지로 리다이렉트
                response.sendRedirect(request.getContextPath() + "/admin/login");
            }
            return false;
        }

        return true; // 로그인 되어 있으면 요청 계속 진행
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        // postHandle은 생략해도 되지만, 일단 빈 메서드로 구현
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) throws Exception {
        // afterCompletion도 생략 가능하지만 빈 메서드로 구현
    }
}
