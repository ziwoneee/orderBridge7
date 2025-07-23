package com.itwillbs.controller;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.itwillbs.domain.MaterialVO;
import com.itwillbs.dto.PagingDTO;
import com.itwillbs.service.MaterialService;

@Controller
@RequestMapping("/material/*")
public class MaterialController {
	
	// mylog
	private static final Logger logger = LoggerFactory.getLogger(MaterialController.class);

	// 서비스 객체를 주입
	@Inject
	private MaterialService mService;
	
	
	// http://localhost:8088/material/list
	// 1. 자재 목록 조회
	/**
	 * 자재 목록 조회 요청 처리
	 * GET 방식 -> /material/list
	 * 
	 */
	@RequestMapping(value="/list", method = RequestMethod.GET)
	public String materialList(@RequestParam(value = "page", defaultValue = "1") int page, Model model) throws Exception {
		logger.info(" materialList() 실행 ");
		logger.info(" /views/material/list.jsp 페이지 이동 ");
		
	    // 1. 전체 데이터 수 조회
	    int totalCount = mService.getMaterialCount();

	    // 2. 페이징 객체 생성
	    PagingDTO paging = new PagingDTO(page, 10, totalCount);

	    // 3. 페이징된 자재 목록 조회
	    List<MaterialVO> materialList = mService.getMaterialListPage(paging);

	    // 4. 데이터 바인딩
	    model.addAttribute("materialList", materialList);
	    model.addAttribute("paging", paging);
		
		// 5) 뷰 페이지로 이동
		return "master/materialList";
		
	}
	

	
	// 2. 자재 신규 등록
	/**
	 * 자재 등록/수정 요청 처리
	 * POST 방식 -> /material/save
	 * 신규등록과 수정 통합 처리 (id 여부로 구분)
	 */
	@RequestMapping(value="/save", method = RequestMethod.POST)
	public String saveMaterial(@ModelAttribute MaterialVO vo) throws Exception {
		logger.info(" saveMaterial() 실행 ");
		
		// 자재ID가 기존에 존재하면 -> 수정
		if(mService.checkMaterial(vo.getMaterialId())) {
			logger.info("기존 자재 -> 수정 처리");
			mService.updateMaterial(vo);
		} else {
			logger.info("신규 자재 -> 등록 처리");
			mService.insertMaterial(vo);
		}
		
		// 목록 페이지로 리다이렉트
		return "redirect:/master/materialList";
	}
	
	
	// 3. 자재ID 자동 생성
	private String createNextId(String maxId) {
	    String prefix = "RM-";
	    int nextNum = 1;

	    if (maxId != null) {
	        String numPart = maxId.substring(3); // RM-0001 -> 0001
	        nextNum = Integer.parseInt(numPart) + 1;
	    }

	    return String.format("RM-%04d", nextNum);
	}

	
	
	
} // MaterialController 끝




















