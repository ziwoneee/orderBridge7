package com.itwillbs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.itwillbs.service.DashboardService;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {

        // 오늘의 생산 계획 수
        model.addAttribute("todayPlanCount", dashboardService.getTodayPlanCount());

        // 자재 부족 건수
        model.addAttribute("shortageCount", dashboardService.getShortageMaterialCount());

        // 납기 지연 건수
        model.addAttribute("delayedCount", dashboardService.getDelayedDeliveryCount());

        // 월별 라벨 (차트용)
        model.addAttribute("monthLabels", dashboardService.getMonthLabels());

        // 월별 생산 계획 수 (차트용)
        model.addAttribute("monthlyPlanCounts", dashboardService.getMonthlyPlanCounts());

        return "admin/dashboard";
    }
}
