package com.itwillbs.controller;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.WorkOrderDTO;
import com.itwillbs.dto.ProductionResultDTO; // ✅ 추가
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.ProductionResultVO;
import com.itwillbs.service.ProductionResultService;
import com.itwillbs.service.WorkOrderService;

/**
 * 생산 실적 컨트롤러
 * - /production/result 하위 URL 관리
 * - 목록 + 등록(완제품 자동입고 연동)
 */
@Controller
@RequestMapping("/production/result")
public class ProductionResultController {
    
    @Inject
    private ProductionResultService productionResultService;
    
    @Inject
    private WorkOrderService workOrderService;
    
    // ======================= 목록 =========================
    /**
     * 생산 실적 목록
     * - 검색/정렬/페이징은 SearchCriteria로 자동 바인딩
     * - Service에서 리스트/총건수 가져와 PageMaker 생성 후 JSP로 전달
     */
    @GetMapping("/list")
    public String list(@ModelAttribute SearchCriteria cri, Model model) {
        // 기본 정렬(프론트가 안 주면 최신 등록일 내림차순)
        if (cri.getSortColumn() == null || cri.getSortColumn().isEmpty()) {
            cri.setSortColumn("created_at"); // DB 컬럼명(snake_case)
            cri.setSortOrder("desc");
        }
        
        // 목록 + 총건수
        var list = productionResultService.getList(cri);
        int total = productionResultService.getTotalCount(cri);
        
        // PageMaker 생성
        PageMaker pageMaker = new PageMaker(cri, total);
        
        // 뷰로 전달
        model.addAttribute("list", list);
        model.addAttribute("pageMaker", pageMaker);
        model.addAttribute("cri", cri);
        
        // /WEB-INF/views/production/result/list.jsp
        return "production/result/list";
    }
    
    // ======================= 등록 폼 =========================
    /**
     * 생산 실적 등록 페이지 
     */
    @GetMapping("/form")
    public String form(Model model) {
        // 진행중/완료 상태의 작업지시만 조회
        List<WorkOrderDTO> workOrderList = workOrderService.getInProgressOrders();
        
        System.out.println("=== 디버깅 정보 ===");
        System.out.println("진행중/완료 작업지시 목록 크기: " + (workOrderList != null ? workOrderList.size() : 0));
        if (workOrderList != null) {
            for (WorkOrderDTO order : workOrderList) {
                System.out.println("작업지시: " + order.getOrderId() + ", 상태: " + order.getStatus() + ", 제품: " + order.getProductName());
            }
        }
        
        model.addAttribute("workOrderList", workOrderList);
        return "production/result/form";
    }
    
    // ======================= 상세 페이지 =========================
    /**
     * 생산 실적 상세 조회
     */
    @GetMapping("/detail")
    public String detail(@RequestParam("resultId") String resultId, Model model) {
        try {
            // ✅ Service에서 상세 정보 조회
            ProductionResultDTO result = productionResultService.getDetail(resultId);
            model.addAttribute("result", result);
            
            return "production/result/detail";
        } catch (IllegalArgumentException e) {
            // 존재하지 않는 실적 ID인 경우
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/production/result/list";
        } catch (Exception e) {
            // 기타 오류
            model.addAttribute("errorMessage", "상세 정보를 조회하는 중 오류가 발생했습니다.");
            return "redirect:/production/result/list";
        }
    }
    
    // =================완제품 입고자동등록 (아름)=========================
    /**
     * 생산 결과 등록
     * - 저장 시 Service에서 자동으로 입고도 처리
     * - 등록 후 목록으로 이동
     */
    @PostMapping("/register")
    public String registerProductionResult(ProductionResultVO vo, 
                                         RedirectAttributes redirectAttributes) {
        try {
            // ✅ 등록일시 설정 및 불량품수량 기본값 처리 (최소한의 처리)
            vo.setCreatedAt(new Date());
            if (vo.getDefectQty() == null) {
                vo.setDefectQty(0);
            }
            
            productionResultService.insertResult(vo);
            redirectAttributes.addFlashAttribute("successMessage", "생산 실적이 등록되었습니다.");
            
        } catch (Exception e) {
            System.err.println("생산 실적 등록 오류: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "생산 실적 등록 중 오류가 발생했습니다.");
        }
        
        return "redirect:/production/result/list";
    }
}