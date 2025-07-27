package com.itwillbs.controller;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialOutboundDetailDTO;
import com.itwillbs.dto.MaterialOutboundSummaryDTO;
import com.itwillbs.service.MaterialOutboundService;

/*
 *  자재 출고 관리 컨트롤러
 *  - 출고 목록 조회 및 처리 관련 요청 처리
 */
@Controller
@RequestMapping("/material/outbound/*")
public class MaterialOutboundController {
	
	// mylog
	private static final Logger logger = LoggerFactory.getLogger(MaterialOutboundController.class);

	
	@Inject
	private MaterialOutboundService moService;
	
	//http://localhost:8088/material/outbound/list
	// 출고 목록 조회 요청
	@GetMapping("list")
	public String getOutboundList(SearchCriteria cri, Model model) throws Exception {
		logger.info(" getOutboundList 호출 ");
		
		
		// 전체 건수 조회
		int totalCount = moService.getMaterialOutboundCount(cri);

		// totalCount 세팅 (SearchCriteria 내부 사용 시 필요)
		cri.setTotalCount(totalCount);

		// PageMaker 생성
		PageMaker pageMaker = new PageMaker(cri, totalCount);
		
		// 1. 서비스 호출하여 출고 목록 가져오기
		List<MaterialOutboundSummaryDTO> outboundList = moService.getOutboundList(cri);

		// 2. 모델에 데이터 저장
		model.addAttribute("outList", outboundList);
		model.addAttribute("pageMaker", pageMaker);
		model.addAttribute("cri", cri); // 검색 조건 유지용
		
		model.addAttribute("menu", "material");
		
		return "/material/out/list"; // JSP로 이동
	}
	
	
	@GetMapping("detail")
	@ResponseBody
	public ResponseEntity<MaterialOutboundDetailDTO> getOutboundDetail(@RequestParam("outboundId") String outboundId) {
	    try {
	        MaterialOutboundDetailDTO detailDTO = moService.getOutboundDetail(outboundId);
	        return ResponseEntity.ok(detailDTO);
	    } catch (Exception e) {
	        logger.error("출고 상세 조회 실패", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}


	
	
	
	
	
	
	
	
	
	
	
	
} // MaterialOutboundController 끝
