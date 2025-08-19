package com.itwillbs.controller;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.AiPredictionLogDTO;
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
                       SearchCriteria cri,
                       Model model){
        
        // 기본값
        if (cri.getPage() <= 0) cri.setPage(1);

        // 페이지당 개수: 허용값만 반영 (10/25/50/100/200), 기본 50
        int[] allowed = {10,25,50,100,200};
        int def = 50;
        int in = cri.getPerPageNum() > 0 ? cri.getPerPageNum() : def;
        int perPage = java.util.Arrays.stream(allowed).anyMatch(x -> x == in) ? in : def;
        perPage = Math.min(perPage, 500); 
        cri.setPerPageNum(perPage);

        // 전체 개수
        int totalCount = logService.getLogCount(q, from, to);
        PageMaker pageMaker = new PageMaker(cri, totalCount);

        // ✅ 페이징 목록 조회
        List<AiPredictionLogDTO> logs = logService.searchWithPaging(q, from, to, cri);

        // Model에 담기
        model.addAttribute("logs", logs);
        model.addAttribute("pageMaker", pageMaker);
        model.addAttribute("cri", cri);
        model.addAttribute("q", q);
        model.addAttribute("from", from);
        model.addAttribute("to", to);

        return "ai/pred_log_list";
    }


}
