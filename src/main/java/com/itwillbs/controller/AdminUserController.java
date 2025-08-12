package com.itwillbs.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
    
 // ==================== 설정 페이지 관련 메서드들 ====================
    
    /**
     * 설정 페이지 (관리자 계정 관리)
     * - 최고관리자: 전체 관리자 목록 조회 및 관리
     * - 일반관리자: 본인 정보 수정만 가능
     */
    @GetMapping("/admin/settings/accounts")
    public String accounts(HttpSession session, Model model,
                          @RequestParam(required = false) String search,
                          @RequestParam(required = false) String role,
                          @RequestParam(required = false) String status) {
        
        // 로그인 체크
        AdminUserVO loginAdmin = (AdminUserVO) session.getAttribute("loginAdmin");
        if (loginAdmin == null) {
            return "redirect:/admin/login";
        }
        
        // 최고관리자인 경우에만 전체 목록 조회
        if ("SUPER".equals(loginAdmin.getRoleId())) {
            try {
                List<AdminUserVO> adminList = adminUserService.getAdminList(search, role, status);
                model.addAttribute("adminList", adminList);
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("adminList", List.of()); // 빈 리스트
            }
        }
        
        return "admin/settings/accounts";
    }
    
    /**
     * 관리자 추가 (최고관리자만)
     */
    @PostMapping("/admin/settings/accounts")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addAdmin(@RequestBody AdminUserVO adminVO, 
                                                       HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 권한 체크
            AdminUserVO loginAdmin = (AdminUserVO) session.getAttribute("loginAdmin");
            if (loginAdmin == null || !"SUPER".equals(loginAdmin.getRoleId())) {
                result.put("success", false);
                result.put("message", "권한이 없습니다.");
                return ResponseEntity.ok(result);
            }
            
            // 중복 체크
            AdminUserVO existingAdmin = adminUserService.findByAdminId(adminVO.getAdminId());
            if (existingAdmin != null) {
                result.put("success", false);
                result.put("message", "이미 존재하는 사번입니다.");
                return ResponseEntity.ok(result);
            }
            
            // 관리자 등록
            adminUserService.insertAdmin(adminVO);
            result.put("success", true);
            result.put("message", "관리자가 성공적으로 등록되었습니다.");
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "등록 중 오류가 발생했습니다.");
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 관리자 상세 조회 (최고관리자만)
     */
    @GetMapping("/admin/settings/accounts/{adminId}")
    @ResponseBody
    public ResponseEntity<AdminUserVO> getAdmin(@PathVariable String adminId, 
                                               HttpSession session) {
        try {
            // 권한 체크
            AdminUserVO loginAdmin = (AdminUserVO) session.getAttribute("loginAdmin");
            if (loginAdmin == null || !"SUPER".equals(loginAdmin.getRoleId())) {
                return ResponseEntity.status(403).build();
            }
            
            AdminUserVO admin = adminUserService.findByAdminId(adminId);
            if (admin == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(admin);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * 관리자 정보 수정 (최고관리자만)
     */
    @PutMapping("/admin/settings/accounts/{adminId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateAdmin(@PathVariable String adminId,
                                                          @RequestBody AdminUserVO adminVO,
                                                          HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 권한 체크
            AdminUserVO loginAdmin = (AdminUserVO) session.getAttribute("loginAdmin");
            if (loginAdmin == null || !"SUPER".equals(loginAdmin.getRoleId())) {
                result.put("success", false);
                result.put("message", "권한이 없습니다.");
                return ResponseEntity.ok(result);
            }
            
            adminVO.setAdminId(adminId); // URL의 adminId 사용
            adminUserService.updateAdmin(adminVO);
            result.put("success", true);
            result.put("message", "관리자 정보가 성공적으로 수정되었습니다.");
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "수정 중 오류가 발생했습니다.");
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 관리자 삭제 (최고관리자만)
     */
    @DeleteMapping("/admin/settings/accounts/{adminId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAdmin(@PathVariable String adminId,
                                                          HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 권한 체크
            AdminUserVO loginAdmin = (AdminUserVO) session.getAttribute("loginAdmin");
            if (loginAdmin == null || !"SUPER".equals(loginAdmin.getRoleId())) {
                result.put("success", false);
                result.put("message", "권한이 없습니다.");
                return ResponseEntity.ok(result);
            }
            
            // 본인 계정 삭제 방지
            if (adminId.equals(loginAdmin.getAdminId())) {
                result.put("success", false);
                result.put("message", "본인 계정은 삭제할 수 없습니다.");
                return ResponseEntity.ok(result);
            }
            
            adminUserService.deleteAdmin(adminId);
            result.put("success", true);
            result.put("message", "관리자가 성공적으로 삭제되었습니다.");
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "삭제 중 오류가 발생했습니다.");
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 내 정보 수정 (일반 관리자)
     */
    @PutMapping("/admin/settings/accounts/my-info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateMyInfo(@RequestBody AdminUserVO adminVO,
                                                           HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 로그인 체크
            AdminUserVO loginAdmin = (AdminUserVO) session.getAttribute("loginAdmin");
            if (loginAdmin == null) {
                result.put("success", false);
                result.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(result);
            }
            
            // 본인 계정만 수정 가능
            adminVO.setAdminId(loginAdmin.getAdminId());
            adminUserService.updateMyInfo(adminVO);
            
            // 세션 정보 업데이트
            AdminUserVO updatedAdmin = adminUserService.findByAdminId(loginAdmin.getAdminId());
            session.setAttribute("loginAdmin", updatedAdmin);
            
            result.put("success", true);
            result.put("message", "정보가 성공적으로 수정되었습니다.");
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "수정 중 오류가 발생했습니다.");
        }
        
        return ResponseEntity.ok(result);
    }
 
}