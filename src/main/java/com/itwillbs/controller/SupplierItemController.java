package com.itwillbs.controller;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    @GetMapping("list")
    @ResponseBody
    public List<SupplierItemVO> getSuppliedItems(@RequestParam String supplierId) {
    	
        return siService.getSuppliedItemsBySupplierId(supplierId);
    }


    
    
    
    
    
    
    
    
    
} // SupplierItemController 끝
