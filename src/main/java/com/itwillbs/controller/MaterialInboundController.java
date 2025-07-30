package com.itwillbs.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialInboundSummaryDTO;
import com.itwillbs.dto.UnreceivedOrderDTO;
import com.itwillbs.service.MaterialInboundService;

/**
 * 자재 입고 - 미입고 발주 목록 조회 컨트롤러
 */
@Controller
@RequestMapping("/material/inbound")
public class MaterialInboundController {
	
	// mylog
	private static final Logger logger = LoggerFactory.getLogger(MaterialInboundController.class);
	
	@Inject
	private MaterialInboundService miService;
	
	
	// 입고 목록 조회
	@GetMapping("/list")
	public String listInbound(SearchCriteria cri, Model model) throws Exception {
		

	    // 전체 개수
	    int totalCount = miService.getInboundListCount(cri);
	    
	    // cri에도 세팅 (jsp에서 사용 가능하게)
	    cri.setTotalCount(totalCount);

	    // 페이지 정보 생성
	    PageMaker pageMaker = new PageMaker(cri, totalCount);
	    
	    // 목록 조회
	    List<MaterialInboundSummaryDTO> list = miService.getInboundList(cri);

	    // View 전달
	    model.addAttribute("inboundList", list);
	    model.addAttribute("pageMaker", pageMaker);
	    model.addAttribute("cri", cri);
	    
		// 메뉴 하이라이트용
	    model.addAttribute("menu", "material"); 

	    return "material/inbound/list";
	}

	

	
	/**
	 * 미입고 발주 목록 - 페이징 JSON 응답
	 * - 요청 예시: /material/inbound/unreceived-orders?page=1&perPageNum=10
	 */
	@GetMapping("/unreceived-orders")
	@ResponseBody
	public ResponseEntity<?> getUnreceivedOrders(SearchCriteria cri) {
	    try {
	        int totalCount = miService.getUnreceivedOrdersCount();
	        cri.setTotalCount(totalCount);

	        PageMaker pageMaker = new PageMaker(cri, totalCount);
	        List<UnreceivedOrderDTO> list = miService.getUnreceivedOrdersPaging(cri);

	        Map<String, Object> result = new HashMap<>();
	        result.put("list", list);
	        result.put("pageMaker", pageMaker);
	        result.put("cri", cri);

	        return ResponseEntity.ok().body(result);

	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body("미입고 발주 목록 불러오기 실패");
	    }
	}


	/**
	 * [POST] 미입고 발주건을 입고관리 테이블(material_inbound + material_inbound_item)에 저장
	 * - 조건: 아직 입고되지 않은 발주항목(order_item_id 기준)
	 * - 수행: 발주번호(order_id)별로 inbound_id를 생성하여 insert
	 */
	@PostMapping("/insert-unreceived")
	@ResponseBody
	public ResponseEntity<String> insertUnreceivedOrders(@RequestParam(required = false) String[] orderIds) {
	    try {
	        if (orderIds == null || orderIds.length == 0) {
	            // 파라미터가 없으면 전체 미입고건 처리 (기존 방식)
	            miService.insertUnreceivedOrders();
	            return ResponseEntity.ok("전체 미입고건 DB 저장 성공");
	        } else {
	            // 선택된 발주 ID만 처리 (새로운 방식)
	            miService.insertSelectedUnreceivedOrders(orderIds);
	            return ResponseEntity.ok("선택된 발주건 DB 저장 성공");
	        }
	    } catch (Exception e) {
	        logger.error("미입고건 DB 저장 실패", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                           .body("DB 저장 실패: " + e.getMessage());
	    }
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
} // MaterialInboundConroller 끝
