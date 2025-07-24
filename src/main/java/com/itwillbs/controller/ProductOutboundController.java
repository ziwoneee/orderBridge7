package com.itwillbs.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String registerOutbound(ProductOutboundVO vo) {
        outboundService.registerOutbound(vo);  // 출고 + 재고 감소
        return "redirect:/product/stocklist";
    }
    
          
        @GetMapping("/list")
        public String showOutboundList(@ModelAttribute SearchCriteria cri, Model model) {
            // 빈 문자열을 null로 처리
            if (cri.getStartDate() != null && cri.getStartDate().trim().isEmpty()) cri.setStartDate(null);
            if (cri.getEndDate() != null && cri.getEndDate().trim().isEmpty()) cri.setEndDate(null);

            List<ProductOutboundVO> outboundList = outboundService.searchOutboundList(cri);
            int totalCount = outboundService.countOutboundList(cri);
            PageMaker pageMaker = new PageMaker(cri, totalCount);

            model.addAttribute("outboundList", outboundList);
            model.addAttribute("cri", cri);
            model.addAttribute("pageMaker", pageMaker);
            return "product/outboundList";  // JSP 경로
        }
    }


