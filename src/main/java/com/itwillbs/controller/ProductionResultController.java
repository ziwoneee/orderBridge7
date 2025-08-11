package com.itwillbs.controller;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.ProductionResultVO;
import com.itwillbs.service.ProductionResultService;

/**
 * 생산 실적 컨트롤러
 * - /production/result 하위 URL 관리
 * - 목록 + 등록(완제품 자동입고 연동)
 */
@Controller
@RequestMapping("/production/result")
public class ProductionResultController {

    @Inject
    private ProductionResultService productionResultService;

    // ======================= 목록 =========================
    /**
     * 생산 실적 목록
     * - 검색/정렬/페이징은 SearchCriteria로 자동 바인딩
     * - Service에서 리스트/총건수 가져와 PageMaker 생성 후 JSP로 전달
     */
    @GetMapping("/list")
    public String list(@ModelAttribute SearchCriteria cri, Model model) {
        // 기본 정렬(프론트가 안 주면 최신 등록일 내림차순)
        if (cri.getSortColumn() == null || cri.getSortColumn().isEmpty()) {
            cri.setSortColumn("created_at"); // DB 컬럼명(snake_case)
            cri.setSortOrder("desc");
        }

        // 목록 + 총건수
        var list = productionResultService.getList(cri);
        int total = productionResultService.getTotalCount(cri);

        // PageMaker 생성
        PageMaker pageMaker = new PageMaker(cri, total);

        // 뷰로 전달
        model.addAttribute("list", list);
        model.addAttribute("pageMaker", pageMaker);
        model.addAttribute("cri", cri);

        // /WEB-INF/views/production/result/list.jsp
        return "production/result/list";
    }

    // =================완제품 입고자동등록 (아름)=========================
    /**
     * 생산 결과 등록
     * - 저장 시 Service에서 자동으로 입고도 처리
     * - 등록 후 목록으로 이동
     */
    @PostMapping("/register")
    public String registerProductionResult(ProductionResultVO vo) {
        productionResultService.insertResult(vo);
        return "redirect:/production/result/list";
    }
}

	
	
	

