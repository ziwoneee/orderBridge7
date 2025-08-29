package com.itwillbs.persistence;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.StockReservationVO;
import com.itwillbs.dto.LotStockDTO;
import com.itwillbs.dto.ReservationDetailDTO;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class StockReservationDAOImpl implements StockReservationDAO {

    @Autowired
    private SqlSession sqlSession;

    private static final String NAMESPACE = "com.itwillbs.mapper.StockReservationMapper";

    /**
     * ✅ 예약 등록
     */
    @Override
    public void insertReservation(StockReservationVO vo) {
        sqlSession.insert(NAMESPACE + ".insertReservation", vo);
    }

    /**
     * ✅ 특정 수주번호 + 제품에 대한 예약 해제
     */
    @Override
    public void deleteReservation(String clOrderId, String productId) {
        Map<String, Object> param = new java.util.HashMap<>();
        param.put("clOrderId", clOrderId);
        param.put("productId", productId);
        sqlSession.delete(NAMESPACE + ".deleteReservation", param);
    }

    /**
     * ✅ 수주번호 + 제품 기준 예약 수량 조회
     */
    @Override
    public int getReservedQty(String clOrderId, String productId) {
        Map<String, Object> param = new java.util.HashMap<>();
        param.put("clOrderId", clOrderId);
        param.put("productId", productId);
        return sqlSession.selectOne(NAMESPACE + ".getReservedQty", param);
    }

    /**
     * ✅ 수주번호 기준 전체 예약 목록 조회
     */
    @Override
    public List<StockReservationVO> getReservationsByOrderId(String clOrderId) {
        return sqlSession.selectList(NAMESPACE + ".getReservationsByOrderId", clOrderId);
    }

    /**
     * ✅ 수주번호 기준 전체 예약 삭제
     */
    @Override
    public void deleteByOrderId(String clOrderId) {
        sqlSession.delete(NAMESPACE + ".deleteByOrderId", clOrderId);
    }

    /**
     * ✅ 제품 기준 유통기한 오름차순 LOT별 가용 재고 조회
     */
    @Override
    public List<LotStockDTO> getAvailableLotsOrdered(String productId) {
        return sqlSession.selectList(NAMESPACE + ".getAvailableLotsOrdered", productId);
    }

    /**
     * ✅ 제품 + LOT 기준 현재 예약 수량 조회
     */
    @Override
    public int getReservedQtyByProductAndLot(Map<String, Object> param) {
        return sqlSession.selectOne(NAMESPACE + ".getReservedQtyByProductAndLot", param);
    }

    /**
     * ✅ 예약이 존재하는 전체 수주번호 목록 (중복 제거)
     */
    @Override
    public List<String> getAllReservedOrderIds() {
        return sqlSession.selectList(NAMESPACE + ".getAllReservedOrderIds");
    }

    /**
     * ✅ 특정 수주번호에 대한 예약 내역 존재 여부 (count 반환)
     */
    @Override
    public int countReservationsByOrderId(String clOrderId) {
        return sqlSession.selectOne(NAMESPACE + ".countReservationsByOrderId", clOrderId);
    }
    
    /**
     * ✅ 예약 내역 확인 
     */
    @Override
    public List<StockReservationVO> getFilteredReservationList(SearchCriteria cri) {
        return sqlSession.selectList(NAMESPACE + ".getFilteredReservationList", cri);
    }

    @Override
    public int countFilteredReservationList(SearchCriteria cri) {
        return sqlSession.selectOne(NAMESPACE + ".countFilteredReservationList", cri);
    }
    
    // ==========================
    // 모달 상세 (LOT + 수주 단건)
    // ==========================
    @Override
    public ReservationDetailDTO selectReservationDetailForModal(String lotNo, String clOrderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("lotNo", lotNo);
        params.put("clOrderId", clOrderId);
        return sqlSession.selectOne(NAMESPACE + ".selectReservationDetailForModal", params);
    }
    

}
