package com.itwillbs.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.itwillbs.service.MrpPlanningService;


/**
 * MRP 페이지 전용 컨트롤러
 * - JSP 반환
 * - 데이터는 MrpApiController의 /data 엔드포인트로 AJAX 호출
 */
@Controller
@RequestMapping("/mrp")
public class MrpPageController {
	
	/** 총소요 페이지: http://localhost:8088/mrp/gross */
    @GetMapping("/gross")
    public String grossPage() {
        return "mrp/mrpGross"; // /WEB-INF/views/mrp/mrpGross.jsp
    }

    /** (선택) 가용/순소요 페이지 http://localhost:8088/mrp/nettingPage */
    @GetMapping("/nettingPage")
    public String nettingPage() {
        return "mrp/mrpNetting";
    }

    /** (선택) 부족분 페이지 http://localhost:8088/mrp/shortagePage */
    @GetMapping("/shortagePage")
    public String shortagePage() {
        return "mrp/mrpShortage";
    }

    /** (선택) 발주추천 페이지 http://localhost:8088/mrp/recommendPoPage */
    @GetMapping("/recommendPoPage")
    public String recommendPoPage() {
        return "mrp/mrpRecommendPo";
    }

} // MrpPageController 끝
