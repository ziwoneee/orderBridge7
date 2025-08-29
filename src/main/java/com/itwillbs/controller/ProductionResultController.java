package com.itwillbs.controller;

import java.text.SimpleDateFormat;
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
import com.itwillbs.dto.ProductionResultDTO;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.ProductionResultVO;
import com.itwillbs.service.ProductionResultService;
import com.itwillbs.service.WorkOrderService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/production/result")
public class ProductionResultController {

    @Inject
    private ProductionResultService productionResultService;

    @Inject
    private WorkOrderService workOrderService;

    // ======================= 목록 =========================
    @GetMapping("/list")
    public String list(@ModelAttribute SearchCriteria cri, Model model) {
        // 기본 정렬 (최신순)
        if (cri.getSortColumn() == null || cri.getSortColumn().isEmpty()) {
            cri.setSortColumn("created_at");
            cri.setSortOrder("desc");
        }
        // 여기서는 'WAITING' 실적은 나오지 않는 게 정상 (서비스/쿼리에서 이미 필터링 권장)
        var list = productionResultService.getList(cri);
        int total = productionResultService.getTotalCount(cri);

        model.addAttribute("menu", "production");
        model.addAttribute("list", list);
        model.addAttribute("pageMaker", new PageMaker(cri, total));
        model.addAttribute("cri", cri);
        return "production/result/list";
    }

    // ======================= 등록 폼 =========================
    @GetMapping("/form")
    public String form(Model model) {
        // 보완모드 / READY 포함 없음. IN_PROGRESS 작업지시만 노출.
        List<WorkOrderDTO> workOrderList = workOrderService.getInProgressOnlyOrders();
        model.addAttribute("workOrderList", workOrderList);
        
        // 현재 날짜를 yyyyMMdd 형식으로 포맷팅하여 JSP에 전달
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String todayStr = sdf.format(new Date());
        model.addAttribute("todayStr", todayStr);
        model.addAttribute("menu", "production");
        
        return "production/result/form";
    }

    // ======================= 상세 =========================
    @GetMapping("/detail")
    public String detail(@RequestParam("resultId") String resultId,
                         Model model,
                         RedirectAttributes ra) {
    	
    	model.addAttribute("menu", "production");
    	
        try {
            ProductionResultDTO result = productionResultService.getDetail(resultId);
            model.addAttribute("result", result);
            return "production/result/detail";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/production/result/list";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "상세 정보를 조회하는 중 오류가 발생했습니다.");
            return "redirect:/production/result/list";
        }
    }

    // ======================= 생산 실적 등록 =========================
    @PostMapping("/register")
    public String registerProductionResult(ProductionResultVO vo, RedirectAttributes ra) {
        try {
            log.info("등록 요청 데이터: {}", vo);
            
            // 기본값 보정
            vo.setCreatedAt(new Date());
            if (vo.getDefectQty() == null) vo.setDefectQty(0);

            productionResultService.insertResult(vo);
            ra.addFlashAttribute("successMessage", "생산 실적이 등록되었습니다.");
            
        } catch (Exception e) {
            log.error("생산 실적 등록 실패: {}", e.getMessage(), e);
            ra.addFlashAttribute("errorMessage", "생산 실적 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/production/result/list";
    }
}