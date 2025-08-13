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
import com.itwillbs.dto.ProductionResultDTO;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.ProductionResultVO;
import com.itwillbs.service.ProductionResultService;
import com.itwillbs.service.WorkOrderService;

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
        if (cri.getSortColumn() == null || cri.getSortColumn().isEmpty()) {
            cri.setSortColumn("created_at");
            cri.setSortOrder("desc");
        }
        
        var list = productionResultService.getList(cri);
        int total = productionResultService.getTotalCount(cri);
        
        PageMaker pageMaker = new PageMaker(cri, total);
        
        model.addAttribute("list", list);
        model.addAttribute("pageMaker", pageMaker);
        model.addAttribute("cri", cri);
        
        return "production/result/list";
    }
    
    // ======================= 등록 폼 =========================
    @GetMapping("/form")
    public String form(Model model) {
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
    @GetMapping("/detail")
    public String detail(@RequestParam("resultId") String resultId, Model model) {
        try {
            ProductionResultDTO result = productionResultService.getDetail(resultId);
            model.addAttribute("result", result);
            
            return "production/result/detail";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/production/result/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "상세 정보를 조회하는 중 오류가 발생했습니다.");
            return "redirect:/production/result/list";
        }
    }
    
    // ======================= 생산 실적 등록 =========================
    @PostMapping("/register")
    public String registerProductionResult(ProductionResultVO vo, 
                                         RedirectAttributes redirectAttributes) {
        try {
            vo.setCreatedAt(new Date());
            if (vo.getDefectQty() == null) {
                vo.setDefectQty(0);
            }
            
            // ✅ 기존 등록 로직
            productionResultService.insertResult(vo);
            
            // ✅ 보완생산 필요 여부 체크
            boolean needSupplement = productionResultService.checkNeedSupplement(vo.getOrderId());
            int shortageQty = productionResultService.getShortageQty(vo.getOrderId());
            
            if (needSupplement && shortageQty > 0) {
                redirectAttributes.addFlashAttribute("supplementMessage", 
                    "보완생산이 필요합니다. 부족수량: " + shortageQty + "개");
                redirectAttributes.addFlashAttribute("orderId", vo.getOrderId());
                redirectAttributes.addFlashAttribute("shortageQty", shortageQty);
                return "redirect:/production/result/supplement-alert?orderId=" + vo.getOrderId();
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "생산 실적이 등록되었습니다.");
            
        } catch (Exception e) {
            System.err.println("생산 실적 등록 오류: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "생산 실적 등록 중 오류가 발생했습니다.");
        }
        
        return "redirect:/production/result/list";
    }

    // ✅ 보완생산 알림 페이지
    @GetMapping("/supplement-alert")
    public String supplementAlert(@RequestParam String orderId, Model model) {
        try {
            // 작업지시 정보 조회
            WorkOrderDTO workOrder = workOrderService.getWorkOrderDetail(orderId);
            int shortageQty = productionResultService.getShortageQty(orderId);
            
            model.addAttribute("workOrder", workOrder);
            model.addAttribute("shortageQty", shortageQty);
            model.addAttribute("orderId", orderId);
            
            return "production/result/supplement-alert";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "작업지시 정보를 조회할 수 없습니다.");
            return "redirect:/production/result/list";
        }
    }
}