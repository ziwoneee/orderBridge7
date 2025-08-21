package com.itwillbs.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itwillbs.domain.AdminUserVO;
import com.itwillbs.domain.MaterialOutboundVO;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.WorkOrderVO;
import com.itwillbs.mapper.MaterialOutboundMapper;
import com.itwillbs.persistence.MaterialInboundDAO;
import com.itwillbs.service.MaterialOutboundService;

/**
 * [출고 관리 컨트롤러]
 * - 출고 목록 조회
 * - 작업지시 목록(대기 상태) 조회
 * - 작업지시 선택 시 자재+재고 조회
 * - 출고 등록 (헤더 + LOT별 항목)
 * - 출고 처리 (재고 차감 + 상태 변경)
 */
@Controller
@RequestMapping("/material/outbound")
public class MaterialOutboundController {
	
	@Inject
	private MaterialOutboundService moService;
	
	@Inject
	private MaterialOutboundMapper outboundMapper;
	
	@Inject
    private MaterialInboundDAO inboundDAO;   
	
	// mylog
	private static final Logger logger = LoggerFactory.getLogger(MaterialOutboundController.class);
	
	// 목록(페이징)
    @RequestMapping(value="/list", method=RequestMethod.GET)
    public String list(SearchCriteria cri, Model model) throws Exception {
    	
    	logger.info("list() called - SearchCriteria: {}", cri);
    	    
	    // SearchCriteria 기본값 설정
	    if (cri == null) {
	        cri = new SearchCriteria();
	    }
    	    
	    // 기본값 보정
	    if (cri.getPage() <= 0) cri.setPage(1);
	    if (cri.getPerPageNum() <= 0) cri.setPerPageNum(10);
    	    
	    // 기본 정렬 설정
	    if (cri.getSortColumn() == null || cri.getSortColumn().isEmpty()) {
	        cri.setSortColumn("outbound_date");
	        cri.setSortOrder("desc");
	    }
	    
	    try {
	    	// 총 건수
	        int totalCount = moService.getOutboundCount(cri);

	        // ✅ PageMaker 생성 (중요!)
	        PageMaker pageMaker = new PageMaker(cri, totalCount);
	        
	        // 서비스 호출
	        model.addAttribute("list", moService.getOutboundList(cri));
	        
	        model.addAttribute("totalCount", moService.getOutboundCount(cri));
	        model.addAttribute("pendingCount", moService.getOutboundCountByStatus("DRAFT"));
	        model.addAttribute("completedCount", moService.getOutboundCountByStatus("ISSUED"));
	        model.addAttribute("waitingOrders", moService.getWaitingOrders());
	        
	        model.addAttribute("pageMaker", pageMaker);
	        model.addAttribute("cri", cri);
	        model.addAttribute("menu", "material");
	        
	        logger.info("list() completed successfully");
	        
	    } catch (Exception e) {
	        logger.error("list() error: ", e);
	        throw e;
	    }
	    
	    return "material/out/list";
    }

    // [AJAX] 대기 작업지시 목록
    @ResponseBody
    @RequestMapping(value="/order-list", method=RequestMethod.GET)
    public List<WorkOrderVO> orderList() throws Exception {
    	
    	List<WorkOrderVO> list = moService.getWaitingOrders();
    	logger.info("WAITING orders count: {}", list.size());
    	
        return moService.getWaitingOrders();
    }
    
    
    

    @GetMapping("/register")
    public String register(
            @RequestParam(value = "workOrderId", required = false) String workOrderId,
            @RequestParam(value = "inboundId",   required = false) String inboundId,
            HttpSession session,
            Model model
    ) throws Exception {

        // 세션에서 로그인한 사용자 정보 가져오기
    	AdminUserVO loginUser = (AdminUserVO) session.getAttribute("loginAdmin");
        String handledBy = "admin"; // 기본값
        
        
        if (loginUser != null && loginUser.getName() != null) {
            handledBy = loginUser.getName(); // 로그인한 사용자의 이름 사용
        }
        
        model.addAttribute("handledBy", handledBy); // JSP에서 사용할 수 있도록 추가

        // 1) workOrderId가 있으면 화면 데이터 세팅
        if (workOrderId != null && !workOrderId.isEmpty()) {
            model.addAttribute("wo", moService.getWorkOrderWithStockMap(workOrderId));
            model.addAttribute("workOrderId", workOrderId);
            model.addAttribute("inboundId", inboundId);
            return "material/out/register";
        }

        // 2) inboundId만 있으면 서버에서 매핑 시도
        if (inboundId != null && !inboundId.isEmpty()) {
            String resolved = outboundMapper.findWorkOrderIdByInbound(inboundId);
            if (resolved != null && !resolved.isEmpty()) {
                return "redirect:/material/outbound/register?workOrderId="
                        + URLEncoder.encode(resolved, StandardCharsets.UTF_8)
                        + "&inboundId=" + URLEncoder.encode(inboundId, StandardCharsets.UTF_8);
            }
            // 매핑 실패 → 화면에서 수동 선택 유도
            model.addAttribute("inboundId", inboundId);
            model.addAttribute("workOrderResolveFail", true);
            return "material/out/register";
        }

        // 3) 둘 다 없음 → 빈 화면(작업지시 선택 유도)
        return "material/out/register";
    }
    
