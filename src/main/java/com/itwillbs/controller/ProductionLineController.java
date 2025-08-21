package com.itwillbs.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.itwillbs.domain.ProductionLineVO;
import com.itwillbs.dto.WorkOrderDTO;
import com.itwillbs.service.ProductionLineService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/line")
@Slf4j
public class ProductionLineController {

    @Autowired
    private ProductionLineService productionLineService;

    /**
     * 생산라인 목록 페이지
     */
    @GetMapping("/list")
    public String getLineList(Model model) {
        log.info("생산라인 목록 조회 요청");

        // 전체 라인 목록 조회
        List<ProductionLineVO> lineList = productionLineService.getAllLines();
        
        // 상태별 개수 조회
        int activeCount = productionLineService.getCountByStatus("ACTIVE");
        int inactiveCount = productionLineService.getCountByStatus("INACTIVE");

        model.addAttribute("lineList", lineList);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);
        model.addAttribute("totalCount", lineList.size());
        model.addAttribute("menu", "production");

        log.info("생산라인 목록 조회 완료 - 총 {}건", lineList.size());
        return "line/list";
    }

    /**
     * 생산라인 상세 조회 (모달) - 간단 버전
     */
    @GetMapping("/detail")
    public String getLineDetail(@RequestParam("lineId") String lineId, Model model) {
        log.info("생산라인 상세 조회 요청 - ID: {}", lineId);

        ProductionLineVO line = productionLineService.getProductionLineDetail(lineId);
        WorkOrderDTO currentWork = productionLineService.getCurrentWorkByLine(lineId);
        List<WorkOrderDTO> waitingWorks = productionLineService.getWaitingWorksByLine(lineId);

        model.addAttribute("line", line);
        model.addAttribute("currentWork", currentWork);
        model.addAttribute("waitingWorks", waitingWorks);
        model.addAttribute("waitingWorkCount", waitingWorks == null ? 0 : waitingWorks.size());

        return "line/detail-modal";
    }

    /**
     * 생산라인 상태 변경
     */
    @PostMapping("/updateStatus")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStatus(
            @RequestParam("lineId") String lineId,
            @RequestParam("status") String status) {
        
        log.info("생산라인 상태 변경 요청 - ID: {}, 상태: {}", lineId, status);
        
        Map<String, Object> response = new HashMap<>();
        try {
            int result = productionLineService.updateStatus(lineId, status);
            response.put("success", result > 0);
            response.put("message", result > 0 ? "상태가 변경되었습니다." : "상태 변경에 실패했습니다.");
        } catch (Exception e) {
            log.error("상태 변경 오류", e);
            response.put("success", false);
            response.put("message", "오류 발생: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}