package com.itwillbs.persistence;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.ProductStockTransactionVO;
import com.itwillbs.domain.ProductStockVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.LotStockDTO;

@Repository
public class ProductStockDAOImpl implements ProductStockDAO {

    @Autowired
    private SqlSession sqlSession;
    
    @Autowired
    private ProductStockDAO productStockDAO;

    
       
    private static final String NAMESPACE = "com.itwillbs.mapper.ProductStockMapper";

    
    //재고리스트
    @Override
    public List<ProductStockVO> getStockList(SearchCriteria cri) {
        return sqlSession.selectList(NAMESPACE + ".getStockList", cri);
    }

    @Override
    public int getStockCount(SearchCriteria cri) {
        return sqlSession.selectOne(NAMESPACE + ".getStockCount", cri);
    }
    
    //입출고 업데이트
    @Override
    public void upsertStockQty(String productId, String lotNo, int qty) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("productId", productId);
        paramMap.put("lotNo", lotNo);
        paramMap.put("qty", qty);
        sqlSession.insert(NAMESPACE + ".upsertStockQty", paramMap);
    }

    @Override
    public void decreaseStockQty(String productId, String lotNo, int qty) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("productId", productId);
        paramMap.put("lotNo", lotNo);
        paramMap.put("qty", qty);
        sqlSession.update(NAMESPACE + ".decreaseStockQty", paramMap);
    }
    
    
    //입출고 모달창
    @Override
    public List<ProductStockTransactionVO> getStockDetail(String productId, String lotNo) {
        return sqlSession.selectList("ProductStockMapper.getStockDetail", Map.of(
            "productId", productId,
            "lotNo", lotNo
        ));
    }

    /**
     * ✅ 유통기한 빠른 순으로 LOT별 가용 재고 목록 조회
     */
    @Override
    public List<LotStockDTO> getAvailableLotsOrdered(String productId) {
        return sqlSession.selectList(NAMESPACE + ".getAvailableLotsOrdered", productId);
    }
    
    // ✅ LOT 번호로 입출고 이력 조회
    @Override
    public List<ProductStockTransactionVO> getLotHistoryByLot(String lotNo) {
        return sqlSession.selectList(NAMESPACE + ".getLotHistoryByLot", lotNo);
    }
    
    @Override
    public ProductStockVO getLotSummary(String lotNo) {
        return sqlSession.selectOne(NAMESPACE + ".getLotSummary", lotNo);
    }
    
    //재고 저장
    @Override
    public void insertTransaction(String type, String lotNo, int qty, String productId, String clientId, String manager) {
        ProductStockTransactionVO tx = new ProductStockTransactionVO();
        tx.setType(type);
        tx.setLotNo(lotNo);
        tx.setQty(qty);
        tx.setProductId(productId);
        tx.setClientId(clientId);
        tx.setManager(manager);
        tx.setRegDate(new Date());
        
        productStockDAO.insertTransaction(tx);

        // ✅ product_stock 테이블 업데이트
        Map<String, Object> stockParam = new HashMap<>();
        stockParam.put("productId", productId);
        stockParam.put("lotNo", lotNo);
        stockParam.put("qty", qty);

        if ("입고".equals(type)) {
            productStockDAO.insertOrUpdateStock(stockParam); // 아래에 Mapper 예시 나옵니다
        } else if ("출고".equals(type)) {
            productStockDAO.decreaseStockQty(stockParam);
        }
    }


    
    //예약시 수량 증감
    @Override
    public void increaseReservedQty(String productId, String lotNo, int qty) {
        Map<String, Object> param = new HashMap<>();
        param.put("productId", productId);
        param.put("lotNo", lotNo);
        param.put("qty", qty);
        sqlSession.update(NAMESPACE + ".increaseReservedQty", param);
    }

    @Override
    public void decreaseReservedQty(String productId, String lotNo, int qty) {
        Map<String, Object> param = new HashMap<>();
        param.put("productId", productId);
        param.put("lotNo", lotNo);
        param.put("qty", qty);
        sqlSession.update(NAMESPACE + ".decreaseReservedQty", param);
    }
    
    @Override
    public void insertOrUpdateStock(Map<String, Object> param) {
        sqlSession.insert(NAMESPACE + ".insertOrUpdateStock", param);
    }

    @Override
    public void insertTransaction(ProductStockTransactionVO tx) {
        sqlSession.insert(NAMESPACE + ".insertTransaction", tx);
    }


    @Override
    public void decreaseStockQty(Map<String, Object> stockParam) {
        sqlSession.update(NAMESPACE + ".decreaseStockQty", stockParam);
    }





    
}
