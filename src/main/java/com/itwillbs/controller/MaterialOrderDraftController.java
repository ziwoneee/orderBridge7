package com.itwillbs.controller;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.itwillbs.domain.AdminUserVO;
import com.itwillbs.dto.PurchaseDraftRequest;
import com.itwillbs.dto.PurchaseDraftResult;
import com.itwillbs.service.MaterialOrderService;

@Controller
@RequestMapping("/material/order")
public class MaterialOrderDraftController {
    
    @Inject
    private MaterialOrderService materialOrderService;
    
    /**
     * 로그인 ID 찾기: MaterialOrderController와 동일한 방식 사용
     */
    private String resolveLoginAdminId(HttpSession session) {
        // 1. loginAdmin 키 확인 (실제 사용되는 키)
        Object la = session.getAttribute("loginAdmin");
        if (la instanceof AdminUserVO) {
            String adminId = ((AdminUserVO) la).getAdminId();
            System.out.println("loginAdmin에서 찾은 adminId: " + adminId);
            return adminId;
        }

        // 2. 기존 키들도 확인 (호환성)
        Object au = session.getAttribute("adminUser");
        if (au instanceof AdminUserVO) {
            String adminId = ((AdminUserVO) au).getAdminId();
            System.out.println("adminUser에서 찾은 adminId: " + adminId);
            return adminId;
        }

        Object lu = session.getAttribute("loginUser");
        if (lu instanceof AdminUserVO) {
            String adminId = ((AdminUserVO) lu).getAdminId();
            System.out.println("loginUser에서 찾은 adminId: " + adminId);
            return adminId;
        }

        // 3. Spring Security 확인
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                if (principal instanceof AdminUserVO) {
                    String adminId = ((AdminUserVO) principal).getAdminId();
                    System.out.println("Spring Security AdminUserVO에서 찾은 adminId: " + adminId);
                    return adminId;
                }
                if (principal instanceof UserDetails) {
                    String username = ((UserDetails) principal).getUsername();
                    System.out.println("Spring Security UserDetails에서 찾은 username: " + username);
                    return username;
                }
                if (principal instanceof String) {
                    String username = (String) principal;
                    System.out.println("Spring Security String에서 찾은 username: " + username);
                    return username;
                }
            }
        } catch (Throwable e) {
            System.out.println("Spring Security 확인 중 오류: " + e.getMessage());
        }
        
        System.out.println("세션에서 로그인 정보를 찾을 수 없습니다.");
        return null;
    }
    
    /**
     * 세션 디버깅용 메서드
     */
    private void logSessionContents(HttpSession session) {
        System.out.println("=== 세션 내용 디버깅 ===");
        java.util.Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            Object value = session.getAttribute(name);
            String valueType = (value != null) ? value.getClass().getSimpleName() : "null";
            System.out.println("세션 키: " + name + ", 값 타입: " + valueType + ", 값: " + value);
        }
        System.out.println("=== 세션 디버깅 끝 ===");
    }

    // 부족분으로 발주 초안 생성
    @PostMapping("/draft")
    @ResponseBody
    public ResponseEntity<?> createDraft(@RequestBody PurchaseDraftRequest req,
                                      HttpSession session) throws Exception {
        
        // 세션 내용 전체 디버깅
        logSessionContents(session);
        
        // 직접 세션에서 확인하기
        Object directLoginAdmin = session.getAttribute("loginAdmin");
        System.out.println("직접 session.getAttribute('loginAdmin'): " + directLoginAdmin);
        
        if (directLoginAdmin instanceof AdminUserVO) {
            AdminUserVO adminUser = (AdminUserVO) directLoginAdmin;
            
            System.out.println("AdminUserVO 객체 내용 - adminId: " + adminUser.getAdminId() + 
                             ", name: " + adminUser.getName() + ", roleId: " + adminUser.getRoleId());
        }
        
        // MaterialOrderController와 동일한 방식으로 로그인 ID 확보
        String adminId = resolveLoginAdminId(session);
        
        if (adminId == null || adminId.trim().isEmpty()) {
            System.out.println("로그인 세션을 찾을 수 없습니다.");
            
            // 강제로 한 번 더 직접 확인
            Object fallback = session.getAttribute("loginAdmin");
            if (fallback instanceof AdminUserVO) {
                adminId = ((AdminUserVO) fallback).getAdminId();
                System.out.println("강제 fallback으로 찾은 adminId: " + adminId);
            }
            
            if (adminId == null || adminId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of("success", false, "message", "로그인 세션이 만료되었습니다.")
                );
            }
        }
        
        System.out.println("최종 확정된 adminId: " + adminId);
        req.setRequestedBy(adminId);
        
        // DTO에 제대로 설정되었는지 재확인
        System.out.println("DTO에 설정된 requestedBy: " + req.getRequestedBy());
        
        int itemsSize = (req.getItems() == null) ? 0 : req.getItems().size();
        System.out.println("draft req workOrderId=" + req.getWorkOrderId() + ", items=" + itemsSize);
        
        if (req.getItems() != null) {
            req.getItems().forEach(i ->
                System.out.println("item materialId=" + i.getMaterialId() + ", lackQty=" + i.getLackQty())
            );
        }
        
        PurchaseDraftResult result = materialOrderService.createDraftFromShortages(req);
        
        return ResponseEntity.ok(result);
    }
}