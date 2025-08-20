package com.itwillbs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.itwillbs.service.DashboardService;

@Controller
public class DashboardController {

  @Autowired private DashboardService dashboardService;

  @GetMapping("/admin/home")
  public String home(Model model) {
    model.addAttribute("dash", dashboardService.getDashboard());
    return "main/dashboard";
  }
}
