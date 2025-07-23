package com.itwillbs.controller;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.PostMapping;

import com.itwillbs.domain.ProductionResultVO;
import com.itwillbs.service.ProductionResultService;

public class ProductionResultController {
	
	@Inject
	private ProductionResultService productionResultService;

	//완제품 입고자동등록 (아름 시작)
	@PostMapping("/register")
	public String registerProductionResult(ProductionResultVO vo) {
	    productionResultService.insertResult(vo); // 저장 시 자동으로 입고도 처리됨
	    return "redirect:/production/result/list";
	}
	//완제품 입고자동등록 (아름 끝)
	
}
