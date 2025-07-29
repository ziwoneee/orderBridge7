package com.itwillbs.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itwillbs.domain.ProductStockTransactionVO;
import com.itwillbs.domain.ProductStockVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.LotStockDTO;
import com.itwillbs.persistence.ProductStockDAO;
import com.itwillbs.persistence.StockReservationDAO;

@Service
public class ProductStockServiceImpl implements ProductStockService {

    @Autowired
    private ProductStockDAO productStockDAO;

    @Autowired
    private StockReservationDAO stockReservationDAO; // ✅ 예약 DAO 추가

    @Override
    public List<ProductStockVO> getStockList(SearchCriteria cri) {
        List<ProductStockVO> list = productStockDAO.getStockList(cri);

        for (ProductStockVO vo : list) {
            // ✅ 예약 수량 조회
            Map<String, Object> param = new HashMap<>();
            param.put("productId", vo.getProductId());
            param.put("lotNo", vo.getLotNo());

            int reservedQty = stockReservationDAO.getReservedQtyByProductAndLot(param);
            vo.setReservedQty(reservedQty); // VO에 세팅
            vo.setAvailableQty(vo.getStockQty() - reservedQty); // 가용 수량 계산
        }

        return list;
    }

    @Override
    public int getStockCount(SearchCriteria cri) {
        return productStockDAO.getStockCount(cri);
    }

    @Override
    public List<ProductStockTransactionVO> getStockDetail(String productId, String lotNo) {
        return productStockDAO.getStockDetail(productId, lotNo);
    }

    @Override
    public List<LotStockDTO> getAvailableLotsOrdered(String productId) {
        return productStockDAO.getAvailableLotsOrdered(productId);
    }
}
