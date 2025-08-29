package com.itwillbs.controller;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itwillbs.domain.MaterialVO;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.SupplierItemVO;
import com.itwillbs.domain.SupplierVO;
import com.itwillbs.service.MaterialService;
import com.itwillbs.service.SupplierItemService;
import com.itwillbs.service.SupplierService;

/**
 * 공급 품목 컨트롤러 - Ajax 기반 공급 품목 조회 등 처리
 */
@Controller
@RequestMapping("/supplierItem")
public class SupplierItemController {

	@Inject
	private SupplierService sService;

	@Inject
	private SupplierItemService siService;

	@Inject
	private MaterialService materialService;


	/**
	 * 공급 품목 페이지 컨트롤러 - 거래처 ID를 기준으로 협력사 정보 + 공급 품목 페이지 이동
	 */
	// SupplierItemController.java
	@GetMapping("list")
	@ResponseBody
	public List<SupplierItemVO> getSupplierItemList(@RequestParam("supplierId") String supplierId) throws Exception {
	    System.out.println("✅ Ajax 요청 도착 - supplierId: " + supplierId);

	    // Service는 반드시 List<SupplierItemVO> 반환
	    List<SupplierItemVO> itemList = siService.getItemsBySupplier(supplierId);

	    System.out.println("✅ 조회된 품목 수: " + itemList.size());
	    for (SupplierItemVO item : itemList) {
	        System.out.println("▶ 자재명: " + item.getMaterialName() + ", 유형: " + item.getMaterialType());
	    }

	    // 그냥 그대로 JSON으로 내려보냄 (Map 변환/캐스팅 절대 금지)
	    return itemList;
	}


	@GetMapping("items")
	public String showSupplierItemsPaged(@RequestParam("supplierId") String supplierId, SearchCriteria cri, Model model)
			throws Exception {
		
		// 방어 로직 추가
	    if (cri.getPage() <= 0) cri.setPage(1);
	    if (cri.getPerPageNum() <= 0) cri.setPerPageNum(10);

		// 거래처 기본 정보
		SupplierVO supplier = sService.getSupplierById(supplierId);
		model.addAttribute("supplier", supplier);
		model.addAttribute("supplierId", supplierId);

		// 전체 품목 개수 조회
		int totalCount = siService.getItemCountBySupplier(supplierId);
		cri.setTotalCount(totalCount);

		// 페이징 계산
		PageMaker pageMaker = new PageMaker(cri, totalCount);

		// 페이징된 품목 목록 조회
		List<SupplierItemVO> itemList = siService.getItemListBySupplierWithPaging(supplierId, cri);

		model.addAttribute("itemList", itemList);
		model.addAttribute("pageMaker", pageMaker);
		model.addAttribute("cri", cri);

		// 자재 전체 목록도 필요하다면 같이 넘김
		List<MaterialVO> materialList = materialService.getAllMaterials();
		model.addAttribute("materialList", materialList);

		model.addAttribute("menu", "basic");

		return "supplier/items";
	}

	/**
	 * 공급 품목 등록 처리 - POST 요청으로 등록 폼 데이터를 전달받아 DB에 저장
	 */
	@PostMapping("/register")
	@ResponseBody
	public ResponseEntity<String> registerItem(SupplierItemVO item) {
		try {
			siService.registerItem(item);
			return ResponseEntity.ok("등록 성공");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("등록 실패: " + e.getMessage());
		}
	}
	
	
	/**
	 * 공급 품목 중복 확인
	 * @param supplierId 거래처 ID
	 * @param materialId 자재 ID
	 * @return true: 중복 있음 / false: 중복 없음
	 */
	@GetMapping("/check")
	@ResponseBody
	public boolean checkDuplicate(@RequestParam("supplierId") String supplierId,
	                              @RequestParam("materialId") String materialId,
	                              @RequestParam(value = "itemId", required = false) String itemId) throws Exception {

		return siService.isDuplicateItem(supplierId, materialId, itemId);
	}
	

	/**
	 * 공급 품목 수정
	 */
	@PostMapping("/update")
	@ResponseBody
	public String updateItem(SupplierItemVO item) throws Exception {
		siService.updateItem(item);
		return "success";
	}

	/**
	 * 공급 품목 단건 조회 (수정폼용)
	 */
	@GetMapping("/get")
	@ResponseBody
	public SupplierItemVO getItem(@RequestParam("id") String itemId) throws Exception {
		return siService.getItemById(itemId);
	}
	
	
	

} // SupplierItemController 끝
