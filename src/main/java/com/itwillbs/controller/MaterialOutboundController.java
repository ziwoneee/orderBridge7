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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.WorkOrderVO;
import com.itwillbs.dto.MaterialOutboundDetailDTO;
import com.itwillbs.dto.MaterialOutboundSummaryDTO;
import com.itwillbs.dto.WorkOrderDTO;
import com.itwillbs.service.MaterialOutboundService;
import com.itwillbs.service.WorkOrderService;

/*
 *  자재 출고 관리 컨트롤러
 *  - 출고 목록 조회 및 처리 관련 요청 처리
 */
@Controller
@RequestMapping("/material/outbound/*")
public class MaterialOutboundController {
	
	@Inject
	private MaterialOutboundService moService;
	
	@Inject
	private WorkOrderService workOrderService;
	
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
	
	
	/**
     * 1. [작업지시서 목록 불러오기 - Modal에서 호출되는 Ajax]
     * - status = 'WAITING' 인 작업지시서만 조회
     */
	@GetMapping("order-list")
	@ResponseBody
	public ResponseEntity<List<WorkOrderVO>> getWaitingOrders() {
	    try {
	        // status = 'WAITING' 인 작업지시서 목록 조회
	        List<WorkOrderVO> list = moService.getWaitingOrders();
	        return ResponseEntity.ok(list);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}
	
	 /**
     * 2. [선택된 작업지시서에 따른 자재 목록 조회]
     * - 작업지시서 번호를 기반으로 필요한 자재 목록 + 지시서 정보 반환
     * - 출고 등록 화면에 사용
     */
	@GetMapping("/load-order-details")
	@ResponseBody
	public ResponseEntity<MaterialOutboundDetailDTO> getOrderDetails(@RequestParam("workOrderNo") String workOrderNo) {
	    try {
	        MaterialOutboundDetailDTO detail = moService.getOutboundDetailByWorkOrder(workOrderNo);
	        return ResponseEntity.ok(detail);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}

	/**
	 * 2. 출고 등록 페이지 이동
	 * - 작업지시서(WAITING) 목록 조회 후 register.jsp 이동
	 */
	@GetMapping("/register")
	public String showRegisterPage(Model model) {
	    // 1. 대기 상태 작업지시 목록 조회
	    List<WorkOrderDTO> waitingList = workOrderService.getWaitingWorkOrders();

	    // 2. JSP로 전달
	    model.addAttribute("waitingList", waitingList);

	    return "/material/out/register"; // JSP 경로
	}

	
	/**
	 * 3. 출고 등록 처리
	 * - 작업지시서 기반 출고 등록
	 * - 출고 마스터 + 출고 자재 항목 저장
	 */
	@PostMapping("/register")
	@ResponseBody
	public ResponseEntity<String> registerOutbound(@RequestBody MaterialOutboundDetailDTO dto) {
	    try {
	        moService.registerOutbound(dto);
	        return ResponseEntity.ok("출고등록 완료");
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body("출고등록 실패: " + e.getMessage());
	    }
	}

	
	
	
	
} // MaterialOutboundController 끝
