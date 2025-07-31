package com.itwillbs.controller;

import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.ShipmentCompletedDTO;
import com.itwillbs.dto.ShipmentCompletedGroupDTO;
import com.itwillbs.dto.ShipmentPendingGroupDTO;
import com.itwillbs.service.ClientDeliveryService;
import com.itwillbs.service.StockReservationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/shipment")
public class ClientDeliveryController {

    @Autowired
    private ClientDeliveryService deliveryService;
    
    @Autowired
    private StockReservationService reservationService;

//    // ✅ 출하대기 그룹형 목록 조회
//    @GetMapping("/pending")
//    public String getGroupedPendingList(Model model) {
//        List<ShipmentPendingGroupDTO> groupedList = deliveryService.getPendingShipmentGroupedList();
//        model.addAttribute("groupedList", groupedList);
//        return "clientDelivery/list";
//    }

    // ✅ 수주번호 단위 출하 처리
    @PostMapping("/process")
    public String processShipment(@RequestParam("clOrderIds") List<String> clOrderIds,
                                   RedirectAttributes rttr) {
        // 각 수주번호에 대해 출하 처리 로직 호출
        for (String clOrderId : clOrderIds) {
            deliveryService.processShipmentByOrderId(clOrderId);
        }

        rttr.addFlashAttribute("message", "출하 처리가 완료되었습니다.");
        return "redirect:/shipment/list";
    }
    
    // 출하 완료 목록 보기
    @GetMapping("/completed")
    public String showCompletedShipmentList(@ModelAttribute SearchCriteria cri, Model model) {
        // ✅ 정렬 컬럼 화이트리스트
    	List<String> allowed = Arrays.asList("deliveryId", "clOrderId", "deliveryDate", "productName", "clientName", "lotNo", "trackingNumber");

        // ✅ 기본값 설정
        if (cri.getSortColumn() == null) {
            cri.setSortColumn("deliveryDate");
        }
        if (!"asc".equalsIgnoreCase(cri.getSortOrder()) && !"desc".equalsIgnoreCase(cri.getSortOrder())) {
            cri.setSortOrder("desc");
        }

        // ✅ 빈 문자열 처리
        if (cri.getStartDate() != null && cri.getStartDate().trim().isEmpty()) cri.setStartDate(null);
        if (cri.getEndDate() != null && cri.getEndDate().trim().isEmpty()) cri.setEndDate(null);

        // ✅ 데이터 조회
        List<ShipmentCompletedDTO> completedList = deliveryService.searchCompletedShipmentList(cri);
        int totalCount = deliveryService.countCompletedShipmentList(cri);
        PageMaker pageMaker = new PageMaker(cri, totalCount);

        model.addAttribute("completedList", completedList);
        model.addAttribute("pageMaker", pageMaker);
        model.addAttribute("cri", cri);

        return "clientDelivery/completed";
    }

    
    @GetMapping("/list")
    public String showShipmentTabs(@ModelAttribute SearchCriteria cri,
                                   @RequestParam(value = "tab", required = false, defaultValue = "pending") String tab,
                                   Model model) {
    	System.out.println(cri);
        // ✅ 출하대기 목록
        List<ShipmentPendingGroupDTO> groupedList = deliveryService.searchPendingGroupedList(cri);
        System.out.println(groupedList);
        int totalPending = deliveryService.countPendingGroupedList(cri);
        System.out.println(totalPending);
        PageMaker pendingPage = new PageMaker(cri, totalPending);

        model.addAttribute("groupedList", groupedList);
        model.addAttribute("pendingPage", pendingPage);

        // ✅ 예약된 수주번호 목록 전달
        List<String> reservedOrderIds = reservationService.getReservedOrderIds();
        model.addAttribute("reservedOrderIds", reservedOrderIds);

        // ✅ 출하완료 검색조건 보정
        List<String> allowed = Arrays.asList("deliveryId", "clOrderId", "deliveryDate", "productName", "clientName", "lotNo", "trackingNumber");

        if (cri.getSortColumn() == null ) {
            cri.setSortColumn("deliveryDate");
        }

        if (!"asc".equalsIgnoreCase(cri.getSortOrder()) && !"desc".equalsIgnoreCase(cri.getSortOrder())) {
            cri.setSortOrder("desc");
        }

        if (cri.getStartDate() != null && cri.getStartDate().trim().isEmpty()) cri.setStartDate(null);
        if (cri.getEndDate() != null && cri.getEndDate().trim().isEmpty()) cri.setEndDate(null);

        // ✅ 출하완료 flat 리스트 (기존 테이블용, 유지)
        List<ShipmentCompletedDTO> completedList = deliveryService.searchCompletedShipmentList(cri);
        int totalCount = deliveryService.countCompletedShipmentList(cri);
        PageMaker pageMaker = new PageMaker(cri, totalCount);

        // ✅ 출하완료 그룹 리스트 (모달용 추가)
        List<ShipmentCompletedGroupDTO> groupedCompletedList = deliveryService.getCompletedGroupedList(cri);

        // ✅ 모델에 전달
        model.addAttribute("completedList", completedList); // (선택: 기존 flat 테이블 유지 시)
        model.addAttribute("groupedCompletedList", groupedCompletedList); // 👉 모달용!
        model.addAttribute("pageMaker", pageMaker);
        model.addAttribute("cri", cri);
        model.addAttribute("tab", tab);

        return "clientDelivery/list";
    }

    
    @GetMapping("/reserve")
    public String reserveStock(@RequestParam("clOrderId") String clOrderId, RedirectAttributes rttr) {
        try {
            reservationService.reserveStockByOrderId(clOrderId);
            rttr.addFlashAttribute("reservedOrderId", clOrderId);
            rttr.addFlashAttribute("message", "재고 예약이 완료되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("message", "예약 중 오류가 발생했습니다.");
        }
        return "redirect:/shipment/list?tab=pending";
    }



}
