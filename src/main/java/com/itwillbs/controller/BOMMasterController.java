package com.itwillbs.controller;


import com.itwillbs.domain.BOMDetailVO;
import com.itwillbs.domain.BOMMasterVO;
import com.itwillbs.domain.MaterialVO;
import com.itwillbs.domain.ProductVO;
import com.itwillbs.service.BOMMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/master/bom")
public class BOMMasterController {

    @Autowired
    private BOMMasterService bomMasterService;

    @GetMapping("/list")
    public String bomList(Model model) {
        // BOM 목록
        List<BOMMasterVO> bomList = bomMasterService.getAllBOM();
        bomList.sort((a, b) -> {
            // 둘 다 같으면 0
            if (a.getStatus().equals(b.getStatus())) return 0;
            // a가 INACTIVE면 뒤로
            return a.getStatus().equals("INACTIVE") ? 1 : -1;
        });
        
        
        model.addAttribute("bomList", bomList); 
        model.addAttribute("menu", "basic");
    
        return "master/bomList";
    }

    // BOM 등록 폼 이동
    @GetMapping("/insert")
    public String insertBOMForm(Model model) {
    	// 제품목록, 원자재목록(DAO/Service에서 조회)
        List<ProductVO> productList = bomMasterService.getAllProducts();
        List<MaterialVO> materialList = bomMasterService.getAllMaterials();
        model.addAttribute("productList", productList);
        model.addAttribute("materialList", materialList);  	    	
        model.addAttribute("menu", "basic");
    	
    	return "master/bomInsert";
    }

    // BOM 등록 처리
    @PostMapping("/insert")
    public String insertBOM(@ModelAttribute BOMMasterVO bomMasterVO) {
    	
    	 // BOM ID 자동생성
        String nextBOMId = bomMasterService.createNextBOMId();
        bomMasterVO.setBomId(nextBOMId);

        // 2. 등록일(옵션)도 여기서 세팅 가능. (예: new java.util.Date())
        bomMasterVO.setBomDate(new java.sql.Date(System.currentTimeMillis()));
     
        //3.서비스에 저장
        bomMasterService.insertBOM(bomMasterVO);
        return "redirect:/master/bom/list";
    }

   
    //BOM 상태 처리
    @PostMapping("/updateStatus")
    @ResponseBody
    public String updateStatus(@RequestParam String bomId, @RequestParam String status) {
        bomMasterService.updateBOMStatus(bomId, status);
        return "success";
    }
    
    //상세조회
    // BOM 상세 페이지
    @GetMapping("/detail/{bomId}")
    public String getBOMDetail(@PathVariable String bomId, Model model) {
        BOMMasterVO bomMaster = bomMasterService.getBOMDetail(bomId);

        List<BOMDetailVO> soupList = bomMaster.getDetails().stream()
            .filter(d -> "육수".equals(d.getMaterialType()))
            .collect(Collectors.toList());

        List<BOMDetailVO> solidList = bomMaster.getDetails().stream()
            .filter(d -> "원료".equals(d.getMaterialType()))
            .collect(Collectors.toList());
       
        List<BOMDetailVO> packagingList = bomMaster.getDetails().stream()
            .filter(d -> "포장재".equals(d.getMaterialType()))
            .collect(Collectors.toList());

        model.addAttribute("bomMaster", bomMaster);
        model.addAttribute("soupList", soupList);
        model.addAttribute("solidList", solidList);
        model.addAttribute("packagingList", packagingList); 
        model.addAttribute("menu", "basic");

        return "master/bomDetail";
    }


    // BOM 상세 삭제
    @PostMapping("/detail/delete")
    public String deleteBOMDetail(@RequestParam int bomDetailId, @RequestParam String bomId) {
        bomMasterService.deleteBOMDetail(bomDetailId);
        return "redirect:/master/bom/detail/" + bomId;
    }


    // BOM 상세 수정 처리
    @PostMapping("/detail/update")
    public String updateBOMDetail(@ModelAttribute BOMDetailVO bomDetailVO) {
        bomMasterService.updateBOMDetail(bomDetailVO);
        return "redirect:/master/bom/detail/" + bomDetailVO.getBomId();
    }

}
    
