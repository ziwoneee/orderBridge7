package com.itwillbs.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itwillbs.domain.MaterialInventoryVO;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.service.MaterialInventoryService;
import com.itwillbs.service.MaterialReservationService;

@Controller
@RequestMapping("/material/inventory") // 자재관리 > 재고현황
public class MaterialInventoryController {
	
	// mylog
	private static final Logger logger = LoggerFactory.getLogger(MaterialInventoryController.class);

	
	// 서비스 객체 주입
	@Inject
	private MaterialInventoryService miService;
	
	@Inject
    private MaterialReservationService reservationService;
	
	
	/**
	 * 자재 재고 요약 목록 페이지 요청 처리
	 * - 자재별 1행으로 요약 표시
	 * - 검색 + 페이징 포함
	 * @throws Exception 
	 */
	@GetMapping("/summary")
	public String inventorySummaryList(SearchCriteria cri, Model model) throws Exception {
	    logger.info(" inventorySummaryList() 호출 ");
	    logger.info("검색 조건: {}", cri);

	    // 1. 요약 목록 조회 (자재 ID 기준 1행 요약)
	    List<MaterialInventoryVO> summaryList = miService.getInventorySummaryList(cri);

	    // 2. 전체 건수 조회 (페이징용)
	    int totalCount = miService.getInventoryCount(cri); // 기존 사용

	    // 3. PageMaker 생성
	    PageMaker pageMaker = new PageMaker(cri, totalCount);

	    // 4. 모델에 담기
	    model.addAttribute("summaryList", summaryList);   // 요약 목록
	    model.addAttribute("pageMaker", pageMaker);       // 페이징 정보
	    model.addAttribute("cri", cri);                   // 검색 조건 유지
	    model.addAttribute("menu", "material");           // 메뉴 활성화용
	    model.addAttribute("now", new Date());            // 현재 시간 (선택)

	    // 5. 뷰 리턴
	    return "material/inventory/summary"; // → JSP 파일명
	}
	
	
	// material_id로 LOT 목록 조회
	@GetMapping("/lot-details")
	@ResponseBody
	public ResponseEntity<List<MaterialInventoryVO>> getLotDetails(@RequestParam("materialId") String materialId) throws Exception {
	    logger.info("LOT 조회 요청 - materialId: {}", materialId);
	    
	    List<MaterialInventoryVO> lotList = miService.getLotListByMaterialId(materialId);
	    
	    logger.info("조회된 LOT 데이터 개수: {}", lotList != null ? lotList.size() : 0);
	    if (lotList != null && !lotList.isEmpty()) {
	        logger.info("첫 번째 LOT 데이터: {}", lotList.get(0));
	    }
	    
	    return ResponseEntity.ok(lotList);
	}


	
	@GetMapping(value="/availability", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String,Object> availability(
            @RequestParam String materialId,
            @RequestParam(required=false) String workOrderId) throws Exception {

        int onhand        = reservationService.selectOnhand(materialId);
        int reservedTotal = reservationService.sumReservedByMaterial(materialId);
        int woReserved    = (workOrderId == null || workOrderId.isEmpty())
                ? 0 : reservationService.selectWoReserved(workOrderId, materialId);

        // 이번 WO가 쓸 수 있는 가용 = onhand - (전체예약 - 이 WO가 이미 잡아둔 예약)
        int availableForThisWO = Math.max(0, onhand - (reservedTotal - woReserved));

        Map<String,Object> res = new HashMap<>();
        res.put("onhandTotal", onhand);
        res.put("reservedTotal", reservedTotal);
        res.put("woReserved", woReserved);
        res.put("availableForThisWO", availableForThisWO);
        return res;
    }
	
	
	
	
	
	
	
	
} // MaterialInventoryController 끝

