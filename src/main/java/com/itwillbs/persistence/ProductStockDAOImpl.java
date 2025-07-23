package com.itwillbs.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.ProductStockTransactionVO;
import com.itwillbs.domain.ProductStockVO;
import com.itwillbs.domain.SearchCriteria;

@Repository
public class ProductStockDAOImpl implements ProductStockDAO {

    @Autowired
    private SqlSession sqlSession;
    
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



    
    
}
