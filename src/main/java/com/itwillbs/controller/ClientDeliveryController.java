package com.itwillbs.controller;

import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.StockReservationVO;
import com.itwillbs.dto.ReservationDetailDTO;
import com.itwillbs.dto.ShipmentCompletedDTO;
import com.itwillbs.dto.ShipmentCompletedGroupDTO;
import com.itwillbs.dto.ShipmentPendingGroupDTO;
import com.itwillbs.service.ClientDeliveryService;
import com.itwillbs.service.StockReservationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/shipment")
public class ClientDeliveryController {

    @Autowired
    private ClientDeliveryService deliveryService;
    
    @Autowired
    private StockReservationService reservationService;


    // ✅ 수주번호 단위 출하 처리
    @PostMapping("/process")
    public String processShipment(@RequestParam("clOrderIds") List<String> clOrderIds,
                                   RedirectAttributes rttr) {

        // 예약되지 않은 수주번호 목록 필터링
        List<String> notReserved = clOrderIds.stream()
                .filter(id -> !reservationService.isReserved(id)) // 예약 여부 확인
                .collect(Collectors.toList());

        // 예약 안된 건이 있으면 출하 중단
        if (!notReserved.isEmpty()) {
            rttr.addFlashAttribute("message",
                    "다음 수주번호는 예약되지 않아 출하할 수 없습니다: " + notReserved);
            rttr.addFlashAttribute("messageType", "danger");
            return "redirect:/shipment/list?tab=pending";
        }

        // 예약된 건만 출하 처리
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
        model.addAttribute("menu", "sales");

        // ✅ 공백 날짜 → null (빈 값으로 인한 WHERE 오류 방지)
        if (cri.getStartDate() != null && cri.getStartDate().trim().isEmpty()) cri.setStartDate(null);
        if (cri.getEndDate()   != null && cri.getEndDate().trim().isEmpty())   cri.setEndDate(null);

        // ✅ 배지 숫자는 어떤 탭이든 항상 먼저 계산해서 모델에 넣음
        int pendingCount      = deliveryService.countPendingGroupedList(cri);
        int completedCount    = deliveryService.countCompletedShipmentList(cri);

        // ⚠️ 예약 카운트는 '목록 사이즈'가 아니라 'COUNT 쿼리'로!
        int reservationCount  = reservationService.countFilteredReservationList(cri);

        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("reservationCount", reservationCount);

        // ✅ 예약된 수주번호 맵(기존)
        List<String> reservedOrderIds = reservationService.getReservedOrderIds();
        Map<String, Boolean> reservedMap = reservedOrderIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toMap(id -> id, id -> Boolean.TRUE, (a,b)->a));
        model.addAttribute("reservedMap", reservedMap);

        // ✅ 예약 탭
        if ("reservation".equals(tab)) {
            List<StockReservationVO> reservationList = reservationService.getFilteredReservationList(cri);
            int totalReservationCount = reservationService.countFilteredReservationList(cri);
            PageMaker reservationPage = new PageMaker(cri, totalReservationCount);
            model.addAttribute("reservationList", reservationList);
            model.addAttribute("reservationPage", reservationPage);
            return "clientDelivery/list";
        }

        // ✅ 출하대기
        List<ShipmentPendingGroupDTO> groupedList = deliveryService.searchPendingGroupedList(cri);
        PageMaker pendingPage = new PageMaker(cri, pendingCount);
        model.addAttribute("groupedList", groupedList);
        model.addAttribute("pendingPage", pendingPage);

        // ✅ 출하완료 (정렬 화이트리스트는 기존 로직 유지)
        List<String> allowed = Arrays.asList("deliveryId", "clOrderId", "deliveryDate", "productName", "clientName", "lotNo", "trackingNumber");
        if (cri.getSortColumn() == null || !allowed.contains(cri.getSortColumn())) cri.setSortColumn("deliveryDate");
        if (!"asc".equalsIgnoreCase(cri.getSortOrder()) && !"desc".equalsIgnoreCase(cri.getSortOrder())) cri.setSortOrder("desc");

        List<ShipmentCompletedDTO> completedList = deliveryService.searchCompletedShipmentList(cri);
        PageMaker completedPage = new PageMaker(cri, completedCount);
        List<ShipmentCompletedGroupDTO> groupedCompletedList = deliveryService.getCompletedGroupedList(cri);

        model.addAttribute("completedList", completedList);
        model.addAttribute("groupedCompletedList", groupedCompletedList);
        model.addAttribute("pageMaker", completedPage);

        return "clientDelivery/list";
    }



 // ✅ 예약 등록
    @PostMapping("/reserve")
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
    @PostMapping("/unreserve")
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

   //예약 상세 모달  
    @GetMapping(value = "/reservation/detail", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public ResponseEntity<?> getReservationDetail(
            @RequestParam String lotNo,
            @RequestParam String clOrderId) {

        try {
            ReservationDetailDTO dto = reservationService.getReservationDetail(lotNo, clOrderId);
            if (dto == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"message\":\"not found\"}");
            }
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\":\"server error\"}");
        }

    }
}
