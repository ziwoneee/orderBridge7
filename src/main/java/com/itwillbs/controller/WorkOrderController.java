package com.itwillbs.controller;

import java.util.List;
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
    
    @Autowired
    private WorkOrderService workOrderService;
    
    // 작업지시 목록
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
    
    // 작업지시 상세
    @GetMapping("/detail/{orderId}")
    @ResponseBody
    public WorkOrderDTO getDetail(@PathVariable("orderId") String orderId) {
        log.info("작업지시 상세 조회 - ID: {}", orderId);
        return workOrderService.getWorkOrderDetail(orderId);
    }
}