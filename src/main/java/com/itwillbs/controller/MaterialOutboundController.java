package com.itwillbs.controller;

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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itwillbs.domain.MaterialOutboundVO;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.WorkOrderVO;
import com.itwillbs.dto.MaterialOutboundDetailDTO;
import com.itwillbs.dto.MaterialOutboundSummaryDTO;
import com.itwillbs.dto.WorkOrderDTO;
import com.itwillbs.service.MaterialOutboundService;
import com.itwillbs.service.WorkOrderService;

/**
 * [출고 관리 컨트롤러]
 * - 출고 목록 조회
 * - 작업지시 목록(대기 상태) 조회
 * - 작업지시 선택 시 자재+재고 조회
 * - 출고 등록 (헤더 + LOT별 항목)
 * - 출고 처리 (재고 차감 + 상태 변경)
 */
@Controller
@RequestMapping("/material/outbound")
public class MaterialOutboundController {
	
	@Inject
	private MaterialOutboundService moService;
	
	// mylog
	private static final Logger logger = LoggerFactory.getLogger(MaterialOutboundController.class);
	
	// 목록(페이징)
    @RequestMapping(value="/list", method=RequestMethod.GET)
    public String list(SearchCriteria cri, Model model) throws Exception {
    	
        model.addAttribute("list", moService.getOutboundList(cri));
        model.addAttribute("totalCount", moService.getOutboundCount(cri));
        model.addAttribute("pendingCount", moService.getOutboundCountByStatus("DRAFT"));
        model.addAttribute("completedCount", moService.getOutboundCountByStatus("ISSUED"));

        model.addAttribute("waitingOrders", moService.getWaitingOrders());
        
        return "material/out/list"; // /WEB-INF/views/material/out/list.jsp
    }

    // [AJAX] 대기 작업지시 목록
    @ResponseBody
    @RequestMapping(value="/order-list", method=RequestMethod.GET)
    public List<WorkOrderVO> orderList() throws Exception {
    	
    	List<WorkOrderVO> list = moService.getWaitingOrders();
    	logger.info("WAITING orders count: {}", list.size());
    	
        return moService.getWaitingOrders();
    }

    // 등록 화면 진입(작업지시 선택 후)
    @RequestMapping(value="/register", method=RequestMethod.GET)
    public String registerPage(@RequestParam("workOrderId") String workOrderId, Model model) throws Exception {
        model.addAttribute("wo", moService.getWorkOrderWithStockMap(workOrderId));
        return "material/out/register";
    }

    // 등록 저장(VO 한 방에 받기: List로 바인딩)
    @RequestMapping(value="/register", method=RequestMethod.POST)
    public String register(MaterialOutboundVO vo) throws Exception {
    	logger.info("register workOrderNo={}", vo.getWorkOrderNo()); // null이면 바인딩 문제
    	
    	vo.setStatus("DRAFT");	// 미출고
        moService.registerOutbound(vo);
        
        return "redirect:/material/out/list";
    }

    // [AJAX] 출고 상세
    @ResponseBody
    @RequestMapping(value="/detail", method=RequestMethod.GET)
    public Map<String,Object> detail(@RequestParam("outboundId") String outboundId) throws Exception {
        return moService.getOutboundDetailMap(outboundId);
    }

    // [AJAX] 출고 처리
    @ResponseBody
    @RequestMapping(value="/process", method=RequestMethod.POST)
    public String process(@RequestParam("outboundId") String outboundId) throws Exception {
        moService.processOutbound(outboundId);
        return "OK";
    }
    
    // [AJAX] 특정 작업지시의 자재 + 현재고 집계 (register.jsp가 호출)
    @ResponseBody
    @RequestMapping(value="/work-order", method=RequestMethod.GET)
    public Map<String, Object> workOrder(@RequestParam("workOrderId") String workOrderId) throws Exception {
        return moService.getWorkOrderWithStockMap(workOrderId);
    }
	
	
} // MaterialOutboundController 끝
