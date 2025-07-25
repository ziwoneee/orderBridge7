package com.itwillbs.controller;

import com.itwillbs.dto.ShipmentPendingGroupDTO;
import com.itwillbs.service.ClientDeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/shipment")
public class ClientDeliveryController {

    @Autowired
    private ClientDeliveryService deliveryService;

    // ✅ 출하대기 그룹형 목록 조회
    @GetMapping("/pending")
    public String getGroupedPendingList(Model model) {
        List<ShipmentPendingGroupDTO> groupedList = deliveryService.getPendingShipmentGroupedList();
        model.addAttribute("groupedList", groupedList);
        return "clientDelivery/list";
    }

    // ✅ 수주번호 단위 출하 처리
    @PostMapping("/process")
    public String processShipment(@RequestParam("clOrderIds") List<String> clOrderIds,
                                   RedirectAttributes rttr) {
        // 각 수주번호에 대해 출하 처리 로직 호출
        for (String clOrderId : clOrderIds) {
            deliveryService.processShipmentByOrderId(clOrderId);
        }

        rttr.addFlashAttribute("message", "출하 처리가 완료되었습니다.");
        return "redirect:/shipment/pending";
    }
}
