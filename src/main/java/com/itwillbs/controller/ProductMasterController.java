package com.itwillbs.controller;

import com.itwillbs.domain.ProductVO;
import com.itwillbs.service.ProductMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ProductMasterController {

    @Autowired
    private ProductMasterService productMasterService;

    
    //
    // 완제품 목록 조회
    @GetMapping("/master/product/list")
    public String getProductList(Model model) {
        List<ProductVO> productList = productMasterService.getAllProducts();
        model.addAttribute("productList", productList);
        return "master/productList"; // JSP or Thymeleaf view name
    }
    
    //제품 수정
    @PostMapping("/master/product/update")
    public String updateProduct(ProductVO productVO) {
        productMasterService.updateProduct(productVO);
        return "redirect:/master/product/list";
    }
    
    
    //제품 등록
    @PostMapping("/master/product/insert")
    public String insertProduct(ProductVO productVO) {
        // 자동생성 로직 (예: "FG-" + 3자리 일련번호)
        String nextProductId = productMasterService.createNextProductId();
        productVO.setProductId(nextProductId);
        productMasterService.insertProduct(productVO);
        return "redirect:/master/product/list";
    }
    
//    //제품 소프트삭제
//    @PostMapping("/master/product/delete")
//    public String softDeleteProduct(@RequestParam("productId") String productId) {
//        productMasterService.softDeleteProduct(productId);
//        return "redirect:/master/product/list";
//    }

    
    

}
