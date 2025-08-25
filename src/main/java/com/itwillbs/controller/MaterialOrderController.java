package com.itwillbs.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itwillbs.domain.AdminUserVO;
import com.itwillbs.domain.MaterialOrderItemVO;
import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.MaterialVO;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.SupplierVO;
import com.itwillbs.dto.MaterialOrderDTO;
import com.itwillbs.dto.SupplierItemDTO;
import com.itwillbs.service.MaterialOrderService;
import com.itwillbs.service.MaterialService;
import com.itwillbs.service.SupplierService;

/**
 * 자재 발주 관리 - 발주 목록 조회 컨트롤러
 */
@Controller
@RequestMapping("/material/order")
public class MaterialOrderController {
	
	private static final Logger logger = LoggerFactory.getLogger(MaterialOrderController.class);

	@Inject
	private MaterialOrderService mOrderService;
	
	@Inject
	private SupplierService supplierService;
	
	@Inject
	private MaterialService materialService;
	
	
    private void setRegisterPageData(Model model) throws Exception {
        model.addAttribute("supplierList", supplierService.getAllSuppliers());
        model.addAttribute("materialList", materialService.getAllMaterials());
        model.addAttribute("menu", "material");
    }
	
	
	// ✅ Date 바인딩 설정 추가
	@InitBinder
	public void initBinder(WebDataBinder binder) {
	    // Date 타입 바인딩 설정
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    dateFormat.setLenient(false);
	    binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	    
	    // totalPrice 바인딩 무시 (서버에서 계산)
	    binder.setDisallowedFields("orderItems[].totalPrice");
	}

	
	// 발주 목록 조회 (검색, 정렬, 페이징 포함)
	@GetMapping("/list")
	public String orderList(SearchCriteria cri, Model model) throws Exception {
		
		if (cri.getStatus() == null || cri.getStatus().isBlank() || "ALL".equalsIgnoreCase(cri.getStatus())) {
	        cri.setStatus(null);
	    }

		
		if (cri.getSortColumn() == null || cri.getSortColumn().isBlank()) {
	        cri.setSortColumn("order_date");
	    }
	    
        List<MaterialOrderVO> orderList = mOrderService.getOrderList(cri);
        int totalCount = mOrderService.getTotalCount(cri);
        PageMaker pageMaker = new PageMaker(cri, totalCount);

        model.addAttribute("orderList", orderList);
        model.addAttribute("pageMaker", pageMaker);
        model.addAttribute("cri", cri);
	    model.addAttribute("menu", "material");
	    
	    // 각 상태별 카운트를 계산하여 Model에 추가
	    Map<String, Integer> statusCounts = mOrderService.getStatusCounts();
	    model.addAttribute("draftCount", statusCounts.getOrDefault("초안", 0)); // 1
	    model.addAttribute("requestCount", statusCounts.getOrDefault("요청", 0)); // 4  
	    model.addAttribute("approvedCount", statusCounts.getOrDefault("승인", 0)); // 7
	    model.addAttribute("completedCount", statusCounts.getOrDefault("입고완료", 0)); // 4
	    model.addAttribute("canceledCount", statusCounts.getOrDefault("취소", 0)); // 0
	    
	    return "material/order/list";
	}
	
	/** 로그인 ID 찾기: 실제 세션 키 사용 */
	private String resolveLoginAdminId(HttpSession session) {
	    // 1. loginAdmin 키 확인 (실제 사용되는 키)
	    Object la = session.getAttribute("loginAdmin");
	    if (la instanceof AdminUserVO) return ((AdminUserVO) la).getAdminId();

	    // 2. 기존 키들도 확인 (호환성)
	    Object au = session.getAttribute("adminUser");
	    if (au instanceof AdminUserVO) return ((AdminUserVO) au).getAdminId();

	    Object lu = session.getAttribute("loginUser");
	    if (lu instanceof AdminUserVO) return ((AdminUserVO) lu).getAdminId();

	    // 3. Spring Security 확인
	    try {
	        var auth = org.springframework.security.core.context.SecurityContextHolder
	                     .getContext().getAuthentication();
	        if (auth != null && auth.isAuthenticated()) {
	            var p = auth.getPrincipal();
	            if (p instanceof AdminUserVO) return ((AdminUserVO) p).getAdminId();
	            if (p instanceof org.springframework.security.core.userdetails.UserDetails)
	                return ((org.springframework.security.core.userdetails.UserDetails)p).getUsername();
	            if (p instanceof String) return (String)p;
	        }
	    } catch (Throwable ignored) {}
	    return null;
	}

