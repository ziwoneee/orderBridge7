package com.itwillbs.controller;

import java.util.ArrayList;
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
	public ResponseEntity<Map<String, Object>> getLotDetails(@RequestParam("materialId") String materialId) throws Exception {
	    logger.info("LOT 조회 요청 - materialId: {}", materialId);
	    
	    Map<String, Object> result = new HashMap<>();
	    
	    try {
	        // 1. 자재 기본 정보 조회 (LOT 유무와 관계없이)
	        MaterialInventoryVO materialInfo = miService.getMaterialInfo(materialId);
	        
	        // 2. LOT 목록 조회
	        List<MaterialInventoryVO> lotList = miService.getLotListByMaterialId(materialId);
	        
	        // 3. 자재 정보가 있으면 설정, 없으면 materialId만 설정
	        if (materialInfo != null) {
	            result.put("materialId", materialInfo.getMaterialId());
	            result.put("materialName", materialInfo.getMaterialName());
	            result.put("materialType", materialInfo.getMaterialType());
	            result.put("unit", materialInfo.getUnit());
	        } else {
	            // 자재 정보가 없는 경우 최소한의 정보 설정
	            result.put("materialId", materialId);
	            result.put("materialName", "정보 없음");
	            result.put("materialType", "-");
	            result.put("unit", "-");
	        }
	        
	        // 4. LOT 목록 설정 (빈 배열이어도 포함)
	        result.put("lotList", lotList != null ? lotList : new ArrayList<>());
	        
	        logger.info("조회 결과 - 자재정보: {}, LOT 개수: {}", 
	                   materialInfo != null ? "있음" : "없음", 
	                   lotList != null ? lotList.size() : 0);
	        
	        return ResponseEntity.ok(result);
	        
	    } catch (Exception e) {
	    	logger.error("LOT 조회 중 오류 발생 - materialId: {}", materialId, e);
	        
	        // 오류 발생 시에도 최소한의 정보 반환
	        result.put("materialId", materialId);
	        result.put("materialName", "조회 실패");
	        result.put("materialType", "-");
	        result.put("unit", "-");
	        result.put("lotList", new ArrayList<>());
	        result.put("error", "데이터 조회 중 오류가 발생했습니다.");
	        
	        return ResponseEntity.ok(result);
	    }
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

