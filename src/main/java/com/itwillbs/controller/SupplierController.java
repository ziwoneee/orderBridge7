package com.itwillbs.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.SupplierVO;
import com.itwillbs.service.SupplierService;

@Controller
@RequestMapping("/supplier/*")
public class SupplierController {

	// mylog
	private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);

	// 서비스 객체 주입
	@Inject
	private SupplierService sService;

	// http://localhost:8088/supplier/list
	// 협력사 목록 페이지
	@GetMapping("list")
	public String listSuppliers(SearchCriteria cri, Model model) throws Exception {

		// 전체 건수 조회
		int totalCount = sService.getSupplierCount(cri);

		// totalCount 세팅 (SearchCriteria 내부 사용 시 필요)
		cri.setTotalCount(totalCount);

		// PageMaker 생성
		PageMaker pageMaker = new PageMaker(cri, totalCount);

		// 실제 데이터 조회
		List<SupplierVO> supplierList = sService.getSupplierList(cri);

		// Model 전달
		model.addAttribute("supplierList", supplierList);
		model.addAttribute("pageMaker", pageMaker);
		model.addAttribute("cri", cri); // 검색 조건 유지용

		// 결과를 협력사 목록 JSP로 전달
		return "supplier/list";
	}

	// 협력사 상세 페이지 (탭 포함된 전체 화면)
	@GetMapping("view")
	public String viewSupplier(@RequestParam("supplierId") String supplierId, Model model) throws Exception {

		logger.info("viewSupplier 호출 거래처 상세 페이지 진입: " + supplierId);

		// 1. 거래처 정보 조회
		SupplierVO supplier = sService.getSupplierById(supplierId);
		model.addAttribute("supplierVO", supplier);

		// 2. (추후) 공급 품목 리스트도 여기에 추가 예정

		// 3. 상세 JSP로 이동
		return "supplier/view"; // /WEB-INF/views/supplier/view.jsp
	}

	// 협력사 등록 폼 이동
	@GetMapping("register")
	public String registerForm() {

		logger.info(" registerForm() 호출 ");

		return "supplier/register";
	}

	// 협력사 등록 처리
	@PostMapping("register")
	public String registerSupplier(SupplierVO vo, RedirectAttributes rttr, Model model) {

		try {

			// 1) insertSupplier() 호출 시 ID 자동 생성 포함 + DB에 저장
			sService.insertSupplier(vo);

			// 2) 성공 시, 등록 완료 메시지와 함께 목록 페이지로 리다이렉트
			rttr.addFlashAttribute("msg", "협력사 신규 등록 완료!");
			return "redirect:/supplier/list";

		} catch (IllegalArgumentException e) {
			// 3) 중복 사업자번호 예외 발생 시 처리
			// - 사용자가 입력했던 정보를 다시 화면에 보여주기 위해 model에 담음
			// - forward 방식으로 register.jsp로 이동 (값 유지됨)
			model.addAttribute("errorMsg", e.getMessage());
			model.addAttribute("supplierVO", vo); // 입력 값 복원
			return "supplier/register"; // forward 방식 (리다이렉트 아님)

		} catch (Exception e) {
			// 4) 기타 예외 발생 시 처리 (DB 오류 등)
			model.addAttribute("errorMsg", "등록 중 오류 발생");
			model.addAttribute("supplierVO", vo); // 입력 값 복원
			return "supplier/register"; // forward 방식
		}

	}

	// 사업자번호 중복확인용
	@GetMapping("/checkBizNo")
	@ResponseBody
	public Map<String, Boolean> checkBusinessNumber(@RequestParam("businessNumber") String businessNumber)
			throws Exception {
		boolean exists = sService.isBusinessNumberExists(businessNumber);
		return Collections.singletonMap("exists", exists);
	}

	// 협력사 정보 수정 기능
	/**
	 * 수정 폼 페이지 이동 - supplier_id로 기존 협력사 정보를 조회하여 edit.jsp로 전달
	 */
	@GetMapping("edit")
	public String editSupplier(@RequestParam(value = "supplierId", required = false) String supplierId, Model model)
			throws Exception {
		SupplierVO supplier = sService.getSupplierById(supplierId);
		model.addAttribute("supplierVO", supplier);
		return "supplier/edit"; // /WEB-INF/views/supplier/edit.jsp
	}

	/**
	 * 협력사 정보 수정 처리 - POST 방식으로 폼 데이터 받아서 DB 업데이트
	 */
	@PostMapping("edit")
	public String updateSupplier(SupplierVO supplier) throws Exception {
		sService.updateSupplier(supplier);
		return "redirect:/supplier/list"; // 수정 후 목록 페이지로 이동
	}

} // SupplierController 끝
