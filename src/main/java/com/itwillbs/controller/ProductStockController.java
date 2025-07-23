package com.itwillbs.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.ProductStockTransactionVO;
import com.itwillbs.domain.ProductStockVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.service.ProductStockService;

@Controller
public class ProductStockController {

    @Autowired
    private ProductStockService productStockService;

    @GetMapping("/product/stocklist")
    public String stockList(SearchCriteria cri, Model model) {
        List<ProductStockVO> stockList = productStockService.getStockList(cri);
        int totalCount = productStockService.getStockCount(cri);
        cri.setTotalCount(totalCount);
        
     // ✅ PageMaker 설정
        PageMaker pageMaker = new PageMaker(cri, totalCount);
        model.addAttribute("pageMaker", pageMaker);
        
        // 날짜 계산 추가
        LocalDate today = LocalDate.now();
        LocalDate expiredLimitDate = today.plusDays(7); // 7일 후

        // 포맷 문자열로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        model.addAttribute("today", today.format(formatter));
        model.addAttribute("expiredLimitDate", expiredLimitDate.format(formatter));

        model.addAttribute("stockList", stockList);
        model.addAttribute("cri", cri);
        return "product/stockList";
    }
    
    //모달창 입출고 리스트
    @GetMapping("/product/transaction")
    @ResponseBody
    public List<ProductStockTransactionVO> getStockDetail(
            @RequestParam("product") String productId,
            @RequestParam("lot") String lotNo) {
        return productStockService.getStockDetail(productId, lotNo);
    }

    
}
