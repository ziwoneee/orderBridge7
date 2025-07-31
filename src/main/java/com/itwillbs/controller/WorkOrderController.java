package com.itwillbs.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
        
        // sortColumn 변환: alias → 실제 컬럼명으로 변환 (중요!)
        if ("due_date".equals(cri.getSortColumn())) {
            cri.setSortColumn("co.cl_delivery_date");
        }
        
        // 정렬 허용 컬럼 목록
        List<String> allowedColumns = List.of(
            "w.order_id", "w.cl_order_id", "p.product_name", "cl.client_name",
            "w.created_at", "w.status", "w.priority", "co.cl_delivery_date"
        );


        // 잘못된 정렬 컬럼/순서 방지
        if (cri.getSortColumn() == null || cri.getSortColumn().trim().isEmpty() || !allowedColumns.contains(cri.getSortColumn())) {
            cri.setSortColumn("w.created_at");
        }

        if (cri.getSortOrder() == null || !(cri.getSortOrder().equals("asc") || cri.getSortOrder().equals("desc"))) {
            cri.setSortOrder("desc");
        }
        
        // 총 개수 및 페이징
        int totalCount = workOrderService.getWorkOrderTotalCount(cri);
        PageMaker pageMaker = new PageMaker(cri, totalCount);
        
        // 목록 조회
        List<WorkOrderDTO> workOrderList = workOrderService.getWorkOrderList(cri);
        
        for (WorkOrderDTO w : workOrderList) {
            log.info("▶ 작업지시번호: {}, 납기일: {}", w.getOrderId(), w.getDueDate());
        }
       
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
        
        List<WorkOrderDTO> confirmedList = workOrderService.getConfirmedOrders(cri);
        int totalCount = workOrderService.getConfirmedOrdersCount(cri);
        
        //  PageMaker 대신 간단한 계산
        int totalPages = (int) Math.ceil((double) totalCount / cri.getPerPageNum());
        
        model.addAttribute("orderList", confirmedList);
        model.addAttribute("cri", cri);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("totalPages", totalPages); //  이것만 추가
        
        return "workOrder/select-order-popup";
    }

    
    /**
     * [GET] 작업지시 등록 팝업 화면
     */
    @GetMapping("/register-popup")
    public String registerPopup(
            @RequestParam("clOrderId") String clOrderId,
            @RequestParam("productId") String productId,
            Model model) {

        log.info("🔥 register-popup 호출: clOrderId=[{}], productId=[{}]", clOrderId, productId);

        // 🔍 파라미터 null 체크 강화
        if (clOrderId == null || clOrderId.trim().isEmpty()) {
            log.error("❌ clOrderId가 비어있음: [{}]", clOrderId);
            throw new IllegalArgumentException("수주번호가 누락되었습니다.");
        }
        
        if (productId == null || productId.trim().isEmpty()) {
            log.error("❌ productId가 비어있음: [{}]", productId);
            throw new IllegalArgumentException("제품ID가 누락되었습니다.");
        }

        log.info("🔍 getOrderDetail 호출 전 - clOrderId=[{}], productId=[{}]", clOrderId, productId);
        
        try {
            WorkOrderDTO detail = workOrderService.getOrderDetail(clOrderId, productId);
            
            log.info(" getOrderDetail 결과: {}", detail);
            
            if (detail == null) {
                log.error(" getOrderDetail 결과 없음 → clOrderId=[{}], productId=[{}]", clOrderId, productId);
                throw new IllegalArgumentException("유효하지 않은 수주 정보입니다.");
            }

            // 각 필드별로 확인
            log.info(" detail 필드별 확인:");
            log.info("  - productName: [{}]", detail.getProductName());
            log.info("  - clientName: [{}]", detail.getClientName());
            log.info("  - dueDate: [{}]", detail.getDueDate());
            log.info("  - requiredQty: [{}]", detail.getRequiredQty());

            model.addAttribute("clOrderId", clOrderId);
            model.addAttribute("productId", productId);
            model.addAttribute("productName", detail.getProductName());
            model.addAttribute("clientName", detail.getClientName());
            model.addAttribute("dueDate", detail.getDueDate());
            model.addAttribute("requiredQty", detail.getRequiredQty());

            return "workOrder/register-popup";
            
        } catch (Exception e) {
            log.error(" getOrderDetail 호출 중 예외 발생:", e);
            throw new IllegalArgumentException("수주 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    
    /**
     * [POST] 작업지시 등록 처리
     */
    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registerWorkOrder(WorkOrderDTO workOrderDTO) {
        
        log.info("▶ 작업지시 등록 요청: {}", workOrderDTO);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 작업지시 등록
            int result = workOrderService.registerWorkOrder(workOrderDTO);
            
            if (result > 0) {
                response.put("success", true);
                response.put("message", "작업지시가 성공적으로 등록되었습니다.");
                response.put("orderId", workOrderDTO.getOrderId());
                log.info("▶ 작업지시 등록 성공: {}", workOrderDTO.getOrderId());
            } else {
                response.put("success", false);
                response.put("message", "작업지시 등록에 실패했습니다.");
            }
            
        } catch (Exception e) {
            log.error("작업지시 등록 실패", e);
            response.put("success", false);
            response.put("message", "시스템 오류가 발생했습니다: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    

    
    
    /**
     * [POST] 작업지시 상태 변경
     */
    @PostMapping("/updateStatus")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStatus(
            @RequestParam("orderId") String orderId, 
            @RequestParam("status") String status) {
        
        log.info("▶ 작업지시 상태 변경: {} -> {}", orderId, status);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            int result = workOrderService.updateWorkOrderStatus(orderId, status);
            
            if (result > 0) {
                response.put("success", true);
                response.put("message", "상태가 성공적으로 변경되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "상태 변경에 실패했습니다.");
            }
            
        } catch (Exception e) {
            log.error("작업지시 상태 변경 실패", e);
            response.put("success", false);
            response.put("message", "시스템 오류가 발생했습니다.");
        }
        
        return ResponseEntity.ok(response);
    }
}