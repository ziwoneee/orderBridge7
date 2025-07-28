package com.itwillbs.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.ProductOutboundVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.service.ProductOutboundService;

@Controller
@RequestMapping("/product/outbound")
public class ProductOutboundController {

    @Autowired
    private ProductOutboundService outboundService;

    @PostMapping("/register")
    public String registerOutbound(ProductOutboundVO vo, RedirectAttributes rttr) {
        try {
            outboundService.registerOutbound(vo);
            rttr.addFlashAttribute("message", "출고 등록 완료");
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "출고 등록 중 오류 발생");
            e.printStackTrace();
        }
        return "redirect:/product/stocklist";
    }

    
          
    @GetMapping("/list")
    public String showOutboundList(@ModelAttribute SearchCriteria cri, Model model) {
        // ✅ 빈 문자열 처리
        if (cri.getStartDate() != null && cri.getStartDate().trim().isEmpty()) cri.setStartDate(null);
        if (cri.getEndDate() != null && cri.getEndDate().trim().isEmpty()) cri.setEndDate(null);

        // ✅ 허용 정렬 컬럼 리스트
        List<String> allowed = Arrays.asList("productName", "lotNo", "outboundDate", "clientName", "manager");

        // ✅ 정렬 컬럼 유효성 체크
        if (cri.getSortColumn() == null || !allowed.contains(cri.getSortColumn())) {
            cri.setSortColumn("outboundDate"); // 기본 정렬 컬럼
        }

        // ✅ 정렬 순서 유효성 체크
        if (!"asc".equalsIgnoreCase(cri.getSortOrder()) && !"desc".equalsIgnoreCase(cri.getSortOrder())) {
            cri.setSortOrder("desc"); // 기본 정렬 순서
        }

        // ✅ 조회
        List<ProductOutboundVO> outboundList = outboundService.searchOutboundList(cri);
        int totalCount = outboundService.countOutboundList(cri);
        PageMaker pageMaker = new PageMaker(cri, totalCount);

        model.addAttribute("outboundList", outboundList);
        model.addAttribute("cri", cri);
        model.addAttribute("pageMaker", pageMaker);
        return "product/outboundList";  // JSP 경로
    }
    }


