package com.itwillbs.controller;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.itwillbs.domain.MaterialOrderItemVO;
import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.MaterialVO;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.SupplierVO;
import com.itwillbs.dto.MaterialOrderDTO;
import com.itwillbs.service.MaterialOrderService;
import com.itwillbs.service.MaterialService;
import com.itwillbs.service.SupplierService;

/**
 * 자재 발주 관리 - 발주 목록 조회 컨트롤러
 */
@Controller
@RequestMapping("/material/order")
public class MaterialOrderController {
	
	// mylog
	private static final Logger logger = LoggerFactory.getLogger(MaterialOrderController.class);

	// 서비스 객체 주입
	@Inject
	private MaterialOrderService mOrderService;
	
	@Inject
	private SupplierService supplierService;
	
	@Inject
	private MaterialService materialService;
	
	
	// 발주 목록 조회 (검색, 정렬, 페이징 포함)
	@GetMapping("/list")
	public String orderList(SearchCriteria cri, Model model) throws Exception {
		
		// 1. 정렬 컬럼이 없으면 기본값 세팅 (안 해주면 ORDER BY desc 에러 발생)
	    if (cri.getSortColumn() == null || cri.getSortColumn().isBlank()) {
	        cri.setSortColumn("order_date"); // 기본 정렬 컬럼: 발주일
	    }
	    
		// 2. 조건에 맞는 발주 목록 조회
        List<MaterialOrderVO> orderList = mOrderService.getOrderList(cri);

        // 3. 전체 건수 조회 (페이징용)
        int totalCount = mOrderService.getTotalCount(cri);
        PageMaker pageMaker = new PageMaker(cri, totalCount);


        // 4. 모델 등록
        model.addAttribute("orderList", orderList);
        model.addAttribute("pageMaker", pageMaker);
        model.addAttribute("cri", cri);
	    model.addAttribute("menu", "material");
	    
	    // JSP 뷰 경로 반환
	    return "material/order/list";
	}

	
	
	// 자재 발주 등록 페이지 이동 (GET)
	@GetMapping("/register")
	public String registerForm(Model model) throws Exception {
	    logger.debug("GET /material/order/register → 발주 등록 폼 이동");

	    // 등록 시 선택할 거래처/자재 목록 불러오기 (예: 드롭다운용)
	    List<SupplierVO> supplierList = supplierService.getAllSuppliers(); // 거래처
	    List<MaterialVO> materialList = materialService.getAllMaterials(); // 자재

	    model.addAttribute("supplierList", supplierList);
	    model.addAttribute("materialList", materialList);
	    model.addAttribute("menu", "material");
	    return "material/order/register";
	}

	// 자재 발주 등록 처리 (POST)
	@PostMapping("/register")
	public String registerOrder(@ModelAttribute MaterialOrderDTO orderDTO) throws Exception {
		logger.debug("등록된 발주 데이터: " + orderDTO);

	    // 기본 정보와 항목 리스트 가져오기
	    MaterialOrderVO order = orderDTO.getOrder();
	    List<MaterialOrderItemVO> itemList = orderDTO.getOrderItems();
	    
	    logger.debug("발주자: " + order.getCreatedBy()); // ❗여기서 NPE 가능
	    logger.debug("항목 수: " + (itemList != null ? itemList.size() : "null"));

	    logger.debug("발주자: " + order.getCreatedBy());
	    logger.debug("항목 수: " + (itemList != null ? itemList.size() : 0));

	    // 서비스 호출 (예시)
	    mOrderService.insertOrder(orderDTO);
	    
	    return "redirect:/material/order/list";
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
} // MaterialOrderController 끝
