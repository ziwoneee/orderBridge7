package com.itwillbs.controller;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itwillbs.domain.ClientOrderVO;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.ProductionPlanDTO;
import com.itwillbs.service.ProductionPlanService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ProductionPlanController {

	private static final Logger logger = LoggerFactory.getLogger(ProductionPlanController.class);

	private final ProductionPlanService productionPlanService;

	// 생산 계획 등록 페이지 진입 시 CONFIRMED 수주 목록 전달
	@GetMapping("/plan/register")
	public String showPlanRegisterPage(SearchCriteria cri, Model model) {

	    // 확정된 상태(CONFIRMED) 수주 총 개수 조회
	    int totalCount = productionPlanService.getConfirmedOrderTotalCount(cri);
	    cri.setTotalCount(totalCount); // 페이징 처리용

	    //  PageMaker 생성
	    PageMaker pageMaker = new PageMaker(cri, totalCount);

	    // 확정된 상태(CONFIRMED) 수주 목록 조회 (페이징 적용)
	    List<ClientOrderVO> confirmedOrders = productionPlanService.getConfirmedOrderList(cri);
	    logger.info("등록 페이지 진입: CONFIRMED 수주 수 = " + confirmedOrders.size());

	    // 모델에 추가
	    model.addAttribute("confirmedOrders", confirmedOrders); // JSP에서 목록 출력
	    model.addAttribute("cri", cri);                         // 검색/정렬 등
	    model.addAttribute("pageMaker", pageMaker);             //  블럭 페이징용 추가

	    return "production/plan-register";
	}

	// 수주 상세 조회 (AJAX 요청 처리)
	@GetMapping("/plan/select")
	public String selectOrderDetail(@RequestParam("clOrderId") String clOrderId, Model model) {
		logger.info("[GET] /plan/select - 수주 상세 조회 clOrderId: {}", clOrderId);

		// 재고 계산 포함된 상세 정보 조회
		List<ProductionPlanDTO> details = productionPlanService.getOrderDetailItemsForPlan(clOrderId);

		model.addAttribute("detailList", details);

		// 조각 화면(JSP) 리턴
		return "production/plan-select-result";
	}

	// 생산계획 등록 컨트롤러 ( 등록된 제품 중 체크된 항목만 등록 처리)
	@PostMapping("/plan/register-form")
	@ResponseBody
	public String registerPlan(@RequestBody List<ProductionPlanDTO> planList) {
		logger.info("생산 계획 등록 요청 수신 - 총 {}건", planList.size());

		try {
			// 서비스에서 등록전, 제품 ID 기준으로 자동 라인 ID 설정 포함
			productionPlanService.registerPlans(planList);

			return "success";

		} catch (Exception e) {
			logger.error("생산 계획 등록 중 오류 발생", e);
			return "fail";
		}
	}

	// [POST] 중복 등록 체크 → true: 중복 / false: 등록 가능
	@PostMapping("/plan/check-duplicate")
	@ResponseBody
	public boolean checkDuplicatePlan(@RequestBody ProductionPlanDTO dto) {
		logger.info(" 중복 체크 요청: " + dto.getClOrderId() + " / " + dto.getProductId());

		return productionPlanService.isDuplicatePlan(dto); // true면 이미 등록됨
	}

	// 생산 목록 화면
	@GetMapping("/plan/list")
	public String planList(Model model, SearchCriteria cri) {
	    logger.info("[GET] /plan/list - 생산 계획 목록 페이지 진입");

	    // 1. 정렬 컬럼 화이트리스트 검사
	    List<String> allowedSortColumns = Arrays.asList(
	        "plan_id", "product_name", "priority", "status", "planned_qty", "due_date", "created_at"
	    );
	    if (!allowedSortColumns.contains(cri.getSortColumn())) {
	        cri.setSortColumn("created_at");
	        cri.setSortOrder("desc");
	    }

	    // 2. 목록 조회
	    List<ProductionPlanDTO> planList = productionPlanService.getPlanList(cri);

	    // 3. 전체 건수
	    int totalCount = productionPlanService.getPlanListCount(cri);

	    // 4. 페이지메이커 생성
	    PageMaker pageMaker = new PageMaker(cri, totalCount);

	    // 5. 모델 바인딩
	    model.addAttribute("planList", planList);
	    model.addAttribute("cri", cri);
	    model.addAttribute("pageMaker", pageMaker);

	    return "production/plan-list";
	}
	
	// 계획 상세 화면 
	@GetMapping("/plan/detail")
	public String getPlanDetail(@RequestParam("planId") String planId, Model model) {
	    logger.info("상세 조회 요청 planId: " + planId);

	    ProductionPlanDTO plan = productionPlanService.getPlanDetail(planId);
	    model.addAttribute("plan", plan);
	    return "production/plan-detail";  // 이 JSP 경로 반드시 존재해야 함
	}
	
	// 계획 선택 확정 컨트롤러 
	@PostMapping("/plan/confirm-bulk")
	@ResponseBody
	public String confirmSelectedPlans(@RequestBody List<String> planIds) {
	    // [1] 선택된 생산계획 ID 리스트 확인
	    logger.info("✅ [선택 확정] 컨트롤러 호출 - planIds: " + planIds);

	    // [2] 서비스에 확정 처리 요청
	    productionPlanService.confirmPlans(planIds);

	    // [3] 성공 응답 반환
	    return "success";
	}

}
