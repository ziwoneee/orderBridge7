package com.itwillbs.controller;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.itwillbs.domain.SupplierItemVO;
import com.itwillbs.service.SupplierItemService;

/**
 * 공급 품목 컨트롤러
 * - Ajax 기반 공급 품목 조회 등 처리
 */
@Controller
@RequestMapping("/supplierItem/*")
public class SupplierItemController {
	
	@Inject
    private SupplierItemService siService;

    /**
     * 특정 거래처의 공급 품목 목록 조회 (Ajax)
     * @param supplierId 거래처 ID
     * @return 공급 품목 리스트 (JSON)
     */
	// http://localhost:8088/supplierItem/items?supplierId=SUP-20250710-001
    @GetMapping("items")
    public String showSupplierItems(@RequestParam String supplierId, Model model) throws Exception {
    	
        // 1. 거래처명 조회용 (선택사항)
        // 2. VO 목록 조회 (화면에서 Ajax로 조회해도 되므로 생략 가능)
        model.addAttribute("supplierId", supplierId);
        
        return "supplier/items"; // → /WEB-INF/views/supplier/items.jsp
    }


    
    
    
    
    
    
    
    
    
} // SupplierItemController 끝
