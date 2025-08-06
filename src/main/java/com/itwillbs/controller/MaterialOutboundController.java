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
import org.springframework.web.bind.annotation.PostMapping;
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
	
	@Inject
	private MaterialOutboundService moService;
	
	// mylog
	private static final Logger logger = LoggerFactory.getLogger(MaterialOutboundController.class);
	
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
		
		// 상태별 개수 조회 (탭 개수 표시용)
	    SearchCriteria pendingCri = new SearchCriteria();
	    pendingCri.setStatus("미출고");
	    int pendingCount = moService.getMaterialOutboundCount(pendingCri);
	    
	    SearchCriteria completedCri = new SearchCriteria();
	    completedCri.setStatus("출고완료");
	    int completedCount = moService.getMaterialOutboundCount(completedCri);
	    
	    // 전체 개수 (검색 조건 없이)
	    SearchCriteria allCri = new SearchCriteria();
	    int allCount = moService.getMaterialOutboundCount(allCri);
		
		
		// 1. 서비스 호출하여 출고 목록 가져오기
		List<MaterialOutboundSummaryDTO> outboundList = moService.getOutboundList(cri);

		// 2. 모델에 데이터 저장
		model.addAttribute("outList", outboundList);
		model.addAttribute("pageMaker", pageMaker);
		model.addAttribute("cri", cri); // 검색 조건 유지용
		
		 // 탭별 개수 추가
	    model.addAttribute("totalCount", allCount);      // 전체 개수
	    model.addAttribute("pendingCount", pendingCount);    // 미출고 개수
	    model.addAttribute("completedCount", completedCount); // 출고완료 개수
		
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


	// 출고처리 요청 (실재고 확인 포함)
	@GetMapping("process")
	public String processOutbound(@RequestParam("outboundId") String outboundId, Model model) {
	    logger.info("출고 처리 요청 - ID: " + outboundId);

	    try {
	        // 출고 처리 서비스 호출 (성공 시 true)
	        boolean result = moService.processOutbound(outboundId);

	        if (result) {
	            model.addAttribute("msg", "출고처리 완료되었습니다.");
	        } else {
	            model.addAttribute("msg", "실재고가 부족하여 출고처리 불가능합니다.");
	        }
	    } catch (Exception e) {
	        logger.error("출고 처리 중 오류", e);
	        model.addAttribute("msg", "출고 처리 중 오류가 발생했습니다.");
	    }

	    // 목록으로 리다이렉트
	    return "redirect:/material/outbound/list";
	}

	

	@PostMapping(value = "process", produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public ResponseEntity<String> processOutboundAjax(@RequestParam("outboundId") String outboundId) {
	    logger.info("Ajax 출고처리 요청 - ID: " + outboundId);

	    try {
	        boolean result = moService.processOutbound(outboundId);

	        if (result) {
	            return ResponseEntity.ok("출고처리 완료되었습니다.");
	        } else {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("실재고가 부족하여 출고처리 불가능합니다.");
	        }
	    } catch (Exception e) {
	        logger.error("Ajax 출고 처리 중 오류", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("출고 처리 중 오류가 발생했습니다.");
	    }
	}

	
	
	
	
} // MaterialOutboundController 끝
