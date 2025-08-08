package com.itwillbs.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itwillbs.domain.ProductStockTransactionVO;
import com.itwillbs.domain.ProductStockVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.StockReservationVO;
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
    
    //입출고 모달
    @Override
    public List<ProductStockTransactionVO> getStockDetailByLot(String lotNo) {
    	return productStockDAO.getStockDetail(lotNo);
    }
    
    //재고저장
    @Override
    public void insertTransaction(String type, String lotNo, int qty, String productId,
            String clientId, String manager,
            String inboundId, String outboundId, String clOrderId) {
    	System.out.println("@@@@@@@@"+outboundId);

        // ✅ 중복 방지: Map에 파라미터 세팅
    	Map<String, Object> param = new HashMap<>();
    	param.put("type", type);
    	param.put("lotNo", lotNo);
    	param.put("qty", qty);
    	param.put("productId", productId);
    	param.put("clientId", clientId);
    	param.put("inboundId", inboundId);
    	param.put("outboundId", outboundId);
    	param.put("clOrderId", clOrderId);

    	boolean exists = productStockDAO.existsTransaction(param);
    	if (exists) {
    	    System.out.println("⚠️ 이미 같은 재고 이력이 존재합니다. 중복 저장 방지됨.");
    	    return;
    	}


        // ✅ 새로운 이력 저장
    	ProductStockTransactionVO tx = new ProductStockTransactionVO();
    	tx.setType(type);
    	tx.setLotNo(lotNo);
    	tx.setQty(qty);
    	tx.setProductId(productId);
    	tx.setClientId(clientId);
    	tx.setManager(manager);
    	tx.setRegDate(new Date());
    	tx.setInboundId(inboundId);      
    	tx.setOutboundId(outboundId);   
    	tx.setClOrderId(clOrderId);      

    	productStockDAO.insertTransaction(tx);

    }


    //
    @Override
    public ProductStockVO getLotSummary(String lotNo) {
        return productStockDAO.getLotSummary(lotNo);
    }

   
    
}
