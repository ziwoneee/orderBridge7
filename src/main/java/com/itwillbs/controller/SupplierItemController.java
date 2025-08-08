package com.itwillbs.controller;

import java.util.List;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itwillbs.domain.MaterialVO;
import com.itwillbs.domain.SupplierItemVO;
import com.itwillbs.domain.SupplierVO;
import com.itwillbs.service.MaterialService;
import com.itwillbs.service.SupplierItemService;
import com.itwillbs.service.SupplierService;

/**
 * 공급 품목 컨트롤러
 * - Ajax 기반 공급 품목 조회 등 처리
 */
@Controller
@RequestMapping("/supplierItem/*")
public class SupplierItemController {
	
	@Inject
	private SupplierService sService;
	
	@Inject
    private SupplierItemService siService;
	
	@Inject
	private MaterialService materialService;

    /**
     * 특정 거래처의 공급 품목 목록 조회 (Ajax)
     * @param supplierId 거래처 ID
     * @return 공급 품목 리스트 (JSON)
     */
	// http://localhost:8088/supplierItem/items?supplierId=SUP-20250710-001
    @GetMapping("items")
    public String showSupplierItems(@RequestParam("supplierId") String supplierId, Model model) throws Exception {
    	
		SupplierVO supplier = sService.getSupplierById(supplierId);
		List<MaterialVO> materialList = materialService.getAllMaterials();
    	
		model.addAttribute("supplier", supplier);
        model.addAttribute("supplierId", supplierId);
        model.addAttribute("materialList", materialList);
        model.addAttribute("menu", "basic");
        
        return "supplier/items"; 
    }


    
    /**
     * 공급 품목 페이지 컨트롤러
     * - 거래처 ID를 기준으로 협력사 정보 + 공급 품목 페이지 이동
     */
    @GetMapping("list")
    @ResponseBody
    public List<SupplierItemVO> getSupplierItemList(@RequestParam("supplierId") String supplierId, Model model) throws Exception {
    	
    	System.out.println("✅ Ajax 요청 도착 - supplierId: " + supplierId);
    	List<SupplierItemVO> itemList = siService.getItemsBySupplier(supplierId);
    	
        
        // 2. model에 담기
        model.addAttribute("supplierId", supplierId); // JS에서도 쓰기 위해 따로 넘김
    	
    	System.out.println("✅ 조회된 품목 수: " + itemList.size());
    	for (SupplierItemVO item : itemList) {
            System.out.println("▶ 자재명: " + item.getMaterialName() + ", 유형: " + item.getMaterialType());
        }
    	
    	model.addAttribute("menu", "basic");

    	return itemList;
    }

    
    /**
     * 공급 품목 등록 처리
     * - POST 요청으로 등록 폼 데이터를 전달받아 DB에 저장
     */
    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<String> registerItem(SupplierItemVO item) {
        try {
            siService.registerItem(item);
            return ResponseEntity.ok("등록 성공");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("등록 실패: " + e.getMessage());
        }
    }
    
    
    
    /**
     * 공급 품목 수정
     */
    @PostMapping("/update")
    @ResponseBody
    public String updateItem(SupplierItemVO vo) throws Exception {
    	siService.updateItem(vo);
        return "success";
    }

    
    
    
    
    
    
    
    
} // SupplierItemController 끝