	/** 로그인 사용자 이름 찾기: 실제 세션 키 사용 */
	private String resolveLoginAdminName(HttpSession session) {
	    // 1. loginAdmin에서 name 확인 (실제 사용되는 키)
	    Object la = session.getAttribute("loginAdmin");
	    if (la instanceof AdminUserVO) {
	        AdminUserVO loginAdmin = (AdminUserVO) la;
	        if (loginAdmin.getName() != null && !loginAdmin.getName().trim().isEmpty()) {
	            return loginAdmin.getName();
	        }
	        // name이 없으면 adminId 반환
	        return loginAdmin.getAdminId();
	    }

	    // 2. 기존 키들도 확인 (호환성)
	    Object au = session.getAttribute("adminUser");
	    if (au instanceof AdminUserVO) {
	        AdminUserVO adminUser = (AdminUserVO) au;
	        if (adminUser.getName() != null && !adminUser.getName().trim().isEmpty()) {
	            return adminUser.getName();
	        }
	        return adminUser.getAdminId();
	    }

	    Object lu = session.getAttribute("loginUser");
	    if (lu instanceof AdminUserVO) {
	        AdminUserVO loginUser = (AdminUserVO) lu;
	        if (loginUser.getName() != null && !loginUser.getName().trim().isEmpty()) {
	            return loginUser.getName();
	        }
	        return loginUser.getAdminId();
	    }

	    // 3. Spring Security에서 확인
	    try {
	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	        if (auth != null && auth.isAuthenticated()) {
	            Object principal = auth.getPrincipal();
	            if (principal instanceof AdminUserVO) {
	                AdminUserVO adminUser = (AdminUserVO) principal;
	                if (adminUser.getName() != null && !adminUser.getName().trim().isEmpty()) {
	                    return adminUser.getName();
	                }
	                return adminUser.getAdminId();
	            }
	            if (principal instanceof UserDetails) {
	                return ((UserDetails) principal).getUsername();
	            }
	            if (principal instanceof String) {
	                return (String) principal;
	            }
	        }
	    } catch (Throwable ignored) {}
	    
	    // 4. loginId라도 반환
	    String loginId = resolveLoginAdminId(session);
	    return loginId != null ? loginId : "Unknown";
	}

	// 자재 발주 등록 페이지 이동 (GET)
	@GetMapping("/register")
	public String registerForm(Model model, HttpSession session) throws Exception {
	    logger.debug("GET /material/order/register → 발주 등록 폼 이동");
	    
	    String loginId = resolveLoginAdminId(session);
	    String loginName = resolveLoginAdminName(session);
	    
	    logger.info("최종 loginId: {}, loginName: {}", loginId, loginName);
	    
	    model.addAttribute("loginId", loginId == null ? "" : loginId);
	    model.addAttribute("loginName", loginName == null ? "" : loginName);

	    List<SupplierVO> supplierList = supplierService.getAllSuppliers();
	    List<MaterialVO> materialList = materialService.getAllMaterials();

	    model.addAttribute("supplierList", supplierList);
	    model.addAttribute("materialList", materialList);
	    model.addAttribute("menu", "material");
	    return "material/order/register";
	}

