package com.itwillbs.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itwillbs.domain.ProductInboundVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.service.ProductInboundService;
import com.itwillbs.service.ProductionResultService;

@Controller
@RequestMapping("/product/inbound")
public class ProductInboundController {

    @Autowired
    private ProductInboundService inboundService;
    
    @Autowired
    private ProductionResultService productionResultService;

    // ✅ 입고 등록 (재고 현황 반영용)
    @PostMapping("/register")
    public String registerInbound(ProductInboundVO vo) {
        inboundService.registerInbound(vo);
        return "redirect:/product/stocklist";
    }

    // ✅ 입고 내역 조회 - 검색/정렬/날짜/페이징
    @GetMapping("/list")
    public String showInboundList(@ModelAttribute SearchCriteria cri, Model model) {
    	 
    	// ✅ 빈 문자열을 null로 변환 (검색 오류 방지)
        if (cri.getStartDate() != null && cri.getStartDate().trim().isEmpty()) {
            cri.setStartDate(null);
        }
        if (cri.getEndDate() != null && cri.getEndDate().trim().isEmpty()) {
            cri.setEndDate(null);
        }
    	
        List<ProductInboundVO> inboundList = inboundService.searchInboundList(cri);
        int totalCount = inboundService.countInboundList(cri);

        PageMaker pageMaker = new PageMaker(cri, totalCount);

        model.addAttribute("inboundList", inboundList);
        model.addAttribute("pageMaker", pageMaker);
        return "product/inboundList";  // /WEB-INF/views/product/inboundList.jsp
    }
    
    
 // ✅ production_result 기반 자동 입고 저장
    @PostMapping("/saveFromProduction")
    public String saveInboundFromProduction(RedirectAttributes rttr) {
        try {
            productionResultService.saveAllToInbound();
            rttr.addFlashAttribute("msg", "자동 업데이트가 완료되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            rttr.addFlashAttribute("msg", "자동 업데이트 중 오류 발생!");
        }
        return "redirect:/product/inbound/list";
    }

    
    @PostMapping("/autoInboundFromExisting")
    public String autoInboundFromExisting(RedirectAttributes rttr) {
        inboundService.autoInboundFromExistingResults();
        rttr.addFlashAttribute("msg", "기존 실적 데이터를 기반으로 입고가 등록되었습니다.");
        return "redirect:/product/inbound/list";
    }


}
