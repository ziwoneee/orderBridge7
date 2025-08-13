package com.itwillbs.controller;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itwillbs.domain.ApprovalTokenVO;
import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.service.ApprovalTokenService;
import com.itwillbs.service.MaterialOrderService;

@Controller
@RequestMapping("/approval")
public class ApprovalController {

    @Autowired
    private ApprovalTokenService tokenService;

    @Autowired
    private MaterialOrderService orderService;

    // ✅ 승인 링크 클릭 시 호출
    @GetMapping("/confirm")
    public String confirmApproval(@RequestParam("token") String tokenId, Model model) {
        ApprovalTokenVO token = tokenService.findByTokenId(tokenId);

        // 토큰이 없거나 만료되었거나 이미 사용된 경우
        if (token == null || token.isUsed() || token.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            model.addAttribute("status", "expired");
            return "approval/confirm";
        }

        MaterialOrderVO order = orderService.findByOrderId(token.getOrderId());

        model.addAttribute("tokenId", tokenId);
        model.addAttribute("order", order);
        // 상태 없음 → 승인/거절 화면 출력
        return "approval/confirm";
    }

    // ✅ 승인 처리
    @PostMapping("/approve")
    public String approve(@RequestParam("tokenId") String tokenId, Model model) {
        boolean result = tokenService.approve(tokenId);
        model.addAttribute("status", result ? "success" : "expired");
        return "approval/confirm";
    }

    // ✅ 거절 처리
    @PostMapping("/reject")
    public String reject(@RequestParam("tokenId") String tokenId, Model model) {
        boolean result = tokenService.reject(tokenId);
        model.addAttribute("status", result ? "rejected" : "expired");
        return "approval/confirm";
    }
}
