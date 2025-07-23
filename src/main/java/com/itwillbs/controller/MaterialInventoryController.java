package com.itwillbs.controller;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.itwillbs.domain.MaterialInventoryVO;
import com.itwillbs.service.MaterialInventoryService;

@Controller
@RequestMapping("/material/inventory/*") // 자재관리 > 재고현황
public class MaterialInventoryController {
	
	// mylog
	private static final Logger logger = LoggerFactory.getLogger(MaterialInventoryController.class);

	
	// 서비스 객체 주입
	@Inject
	private MaterialInventoryService miService;
	
	
	/**
     * 자재 재고현황 리스트 조회
     * @param materialId 자재 ID (선택 필터)
     * @param materialName 자재명 (선택 필터)
     * @param storageLocation 보관위치 (선택 필터)
     * @param model View에 전달할 데이터 모델
     * @return JSP 페이지 경로
     */
	
	// http://localhost:8088/material/inventory/list
	// 자재 재고현황 리스트 조회
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String getInventoryList(@RequestParam(required = false) String materialId,
            					   @RequestParam(required = false) String materialName,
            					   @RequestParam(required = false) String materialType,
            					   @RequestParam(required = false) String sortColumn,    // 정렬 컬럼
                                   @RequestParam(required = false) String sortDirection, // 정렬 방향 (asc/desc)
            					   Model model) throws Exception {
		
		logger.info(" getInventoryList() 호출 ");
		logger.info("정렬 파라미터: {}, {}", sortColumn, sortDirection);
		
		// 서비스 호출 → 자재 재고 목록 조회 (정렬 조건 포함)
		List<MaterialInventoryVO> inventoryList 
					= miService.getInventoryList(materialId, materialName, materialType, sortColumn, sortDirection);
        
		// 모델에 담기
		model.addAttribute("inventoryList", inventoryList);
		model.addAttribute("sortColumn", sortColumn);         // 현재 정렬 컬럼
	    model.addAttribute("sortDirection", sortDirection);   // 현재 정렬 방향
		
		// 현재 시간 now 객체로 전달
	    model.addAttribute("now", new Date());
	    
		
		// 뷰페이지로 이동
		return "material/inventoryList";
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
} // MaterialInventoryController 끝







