package com.itwillbs.controller;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itwillbs.service.MaterialReservationService;

/**
 * [자재 예약 컨트롤러]
 * - 버튼 액션(등록, 부족분 발주) 엔드포인트
 * - JSON 응답으로 성공/실패만 알려줌 (화면은 JS가 갱신)
 */
@Controller
@RequestMapping("/material/reservation")
public class MaterialReservationController {
	
	private static final Logger logger = LoggerFactory.getLogger(MaterialReservationController.class);
	
	@Inject
    private MaterialReservationService reservationService;

    /** 로그인 사용자ID 꺼내는 헬퍼(없으면 'system') */
    private String getUserId(HttpSession session) {
        Object uid = session != null ? session.getAttribute("loginId") : null;
        return (uid != null) ? uid.toString() : "system";
    }

    /**
     * [등록 버튼]
     * - 전량 예약되면 출고전표(DRAFT) 생성 → outboundId 반환
     * - 부족 남으면 outboundId=null (→ 화면에서 “부족분 발주” 유도)
     */
    @PostMapping(
        value = "/register-or-draft",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public Map<String, Object> registerOrDraft(
            @RequestParam("workOrderNo") String workOrderNo,
            HttpSession session) {
        Map<String,Object> res = new HashMap<>();
        try {
            String userId = getUserId(session);
            String outboundId = reservationService.registerOrDraftOutbound(workOrderNo, userId);

            res.put("ok", true);
            res.put("outboundId", outboundId); // null이면 “부족 있음”
            res.put("message", (outboundId != null)
                    ? "출고전표(DRAFT) 생성 완료"
                    : "부족분이 있어 전표를 만들지 않았습니다. '부족분 발주'를 진행하세요.");
        } catch (Exception e) {
            logger.error("registerOrDraft error", e);
            res.put("ok", false);
            res.put("message", "등록 처리 중 오류가 발생했습니다.");
        }
        return res;
    }

    /**
     * [부족분 발주 버튼]
     * - 가용분 예약 먼저 반영
     * - 남은 수량만 PO 초안 생성 → orderId 반환
     * - 부족 없으면 orderId=null
     */
    @PostMapping(
        value = "/create-shortage-po",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public Map<String, Object> createShortagePO(
            @RequestParam("workOrderNo") String workOrderNo,
            HttpSession session) {
        Map<String,Object> res = new HashMap<>();
        try {
            String userId = getUserId(session);
            String orderId = reservationService.createShortageDraftPO(workOrderNo, userId);

            res.put("ok", true);
            res.put("orderId", orderId); // null이면 “부족 없음”
            res.put("message", (orderId != null)
                    ? "부족분만 발주 초안 생성 완료"
                    : "부족분이 없어 발주를 생성하지 않았습니다.");
        } catch (Exception e) {
            logger.error("createShortagePO error", e);
            res.put("ok", false);
            res.put("message", "부족분 발주 처리 중 오류가 발생했습니다.");
        }
        return res;
    }
    
    
    // 등록 직전 "예약만" 선처리: 전표/PO 생성 안 함
    @PostMapping(value="/reserve-only", produces="application/json")
    @ResponseBody
    public Map<String,Object> reserveOnly(@RequestParam("workOrderNo") String workOrderNo) {
        Map<String,Object> res = new HashMap<>();
        try {
            boolean allOk = reservationService.reserveOnlyForWo(workOrderNo); // 아래 B) 참고
            res.put("ok", true);
            res.put("reservedAll", allOk); // 전량 예약 여부 (정보용)
            res.put("message", allOk ? "예약 완료(전량)" : "예약 완료(일부). 부족분 존재");
        } catch (Exception e) {
            res.put("ok", false);
            res.put("message", "예약 처리 중 오류");
        }
        return res;
    }
    
}
