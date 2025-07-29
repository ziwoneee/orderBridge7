package com.itwillbs.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.dto.WorkOrderDTO;
import com.itwillbs.service.WorkOrderService;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/workorder") 
@Slf4j
public class WorkOrderController {
	
	 private static final Logger logger = LoggerFactory.getLogger(WorkOrderController.class);

    
    @Autowired
    private WorkOrderService workOrderService;
    
    /**
     * 작업 지시 목록 
     * @param cri
     * @param model
     * @return
     */
    @GetMapping("/list")  
    public String list(SearchCriteria cri, Model model) {
        log.info("작업지시 목록 조회 - 조건: {}", cri);
        
        // 총 개수 및 페이징
        int totalCount = workOrderService.getWorkOrderTotalCount(cri);
        PageMaker pageMaker = new PageMaker(cri, totalCount);
        
        // 목록 조회
        List<WorkOrderDTO> workOrderList = workOrderService.getWorkOrderList(cri);
        
        // 탭용 상태별 개수
        int allCount = workOrderService.getAllCount();
        int waitingCount = workOrderService.getCountByStatus("WAITING");
        int inProgressCount = workOrderService.getCountByStatus("IN_PROGRESS");
        int completedCount = workOrderService.getCountByStatus("COMPLETED");
        
        // 모델 바인딩
        model.addAttribute("workOrders", workOrderList);      // 목록
        model.addAttribute("cri", cri);                       // 검색조건
        model.addAttribute("pageMaker", pageMaker);           // 페이징
        model.addAttribute("allCount", allCount);             // 전체 건수
        model.addAttribute("waitingCount", waitingCount);     // 대기 건수
        model.addAttribute("inProgressCount", inProgressCount); // 생산중 건수
        model.addAttribute("completedCount", completedCount); // 완료 건수
        
        return "workOrder/list";  // /WEB-INF/views/workOrder/list.jsp
    }
    
    /**
     * 작업 지시 상세 
     * @param orderId
     * @return
     */
    @GetMapping("/detail/{orderId}")
    @ResponseBody
    public WorkOrderDTO getDetail(@PathVariable("orderId") String orderId) {
        log.info("작업지시 상세 조회 - ID: {}", orderId);
        return workOrderService.getWorkOrderDetail(orderId);
    }

    /**
     * [GET] 확정 수주 목록 팝업 화면
     */
    @GetMapping("/select-order")
    public String selectOrderPopup(SearchCriteria cri, Model model) {
        logger.debug("▶ selectOrderPopup() 호출");

        // 전체 목록
        List<WorkOrderDTO> confirmedList = workOrderService.getConfirmedOrders(cri);
        int totalCount = workOrderService.getConfirmedOrdersCount(cri);

        // 모델로 전달
        model.addAttribute("orderList", confirmedList);
        model.addAttribute("cri", cri);
        model.addAttribute("totalCount", totalCount);

        return "/workorder/select-order-popup"; // → JSP 위치
    }

    /**
     * [GET] 수주번호 클릭 시 상세 제품 조회 (비동기)
     */
    @GetMapping("/select-order-detail")
    public String getOrderDetailList(@RequestParam("clOrderId") String clOrderId, Model model) {
        logger.debug("▶ getOrderDetailList() 호출 - clOrderId: {}", clOrderId);

        List<WorkOrderDTO> detailList = workOrderService.getOrderDetailList(clOrderId);

        model.addAttribute("detailList", detailList);

        return "/workorder/select-detail"; // → 상세 내용만 반환 (ajax용 include)
    }
    
    
    
}
   
    

