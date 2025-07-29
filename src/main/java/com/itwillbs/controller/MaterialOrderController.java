package com.itwillbs.controller;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.service.MaterialOrderService;

/**
 * 자재 발주 관리 - 발주 목록 조회 컨트롤러
 */
@Controller
@RequestMapping("/material/order")
public class MaterialOrderController {
	
	// mylog
	private static final Logger logger = LoggerFactory.getLogger(MaterialOrderController.class);

	// 서비스 객체 주입
	@Inject
	private MaterialOrderService mOrderService;
	
	
	// 발주 목록 조회 (검색, 정렬, 페이징 포함)
	@GetMapping("/list")
	public String orderList(SearchCriteria cri, Model model) {
		
		// 1. 정렬 컬럼이 없으면 기본값 세팅 (안 해주면 ORDER BY desc 에러 발생)
	    if (cri.getSortColumn() == null || cri.getSortColumn().isBlank()) {
	        cri.setSortColumn("order_date"); // 기본 정렬 컬럼: 발주일
	    }
	    
		// 2. 조건에 맞는 발주 목록 조회
        List<MaterialOrderVO> orderList = mOrderService.getOrderList(cri);

        // 3. 전체 건수 조회 (페이징용)
        int totalCount = mOrderService.getTotalCount(cri);
        PageMaker pageMaker = new PageMaker(cri, totalCount);

        // 4. "발주등록" 상태 건수 (탭용)
        int registeredCount = mOrderService.getRegisteredCount(cri);

        // 5. 모델 등록
        model.addAttribute("orderList", orderList);
        model.addAttribute("pageMaker", pageMaker);
        model.addAttribute("cri", cri);
        model.addAttribute("registeredCount", registeredCount);
	    model.addAttribute("menu", "material");
	    
	    // JSP 뷰 경로 반환
	    return "material/order/list";
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
} // MaterialOrderController 끝
