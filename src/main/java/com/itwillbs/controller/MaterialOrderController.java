package com.itwillbs.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.CustomDateEditor;
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
	    
	    return "material/order/list";
	}

	
	// 자재 발주 등록 페이지 이동 (GET)
	@GetMapping("/register")
	public String registerForm(Model model) throws Exception {
	    logger.debug("GET /material/order/register → 발주 등록 폼 이동");

	    List<SupplierVO> supplierList = supplierService.getAllSuppliers();
	    List<MaterialVO> materialList = materialService.getAllMaterials();

	    model.addAttribute("supplierList", supplierList);
	    model.addAttribute("materialList", materialList);
	    model.addAttribute("menu", "material");
	    return "material/order/register";
	}
	

	// 자재 발주 등록 처리 (POST)
	@PostMapping("/register")
	public String registerOrder(@ModelAttribute MaterialOrderDTO orderDTO, Model model) throws Exception {
		logger.info("registerOrder 컨트롤러 진입");
		logger.debug("등록된 발주 데이터: " + orderDTO);

	    MaterialOrderVO order = orderDTO.getOrder();
	    List<MaterialOrderItemVO> itemList = orderDTO.getOrderItems();
	    
	    // 필수값 검증 및 기본값 설정
	    if (order.getOrderStatus() == null || order.getOrderStatus().isEmpty()) {
	        order.setOrderStatus("요청");
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
	        mOrderService.insertOrder(orderDTO); // 납기일 유효성 검사 포함
	    } catch (IllegalArgumentException e) {
	        model.addAttribute("error", e.getMessage());
	        setRegisterPageData(model);
	        model.addAttribute("orderDTO", orderDTO);
	        return "material/order/register";
	    }
	    
	    return "redirect:/material/order/list";
	}
	
	
	/**
	 * 자재명으로 거래처 검색 (Ajax)
	 */
	@GetMapping("/search-suppliers")
	@ResponseBody
	public List<SupplierItemDTO> searchSuppliersByMaterial(@RequestParam("keyword") String keyword) {
	    return mOrderService.searchSuppliersByMaterial(keyword);
	}

	

} 