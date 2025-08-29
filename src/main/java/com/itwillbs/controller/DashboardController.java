package com.itwillbs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import com.itwillbs.dto.DashboardDTO;
import com.itwillbs.service.DashboardService;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // 뷰만 반환 (요구사항 준수)
    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    // 대시보드 데이터 JSON (JSP에서 fetch로 호출)
    @GetMapping("/admin/dashboard/data")
    @ResponseBody
    public DashboardDTO dashboardData() {
        return dashboardService.getDashboardData();
    }
}
