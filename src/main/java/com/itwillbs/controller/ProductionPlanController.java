package com.itwillbs.controller;

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
    	
        // 확정된 상태(CONFIRMED) 수주 목록 조회 (페이징 적용)
        List<ClientOrderVO> confirmedOrders = productionPlanService.getConfirmedOrderList(cri);
        logger.info("등록 페이지 진입: CONFIRMED 수주 수 = " + confirmedOrders.size());
        
        model.addAttribute("confirmedOrders", confirmedOrders); // JSP에서 사용
        model.addAttribute("cri", cri); // 페이징 JSP에서 필요 
        
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
    

    //생산계획 등록 컨트롤러 ( 등록된 제품 중 체크된 항목만 등록 처리)
    @PostMapping("/plan/register-form")
    @ResponseBody
    public String registerPlan(@RequestBody List<ProductionPlanDTO> planList) {
        logger.info("생산 계획 등록 요청 수신 - 총 {}건", planList.size());

        try {
            // 복수 등록 메서드 한 번에 처리
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

        return productionPlanService.isDuplicatePlan(dto);  // true면 이미 등록됨
    }
    
    // 생산 목록 화면 
    @GetMapping("/plan/list")
    public String planList(Model model, SearchCriteria cri) {
        logger.info("[GET] /plan/list - 생산 계획 목록 페이지 진입");

        // 1. 서비스에서 생산 계획 목록을 조회 (검색 조건 + 페이징 적용)
        List<ProductionPlanDTO> planList = productionPlanService.getPlanList(cri);

        // 2. 전체 건수 조회 (페이징 처리용)
        int totalCount = productionPlanService.getPlanListCount(cri);

        // 3. 모델에 데이터 바인딩
        model.addAttribute("planList", planList);
        model.addAttribute("cri", cri);
        model.addAttribute("totalCount", totalCount);

        // 4. 목록 화면으로 이동 (jsp 경로)
        return "production/plan-list";  // /WEB-INF/views/production/plan-list.jsp
    }
    
}
