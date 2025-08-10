// src/main/java/com/itwillbs/controller/MaterialOrderDraftController.java
package com.itwillbs.controller;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.itwillbs.dto.PurchaseDraftRequest;
import com.itwillbs.dto.PurchaseDraftResult;
import com.itwillbs.service.MaterialOrderService;

@Controller
@RequestMapping("/material/order")
public class MaterialOrderDraftController {

	private static final Logger logger = LoggerFactory.getLogger(MaterialOrderDraftController.class);

	
    @Inject
    private MaterialOrderService materialOrderService;

    // 부족분으로 발주 초안 생성
    @PostMapping("/draft")
    @ResponseBody
    public ResponseEntity<PurchaseDraftResult> createDraft(@RequestBody PurchaseDraftRequest req) throws Exception {
    	logger.info("draft req workOrderId={}, items={}", req.getWorkOrderId(),
                req.getItems() == null ? 0 : req.getItems().size());
       if (req.getItems() != null) {
           req.getItems().forEach(i ->
               logger.info("item materialId={}, lackQty={}", i.getMaterialId(), i.getLackQty())
           );
       }
       return ResponseEntity.ok(materialOrderService.createDraftFromShortages(req));
    }
}
