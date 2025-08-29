package com.itwillbs.controller;

import com.itwillbs.domain.AdminUserVO;
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

import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

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
                .filter(id -> !reservationService.isReserved(id))
                .collect(Collectors.toList());

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

    // 출하관리 전체 목록보기
    @GetMapping("/list")
    public String showShipmentTabs(@ModelAttribute SearchCriteria cri,
                                   @RequestParam(value = "tab", required = false, defaultValue = "pending") String tab,
                                   Model model) {

        // 공백 날짜 → null
        if (cri.getStartDate() != null && cri.getStartDate().trim().isEmpty()) cri.setStartDate(null);
        if (cri.getEndDate()   != null && cri.getEndDate().trim().isEmpty())   cri.setEndDate(null);

        // ✅ 정렬 파라미터 소문자/트림 정규화
        if (cri.getSortColumn() != null) cri.setSortColumn(cri.getSortColumn().trim().toLowerCase());
        if (cri.getSortOrder()  != null) cri.setSortOrder(cri.getSortOrder().trim().toLowerCase());

        model.addAttribute("tab", tab);
        model.addAttribute("menu", "sales");

        // 배지 카운트(탭과 무관)
        int pendingCount     = deliveryService.countPendingGroupedList(cri);
        int completedCount   = deliveryService.countCompletedShipmentList(cri);
        int reservationCount = reservationService.countFilteredReservationList(cri);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("reservationCount", reservationCount);

        // 예약된 수주번호 맵
        List<String> reservedOrderIds = reservationService.getReservedOrderIds();
        Map<String, Boolean> reservedMap = reservedOrderIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toMap(id -> id, id -> Boolean.TRUE, (a, b) -> a));
        model.addAttribute("reservedMap", reservedMap);

        // 🔒 탭별 정렬 화이트리스트 & 기본값
        if ("pending".equals(tab)) {
            // Mapper에서 사용하는 키: clorderid / clientname / productname / cldeliverydate (예시)
            List<String> allowed = Arrays.asList("cldeliverydate", "clorderid", "clientname", "productname");
            if (cri.getSortColumn() == null || !allowed.contains(cri.getSortColumn())) {
                cri.setSortColumn("cldeliverydate"); // 기본: 납기 오름차순
            }
            if (!"asc".equals(cri.getSortOrder()) && !"desc".equals(cri.getSortOrder())) {
                cri.setSortOrder("asc");
            }

            List<ShipmentPendingGroupDTO> groupedList = deliveryService.searchPendingGroupedList(cri);
            PageMaker pendingPage = new PageMaker(cri, pendingCount);

            model.addAttribute("groupedList", groupedList);
            model.addAttribute("pendingPage", pendingPage);
            model.addAttribute("cri", cri);
            return "clientDelivery/list";
        }

        if ("reservation".equals(tab)) {
            // 예약 탭은 정렬 의미 없으면 패스 (필요 시 동일 패턴 적용)
            List<StockReservationVO> reservationList = reservationService.getFilteredReservationList(cri);
            PageMaker reservationPage = new PageMaker(cri, reservationService.countFilteredReservationList(cri));
            model.addAttribute("reservationList", reservationList);
            model.addAttribute("reservationPage", reservationPage);
            model.addAttribute("cri", cri);
            return "clientDelivery/list";
        }

        // tab == completed
        {
            // Mapper에서 사용하는 키: clorderid / clientname / deliverydate (그룹 기준)
            List<String> allowed = Arrays.asList("clorderid", "clientname", "deliverydate");
            if (cri.getSortColumn() == null || !allowed.contains(cri.getSortColumn())) {
                cri.setSortColumn("deliverydate"); // 기본: 출하일 내림차순
            }
            if (!"asc".equals(cri.getSortOrder()) && !"desc".equals(cri.getSortOrder())) {
                cri.setSortOrder("desc");
            }

            List<ShipmentCompletedDTO> completedList = deliveryService.searchCompletedShipmentList(cri);
            List<ShipmentCompletedGroupDTO> groupedCompletedList = deliveryService.getCompletedGroupedList(cri);
            PageMaker completedPage = new PageMaker(cri, completedCount);

            model.addAttribute("completedList", completedList);
            model.addAttribute("groupedCompletedList", groupedCompletedList);
            model.addAttribute("pageMaker", completedPage);
            model.addAttribute("cri", cri);
            return "clientDelivery/list";
        }
    }

    @PostMapping("/reserve")
    public String reserveStock(@RequestParam("clOrderId") String clOrderId,
                               HttpSession session,
                               RedirectAttributes rttr) {

        AdminUserVO loginAdmin = (AdminUserVO) session.getAttribute("loginAdmin");
        if (loginAdmin == null) {
            rttr.addFlashAttribute("message", "로그인이 필요합니다.");
            rttr.addFlashAttribute("messageType", "warning");
            return "redirect:/login";
        }

        String managerName = loginAdmin.getName();
        boolean success = reservationService.reserveStockByOrderId(clOrderId, managerName);

        if (success) {
            rttr.addFlashAttribute("message", "수주번호 [" + clOrderId + "] 예약이 완료되었습니다.");
            rttr.addFlashAttribute("messageType", "success");
        } else {
            rttr.addFlashAttribute("message", "수주번호 [" + clOrderId + "] 예약이 실패되었습니다: 재고 부족 또는 이미 예약됨");
            rttr.addFlashAttribute("messageType", "danger");
            rttr.addFlashAttribute("reserveFailedId", clOrderId);
        }

        return "redirect:/shipment/list?tab=pending";
    }

    // ✅ 예약 해지
    @PostMapping("/unreserve")
    public String releaseStock(@RequestParam("clOrderId") String clOrderId, RedirectAttributes rttr) {
        reservationService.deleteReservation(clOrderId);
        rttr.addFlashAttribute("message", "수주번호 [" + clOrderId + "] 예약이 해제되었습니다.");
        rttr.addFlashAttribute("messageType", "info");
        return "redirect:/shipment/list?tab=pending";
    }

    // 출하 취소
    @PostMapping("/cancel")
    public String cancelDelivery(@RequestParam("deliveryId") String deliveryId,
                                 RedirectAttributes rttr) {
        try {
            System.out.println(">> 전달받은 deliveryId: " + deliveryId);
            deliveryService.cancelDelivery(deliveryId);
            rttr.addFlashAttribute("message", "출하가 성공적으로 취소되었습니다.");
            rttr.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            e.printStackTrace();
            rttr.addFlashAttribute("message", "출하 취소 중 오류가 발생했습니다.");
            rttr.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/shipment/list?tab=completed";
    }

    // 예약 상세 모달
    @GetMapping(value = "/reservation/detail", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public ResponseEntity<?> getReservationDetail(@RequestParam String lotNo,
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
