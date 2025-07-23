package com.itwillbs.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itwillbs.domain.ProductStockTransactionVO;
import com.itwillbs.domain.ProductStockVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.persistence.ProductStockDAO;

@Service
public class ProductStockServiceImpl implements ProductStockService {

    @Autowired
    private ProductStockDAO productStockDAO;

    @Override
    public List<ProductStockVO> getStockList(SearchCriteria cri) {
        return productStockDAO.getStockList(cri);
    }

    @Override
    public int getStockCount(SearchCriteria cri) {
        return productStockDAO.getStockCount(cri);
    }
    
    //입출고리스트
    @Override
    public List<ProductStockTransactionVO> getStockDetail(String productId, String lotNo) {
        return productStockDAO.getStockDetail(productId, lotNo);
    }

    
    

}
