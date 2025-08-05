package com.itwillbs.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.dto.BomItemDTO;
import com.itwillbs.dto.WorkOrderDTO;
import com.itwillbs.service.WorkOrderService;

import lombok.extern.slf4j.Slf4j;

/**
 * 작업지시 관리 컨트롤러
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024
 */
@Controller
@RequestMapping("/workorder") 
@Slf4j
public class WorkOrderController {
    
    @Autowired
    private WorkOrderService workOrderService;
    
    // ========================================================================
    // 작업지시 목록 및 조회
    // ========================================================================
    
    /**
     * 작업지시 목록 페이지
     * - 검색, 정렬, 페이징 기능 포함
     * - 상태별 건수 통계 제공
     * 
     * @param cri 검색 조건 (키워드, 상태, 날짜범위, 정렬, 페이징)
     * @param model 뷰 모델
     * @return 작업지시 목록 뷰
     */
    @GetMapping("/list")  
    public String getWorkOrderList(SearchCriteria cri, Model model) {
        log.info("작업지시 목록 조회 요청 - 조건: {}", cri);
        
        // 정렬 컬럼 검증 및 변환
        validateAndConvertSortColumn(cri);
        
        // 데이터 조회
        List<WorkOrderDTO> workOrderList = workOrderService.getWorkOrderList(cri);
        int totalCount = workOrderService.getWorkOrderTotalCount(cri);
        PageMaker pageMaker = new PageMaker(cri, totalCount);
        
        // 상태별 통계 조회
        Map<String, Integer> statusCounts = getStatusCounts();
        
        // 모델 바인딩
        bindListModel(model, workOrderList, cri, pageMaker, statusCounts);
        
        log.info("작업지시 목록 조회 완료 - 총 {}건", workOrderList.size());
        return "workOrder/list";
    }
    
    /**
     * 작업지시 상세 정보 조회 (AJAX)
     * 
     * @param orderId 작업지시번호
     * @return 작업지시 상세 정보
     */
    @GetMapping("/detail/{orderId}")
    public String getWorkOrderDetail(@PathVariable("orderId") String orderId, Model model) {
        WorkOrderDTO workOrder = workOrderService.getWorkOrderDetail(orderId);
        List<BomItemDTO> bomList = workOrderService.calculateMaterialUsage(workOrder.getProductId(), workOrder.getOrderQty());

        model.addAttribute("workOrder", workOrder);
        model.addAttribute("bomList", bomList);

        return "/workOrder/detail-modal";  // 모달 JSP 경로
    }
    
    // ========================================================================
    // 작업지시 등록 관련
    // ========================================================================
    
    /**
     * 확정 수주 선택 팝업 페이지
     * - 작업지시가 아직 등록되지 않은 확정 수주만 표시
     * 
     * @param cri 검색 조건
     * @param model 뷰 모델
     * @return 수주 선택 팝업 뷰
     */
    @GetMapping("/select-order")
    public String showSelectOrderPopup(SearchCriteria cri, Model model) {
        log.info("확정 수주 선택 팝업 요청 - 조건: {}", cri);
        
        // 확정 수주 목록 조회
        List<WorkOrderDTO> confirmedOrders = workOrderService.getConfirmedOrders(cri);
        int totalCount = workOrderService.getConfirmedOrdersCount(cri);
        int totalPages = calculateTotalPages(totalCount, cri.getPerPageNum());
        
        // 모델 바인딩
        model.addAttribute("orderList", confirmedOrders);
        model.addAttribute("cri", cri);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("totalPages", totalPages);
        
        log.info("확정 수주 목록 조회 완료 - 총 {}건", confirmedOrders.size());
        return "workOrder/select-order-popup";
    }
    
