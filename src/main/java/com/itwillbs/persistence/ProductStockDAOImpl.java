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

    private static final String NAMESPACE = "com.itwillbs.mapper.ProductStockMapper";

    // ✅ 재고현황
    @Override
    public List<ProductStockVO> getStockList(SearchCriteria cri) {
        return sqlSession.selectList(NAMESPACE + ".getStockList", cri);
    }

    @Override
    public int getStockCount(SearchCriteria cri) {
        return sqlSession.selectOne(NAMESPACE + ".getStockCount", cri);
    }

    // ✅ 입출고 수량 업데이트
    @Override
    public void upsertStockQty(String productId, String lotNo, int qty) {
        Map<String, Object> param = new HashMap<>();
        param.put("productId", productId);
        param.put("lotNo", lotNo);
        param.put("qty", qty);
        sqlSession.insert(NAMESPACE + ".upsertStockQty", param);
    }

    @Override
    public void decreaseStockQty(String productId, String lotNo, int qty) {
        Map<String, Object> param = new HashMap<>();
        param.put("productId", productId);
        param.put("lotNo", lotNo);
        param.put("qty", qty);
        sqlSession.update(NAMESPACE + ".decreaseStockQty", param);
    }

    @Override
    public void decreaseStockQty(Map<String, Object> stockParam) {
        sqlSession.update(NAMESPACE + ".decreaseStockQty", stockParam);
    }

    @Override
    public void insertOrUpdateStock(Map<String, Object> param) {
        sqlSession.insert(NAMESPACE + ".insertOrUpdateStock", param);
    }

    // ✅ 재고 예약 증감
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

    // ✅ 입출고 이력 조회
    @Override
    public List<ProductStockTransactionVO> getStockDetail(String productId, String lotNo) {
        return sqlSession.selectList(NAMESPACE + ".getStockDetail", Map.of(
            "productId", productId,
            "lotNo", lotNo
        ));
    }

    @Override
    public List<ProductStockTransactionVO> getLotHistoryByLot(String lotNo) {
        return sqlSession.selectList(NAMESPACE + ".getLotHistoryByLot", lotNo);
    }

    @Override
    public ProductStockVO getLotSummary(String lotNo) {
        return sqlSession.selectOne(NAMESPACE + ".getLotSummary", lotNo);
    }

    @Override
    public List<LotStockDTO> getAvailableLotsOrdered(String productId) {
        return sqlSession.selectList(NAMESPACE + ".getAvailableLotsOrdered", productId);
    }

    // ✅ 입출고 이력 저장
    @Override
    public void insertTransaction(ProductStockTransactionVO tx) {
        // 중복 확인
        Map<String, Object> param = new HashMap<>();
        param.put("type", tx.getType());
        param.put("lotNo", tx.getLotNo());
        param.put("qty", tx.getQty());
        param.put("productId", tx.getProductId());
        param.put("clientId", tx.getClientId());
        param.put("inboundId", tx.getInboundId());
        param.put("outboundId", tx.getOutboundId());
        param.put("clOrderId", tx.getClOrderId());

        boolean exists = sqlSession.selectOne(NAMESPACE + ".existsTransaction", param);
        if (exists) {
            System.out.println("⚠️ 이미 같은 재고 이력이 존재합니다. 중복 저장 방지됨.");
            return;
        }

        // 등록
        if (tx.getRegDate() == null) {
            tx.setRegDate(new Date());
        }

        sqlSession.insert(NAMESPACE + ".insertTransaction", tx);
    }

    @Override
    public boolean existsTransaction(Map<String, Object> param) {
        return sqlSession.selectOne(NAMESPACE + ".existsTransaction", param);
    }

    // ✅ 출하 취소시 LOT 재고 복원
    @Override
    public void increaseLotStock(String productId, String lotNo, int qty) {
        Map<String, Object> param = new HashMap<>();
        param.put("productId", productId);
        param.put("lotNo", lotNo);
        param.put("qty", qty);
        sqlSession.update(NAMESPACE + ".increaseLotStock", param);
    }
    
    
 // 모달용: 입·출고 이력 (입고번호/출고번호/수주번호 포함)
    @Override
    public List<ProductStockTransactionVO> getStockDetail(String lotNo) {
        return sqlSession.selectList(NAMESPACE + ".getStockDetail", lotNo);
    }

    
    
}
