package com.itwillbs.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialInboundDTO;
import com.itwillbs.dto.MaterialInboundItemDTO;
import com.itwillbs.dto.MaterialInboundSummaryDTO;
import com.itwillbs.dto.UnreceivedOrderDTO;
import com.itwillbs.service.MaterialInboundService;

@Controller
@RequestMapping("/material/inbound")
public class MaterialInboundController {

    private static final Logger logger = LoggerFactory.getLogger(MaterialInboundController.class);

    @Inject
    private MaterialInboundService miService;

    /** 세션에서 로그인 사용자 ID 추출 (우선순위: admin_id → loginId). 없으면 null */
    private String getUserId(HttpSession session) {
        if (session == null) return null;

        Object uid = session.getAttribute("admin_id");
        if (uid == null) {
            Object obj = session.getAttribute("loginAdmin");
            if (obj instanceof com.itwillbs.domain.AdminUserVO) {
                uid = ((com.itwillbs.domain.AdminUserVO) obj).getAdminId();  // ★ 여기서 꺼냄
            }
        }
        if (uid == null) uid = session.getAttribute("loginId"); // 보조 키
        return (uid != null) ? uid.toString() : null;
    }


    // 입고 목록 화면
    @GetMapping("/list")
    public String listInbound(SearchCriteria cri, Model model) throws Exception {
        int totalCount = miService.getInboundListCount(cri);
        cri.setTotalCount(totalCount);
        PageMaker pageMaker = new PageMaker(cri, totalCount);

        List<MaterialInboundSummaryDTO> list = miService.getInboundList(cri);

        model.addAttribute("inboundList", list);
        model.addAttribute("pageMaker", pageMaker);
        model.addAttribute("cri", cri);

        model.addAttribute("menu", "material");

        Map<String, Integer> statusCounts = miService.getInboundStatusCounts();
        model.addAttribute("pendingCount",   statusCounts.getOrDefault("미입고", 0));
        model.addAttribute("partialCount",   statusCounts.getOrDefault("부분입고", 0));
        model.addAttribute("completedCount", statusCounts.getOrDefault("입고완료", 0));

        return "material/inbound/list";
    }

    /** 미입고 발주 목록 - 페이징 JSON */
    @GetMapping("/unreceived-orders")
    @ResponseBody
    public ResponseEntity<?> getUnreceivedOrders(SearchCriteria cri) {
        try {
            int totalCount = miService.getUnreceivedOrdersCount();
            cri.setTotalCount(totalCount);
            PageMaker pageMaker = new PageMaker(cri, totalCount);
            List<UnreceivedOrderDTO> list = miService.getUnreceivedOrdersPaging(cri);

            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("pageMaker", pageMaker);
            result.put("cri", cri);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("미입고 발주 목록 불러오기 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("미입고 발주 목록 불러오기 실패");
        }
    }

    /**
     * [POST] 미입고 발주건을 입고 테이블에 생성
     * - orderIds 미전달/빈배열: 전체 미입고건
     * - 전달됨: 선택한 발주만
     * 👉 handledBy로 세션 사용자 전달
     */
    @PostMapping("/insert-unreceived")
    @ResponseBody
    public ResponseEntity<String> insertUnreceivedOrders(
    		@RequestParam("orderIds") List<String> orderIds,
            HttpSession session) throws Exception {
    	
    	 String adminId = (String) session.getAttribute("adminId");
    	 miService.insertSelectedUnreceivedOrders(orderIds.toArray(new String[0]), adminId);
    	 return ResponseEntity.ok().build();
    }

    /** [POST] 입고 처리 (마스터) */
    @PostMapping("/process")
    @ResponseBody
    public ResponseEntity<String> processInbound(@RequestParam("inboundId") String inboundId,
                                                 HttpSession session) {
        try {
            miService.processInbound(inboundId);
            return ResponseEntity.ok("입고 처리 완료");
        } catch (Exception e) {
            logger.error("입고 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("입고 처리 실패: " + e.getMessage());
        }
    }

    /** [POST] 개별 자재 항목 입고 처리 */
    @PostMapping("/item/process")
    @ResponseBody
    public ResponseEntity<String> processInboundItem(@RequestBody MaterialInboundItemDTO dto,
            HttpSession session) throws Exception {
    	
    	String adminId = (String) session.getAttribute("adminId"); // 세션 키명 확인
	    miService.processInboundItem(dto, adminId); // ★ 새 오버로드 사용
	    return ResponseEntity.ok("OK");
    }

    /** LOT 번호 생성 */
    @GetMapping("/generate-lot")
    @ResponseBody
    public ResponseEntity<String> generateLotNumber(@RequestParam("materialId") String materialId) {
        try {
            if (materialId == null || materialId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("자재 ID가 누락되었습니다.");
            }
            String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
            int random = new Random().nextInt(900) + 100;
            String lotNumber = "LOT-" + materialId + "-" + today + "-" + random;
            return ResponseEntity.ok(lotNumber);
        } catch (Exception e) {
            logger.error("LOT 번호 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("LOT 번호 생성 실패: " + e.getMessage());
        }
    }

    /** [GET] 입고 상세 */
    @GetMapping("/detail")
    @ResponseBody
    public ResponseEntity<?> getInboundDetail(@RequestParam("inboundId") String inboundId) {
        try {
            MaterialInboundDTO dto = miService.getInboundDetail(inboundId);
            if (dto == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("입고 정보를 찾을 수 없습니다.");
            }
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("입고 상세 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("입고 상세 조회 실패: " + e.getMessage());
        }
    }

    /** [POST] 추가입고 생성 */
    @PostMapping("/additional")
    @ResponseBody
    public ResponseEntity<String> createAdditionalInbound(@RequestParam("orderItemId") String orderItemId,
                                                          HttpSession session) {
        try {
            String handledBy = getUserId(session); // ★ 세션 사용자
            miService.createAdditionalInbound(orderItemId, handledBy); // ★ 전달
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            logger.error("[추가입고] 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }
}
