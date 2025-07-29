package com.itwillbs.persistence;

import com.itwillbs.domain.StockReservationVO;
import com.itwillbs.dto.LotStockDTO;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class StockReservationDAOImpl implements StockReservationDAO {

    @Autowired
    private SqlSession sqlSession;

    private static final String NAMESPACE = "com.itwillbs.mapper.StockReservationMapper";

    @Override
    public void insertReservation(StockReservationVO vo) {
        sqlSession.insert(NAMESPACE + ".insertReservation", vo);
    }

    @Override
    public void deleteReservation(String clOrderId, String productId) {
        // 예약 해제는 수주번호 + 제품ID 기준
        java.util.Map<String, Object> param = new java.util.HashMap<>();
        param.put("clOrderId", clOrderId);
        param.put("productId", productId);
        sqlSession.delete(NAMESPACE + ".deleteReservation", param);
    }

    @Override
    public int getReservedQty(String clOrderId, String productId) {
        java.util.Map<String, Object> param = new java.util.HashMap<>();
        param.put("clOrderId", clOrderId);
        param.put("productId", productId);
        return sqlSession.selectOne(NAMESPACE + ".getReservedQty", param);
    }

    @Override
    public List<StockReservationVO> getReservationsByOrderId(String clOrderId) {
        return sqlSession.selectList(NAMESPACE + ".getReservationsByOrderId", clOrderId);
    }
    
    @Override
    public void deleteByOrderId(String clOrderId) {
        sqlSession.delete(NAMESPACE + ".deleteByOrderId", clOrderId);
    }
    
    @Override
    public List<LotStockDTO> getAvailableLotsOrdered(String productId) {
        return sqlSession.selectList(NAMESPACE + ".getAvailableLotsOrdered", productId);
    }
    
    @Override
    public int getReservedQtyByProductAndLot(Map<String, Object> param) {
        return sqlSession.selectOne(NAMESPACE + ".getReservedQtyByProductAndLot", param);
    }
    
    @Override
    public List<String> getAllReservedOrderIds() {
        return sqlSession.selectList("com.itwillbs.mapper.StockReservationMapper.getAllReservedOrderIds");
    }

    
}