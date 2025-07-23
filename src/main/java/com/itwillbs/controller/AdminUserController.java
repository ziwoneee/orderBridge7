package com.itwillbs.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itwillbs.domain.AdminUserVO;
import com.itwillbs.service.AdminUserService;

@Controller
public class AdminUserController {
	
	// AdminMapper는 MyBatis에서 구현체가 자동으로 생성되는 인터페이스임
	// @Autowired를 사용하면 스프링이 해당 구현체를 자동으로 주입해줌
	// 직접 new AdminMapper() 하지 않아도 사용 가능
	@Autowired
	private AdminUserService adminUserService;


	// http://localhost:8088/admin/login
    // 로그인 폼 이동 (GET)
	// - 쿠키에 저장된 아이디가 있으면 미리 채워줌
	@GetMapping("/admin/login")
	public String showLoginForm(HttpServletRequest request,
								HttpSession session,
								org.springframework.ui.Model model) {
	    String rememberedId = "";
	    Cookie[] cookies = request.getCookies();

	    if (cookies != null) {
	        for (Cookie c : cookies) {
	            if ("rememberAdminId".equals(c.getName())) {
	                rememberedId = c.getValue();
	                break;
	            }
	        }
	    }

	    model.addAttribute("rememberedId", rememberedId); // JSP에서 ${rememberedId}로 사용

	    return "admin/login";
	}

	
	
    // 로그인 처리(POST)
	// - ID/PW 검증 및 계정 잠금 여부 확인
	// - 로그인 성공 시 세션 유지 및 아이디 저장 쿠키 처리
    @PostMapping("/admin/login")
    public String login(AdminUserVO vo,
    					HttpSession session, 
    					HttpServletRequest request,
    					HttpServletResponse response,
    					RedirectAttributes rttr) {
    	
    	// 입력한 ID에 해당하는 관리자 정보 조회
        AdminUserVO dbVO = adminUserService.findByAdminId(vo.getAdminId());

        // 존재하지 않는 계정
        if (dbVO == null) {
            rttr.addFlashAttribute("errorMsg", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "redirect:/admin/login";
        }

        // 잠긴 계정일 경우
        if ("LOCKED".equals(dbVO.getStatus())) {
            rttr.addFlashAttribute("errorMsg", "계정이 잠겨 있습니다. 관리자에게 문의하세요.");
            return "redirect:/admin/login";
        }

        // 비밀번호 일치 여부 확인
        AdminUserVO loginVO = adminUserService.login(vo); // 내부적으로 bcrypt 비교 포함

        if (loginVO != null) {
        	
        	 System.out.println(" 로그인 성공 → 세션 저장: " + loginVO.getAdminId() + " / 이름: " + loginVO.getName());

        	
        	// 로그인 성공: 세션 저장 + 자동 로그아웃 타이머 설정
            session.setAttribute("loginAdmin", loginVO);
            session.setMaxInactiveInterval(30 * 60); // 30분 동안 미사용 시 세션 만료
           
            //  아이디 저장 체크 여부 확인
            String remember = request.getParameter("remember");

            if ("on".equals(remember)) {
            	// 쿠키 생성 (아이디 저장)
                Cookie cookie = new Cookie("rememberAdminId", loginVO.getAdminId());
                cookie.setMaxAge(60 * 60 * 24 * 7); // 7일간 유지 
                cookie.setPath("/");
                response.addCookie(cookie);
            } else {
            	// 기존 쿠키 삭제 
                Cookie cookie = new Cookie("rememberAdminId", null);
                cookie.setMaxAge(0); // 즉시 삭제
                cookie.setPath("/");
                response.addCookie(cookie);
            }
            
            return "redirect:/admin/dashboard";
        } else {
        	// 비밀번호 불일치(ID는 맞음) 
            rttr.addFlashAttribute("errorMsg", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "redirect:/admin/login";
        }
    }
    
    
    
    // 로그아웃 처리 (GET)  
    // - 세션 초기화 및 메시지 전달
    @GetMapping("/admin/logout")
    public String logout(HttpSession session, RedirectAttributes rttr) {
        // 세션 초기화(제거)
        session.invalidate();

        rttr.addFlashAttribute("msg", "로그아웃 되었습니다.");

        return "redirect:/admin/login";
    }
 
}