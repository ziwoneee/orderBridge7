package com.itwillbs.controller;

import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.StockReservationVO;
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

        rttr.addFlashAttribute("message", "수주번호 [" + clOrderIds + "] 출하처리가 완료되었습니다.");
        rttr.addFlashAttribute("messageType", "success");
        return "redirect:/shipment/list";
    }
    
    
//출하관리 전체 목록보기
    
    @GetMapping("/list")
    public String showShipmentTabs(@ModelAttribute SearchCriteria cri,
                                   @RequestParam(value = "tab", required = false, defaultValue = "pending") String tab,
                                   Model model) {

        model.addAttribute("cri", cri);
        model.addAttribute("tab", tab);

        // ✅ 예약관리 탭일 경우
        if ("reservation".equals(tab)) {
            // 예약 리스트 조회
            List<StockReservationVO> reservationList = reservationService.getFilteredReservationList(cri);
            int totalReservationCount = reservationService.countFilteredReservationList(cri);
            PageMaker reservationPage = new PageMaker(cri, totalReservationCount);

            model.addAttribute("reservationList", reservationList);
            model.addAttribute("reservationPage", reservationPage);

            return "clientDelivery/list";  // 기존 JSP 공유
        }

        // ✅ 출하대기 목록
        List<ShipmentPendingGroupDTO> groupedList = deliveryService.searchPendingGroupedList(cri);
        int totalPending = deliveryService.countPendingGroupedList(cri);
        PageMaker pendingPage = new PageMaker(cri, totalPending);
        model.addAttribute("groupedList", groupedList);
        model.addAttribute("pendingPage", pendingPage);

        // ✅ 예약된 수주번호 목록 전달 (출하대기 탭에서 "예약중" 여부 체크용)
        List<String> reservedOrderIds = reservationService.getReservedOrderIds();
        model.addAttribute("reservedOrderIds", reservedOrderIds);

        // ✅ 출하완료 목록
        List<String> allowed = Arrays.asList("deliveryId", "clOrderId", "deliveryDate", "productName", "clientName", "lotNo", "trackingNumber");

        if (cri.getSortColumn() == null || !allowed.contains(cri.getSortColumn())) {
            cri.setSortColumn("deliveryDate");
        }

        if (!"asc".equalsIgnoreCase(cri.getSortOrder()) && !"desc".equalsIgnoreCase(cri.getSortOrder())) {
            cri.setSortOrder("desc");
        }

        if (cri.getStartDate() != null && cri.getStartDate().trim().isEmpty()) cri.setStartDate(null);
        if (cri.getEndDate() != null && cri.getEndDate().trim().isEmpty()) cri.setEndDate(null);

        List<ShipmentCompletedDTO> completedList = deliveryService.searchCompletedShipmentList(cri);
        int totalCompleted = deliveryService.countCompletedShipmentList(cri);
        PageMaker completedPage = new PageMaker(cri, totalCompleted);

        List<ShipmentCompletedGroupDTO> groupedCompletedList = deliveryService.getCompletedGroupedList(cri);

        model.addAttribute("completedList", completedList);
        model.addAttribute("groupedCompletedList", groupedCompletedList);
        model.addAttribute("pageMaker", completedPage); // 출하완료용
        return "clientDelivery/list";
    }


 // ✅ 예약 등록
    @GetMapping("/reserve")
    public String reserveStock(@RequestParam("clOrderId") String clOrderId, RedirectAttributes rttr) {
        boolean success = reservationService.reserveStockByOrderId(clOrderId);

        if (success) {
            rttr.addFlashAttribute("message", "수주번호 [" + clOrderId + "] 예약이 완료되었습니다.");
            rttr.addFlashAttribute("messageType", "success"); // ✅ 성공
        } else {
            rttr.addFlashAttribute("message", "수주번호 [" + clOrderId + "] 예약이 실패되었습니다: 재고 부족 또는 이미 예약됨");
            rttr.addFlashAttribute("messageType", "danger"); // ✅ 실패
            rttr.addFlashAttribute("reserveFailedId", clOrderId);  // ✅ 실패한 ID 전달
        }

        return "redirect:/shipment/list?tab=pending";
    }


    // ✅ 예약 해지 
    @GetMapping("/unreserve")
    public String releaseStock(@RequestParam("clOrderId") String clOrderId, RedirectAttributes rttr) {
        reservationService.deleteReservation(clOrderId);
        rttr.addFlashAttribute("message",  "수주번호 [" + clOrderId + "] 예약이 해제되었습니다.");
        rttr.addFlashAttribute("messageType", "info"); // ✅ 정보
        return "redirect:/shipment/list?tab=pending";
    }
    
    //출하 취소

    @PostMapping("/cancel")
    public String cancelDelivery(@RequestParam("deliveryId") String deliveryId,
                                 RedirectAttributes rttr) {
        try {
            System.out.println(">> 전달받은 deliveryId: " + deliveryId); // ✅ 추가
            deliveryService.cancelDelivery(deliveryId);
            rttr.addFlashAttribute("message", "출하가 성공적으로 취소되었습니다.");
            rttr.addFlashAttribute("messageType", "success"); // ✅ 성공
        } catch (Exception e) {
            e.printStackTrace(); // ✅ 예외 로그 출력
            rttr.addFlashAttribute("message", "출하 취소 중 오류가 발생했습니다.");
            rttr.addFlashAttribute("messageType", "danger"); // ✅ 실패
        }
        return "redirect:/shipment/list?tab=completed";
    }

    
   


}
