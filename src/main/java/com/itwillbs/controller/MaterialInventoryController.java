package com.itwillbs.controller;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
     * 자재 재고현황 리스트 조회 (페이징 + 정렬 지원)
     * @param cri 검색 조건 및 페이징 정보
     * @param model View에 전달할 데이터 모델
     * @return JSP 페이지 경로
     */
	
	// http://localhost:8088/material/inventory/list
	// 자재 재고현황 리스트 조회
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String getInventoryList(SearchCriteria cri, Model model) throws Exception {
		
		logger.info(" getInventoryList() 호출 ");
		logger.info("검색 조건: {}", cri);
		
		// 전체 건수 조회
		int totalCount = miService.getInventoryCount(cri);
		
		// totalCount 세팅
		cri.setTotalCount(totalCount);
		
		// PageMaker 생성
		PageMaker pageMaker = new PageMaker(cri, totalCount);
		
		// 서비스 호출 → 자재 재고 목록 조회 (페이징 + 정렬 조건 포함)
		List<MaterialInventoryVO> inventoryList = miService.getInventoryList(cri);
        
		// 모델에 담기
		model.addAttribute("inventoryList", inventoryList);
		model.addAttribute("pageMaker", pageMaker);
		model.addAttribute("cri", cri); // 검색 조건 유지용
	    model.addAttribute("menu", "material");   
	    
		
		// 현재 시간 now 객체로 전달
	    model.addAttribute("now", new Date());
	    
		
		// 뷰페이지로 이동
		return "material/inventoryList";
	}

	
	
	
	
	
	
	
	
	
} // MaterialInventoryController 끝

