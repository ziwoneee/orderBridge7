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
 // 제품코드 자동생성 (기존 DAO 그대로: selectLastProductId()가 'FG-123' 반환)
    @Override
    public String createNextProductId() {
        String lastId = productMasterDAO.selectLastProductId(); 
        int nextNo = 1;

        if (lastId != null && lastId.startsWith("FG-")) {
            try {
                // 안전하게 하이픈 다음부터 자르기
                int pos = lastId.indexOf('-');
                String numPart = (pos >= 0 && pos + 1 < lastId.length())
                        ? lastId.substring(pos + 1) : "";
                nextNo = Integer.parseInt(numPart) + 1;
            } catch (Exception ignore) {
                // 파싱 실패 시 nextNo=1 유지
            }
        }
        return String.format("FG-%03d", nextNo);
    }

    

    //제품 소프트 삭제
    
    @Override
    public void softDeleteProduct(String productId) {
        productMasterDAO.softDeleteProduct(productId);
    }

    
}




