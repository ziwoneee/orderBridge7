package com.itwillbs.controller;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.itwillbs.domain.MaterialInventoryVO;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.service.MaterialInventoryService;

@Controller
@RequestMapping("/material/inventory/*") // 자재관리 > 재고현황
public class MaterialInventoryController {
	
	// mylog
	private static final Logger logger = LoggerFactory.getLogger(MaterialInventoryController.class);

	
	// 서비스 객체 주입
	@Inject
	private MaterialInventoryService miService;
	
	
	/**
	 * 자재 재고 요약 목록 페이지 요청 처리
	 * - 자재별 1행으로 요약 표시
	 * - 검색 + 페이징 포함
	 * @throws Exception 
	 */
	@GetMapping("/summary")
	public String inventorySummaryList(SearchCriteria cri, Model model) throws Exception {
	    logger.info(" inventorySummaryList() 호출 ");
	    logger.info("검색 조건: {}", cri);

	    // 1. 요약 목록 조회 (자재 ID 기준 1행 요약)
	    List<MaterialInventoryVO> summaryList = miService.getInventorySummaryList(cri);

	    // 2. 전체 건수 조회 (페이징용)
	    int totalCount = miService.getInventoryCount(cri); // 기존 사용

	    // 3. PageMaker 생성
	    PageMaker pageMaker = new PageMaker(cri, totalCount);

	    // 4. 모델에 담기
	    model.addAttribute("summaryList", summaryList);   // 요약 목록
	    model.addAttribute("pageMaker", pageMaker);       // 페이징 정보
	    model.addAttribute("cri", cri);                   // 검색 조건 유지
	    model.addAttribute("menu", "material");           // 메뉴 활성화용
	    model.addAttribute("now", new Date());            // 현재 시간 (선택)

	    // 5. 뷰 리턴
	    return "material/inventory/summary"; // → JSP 파일명
	}


	
	
	
	
	
	
	
	
	
} // MaterialInventoryController 끝