    // 등록 저장(VO 한 방에 받기: List로 바인딩)
    @RequestMapping(value="/register", method=RequestMethod.POST)
    public String register(MaterialOutboundVO vo,
				    		@RequestParam(value = "inboundIds", required = false) String inboundIdsCsv, // ★추가(복수)
				            @RequestParam(value = "inboundId",  required = false) String inboundId,  
				            HttpSession session,
    						RedirectAttributes rttr) throws Exception {
    	
    	logger.info("register workOrderNo={}", vo.getWorkOrderId()); // null이면 바인딩 문제
    	
    	// 세션에서 로그인한 사용자 정보 가져오기
    	AdminUserVO loginUser = (AdminUserVO) session.getAttribute("loginAdmin");
        String handledBy = "admin"; // 기본값
	        
	     // ✅ 디버깅 코드 추가 (POST에서도)
        logger.info("=== POST 메서드 세션 정보 확인 ===");
        logger.info("LoginUser 객체: {}", loginUser);
        
        if (loginUser != null && loginUser.getName() != null) {
            handledBy = loginUser.getName(); // 로그인한 사용자의 이름 사용
        }
        
        // VO에 담당자 설정
        vo.setHandledBy(handledBy);
    	
    	// 1) 출고 헤더/아이템 저장 → DRAFT 생성
    	vo.setStatus("DRAFT");	// 미출고
        moService.registerOutbound(vo);
        
        // 2) usage_status 재계산 (등록 시점부터 '사용'으로 간주)
        //    - 모달에서 넘어온 inboundIdsCsv가 있으면 그걸, 없으면 inboundId 단일을 사용
        if (inboundIdsCsv != null && !inboundIdsCsv.trim().isEmpty()) {
            for (String inb : inboundIdsCsv.split(",")) {
                inb = inb.trim();
                if (!inb.isEmpty()) {
                    inboundDAO.recalcUsageStatusByInboundId(inb);  // ★ 핵심 한 줄
                }
            }
        } else if (inboundId != null && !inboundId.trim().isEmpty()) {
            inboundDAO.recalcUsageStatusByInboundId(inboundId.trim()); // ★ 단수 fallback
        }
        // (선택) 만약 여기서 방금 생성한 outboundId를 알 수 있으면
        // inboundDAO.recalcUsageStatusByOutboundId(outboundId); 로 한 방에 처리해도 됨

        // 3) 후처리
        rttr.addFlashAttribute("successMessage", "등록이 완료되었습니다.");
        return "redirect:/material/outbound/list";
    }


    // [AJAX] 출고 상세
    @ResponseBody
    @RequestMapping(value="/detail", method=RequestMethod.GET)
    public Map<String,Object> detail(@RequestParam("outboundId") String outboundId) throws Exception {
        return moService.getOutboundDetailMap(outboundId);
    }

    // [AJAX] 출고 처리
    @ResponseBody
    @RequestMapping(value="/process", method=RequestMethod.POST)
    public String process(@RequestParam("outboundId") String outboundId) throws Exception {
        moService.processOutbound(outboundId);
        return "OK";
    }
    
    // [AJAX] 특정 작업지시의 자재 + 현재고 집계 (register.jsp가 호출)
    @ResponseBody
    @RequestMapping(value="/work-order", method=RequestMethod.GET)
    public Map<String, Object> workOrder(@RequestParam("workOrderId") String workOrderId) throws Exception {
        return moService.getWorkOrderWithStockMap(workOrderId);
    }
    
    /**
     * 작업지시서 출고등록 요청
     * @param workOrderId 작업지시서 ID
     * @return JSON 형태 결과 (생성여부, 출고ID, 사유)
     */
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> create(@RequestParam("workOrderId") String workOrderId) {
        try {
            MaterialOutboundService.CreateOutboundResult r = moService.createOutboundIfReady(workOrderId);
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                java.util.Map.of("created", false, "reason", "error", "message", e.getMessage())
            );
        }
    }
    
    
    /**
     * 입고 모달 불러오기
     */
    @GetMapping("/inbounds")
    @ResponseBody
    public ResponseEntity<?> getInboundDoneList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String processed) {
        try {
            // 지금은 status/processed 파라미터는 무시하고 목록만 리턴
            List<Map<String,Object>> list = outboundMapper.getInboundDoneList();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("목록 로드 실패");
        }
    }
    
    @GetMapping("/resolve-workorder")
    @ResponseBody
    public ResponseEntity<Map<String,String>> resolveWorkOrder(@RequestParam("inboundId") String inboundId){
        String wo = outboundMapper.findWorkOrderIdByInbound(inboundId);
        if (wo == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(Collections.singletonMap("workOrderId", wo));
    }
    

    /**
     * 특정 입고건의 가용 자재 목록 조회
     * @param inboundId 입고ID
     * @return 가용 자재 목록
     */
    @GetMapping("/available-materials")
    @ResponseBody
    public ResponseEntity<?> getAvailableMaterialsByInbound(
    		@RequestParam("inboundId") String inboundId,
    		@RequestParam String workOrderId) {
    	
        try {
            List<Map<String, Object>> materials = moService.getAvailableMaterialsByInbound(inboundId, workOrderId);
            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            logger.error("getAvailableMaterialsByInbound error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("가용 자재 목록 조회 실패: " + e.getMessage());
        }
    }

    /**
     * 입고건 사용 상태 업데이트
     * @param inboundId 입고ID
     * @return 업데이트 결과
     */
    @PostMapping("/update-inbound-status")
    @ResponseBody
    public ResponseEntity<?> updateInboundUsageStatus(@RequestParam("inboundId") String inboundId) {
        try {
            moService.updateInboundUsageStatus(inboundId);
            return ResponseEntity.ok(Map.of("success", true, "message", "입고건 사용 상태가 업데이트되었습니다."));
        } catch (Exception e) {
            logger.error("updateInboundUsageStatus error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body(Map.of("success", false, "message", "입고건 사용 상태 업데이트 실패: " + e.getMessage()));
        }
    }

	
	
} // MaterialOutboundController 끝