package com.itwillbs.controller;

import com.itwillbs.dto.PredictionResultDTO;
import com.itwillbs.dto.WorkOrderLiteDTO;
import com.itwillbs.service.AiPredictionService;
import com.itwillbs.service.WorkOrderQueryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;

@Controller
@RequestMapping("/ai")
public class AiPredictionController {
	
	private static final Logger logger = LoggerFactory.getLogger(AiPredictionController.class);

    @Inject
    private AiPredictionService service;
    
    @Inject
    private WorkOrderQueryService workOrderQueryService;

    /**
     * 폼 + 결과 뷰 (GET)
     */
    @GetMapping("/eta")
    public String etaView(@RequestParam(required = false) String q, Model model) {
        // ETA 대상 목록 (기본 200건, 검색어 q 있으면 like)
        model.addAttribute("workOrders", workOrderQueryService.findEligibleForEta(q, 200));
        model.addAttribute("q", q);
        model.addAttribute("menu", "ai");
        return "ai/predict_eta";
    }

    /** JSON 응답 (AJAX) */
    @ResponseBody
    @GetMapping("/eta/json")
    public PredictionResultDTO etaJson(@RequestParam String workOrderId) throws Exception {
        return service.predictEtaForWorkOrder(workOrderId);
    }

    /** 동기 제출 ▶ 같은 화면에서 결과 표시 */
    @PostMapping("/eta")
    public String etaPost(@RequestParam String workOrderId, Model model) {
      try {
        PredictionResultDTO res = service.predictEtaForWorkOrder(workOrderId);

        // 작업지시서(요청 납기일)
        var woOpt = workOrderQueryService.findOne(workOrderId);
        var wo = woOpt.orElse(null);

        // ETA 날짜 → 문자열
        var etaLd = java.time.LocalDate.now()
            .plusDays(java.util.Optional.ofNullable(res.getEtaDays()).orElse(0));
        String etaDateStr = etaLd.toString();          // "yyyy-MM-dd"
        model.addAttribute("etaDateStr", etaDateStr);  // ★ JSP에서 사용

        // 요청 납기일 비교(있을 때만)
        if (wo != null && wo.getDueDate() != null) {
          var zone = java.time.ZoneId.systemDefault();
          var dueLd = wo.getDueDate().toInstant().atZone(zone).toLocalDate();
          String dueDateStr = dueLd.toString();
          int etaDelay = (int) java.time.temporal.ChronoUnit.DAYS.between(dueLd, etaLd);

          model.addAttribute("dueDateStr", dueDateStr);
          model.addAttribute("etaDelay", etaDelay);
          model.addAttribute("isDelayed", etaDelay > 0);
        }

        model.addAttribute("result", res);
      } catch (Exception e) {
        model.addAttribute("error", e.getMessage());
      }

      model.addAttribute("workOrders", workOrderQueryService.findEligibleForEta(null, 200));
      model.addAttribute("workOrderId", workOrderId);
      return "ai/predict_eta";
    }


}
