package com.itwillbs.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itwillbs.service.MrpPlanningService;

/**
 * MRP API 전용 컨트롤러
 * - JSON만 반환 (뷰 X)
 * - 페이지는 MrpPageController가 담당
 */
@RestController
@RequestMapping("/mrp")
public class MrpApiController {
	
	@Inject
    private MrpPlanningService mrpService;

	/**
     * 총소요 데이터(JSON)
     * 예: /mrp/gross/data?productId=FG-001&orderQty=100
     *  - 기존 /mrp/gross 에서 /mrp/gross/data 로 분리 (페이지와 충돌 방지)
     */
    @GetMapping("/gross/data")
    public Map<String, Object> gross(String productId, double orderQty) throws Exception {
        List<Map<String, Object>> items = mrpService.getGrossRequirements(productId, orderQty);

        Map<String, Object> res = new HashMap<>();
        res.put("productId", productId);
        res.put("orderQty", orderQty);
        res.put("items", items); // [{materialId, materialName, unit, gross_req}, ...]
        return res;
    }
    
    
    /**
     * 예: http://localhost:8088/mrp/netting?productId=FG-001&orderQty=100
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
    
    
    /**
     * 3-1) 부족분 리스트
     * 예: http://localhost:8088/mrp/shortage?productId=FG-001&orderQty=100
     */
    @GetMapping("/shortage")
    public Map<String, Object> shortage(String productId, double orderQty) throws Exception {
        List<Map<String, Object>> rows = mrpService.getShortages(productId, orderQty);
        Map<String, Object> res = new HashMap<>();
        res.put("productId", productId);
        res.put("orderQty",  orderQty);
        res.put("items",     rows);
        return res;
    }

    /**
     * 3-2) 발주 추천 초안
     * 예: http://localhost:8088/mrp/recommend-po?productId=FG-001&orderQty=100
     */
    @GetMapping("/recommend-po")
    public Map<String, Object> recommendPo(String productId, double orderQty) throws Exception {
        List<Map<String, Object>> rows = mrpService.recommendPO(productId, orderQty);
        Map<String, Object> res = new HashMap<>();
        res.put("productId", productId);
        res.put("orderQty",  orderQty);
        res.put("items",     rows);
        return res;
    }
    
    
    /**
     * 4) 발주 초안 생성
     * 요청 JSON 예:
     * {
     *   "items": [
     *     {
     *       "materialId": "RM-0003",
     *       "supplierId": "SUP-001",
     *       "qty": 120,
     *       "unitPrice": 4500,
     *       "expectedInboundDate": "2025-08-16"
     *     },
     *     ...
     *   ]
     * }
     */
    @PostMapping("/po/draft")
    public Map<String,Object> createPoDraft(@RequestBody Map<String,Object> payload) throws Exception {
        @SuppressWarnings("unchecked")
        var items = (java.util.List<java.util.Map<String,Object>>) payload.get("items");
        var result = mrpService.createPoDraft(items);
        Map<String,Object> res = new HashMap<>();
        res.put("created", result); // [{orderId, supplierId, itemCount, totalAmount}, ...]
        return res;
    }


} // MrpApiController 끝
