package com.itwillbs.controller;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
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

	    return "material/inbound/list";
	}

	
	

	/**
     * 미입고 상태의 발주 목록 조회 (입고처리용)
     * - 아직 입고되지 않은 발주건만 조회
     */
	@GetMapping("/pending-orders")
	public ResponseEntity<?> getPendingInboundOrders() {
	    try {
	        List<MaterialOrderVO> list = miService.getPendingInboundOrders();
	        return ResponseEntity.ok(list);
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("불러오기 실패");
	    }
	}

	
	
    /**
     * 미입고 상태의 발주 목록을 JSON 형태로 반환
     * - 자재 입고가 한 번도 처리되지 않은 발주건만 필터링
     */
    @GetMapping("/unreceived-orders")
    @ResponseBody
    public List<UnreceivedOrderDTO> getUnreceivedOrders() {
        return miService.getUnreceivedOrders();
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
} // MaterialInboundConroller 끝
