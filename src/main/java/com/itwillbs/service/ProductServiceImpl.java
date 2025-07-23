package com.itwillbs.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itwillbs.domain.ProductVO;
import com.itwillbs.persistence.ProductDAO;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDAO productDAO;

    @Override
    public List<ProductVO> getAllProducts() {
        return productDAO.getAllProducts();
    }
}
