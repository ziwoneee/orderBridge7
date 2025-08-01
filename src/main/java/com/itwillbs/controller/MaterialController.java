package com.itwillbs.controller;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.itwillbs.domain.MaterialVO;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
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
	@GetMapping("/list")
	public String materialList(SearchCriteria cri, Model model) throws Exception {
		
		// [1] 정렬 기준 기본값 설정
	    if (cri.getSortColumn() == null || cri.getSortColumn().isEmpty()) {
	        cri.setSortColumn("material_id"); // 기본 정렬 컬럼
	        cri.setSortOrder("asc");          // 기본 정렬 방향
	    }

	    // [2] 페이징 값 유효성 체크 (예외 방지)
	    if (cri.getPage() <= 0) cri.setPage(1);
	    if (cri.getPerPageNum() <= 0) cri.setPerPageNum(10);

	    // [3] 전체 자재 수 조회
	    int totalCount = mService.getMaterialCount(cri);
	    cri.setTotalCount(totalCount); // ← JSP에서도 cri.totalCount로 사용 가능

	    // [4] 자재 목록 조회 (검색 + 정렬 + 페이징 포함)
	    List<MaterialVO> materialList = mService.getMaterialList(cri);

	    // [5] PageMaker 생성 (startPage, endPage 계산용)
	    PageMaker pageMaker = new PageMaker(cri, totalCount);

	    // [6] View에 데이터 전달
	    model.addAttribute("materialList", materialList);
	    model.addAttribute("pageMaker", pageMaker);
	    model.addAttribute("cri", cri);
	    model.addAttribute("menu", "material"); // 상단 메뉴 활성화용 (선택)

	    return "master/materialList"; // 반환할 JSP 경로
	}
	
	
	// 0. 자재 등록 페이지 이동 (GET)
	@GetMapping("/register")
	public String registerForm(Model model) throws Exception {
	    // 자재ID 자동 생성
	    String nextId = createNextId(mService.getMaxMaterialId());
	    MaterialVO vo = new MaterialVO();
	    vo.setMaterialId(nextId); // 생성된 ID를 미리 세팅 (선택)

	    model.addAttribute("material", vo);         // VO 전달
	    model.addAttribute("menu", "material");     // 상단 메뉴 활성화용
	    return "master/materialRegister";           // JSP 위치
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
		return "redirect:/material/list";
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
	
	
	/**
	 * 자재 수정 폼으로 이동
	 * GET /material/edit?materialId=RM-0001
	 */
	@GetMapping("/edit")
	public String editForm(@RequestParam("materialId") String materialId, Model model) throws Exception {
	    MaterialVO vo = mService.getMaterial(materialId); // 기존 정보 조회
	    model.addAttribute("material", vo); // VO 전달
	    model.addAttribute("menu", "material"); // 메뉴 활성화용
	    return "master/materialRegister"; // 같은 등록 JSP 재사용
	}

	
	
	
} // MaterialController 끝


