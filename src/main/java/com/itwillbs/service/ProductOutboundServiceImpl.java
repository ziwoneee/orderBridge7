package com.itwillbs.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itwillbs.domain.ProductOutboundVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.persistence.ProductOutboundDAO;
import com.itwillbs.persistence.ProductStockDAO;

@Service
public class ProductOutboundServiceImpl implements ProductOutboundService {

    @Autowired
    private ProductOutboundDAO outboundDAO;

    @Autowired
    private ProductStockDAO stockDAO;

    @Transactional
    @Override
    public void registerOutbound(ProductOutboundVO vo) {
        outboundDAO.insertOutbound(vo);
        stockDAO.decreaseStockQty(vo.getProductId(), vo.getLotNo(), vo.getOutboundQty());
    }
    
    @Override
    public List<ProductOutboundVO> searchOutboundList(SearchCriteria cri) {
        return outboundDAO.searchOutboundList(cri);
    }

    @Override
    public int countOutboundList(SearchCriteria cri) {
        return outboundDAO.countOutboundList(cri);
    }

}