	// 자재 발주 등록 처리 (POST)
	@PostMapping("/register")
	public String registerOrder(@ModelAttribute MaterialOrderDTO orderDTO,
								Model model,
								HttpSession session,RedirectAttributes rttr) throws Exception {
		
		logger.info("registerOrder 컨트롤러 진입");
		logger.debug("등록된 발주 데이터: " + orderDTO);

	    MaterialOrderVO order = orderDTO.getOrder();
	    List<MaterialOrderItemVO> itemList = orderDTO.getOrderItems();
	    String workOrderId = order.getWorkOrderId();
	    
	    // ★ 로그인 ID 확보(다 실패하면 로그 찍고 에러)
	    String loginId = resolveLoginAdminId(session);
	    if (loginId == null || loginId.isBlank()) {
	        logLoginTrace(session); // 어디가 비었는지 기록
	        throw new IllegalStateException("로그인 세션이 만료되었습니다. 다시 로그인 해 주세요.");
	    }

	    // ★ 담당자는 무조건 로그인 사용자로
	    order.setHandledBy(loginId);

	    
	    // 필수값 검증 및 기본값 설정
	    if (order.getOrderStatus() == null || order.getOrderStatus().isEmpty()) {
	        order.setOrderStatus("초안");
	    }
	    
	    // 발주일이 없으면 현재 날짜로 설정
	    if (order.getOrderDate() == null) {
	        order.setOrderDate(new Date());
	    }
	    
	    // 빈 항목 제거 및 검증
	    if (itemList != null) {
	        itemList.removeIf(item -> 
	            item.getMaterialId() == null || 
	            item.getMaterialId().trim().isEmpty() || 
	            item.getOrderQuantity() <= 0
	        );
	    }
	    
	    if (itemList == null || itemList.isEmpty()) {
	        throw new IllegalArgumentException("발주 항목이 없습니다.");
	    }
	    
		/* 🔹 여기서 work_order_id 세팅 */
	    if (workOrderId != null && !workOrderId.isEmpty()) {
	        for (MaterialOrderItemVO item : itemList) {
	            item.setWorkOrderId(workOrderId);
	        }
	    }
	    
	    // 총금액 계산 (int 기반)
	    for (MaterialOrderItemVO item : itemList) {
	        try {
	            int unitPrice = item.getUnitPrice();
	            int orderQuantity = item.getOrderQuantity();

	            if (unitPrice > 0 && orderQuantity > 0) {
	                int total = unitPrice * orderQuantity;
	                item.setTotalPrice(total);
	            } else {
	                item.setTotalPrice(0);
	            }
	        } catch (Exception e) {
	            logger.error("총금액 계산 오류: " + e.getMessage(), e);
	            item.setTotalPrice(0);
	        }
	    }
	    
	    try {
	    	// 1) 신규 발주 등록
	        mOrderService.insertOrder(orderDTO); // (납기일 검증 포함)

	        // ★★ 2) 등록 직후 '발주 승인요청' 자동 발송 ★★
	        //    - insert 시 생성된 order_id가 VO에 설정되어 있어야 합니다
	        //    - MyBatis: useGeneratedKeys="true" keyProperty="orderId" 확인
	        String createdOrderId = orderDTO.getOrder().getOrderId();
	        if (createdOrderId != null && !createdOrderId.isEmpty()) {
	            mOrderService.sendApprovalRequest(createdOrderId);
	            logger.info("발주 승인요청 메일 발송 완료. orderId=" + createdOrderId);

	            // ✅ FlashAttribute에 성공 메시지 저장
	            rttr.addFlashAttribute("mailMsg", "해당 협력사에 승인요청 메일이 전송되었습니다.");
	        } else {
	            logger.warn("orderId 없음 → 승인요청 메일은 발송되지 않음");
	        }
	        
	    } catch (IllegalArgumentException e) {
	        model.addAttribute("error", e.getMessage());
	        setRegisterPageData(model);
	        model.addAttribute("orderDTO", orderDTO);
	        return "material/order/register";
	    }catch (Exception e) {
	        // 승인요청 메일 발송 실패 등은 등록 자체는 성공했으므로 로그만 남기고 리스트로 이동
	        logger.error("발주 승인요청 처리 중 오류: " + e.getMessage(), e);
	    }

	    return "redirect:/material/order/list";
	}
	
	
	private void logLoginTrace(HttpSession session) {
		
	}


	/**
	 * 자재명으로 거래처 검색 (Ajax)
	 */
	@GetMapping("/search-suppliers")
	@ResponseBody
	public List<SupplierItemDTO> searchSuppliersByMaterial(@RequestParam("keyword") String keyword) {
	    return mOrderService.searchSuppliersByMaterial(keyword);
	}

	
	/**
	 * 거래처 ID로 공급 자재 목록 조회 (Ajax)
	 * 예: /material/order/supplier-items?supplierId=SUP-20250710-002
	 */
	@GetMapping("/supplier-items")
	@ResponseBody
	public List<MaterialVO> getSupplierItems(@RequestParam("supplierId") String supplierId,
											 @RequestParam(value = "keyword", required = false) String keyword)
											 throws Exception {
	    return supplierService.getMaterialsBySupplier(supplierId, keyword);
	}

	
	/**
	 * 발주 초안에서 요청
	 */
	@PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<?> submit(@RequestParam("orderId") String orderId) {
        try {
        	mOrderService.submitOrderRequest(orderId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "orderId", orderId,
                "newStatus", "요청",
                "message", "발주요청으로 전환되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
	
	
	/**
	 * 발주 상세
	 */
	@GetMapping("/detail")
	@ResponseBody
	public Map<String,Object> detail(@RequestParam String orderId) throws Exception {
	    return Map.of(
	        "header", mOrderService.getOrderHeader(orderId),
	        "items",  mOrderService.getOrderItems(orderId)
	    );
	}
	
	//협력사 승인 요청 이메일
	@PostMapping("/request-approval")
	@ResponseBody
	public String requestOrderApproval(@RequestParam("orderId") String orderId) {
	    try {
	        mOrderService.sendApprovalRequest(orderId);  
	        return "success";
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "fail";
	    }
	}
	
	
	/**
	 * 자재별 포장 단위(pack_qty) 조회 - MOQ 계산용
	 */
	@GetMapping(value = "/supplier-pack-qty", produces = "application/json;charset=UTF-8")
	@ResponseBody
	public Map<String, Object> getSupplierPackQty(@RequestParam String materialId) {
	    try {
	        Double v = supplierService.getPackQtyByMaterial(materialId); // null 가능
	        int packQty = (v == null) ? 1 : Math.max(1, v.intValue());   // 음수/0 방어

	        return Map.of(
	            "success", true,
	            "materialId", materialId,
	            "packQty", packQty
	        );
	    } catch (Exception e) {
	        logger.error("pack_qty 조회 실패: " + materialId, e);
	        return Map.of(
	            "success", false,
	            "materialId", materialId,
	            "packQty", 1,
	            "message", e.getMessage()
	        );
	    }
	}



	

} 