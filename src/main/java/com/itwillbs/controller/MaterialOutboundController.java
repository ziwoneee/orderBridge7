package com.itwillbs.controller;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.itwillbs.domain.MaterialOutboundVO;
import com.itwillbs.dto.MaterialOutboundSummaryDTO;
import com.itwillbs.service.MaterialOutboundService;

/*
 *  자재 출고 관리 컨트롤러
 *  - 출고 목록 조회 및 처리 관련 요청 처리
 */
@Controller
@RequestMapping("/material/outbound/*")
public class MaterialOutboundController {
	
	
	@Inject
	private MaterialOutboundService moService;
	
	//http://localhost:8088/material/outbound/list
	// 출고 목록 조회 요청
	@GetMapping("list")
	public String getOutboundList(Model model) throws Exception {
		
		// 1. 서비스 호출하여 출고 목록 가져오기
		List<MaterialOutboundSummaryDTO> outboundList = moService.getOutboundList();

		// 2. 모델에 데이터 저장
		model.addAttribute("outList", outboundList);
		
		model.addAttribute("menu", "material");
		
		MaterialOutboundSummaryDTO dummy = new MaterialOutboundSummaryDTO();
		dummy.setOutboundId("OUT-RM-20250725-001");
		dummy.setWorkOrderNo("WO-20250725-001");
		dummy.setMaterialName("돼지국밥 고기");
		dummy.setSupplierName("한우축산");
		dummy.setRequiredQty(100);
		dummy.setStockStatus("충분");
		dummy.setStatus("미출고");
		dummy.setHandledBy("홍길동");

		outboundList.add(dummy);

		
		return "/material/out/list"; // JSP로 이동
	}

	
	
	
	
	
	
	
	
	
	
	
	
} // MaterialOutboundController 끝
