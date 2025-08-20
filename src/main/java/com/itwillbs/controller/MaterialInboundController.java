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

import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialInboundDTO;
import com.itwillbs.dto.MaterialInboundItemDTO;
import com.itwillbs.dto.MaterialInboundSummaryDTO;
import com.itwillbs.dto.UnreceivedOrderDTO;
import com.itwillbs.service.MaterialInboundService;

/**
 * 자재 입고 - 미입고 발주 목록 조회 컨트롤러
 */
@Controller
@RequestMapping("/material/inbound")
public class MaterialInboundController {
	
	// mylog
	private static final Logger logger = LoggerFactory.getLogger(MaterialInboundController.class);
	
	@Inject
	private MaterialInboundService miService;
	
	
	// 입고 목록 조회
	@GetMapping("/list")
	public String listInbound(SearchCriteria cri, Model model) throws Exception {
		

	    // 전체 개수
	    int totalCount = miService.getInboundListCount(cri);
	    
	    // cri에도 세팅 (jsp에서 사용 가능하게)
	    cri.setTotalCount(totalCount);

	    // 페이지 정보 생성
	    PageMaker pageMaker = new PageMaker(cri, totalCount);
	    
	    // 목록 조회
	    List<MaterialInboundSummaryDTO> list = miService.getInboundList(cri);

	    // View 전달
	    model.addAttribute("inboundList", list);
	    model.addAttribute("pageMaker", pageMaker);
	    model.addAttribute("cri", cri);
	    
		// 메뉴 하이라이트용
	    model.addAttribute("menu", "material"); 
	    
	    // ✅ 각 상태별 카운트를 계산하여 Model에 추가
	    Map<String, Integer> statusCounts = miService.getInboundStatusCounts();
	    model.addAttribute("pendingCount", statusCounts.getOrDefault("미입고", 0));
	    model.addAttribute("partialCount", statusCounts.getOrDefault("부분입고", 0)); 
	    model.addAttribute("completedCount", statusCounts.getOrDefault("입고완료", 0));

	    return "material/inbound/list";
	}

	

	
	/**
	 * 미입고 발주 목록 - 페이징 JSON 응답
	 * - 요청 예시: /material/inbound/unreceived-orders?page=1&perPageNum=10
	 */
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

	        return ResponseEntity.ok().body(result);

	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body("미입고 발주 목록 불러오기 실패");
	    }
	}


	/**
	 * [POST] 미입고 발주건을 입고관리 테이블(material_inbound + material_inbound_item)에 저장
	 * - 조건: 아직 입고되지 않은 발주항목(order_item_id 기준)
	 * - 수행: 발주번호(order_id)별로 inbound_id를 생성하여 insert
	 */
	@PostMapping("/insert-unreceived")
	@ResponseBody
	public ResponseEntity<String> insertUnreceivedOrders(@RequestParam(required = false) String[] orderIds,
			 											HttpSession session) {
	    try {
	    	String adminId = (String) session.getAttribute("admin_id");
	        if (orderIds == null || orderIds.length == 0) {
	            // 파라미터가 없으면 전체 미입고건 처리 (기존 방식)
	            miService.insertUnreceivedOrders();
	            return ResponseEntity.ok("전체 미입고건 DB 저장 성공");
	        } else {
	            // 선택된 발주 ID만 처리 (새로운 방식)
	            miService.insertSelectedUnreceivedOrders(orderIds);
	            return ResponseEntity.ok("선택된 발주건 DB 저장 성공");
	        }
	    } catch (Exception e) {
	        logger.error("미입고건 DB 저장 실패", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                           .body("DB 저장 실패: " + e.getMessage());
	    }
	}

	
	/**
	 * [POST] 입고 처리 수행
	 * - 조건: 해당 inboundId의 모든 자재가 입고 가능 조건을 만족해야 함
	 * - 수행: 입고일 입력 + 상태 '입고완료'로 변경
	 */
	@PostMapping("/process")
	@ResponseBody
	public ResponseEntity<String> processInbound(@RequestParam("inboundId") String inboundId,
												HttpSession session) {
	    try {
	    	String adminId = (String) session.getAttribute("admin_id");
	        miService.processInbound(inboundId); // 서비스 호출
	        return ResponseEntity.ok("입고 처리 완료");
	    } catch (Exception e) {
	        logger.error("입고 처리 중 오류 발생", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body("입고 처리 실패: " + e.getMessage());
	    }
	}

	
	/**
	 * [POST] 개별 자재 항목 입고 처리
	 * - 조건: 자재 수량 > 0, LOT 번호, 유통기한, 창고 정보 필수
	 * - 수행: material_inbound_item 상태 변경 + 재고 반영
	 */
	@PostMapping("/item/process")
	@ResponseBody
	public ResponseEntity<String> processInboundItem(@RequestBody MaterialInboundItemDTO dto) {
	    try {
	        // 서비스 계층 호출
	        miService.processInboundItem(dto);
	        return ResponseEntity.ok("입고처리 완료");
	    } catch (Exception e) {
	        logger.error("개별 입고처리 중 오류 발생", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body("입고처리 실패: " + e.getMessage());
	    }
	}


	/**
	 * LOT 번호 생성 API (수정된 버전)
	 * - 형식: LOT-자재ID-YYYYMMDD-랜덤3자리
	 * - 예시: LOT-RM-0001-20250804-123
	 */
	@GetMapping("/generate-lot")
	@ResponseBody
	public ResponseEntity<String> generateLotNumber(@RequestParam("materialId") String materialId) {
	    try {
	        logger.debug("LOT 요청 materialId: {}", materialId);
	        
	        // 유효성 검사
	        if (materialId == null || materialId.trim().isEmpty()) {
	            return ResponseEntity.badRequest().body("자재 ID가 누락되었습니다.");
	        }
	        
	        // 1. 오늘 날짜를 yyyyMMdd 형식으로 생성
	        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());

	        // 2. 랜덤 3자리 숫자 생성 (100~999)
	        int random = new Random().nextInt(900) + 100;

	        // 3. LOT번호 조합 및 반환
	        String lotNumber = "LOT-" + materialId + "-" + today + "-" + random;
	        
	        logger.debug("생성된 LOT 번호: {}", lotNumber);
	        
	        return ResponseEntity.ok(lotNumber);
	        
	    } catch (Exception e) {
	        logger.error("LOT 번호 생성 중 오류 발생", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                           .body("LOT 번호 생성 실패: " + e.getMessage());
	    }
	}
	
	
	
	
	/**
	 * [GET] 입고 상세 조회 (입고ID 기준)
	 * - 자재 항목 포함한 DTO 반환
	 * - 호출 예: /material/inbound/detail?inboundId=IN-RM-20250804-001
	 */
	@GetMapping("/detail")
	@ResponseBody
	public ResponseEntity<?> getInboundDetail(@RequestParam("inboundId") String inboundId) {
	    try {
	        // 서비스 호출
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
	
	

	/**
	 * 추가입고 처리 요청 → 새로운 입고건 및 항목 등록
	 */
	@PostMapping("/additional")
	@ResponseBody
	public ResponseEntity<String> createAdditionalInbound(@RequestParam("orderItemId") String orderItemId) {
	    try {
	        miService.createAdditionalInbound(orderItemId);
	        return ResponseEntity.ok("success");
	    } catch (Exception e) {
	        logger.error("[추가입고] 처리 중 오류 발생", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
	    }
	}


	
	
	
	
	
} // MaterialInboundConroller 끝