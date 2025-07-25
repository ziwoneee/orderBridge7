package com.itwillbs.persistence;

import com.itwillbs.domain.ClientDeliveryVO;
import com.itwillbs.dto.ShipmentPendingDTO;
import com.itwillbs.dto.ShipmentPendingGroupDTO;
import com.itwillbs.dto.ShipmentProductDTO;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClientDeliveryDAOImpl implements ClientDeliveryDAO {

    @Autowired
    private SqlSession sqlSession;

    private static final String NAMESPACE = "com.itwillbs.mapper.ClientDeliveryMapper";

    // 1. 출하대기 목록 - 평면 리스트
    @Override
    public List<ShipmentPendingDTO> selectPendingShipmentList() {
        return sqlSession.selectList(NAMESPACE + ".selectPendingShipmentList");
    }

    // 2. 출하대기 목록 - 그룹 리스트
    @Override
    public List<ShipmentPendingGroupDTO> getPendingShipmentGroupedList() {
        return sqlSession.selectList(NAMESPACE + ".getPendingShipmentGroupedList");
    }

    // 3. 출하처리 대상 1건 조회
    @Override
    public ShipmentPendingDTO selectShipmentItem(Long orderDetailId) {
        return sqlSession.selectOne(NAMESPACE + ".selectShipmentItem", orderDetailId);
    }

    // 4. 출하 등록
    @Override
    public void insertDelivery(ClientDeliveryVO vo) {
        sqlSession.insert(NAMESPACE + ".insertDelivery", vo);
    }

    // 5. 재고 차감 처리
    @Override
    public void decreaseStock(String productId, int qty) {
        sqlSession.update(NAMESPACE + ".decreaseStock", 
            new java.util.HashMap<String, Object>() {{
                put("productId", productId);
                put("qty", qty);
            }});
    }

    // 6. 수주상세 상태 변경
    @Override
    public void updateOrderDetailStatus(Long orderDetailId, String status) {
        sqlSession.update(NAMESPACE + ".updateOrderDetailStatus", 
            new java.util.HashMap<String, Object>() {{
                put("orderDetailId", orderDetailId);
                put("status", status);
            }});
    }

    // 7. 출하대기 - 평탄화 리스트 (그룹화용)
    @Override
    public List<ShipmentProductDTO> selectPendingShipmentFlatList() {
        return sqlSession.selectList(NAMESPACE + ".selectPendingShipmentFlatList");
    }
}
