package com.itwillbs.controller;

import java.util.Date;

import javax.inject.Inject;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.itwillbs.service.AiPredictionLogService;

@Controller
@RequestMapping("/ai/pred-logs")
public class AiPredictionLogController {
	
	@Inject
	private AiPredictionLogService logService;

    @GetMapping
    public String list(@RequestParam(required=false) String q,
                       @RequestParam(required=false) @DateTimeFormat(pattern="yyyy-MM-dd") Date from,
                       @RequestParam(required=false) @DateTimeFormat(pattern="yyyy-MM-dd") Date to,
                       Model model){
      model.addAttribute("logs", logService.search(q, from, to, 200));
      model.addAttribute("q", q);
      model.addAttribute("from", from);
      model.addAttribute("to", to);
    return "ai/pred_log_list";
  }

}
