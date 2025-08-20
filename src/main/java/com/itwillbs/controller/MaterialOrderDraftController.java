// src/main/java/com/itwillbs/controller/MaterialOrderDraftController.java
package com.itwillbs.controller;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.itwillbs.domain.AdminUserVO;
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
    public ResponseEntity<PurchaseDraftResult> createDraft(@RequestBody PurchaseDraftRequest req,
    														HttpSession session) throws Exception {
    	
    	AdminUserVO login = (AdminUserVO) session.getAttribute("loginAdmin");
        if (login == null) login = (AdminUserVO) session.getAttribute("adminUser");

        String adminId = (login != null && login.getAdminId() != null)
                ? login.getAdminId() : "system";

        req.setRequestedBy(adminId); // ★ 여기서 주입
    	
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
