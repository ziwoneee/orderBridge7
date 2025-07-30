package com.itwillbs.persistence;

import com.itwillbs.domain.ClientDeliveryVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.LotStockDTO;
import com.itwillbs.dto.ShipmentCompletedDTO;
import com.itwillbs.dto.ShipmentPendingDTO;
import com.itwillbs.dto.ShipmentPendingGroupDTO;
import com.itwillbs.dto.ShipmentProductDTO;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ClientDeliveryDAOImpl implements ClientDeliveryDAO {

    private static final String NAMESPACE = "com.itwillbs.mapper.ClientDeliveryMapper";

    @Autowired
    private SqlSession sqlSession;

       /** ✅ 출하 대기 목록 (수주번호 그룹) */
    @Override
    public List<ShipmentPendingGroupDTO> getPendingShipmentGroupedList() {
        return sqlSession.selectList(NAMESPACE + ".getPendingShipmentGroupedList");
    }

    /** ✅ 출하 대기 목록 (제품기준 평면 - 사용 여부에 따라) */
    @Override
    public List<ShipmentProductDTO> selectPendingShipmentFlatList() {
        return sqlSession.selectList(NAMESPACE + ".selectPendingShipmentFlatList");
    }

    /** ✅ 수주 상세 항목 단건 조회 */
    @Override
    public ShipmentPendingDTO selectShipmentItem(Long orderDetailId) {
        return sqlSession.selectOne(NAMESPACE + ".selectShipmentItem", orderDetailId);
    }

    /** ✅ 출하 등록 */
    @Override
    public void insertDelivery(ClientDeliveryVO vo) {
        sqlSession.insert(NAMESPACE + ".insertDelivery", vo);
    }

    /** ✅ LOT별 재고 차감 */
    @Override
    public void decreaseLotStock(String productId, String lotNo, int qty) {
        Map<String, Object> param = new HashMap<>();
        param.put("productId", productId);
        param.put("lotNo", lotNo);
        param.put("qty", qty);
        sqlSession.update(NAMESPACE + ".decreaseLotStock", param);
    }

    /** ✅ 전체 재고 차감 (LOT 무시) */
    @Override
    public void decreaseStock(String productId, int qty) {
        Map<String, Object> param = new HashMap<>();
        param.put("productId", productId);
        param.put("qty", qty);
        sqlSession.update(NAMESPACE + ".decreaseStock", param);
    }

    /** ✅ 수주 상세 상태 업데이트 */
    @Override
    public void updateOrderDetailStatus(Long orderDetailId, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("orderDetailId", orderDetailId);
        param.put("status", status);
        sqlSession.update(NAMESPACE + ".updateOrderDetailStatus", param);
    }

    /** ✅ 수주 마스터 상태 업데이트 */
    @Override
    public void updateClientOrderStatus(String clOrderId, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("clOrderId", clOrderId);
        param.put("status", status);
        sqlSession.update(NAMESPACE + ".updateClientOrderStatus", param);
    }

    /** ✅ 제품 LOT별 잔여 재고 조회 */
    @Override
    public List<LotStockDTO> getAvailableLots(String productId) {
        return sqlSession.selectList(NAMESPACE + ".getAvailableLots", productId);
    }

    /** ✅ 수주번호 기준 출하 대기 항목 조회 */
    @Override
    public List<ShipmentPendingDTO> selectItemsByOrderId(String clOrderId) {
        return sqlSession.selectList(NAMESPACE + ".selectItemsByOrderId", clOrderId);
    }

    /** ✅ 출하되지 않은 수주 상세 건수 확인 */
    @Override
    public int countUnshippedDetails(String clOrderId) {
        return sqlSession.selectOne(NAMESPACE + ".countUnshippedDetails", clOrderId);
    }

    /** ✅ 수주 마스터 상태 변경 (선택적으로 별도 호출 가능) */
    @Override
    public void updateOrderStatus(String clOrderId, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("clOrderId", clOrderId);
        param.put("status", status);
        sqlSession.update(NAMESPACE + ".updateOrderStatus", param);
    }
    /**출하 완료 목록*/
    // ✅ 출하 완료 목록 조회 (검색 + 정렬 + 페이징)
    @Override
    public List<ShipmentCompletedDTO> searchCompletedShipmentList(SearchCriteria cri) {
        return sqlSession.selectList(NAMESPACE + ".searchCompletedShipmentList", cri);
    }

    // ✅ 출하 완료 목록 카운트
    @Override
    public int countCompletedShipmentList(SearchCriteria cri) {
        return sqlSession.selectOne(NAMESPACE + ".countCompletedShipmentList", cri);
    }

    @Override
    public void updateOrderDetailStatus(int detailId, String status) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("detailId", detailId);
        paramMap.put("status", status);
        sqlSession.update(NAMESPACE + ".updateOrderDetailStatus", paramMap);
    }

    
    /** ✅ 출하 대기 목록 (검색 + 정렬 + 페이징 지원) */
    @Override
    public List<ShipmentPendingGroupDTO> searchPendingGroupedList(SearchCriteria cri) {
        return sqlSession.selectList(NAMESPACE + ".searchPendingGroupedList", cri);
    }

    /** ✅ 출하 대기 목록 총 개수 (검색 조건 포함) */
    @Override
    public int countPendingGroupedList(SearchCriteria cri) {
        return sqlSession.selectOne(NAMESPACE + ".countPendingGroupedList", cri);
    }
    
}
