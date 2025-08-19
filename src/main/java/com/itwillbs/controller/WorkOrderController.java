package com.itwillbs.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.itwillbs.domain.SearchCriteria;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.ProductionLineVO;
import com.itwillbs.dto.BomItemDTO;
import com.itwillbs.dto.WorkOrderDTO;
import com.itwillbs.service.ProductionLineService;
import com.itwillbs.service.WorkOrderService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/workorder") 
@Slf4j
public class WorkOrderController {
    
    @Autowired
    private WorkOrderService workOrderService;
    
    @Autowired
    private ProductionLineService productionLineService;
    
    // ========================================================================
    // 작업지시 목록 및 조회
    // ========================================================================
    @GetMapping("/list")
    public String getWorkOrderList(SearchCriteria cri, Model model) {
        log.info("작업지시 목록 조회 요청 - 조건: {}", cri);

        validateAndConvertSortColumn(cri);

        int totalCount = workOrderService.getWorkOrderTotalCount(cri);
        cri.setTotalCount(totalCount);
        PageMaker pageMaker = new PageMaker(cri, totalCount);

        List<WorkOrderDTO> workOrderList = workOrderService.getWorkOrderList(cri);
        Map<String, Integer> statusCounts = getStatusCounts();

        bindListModel(model, workOrderList, cri, pageMaker, statusCounts);

        log.info("작업지시 목록 조회 완료 - 총 {}건", workOrderList.size());
        return "workOrder/list";
    }
    
    @GetMapping("/detail-modal")
    public String getWorkOrderDetailModal(@RequestParam("orderId") String orderId, Model model) {
        log.info("작업지시 상세 요청 - ID: {}", orderId);
        WorkOrderDTO workOrder = workOrderService.getWorkOrderDetail(orderId);
        List<BomItemDTO> bomList = workOrderService.calculateMaterialUsage(
            workOrder.getProductId(), workOrder.getOrderQty());
        List<ProductionLineVO> lineList = productionLineService.getAvailableLines();

        model.addAttribute("workOrder", workOrder);
        model.addAttribute("bomList", bomList);
        model.addAttribute("lineList", lineList);
        return "/workOrder/detail-modal";
    }
    
    // ========================================================================
    // 작업지시 등록/수정/삭제
    // ========================================================================
    @GetMapping("/select-order")
    public String showSelectOrderPopup(SearchCriteria cri, Model model) {
        log.info("확정 수주 선택 팝업 요청 - 조건: {}", cri);
        List<WorkOrderDTO> confirmedOrders = workOrderService.getConfirmedOrders(cri);
        int totalCount = workOrderService.getConfirmedOrdersCount(cri);
        PageMaker pageMaker = new PageMaker(cri, totalCount);
        model.addAttribute("orderList", confirmedOrders);
        model.addAttribute("cri", cri);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pageMaker", pageMaker);
        return "workOrder/select-order-popup";
    }
    
