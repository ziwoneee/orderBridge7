package com.itwillbs.service;


import com.itwillbs.domain.ProductVO;
import com.itwillbs.persistence.ProductMasterDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductMasterServiceImpl implements ProductMasterService {

    @Autowired
    private ProductMasterDAO productMasterDAO;

    @Override
    public List<ProductVO> getAllProducts() {
        return productMasterDAO.selectAllProducts();
    }
    
    @Override
    public void insertProduct(ProductVO productVO) {
        productMasterDAO.insertProduct(productVO);
    }

    @Override
    public void updateProduct(ProductVO productVO) {
        productMasterDAO.updateProduct(productVO);
    }
    
    //제품코드 자동생성
    @Override
    public String createNextProductId() {
        String lastId = productMasterDAO.selectLastProductId(); 
        int nextNo = 1;
        if (lastId != null && lastId.startsWith("FG-")) {
            try {
                nextNo = Integer.parseInt(lastId.substring(3)) + 1;
            } catch(Exception e) {}
        }
        return String.format("FG-%03d", nextNo); 
    
    }
    

    //제품 소프트 삭제
    
    @Override
    public void softDeleteProduct(String productId) {
        productMasterDAO.softDeleteProduct(productId);
    }

    
}




