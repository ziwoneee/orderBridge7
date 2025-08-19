package com.itwillbs.controller;

import java.util.HashMap;
import java.util.List;
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
            @RequestParam("workOrderId") String workOrderId,
            HttpSession session) {
        Map<String,Object> res = new HashMap<>();
        try {
            String userId = getUserId(session);
            String outboundId = reservationService.registerOrDraftOutbound(workOrderId, userId);

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
     * 부족분 발주 생성 (리드타임 자동 적용)
     * - 입력: workOrderId (필수), leadDays (선택 오버라이드)
     * - 동작: Service에서
     *        (1) 작업지시 납기일 조회
     *        (2) 자재별 부족분 계산
     *        (3) 자재→거래처로 묶어서 발주 헤더/아이템 생성
     *        (4) 발주 헤더 expected_arrived_date = 작업지시 due_date - leadDays
     * - 출력: { ok, orderIds[], message }
     */
    @PostMapping(
        value = "/create-shortage-po",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public Map<String, Object> createShortagePO(
            @RequestParam("workOrderId") String workOrderId,
            @RequestParam(value = "leadDays", required = false) Integer leadDays,
            HttpSession session) {
    	
        Map<String,Object> res = new HashMap<>();
        try {
        	// (선택) 세션 사용자ID가 필요하면 꺼내 쓰세요
            // String userId = ((MemberVO)session.getAttribute("loginMember")).getMemberId();
        	String userId = getUserId(session);
        	String orderId = reservationService.createShortageDraftPO(workOrderId, userId, leadDays);

        	res.put("ok", true);
        	res.put("orderId", orderId);
        	res.put("message",
        	    (orderId == null)
        	        ? "부족분이 없어 발주를 생성하지 않았습니다."
        	        : "부족분 발주 생성 완료 (1건)"
        	);
        } catch (Exception e) {
        	LoggerFactory.getLogger(getClass())
            .error("create-shortage-po 실패: workOrderId=" + workOrderId + ", leadDays=" + leadDays, e);
            res.put("ok", false);
            res.put("message", "부족분 발주 생성 중 오류: " + e.getMessage());
        }
        return res;
    }
    
    
    // 등록 직전 "예약만" 선처리: 전표/PO 생성 안 함
    @PostMapping(value="/reserve-only", produces="application/json")
    @ResponseBody
    public Map<String,Object> reserveOnly(@RequestParam("workOrderId") String workOrderId) {
        Map<String,Object> res = new HashMap<>();
        try {
            boolean allOk = reservationService.reserveOnlyForWo(workOrderId); // 아래 B) 참고
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
