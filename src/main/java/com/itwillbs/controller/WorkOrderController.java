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
    
    @Autowired
    private ProductionLineService productionLineService;
    
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

        validateAndConvertSortColumn(cri);

        int totalCount = workOrderService.getWorkOrderTotalCount(cri);
        cri.setTotalCount(totalCount);  // totalCount를 cri에 설정 (한 번만 수행)
        PageMaker pageMaker = new PageMaker(cri, totalCount);

        List<WorkOrderDTO> workOrderList = workOrderService.getWorkOrderList(cri);
        Map<String, Integer> statusCounts = getStatusCounts();

        bindListModel(model, workOrderList, cri, pageMaker, statusCounts);

        log.info("작업지시 목록 조회 완료 - 총 {}건", workOrderList.size());
        return "workOrder/list";
    }
    
    
    /**
     * 작업지시 상세 정보 조회 (인라인 편집 가능)
     */
    @GetMapping("/detail-modal")
    public String getWorkOrderDetailModal(@RequestParam("orderId") String orderId, Model model) {
        log.info("작업지시 상세 요청 - ID: {}", orderId);

        // 작업지시 상세 조회
        WorkOrderDTO workOrder = workOrderService.getWorkOrderDetail(orderId);
        log.info("불러온 작업지시: {}", workOrder);

        // BOM 자재 목록 조회
        List<BomItemDTO> bomList = workOrderService.calculateMaterialUsage(
            workOrder.getProductId(), workOrder.getOrderQty());
        log.info("계산된 BOM 자재 수: {}", bomList.size());
        
        // 라인 목록 조회 (수정 모드에서 사용)
        List<ProductionLineVO> lineList = productionLineService.getAvailableLines();

        model.addAttribute("workOrder", workOrder);
        model.addAttribute("bomList", bomList);
        model.addAttribute("lineList", lineList);

        return "/workOrder/detail-modal";  
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
        
        // 확정 수주 목록 + 총 개수
        List<WorkOrderDTO> confirmedOrders = workOrderService.getConfirmedOrders(cri);
        int totalCount = workOrderService.getConfirmedOrdersCount(cri);

        // PageMaker 생성
        PageMaker pageMaker = new PageMaker(cri, totalCount);

        // 모델 바인딩
        model.addAttribute("orderList", confirmedOrders);
        model.addAttribute("cri", cri);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pageMaker", pageMaker);  

        log.info("확정 수주 목록 조회 완료 - 총 {}건", confirmedOrders.size());
        return "workOrder/select-order-popup";
    }
    
    /**
     * 작업지시 등록 팝업 페이지 (병합 수주용)
     * - 여러 수주(clOrderIds[])를 병합해서 하나의 작업지시로 등록하는 경우 사용
     * - productId, 수량, 납기일 등의 정보는 프론트에서 계산되어 전달됨
     * 
     * @param clOrderIds 병합된 수주 번호 목록
     * @param productId 제품 ID
     * @param orderQty 총 작업지시 수량
     * @param dueDate 납기일
     * @param model JSP 모델
     * @return 작업지시 등록 화면
     */
    @GetMapping("/register-popup")
    public String showMergedRegisterPopup(
            @RequestParam("clOrderIds") List<String> clOrderIds,
            @RequestParam("productId") String productId,
            @RequestParam("orderQty") int orderQty,
            @RequestParam("dueDate") String dueDate,
            Model model) {

        // 등록 파라미터 유효성 검사
        validateRegistrationParams(clOrderIds, productId);

        // 제품명 및 거래처명 등 대표 정보 조회 (1건 기준)
        WorkOrderDTO orderDetail = workOrderService.getOrderDetail(clOrderIds.get(0), productId);

        // 생산 라인 목록 조회
        List<ProductionLineVO> lineList = productionLineService.getAvailableLines();

        // 모델 바인딩
        model.addAttribute("clOrderIds", clOrderIds);              // 수주 ID 리스트
        try {
            model.addAttribute("clOrderIdsJson", new ObjectMapper().writeValueAsString(clOrderIds));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            model.addAttribute("clOrderIdsJson", "[]"); // ← JSON 오류 시 기본값
        }
        model.addAttribute("productId", productId);                // 제품 ID
        model.addAttribute("productName", orderDetail.getProductName()); // 제품명
        model.addAttribute("clientNames", orderDetail.getClientNames()); // 거래처명 목록
        model.addAttribute("requiredQty", orderQty);               // 총 수량
        model.addAttribute("dueDate", dueDate);                    // 납기일 (String)
        model.addAttribute("lineList", lineList);                  // 생산 라인 목록

        return "workOrder/register-popup";
    }
    
    /**
     * 작업지시 등록 처리 (AJAX)
     * 
     * @param workOrderDTO 작업지시 정보
     * @return 등록 결과
     */
    /**
     * 작업지시 등록 처리 (POST)
     * - 병합 수주 기반 등록
     */
    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> registerWorkOrder(@RequestBody WorkOrderDTO workOrderDTO, HttpSession session) {
        log.info(" 작업지시 등록 요청 (Controller) - DTO: {}", workOrderDTO);

        Map<String, Object> response = new HashMap<>();

        try {
            String loginUserName = (String) session.getAttribute("userName");
            workOrderDTO.setOrderManager(loginUserName);

            int result = workOrderService.registerWorkOrder(workOrderDTO);

            if (result > 0) {
                response.put("success", true);
                response.put("message", "작업지시 등록이 완료되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "작업지시 등록에 실패했습니다.");
            }

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);

        } catch (Exception e) {
            log.error(" 작업지시 등록 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "오류 발생: " + e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }
    
    // ========================================================================
    // 작업지시 수정 및 삭제
    // ========================================================================
    
    /**
     * 작업지시 수정 처리 (인라인 편집)
     */
    @PostMapping("/edit")
    @ResponseBody
    public ResponseEntity<String> updateWorkOrder(@ModelAttribute WorkOrderDTO dto) {
        log.info("작업지시 수정 처리 요청: {}", dto);
        
        try {
            // 1. 기존 데이터 조회
            WorkOrderDTO origin = workOrderService.getWorkOrderDetail(dto.getOrderId());
            
            // 2. 상태 확인
            if (!"WAITING".equals(origin.getStatus())) {
                return ResponseEntity.badRequest().body("대기 상태(WAITING)인 작업지시만 수정 가능합니다.");
            }
            
            // 3. 수정할 필드만 설정 (나머지는 기존 값 유지)
            origin.setLineId(dto.getLineId());
            origin.setRemarks(dto.getRemarks());
            origin.setPriority(dto.getPriority()); // ← 이 줄 추가!
            
            // 4. 수정 처리
            workOrderService.updateWorkOrder(origin);
            
            return ResponseEntity.ok("success");
            
        } catch (Exception e) {
            log.error("작업지시 수정 중 예외 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("수정 중 오류 발생: " + e.getMessage());
        }
    }
    
    /**
     * 작업지시 삭제 처리 (소프트 삭제)
     * - 상태가 'WAITING'인 경우만 가능
     * - 실제로는 DB에서 삭제하지 않고 is_deleted = true 처리
     */
    @DeleteMapping("/delete/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteWorkOrder(@PathVariable("orderId") String orderId) {
        log.info("작업지시 삭제 요청 - ID: {}", orderId);

        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 기존 작업지시 조회
            WorkOrderDTO dto = workOrderService.getWorkOrderDetail(orderId);

            // 2. 삭제 조건 체크
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

            // 3. 소프트 삭제 처리 (void → return 없음)
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
    
    /**
     * 작업지시 상태 변경 (AJAX)
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
        // 허용 컬럼 키(스네이크 케이스)
        Map<String, Boolean> allowed = Map.of(
            "order_id",   true,
            "product_name", true,
            "created_at", true,
            "status",     true,
            "priority",   true,
            "due_date",   true,
            "order_qty",  true
        );

        // sortColumn 정규화: null/빈칸이면 그대로 두고(= Mapper 기본 정렬 사용)
        String raw = cri.getSortColumn();
        if (raw != null) {
            raw = raw.trim();
            // 카멜로 들어오면 스네이크로 변환
            raw = raw.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
            if (!allowed.containsKey(raw)) {
                raw = null; // 허용 안 되면 Mapper 기본 정렬로
            }
        }
        cri.setSortColumn(raw); // "created_at" 같은 키 그대로 넘김

        // sortOrder 정규화: ASC/DESC만 허용
        String order = cri.getSortOrder();
        if (order == null || order.isBlank()) {
            cri.setSortOrder(null); // null이면 Mapper에서 기본 분기 타게 함
        } else {
            order = order.trim().toUpperCase();
            if (!order.equals("ASC") && !order.equals("DESC")) {
                cri.setSortOrder(null);
            } else {
                cri.setSortOrder(order);
            }
        }
    }
    
    /**
     * 상태별 통계 조회
     */
    private Map<String, Integer> getStatusCounts() {
        Map<String, Integer> statusCounts = new HashMap<>();
        statusCounts.put("all",        workOrderService.getAllCount());
        statusCounts.put("waiting",    workOrderService.getCountByStatus("WAITING"));
        statusCounts.put("ready",      workOrderService.getCountByStatus("READY"));         // ★ 추가
        statusCounts.put("inProgress", workOrderService.getCountByStatus("IN_PROGRESS"));
        statusCounts.put("completed",  workOrderService.getCountByStatus("COMPLETED"));
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
	model.addAttribute("allCount",        statusCounts.get("all"));
	model.addAttribute("waitingCount",    statusCounts.get("waiting"));
	model.addAttribute("readyCount",      statusCounts.get("ready"));      // ★ 추가
	model.addAttribute("inProgressCount", statusCounts.get("inProgress"));
	model.addAttribute("completedCount",  statusCounts.get("completed"));
	}
    
    /**
     * 등록 팝업 파라미터 검증
     */
    private void validateRegistrationParams(List<String> clOrderIds, String productId) {
        if (clOrderIds == null || clOrderIds.isEmpty()) {
            throw new IllegalArgumentException("수주번호 목록이 누락되었습니다.");
        }

        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("제품ID가 누락되었습니다.");
        }
    }
    

}