    /**
     * 작업지시 등록 팝업 페이지
     * 
     * @param clOrderId 수주번호
     * @param productId 제품ID
     * @param model 뷰 모델
     * @return 작업지시 등록 팝업 뷰
     */
    @GetMapping("/register-popup")
    public String showRegisterPopup(
            @RequestParam("clOrderId") String clOrderId,
            @RequestParam("productId") String productId,
            Model model) {
        
        log.info("작업지시 등록 팝업 요청 - 수주번호: {}, 제품ID: {}", clOrderId, productId);
        
        // 파라미터 검증
        validateRegistrationParams(clOrderId, productId);
        
        try {
            // 수주 상세 정보 조회
            WorkOrderDTO orderDetail = workOrderService.getOrderDetail(clOrderId, productId);
            
            if (orderDetail == null) {
                throw new IllegalArgumentException("해당 수주 정보를 찾을 수 없습니다.");
            }
            
            // 모델 바인딩
            bindRegistrationModel(model, clOrderId, productId, orderDetail);
            
            log.info("작업지시 등록 팝업 데이터 준비 완료");
            return "workOrder/register-popup";
            
        } catch (Exception e) {
            log.error("작업지시 등록 팝업 데이터 조회 실패 - 수주번호: {}, 제품ID: {}", clOrderId, productId);
            throw new IllegalArgumentException("수주 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 작업지시 등록 처리 (AJAX)
     * 
     * @param workOrderDTO 작업지시 정보
     * @return 등록 결과
     */
    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registerWorkOrder(WorkOrderDTO workOrderDTO) {
        log.info("작업지시 등록 요청: {}", workOrderDTO);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            int result = workOrderService.registerWorkOrder(workOrderDTO);
            
            if (result > 0) {
                response.put("success", true);
                response.put("message", "작업지시가 성공적으로 등록되었습니다.");
                response.put("orderId", workOrderDTO.getOrderId());
                log.info("작업지시 등록 성공 - ID: {}", workOrderDTO.getOrderId());
            } else {
                response.put("success", false);
                response.put("message", "작업지시 등록에 실패했습니다.");
                log.warn("작업지시 등록 실패 - 결과: {}", result);
            }
            
        } catch (Exception e) {
            log.error("작업지시 등록 중 시스템 오류", e);
            response.put("success", false);
            response.put("message", "시스템 오류가 발생했습니다: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    // ========================================================================
    // 작업지시 상태 관리
    // ========================================================================
    
    /**
     * 작업지시 상태 변경 (AJAX)
     * 
     * @param orderId 작업지시번호
     * @param status 변경할 상태
     * @return 변경 결과
     */
    @PostMapping("/updateStatus")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateWorkOrderStatus(
            @RequestParam("orderId") String orderId, 
            @RequestParam("status") String status) {
        
        log.info("작업지시 상태 변경 요청 - ID: {}, 상태: {}", orderId, status);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            int result = workOrderService.updateWorkOrderStatus(orderId, status);
            
            if (result > 0) {
                response.put("success", true);
                response.put("message", "상태가 성공적으로 변경되었습니다.");
                log.info("작업지시 상태 변경 성공 - ID: {}, 상태: {}", orderId, status);
            } else {
                response.put("success", false);
                response.put("message", "상태 변경에 실패했습니다.");
                log.warn("작업지시 상태 변경 실패 - ID: {}, 상태: {}", orderId, status);
            }
            
        } catch (Exception e) {
            log.error("작업지시 상태 변경 중 시스템 오류 - ID: {}, 상태: {}", orderId, status);
            response.put("success", false);
            response.put("message", "시스템 오류가 발생했습니다.");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 작업지시 삭제 처리 (AJAX)
     * 
     * @param orderId 작업지시번호
     * @return 삭제 결과
     */
    @DeleteMapping("/delete/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteWorkOrder(@PathVariable("orderId") String orderId) {
        log.info("작업지시 삭제 요청 - ID: {}", orderId);
        Map<String, Object> response = new HashMap<>();

        try {
            workOrderService.deleteWorkOrder(orderId);
            response.put("success", true);
            response.put("message", "삭제 완료");
            log.info("작업지시 삭제 완료 - ID: {}", orderId);
        } catch (Exception e) {
            log.error("작업지시 삭제 실패 - ID: {}", orderId, e);
            response.put("success", false);
            response.put("message", "삭제 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(response);
    }
    
    /**
     * 작업지시 수정 폼 페이지
     * 
     * @param orderId 작업지시번호
     * @param model 뷰 모델
     * @return 수정 폼 JSP
     */
    @GetMapping("/edit")
    public String editWorkOrderForm(@RequestParam("orderId") String orderId, Model model) {
        log.info("작업지시 수정 폼 요청 - ID: {}", orderId);

        WorkOrderDTO dto = workOrderService.getWorkOrderDetail(orderId);

        if (!"WAITING".equals(dto.getStatus())) {
            throw new IllegalStateException("대기 상태(WAITING)인 작업지시만 수정 가능합니다.");
        }

        model.addAttribute("order", dto);
        return "workOrder/edit"; // edit.jsp
    }
    
    /**
     * 작업지시 수정 처리
     * 
     * @param dto 수정된 작업지시 정보
     * @return 목록 리다이렉트
     */
    @PostMapping("/edit")
    public String updateWorkOrder(@ModelAttribute WorkOrderDTO dto, RedirectAttributes rttr) {
        log.info("작업지시 수정 처리 요청 - {}", dto);

        try {
            workOrderService.updateWorkOrder(dto);
            rttr.addFlashAttribute("message", "작업지시 수정 완료");
        } catch (Exception e) {
            log.error("작업지시 수정 실패", e);
            rttr.addFlashAttribute("message", "수정 중 오류 발생: " + e.getMessage());
        }

        return "redirect:/workorder/list";
    }
    
    
    // ========================================================================
    // BOM 관련
    // ========================================================================
    
    /**
     * 제품별 BOM 자재 목록 조회 (AJAX)
     * - 작업지시 등록 시 자재 소요량 계산용
     * 
     * @param productId 제품ID
     * @param orderQty 지시 수량
     * @return BOM 자재 목록 (총 소요량 포함)
     */
    @GetMapping("/getBomByProduct")
    @ResponseBody
    public List<BomItemDTO> getBomByProduct(
            @RequestParam String productId,
            @RequestParam int orderQty) {
        
        log.info("BOM 자재 소요량 계산 요청 - 제품ID: {}, 수량: {}", productId, orderQty);
        
        List<BomItemDTO> bomItems = workOrderService.calculateMaterialUsage(productId, orderQty);
        
        log.info("BOM 자재 소요량 계산 완료 - 제품ID: {}, 자재 종류: {}개", productId, bomItems.size());
        return bomItems;
    }
    
    // ========================================================================
    // Private Helper Methods
    // ========================================================================
    
    /**
     * 정렬 컬럼 검증 및 변환
     */
    private void validateAndConvertSortColumn(SearchCriteria cri) {
        // 허용된 정렬 컬럼 목록
        List<String> allowedColumns = List.of(
            "w.order_id", "w.cl_order_id", "p.product_name", "cl.client_name",
            "w.created_at", "w.status", "w.priority", "co.cl_delivery_date"
        );
        
        // alias → 실제 컬럼명 변환
        if ("due_date".equals(cri.getSortColumn())) {
            cri.setSortColumn("co.cl_delivery_date");
        }
        
        // 잘못된 정렬 컬럼 방지
        if (cri.getSortColumn() == null || 
            cri.getSortColumn().trim().isEmpty() || 
            !allowedColumns.contains(cri.getSortColumn())) {
            cri.setSortColumn("w.created_at");
        }
        
        // 잘못된 정렬 순서 방지
        if (cri.getSortOrder() == null || 
            !(cri.getSortOrder().equals("asc") || cri.getSortOrder().equals("desc"))) {
            cri.setSortOrder("desc");
        }
    }
    
    /**
     * 상태별 통계 조회
     */
    private Map<String, Integer> getStatusCounts() {
        Map<String, Integer> statusCounts = new HashMap<>();
        statusCounts.put("all", workOrderService.getAllCount());
        statusCounts.put("waiting", workOrderService.getCountByStatus("WAITING"));
        statusCounts.put("inProgress", workOrderService.getCountByStatus("IN_PROGRESS"));
        statusCounts.put("completed", workOrderService.getCountByStatus("COMPLETED"));
        return statusCounts;
    }
    
    /**
     * 목록 조회 모델 바인딩
     */
    private void bindListModel(Model model, List<WorkOrderDTO> workOrderList, 
                              SearchCriteria cri, PageMaker pageMaker, 
                              Map<String, Integer> statusCounts) {
        model.addAttribute("workOrders", workOrderList);
        model.addAttribute("cri", cri);
        model.addAttribute("pageMaker", pageMaker);
        model.addAttribute("allCount", statusCounts.get("all"));
        model.addAttribute("waitingCount", statusCounts.get("waiting"));
        model.addAttribute("inProgressCount", statusCounts.get("inProgress"));
        model.addAttribute("completedCount", statusCounts.get("completed"));
    }
    
    /**
     * 등록 팝업 파라미터 검증
     */
    private void validateRegistrationParams(String clOrderId, String productId) {
        if (clOrderId == null || clOrderId.trim().isEmpty()) {
            throw new IllegalArgumentException("수주번호가 누락되었습니다.");
        }
        
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("제품ID가 누락되었습니다.");
        }
    }
    
    /**
     * 등록 팝업 모델 바인딩
     */
    private void bindRegistrationModel(Model model, String clOrderId, String productId, 
                                     WorkOrderDTO orderDetail) {
        model.addAttribute("clOrderId", clOrderId);
        model.addAttribute("productId", productId);
        model.addAttribute("productName", orderDetail.getProductName());
        model.addAttribute("clientName", orderDetail.getClientName());
        model.addAttribute("dueDate", orderDetail.getDueDate());
        model.addAttribute("requiredQty", orderDetail.getRequiredQty());
    }
    
    /**
     * 전체 페이지 수 계산
     */
    private int calculateTotalPages(int totalCount, int perPageNum) {
        return (int) Math.ceil((double) totalCount / perPageNum);
    }
}