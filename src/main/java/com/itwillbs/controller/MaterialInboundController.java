package com.itwillbs.controller;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialInboundSummaryDTO;
import com.itwillbs.service.MaterialInboundService;

@Controller
@RequestMapping("/material/inbound")
public class MaterialInboundController {
	
	// mylog
	private static final Logger logger = LoggerFactory.getLogger(MaterialInboundController.class);
	
	@Inject
	private MaterialInboundService inboundService;
	
	
	// 입고 목록 조회
	@GetMapping("/list")
	public String listInbound(SearchCriteria cri, Model model) throws Exception {
		

	    // 전체 개수
	    int totalCount = inboundService.getInboundListCount(cri);
	    
	    // cri에도 세팅 (jsp에서 사용 가능하게)
	    cri.setTotalCount(totalCount);

	    // 페이지 정보 생성
	    PageMaker pageMaker = new PageMaker(cri, totalCount);
	    
	    // 목록 조회
	    List<MaterialInboundSummaryDTO> list = inboundService.getInboundList(cri);

	    // View 전달
	    model.addAttribute("inboundList", list);
	    model.addAttribute("pageMaker", pageMaker);
	    model.addAttribute("cri", cri);
	    
		// 메뉴 하이라이트용
	    model.addAttribute("menu", "material"); 

	    return "material/inbound/list";
	}

	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
} // MaterialInboundConroller 끝