    @GetMapping("/register-popup")
    public String showMergedRegisterPopup(
            @RequestParam("clOrderIds") List<String> clOrderIds,
            @RequestParam("productId") String productId,
            @RequestParam("orderQty") int orderQty,
            @RequestParam("dueDate") String dueDate,
            Model model) {

        validateRegistrationParams(clOrderIds, productId);

        WorkOrderDTO orderDetail = workOrderService.getOrderDetail(clOrderIds.get(0), productId);
        List<ProductionLineVO> lineList = productionLineService.getAvailableLines();

        model.addAttribute("clOrderIds", clOrderIds);
        try {
            model.addAttribute("clOrderIdsJson", new ObjectMapper().writeValueAsString(clOrderIds));
        } catch (JsonProcessingException e) {
            model.addAttribute("clOrderIdsJson", "[]");
        }
        model.addAttribute("productId", productId);
        model.addAttribute("productName", orderDetail.getProductName());
        model.addAttribute("clientNames", orderDetail.getClientNames());
        model.addAttribute("requiredQty", orderQty);
        model.addAttribute("dueDate", dueDate);
        model.addAttribute("lineList", lineList);

        return "workOrder/register-popup";
    }
    
    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> registerWorkOrder(@RequestBody WorkOrderDTO workOrderDTO, HttpSession session) {
        log.info("작업지시 등록 요청 - DTO: {}", workOrderDTO);

        Map<String, Object> response = new HashMap<>();
        try {
            String loginUserName = (String) session.getAttribute("userName");
            workOrderDTO.setOrderManager(loginUserName);

            int result = workOrderService.registerWorkOrder(workOrderDTO);
            response.put("success", result > 0);
            response.put("message", result > 0 ? "작업지시 등록이 완료되었습니다." : "작업지시 등록에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
        } catch (Exception e) {
            log.error("작업지시 등록 오류", e);
            response.put("success", false);
            response.put("message", "오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(response);
        }
    }
    
    @PostMapping("/edit")
    @ResponseBody
    public ResponseEntity<String> updateWorkOrder(@ModelAttribute WorkOrderDTO dto) {
        log.info("작업지시 수정 처리 요청: {}", dto);
        try {
            WorkOrderDTO origin = workOrderService.getWorkOrderDetail(dto.getOrderId());
            if (!"WAITING".equals(origin.getStatus())) {
                return ResponseEntity.badRequest().body("대기 상태(WAITING)인 작업지시만 수정 가능합니다.");
            }
            origin.setLineId(dto.getLineId());
            origin.setRemarks(dto.getRemarks());
            origin.setPriority(dto.getPriority());
            workOrderService.updateWorkOrder(origin);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            log.error("작업지시 수정 중 예외", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("수정 중 오류: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/delete/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteWorkOrder(@PathVariable("orderId") String orderId) {
        log.info("작업지시 삭제 요청 - ID: {}", orderId);

        Map<String, Object> response = new HashMap<>();
        try {
            WorkOrderDTO dto = workOrderService.getWorkOrderDetail(orderId);
            if (dto == null || (dto.getIsDeleted() != null && dto.getIsDeleted())) {
                response.put("success", false);
                response.put("message", "이미 삭제된 작업지시입니다.");
                return ResponseEntity.ok(response);
            }
            if (!"WAITING".equals(dto.getStatus())) {
                response.put("success", false);
                response.put("message", "대기 상태인 작업지시만 삭제할 수 있습니다.");
                return ResponseEntity.ok(response);
            }
            workOrderService.deleteWorkOrder(orderId);
            response.put("success", true);
            response.put("message", "작업지시가 삭제(숨김처리)되었습니다.");
        } catch (Exception e) {
            log.error("작업지시 삭제 실패 - ID: {}", orderId, e);
            response.put("success", false);
            response.put("message", "시스템 오류가 발생했습니다.");
        }
        return ResponseEntity.ok(response);
    }
    
    // ========================================================================
    // 상태 변경 & 생산 시작
    // ========================================================================
    /**
     * 상태 변경(AJAX). 
     * ※ IN_PROGRESS 전환은 전용 API(/start/{orderId})를 사용하도록 막음.
     */
    @PostMapping("/updateStatus")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateWorkOrderStatus(
            @RequestParam("orderId") String orderId, 
            @RequestParam("status") String status) {
        
        log.info("작업지시 상태 변경 요청 - ID: {}, 상태: {}", orderId, status);
        Map<String, Object> response = new HashMap<>();
        try {
            if ("IN_PROGRESS".equalsIgnoreCase(status)) {
                response.put("success", false);
                response.put("message", "생산 시작은 전용 버튼을 사용하세요.");
                return ResponseEntity.ok(response);
            }
            int result = workOrderService.updateWorkOrderStatus(orderId, status);
            response.put("success", result > 0);
            response.put("message", result > 0 ? "상태가 변경되었습니다." : "상태 변경에 실패했습니다.");
        } catch (Exception e) {
            
            response.put("success", false);
            response.put("message", "시스템 오류가 발생했습니다.");
        }
        return ResponseEntity.ok(response);
    }

    /**
     * READY → IN_PROGRESS 전환(생산 시작 버튼)
     */
    @PostMapping("/start/{orderId}")
    @ResponseBody
    public Map<String, Object> startProduction(@PathVariable String orderId) {
        Map<String, Object> res = new HashMap<>();
        try {
            WorkOrderDTO wo = workOrderService.getWorkOrderDetail(orderId);
            if (wo == null) {
                res.put("success", false);
                res.put("message", "작업지시를 찾을 수 없습니다.");
                return res;
            }
            if (!"READY".equals(wo.getStatus())) {
                res.put("success", false);
                res.put("message", "준비완료(READY) 상태에서만 생산을 시작할 수 있습니다.");
                return res;
            }
            if (wo.getLineId() == null || wo.getLineId().isBlank()) {
                res.put("success", false);
                res.put("message", "라인을 먼저 지정하세요.");
                return res;
            }

            int updated = workOrderService.updateWorkOrderStatus(orderId, "IN_PROGRESS");
            res.put("success", updated > 0);
            res.put("message", updated > 0 ? "생산을 시작했습니다." : "상태 변경에 실패했습니다.");
            return res;
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "시스템 오류가 발생했습니다.");
            return res;
        }
    }
    
    // ========================================================================
    // BOM
    // ========================================================================
    @GetMapping("/getBomByProduct")
    @ResponseBody
    public List<BomItemDTO> getBomByProduct(@RequestParam String productId, @RequestParam int orderQty) {
        log.info("BOM 자재 소요량 계산 요청 - 제품ID: {}, 수량: {}", productId, orderQty);
        return workOrderService.calculateMaterialUsage(productId, orderQty);
    }
    
    // ========================================================================
    // Private Helpers
    // ========================================================================
    private void validateAndConvertSortColumn(SearchCriteria cri) {
        Map<String, Boolean> allowed = Map.of(
            "order_id", true, "product_name", true, "created_at", true,
            "status", true, "priority", true, "due_date", true, "order_qty", true
        );
        String raw = cri.getSortColumn();
        if (raw != null) {
            raw = raw.trim().replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
            if (!allowed.containsKey(raw)) raw = null;
        }
        cri.setSortColumn(raw);

        String order = cri.getSortOrder();
        if (order == null || order.isBlank()) {
            cri.setSortOrder(null);
        } else {
            order = order.trim().toUpperCase();
            cri.setSortOrder(("ASC".equals(order) || "DESC".equals(order)) ? order : null);
        }
    }
    
    private Map<String, Integer> getStatusCounts() {
        Map<String, Integer> statusCounts = new HashMap<>();
        statusCounts.put("all",        workOrderService.getAllCount());
        statusCounts.put("waiting",    workOrderService.getCountByStatus("WAITING"));
        statusCounts.put("ready",      workOrderService.getCountByStatus("READY"));
        statusCounts.put("inProgress", workOrderService.getCountByStatus("IN_PROGRESS"));
        statusCounts.put("completed",  workOrderService.getCountByStatus("COMPLETED"));
        return statusCounts;
    }
    
    private void bindListModel(Model model, List<WorkOrderDTO> workOrderList, 
            SearchCriteria cri, PageMaker pageMaker, Map<String, Integer> statusCounts) {
        model.addAttribute("workOrders", workOrderList);
        model.addAttribute("cri", cri);
        model.addAttribute("pageMaker", pageMaker);
        model.addAttribute("allCount",        statusCounts.get("all"));
        model.addAttribute("waitingCount",    statusCounts.get("waiting"));
        model.addAttribute("readyCount",      statusCounts.get("ready"));
        model.addAttribute("inProgressCount", statusCounts.get("inProgress"));
        model.addAttribute("completedCount",  statusCounts.get("completed"));
    }
    
    private void validateRegistrationParams(List<String> clOrderIds, String productId) {
        if (clOrderIds == null || clOrderIds.isEmpty()) {
            throw new IllegalArgumentException("수주번호 목록이 누락되었습니다.");
        }
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("제품ID가 누락되었습니다.");
        }
    }
}
