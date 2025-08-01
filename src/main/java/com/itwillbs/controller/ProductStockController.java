package com.itwillbs.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

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
import com.itwillbs.dto.LotStockDTO;
import com.itwillbs.service.ProductStockService;

@Controller
public class ProductStockController {

    @Autowired
    private ProductStockService productStockService;

    @GetMapping("/product/stocklist")
    public String stockList(SearchCriteria cri, Model model) {

        // ✅ 허용된 정렬 컬럼
        List<String> allowedSortColumns = Arrays.asList("product_name", "lot_no", "reg_date", "expire_date");

        // ✅ 정렬 컬럼 유효성 검사
        if (cri.getSortColumn() == null || !allowedSortColumns.contains(cri.getSortColumn())) {
            cri.setSortColumn("reg_date"); // 기본 정렬
        }

        // ✅ 정렬 순서 유효성 검사
        if (!"asc".equalsIgnoreCase(cri.getSortOrder()) && !"desc".equalsIgnoreCase(cri.getSortOrder())) {
            cri.setSortOrder("desc");
        }

        // ✅ 데이터 조회
        List<ProductStockVO> stockList = productStockService.getStockList(cri);
        int totalCount = productStockService.getStockCount(cri);
        cri.setTotalCount(totalCount);

        PageMaker pageMaker = new PageMaker(cri, totalCount);
        model.addAttribute("pageMaker", pageMaker);

        // 날짜 계산
        LocalDate today = LocalDate.now();
        LocalDate expiredLimitDate = today.plusDays(7);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        model.addAttribute("today", today.format(formatter));
        model.addAttribute("expiredLimitDate", expiredLimitDate.format(formatter));

        model.addAttribute("stockList", stockList);
        model.addAttribute("cri", cri);

        return "product/stockList";
    }

    // ✅ 모달 상세 내역
    @GetMapping("/product/transaction")
    @ResponseBody
    public List<ProductStockTransactionVO> getStockDetail(@RequestParam("lot") String lotNo) {
        System.out.println("받은 lotNo : " + lotNo);
        return productStockService.getStockDetailByLot(lotNo);
    }


    
 // ✅ 제품별 LOT별 가용 재고 조회 (출고/예약 고려)
    @GetMapping("/product/available-lots")
    @ResponseBody
    public List<LotStockDTO> getAvailableLots(@RequestParam("productId") String productId) {
        return productStockService.getAvailableLotsOrdered(productId);
    }


}
