package com.itwillbs.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itwillbs.service.MrpPlanningService;

/**
 * 테스트용 REST 컨트롤러.
 * - 브라우저/포스트맨에서 간단히 호출해서 동작 확인한다.
 * - 예: /mrp/gross?productId=FG-001&orderQty=100
 */
@RestController
@RequestMapping("/mrp")
public class MrpController {
	
	@Inject
    private MrpPlanningService mrpService;

    @GetMapping("/gross")
    public Map<String, Object> gross(String productId, double orderQty) throws Exception {
        List<Map<String, Object>> items = mrpService.getGrossRequirements(productId, orderQty);

        Map<String, Object> res = new HashMap<>();
        res.put("productId", productId);
        res.put("orderQty", orderQty);
        res.put("items", items); // [{materialId, materialName, unit, gross_req}, ...]
        return res;
    }
    
    
    /**
     * 예: /mrp/netting?productId=FG-001&orderQty=100
     * 결과: 자재별 총소요/재고/예약/가용/순소요
     */
    @GetMapping("/netting")
    public Map<String, Object> netting(String productId, double orderQty) throws Exception {
        List<Map<String, Object>> rows = mrpService.getNetting(productId, orderQty);
        Map<String, Object> res = new HashMap<>();
        res.put("productId", productId);
        res.put("orderQty", orderQty);
        res.put("items", rows);
        return res;
    }

} // MrpController 끝
