package com.itwillbs.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.MaterialInboundItemVO;
import com.itwillbs.domain.MaterialInboundVO;
import com.itwillbs.domain.MaterialInventoryVO;
import com.itwillbs.domain.MaterialOrderItemVO;
import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialInboundItemDTO;
import com.itwillbs.dto.MaterialInboundSummaryDTO;
import com.itwillbs.dto.UnreceivedOrderDTO;

@Repository
public class MaterialInboundDAOImpl implements MaterialInboundDAO {
	
	@Inject
	private SqlSession sqlSession;

	private static final String NAMESPACE = "com.itwillbs.mapper.MaterialInboundMapper.";


	// 입고 목록 조회
	@Override
	public List<MaterialInboundSummaryDTO> getInboundList(SearchCriteria cri) throws Exception {

		return sqlSession.selectList(NAMESPACE + "getInboundList", cri);
	}


	// 목록 전체 수 조회
	@Override
	public int getInboundListCount(SearchCriteria cri) throws Exception {

		return sqlSession.selectOne(NAMESPACE + "getInboundListCount", cri);
	}

	// material_order 기준으로 아직 입고기록이 없는 발주건만 조회
	@Override
    public List<MaterialOrderVO> selectPendingInboundOrders() throws Exception {
        return sqlSession.selectList(NAMESPACE + "selectPendingInboundOrders");
    }
	
	/**
     * 아직 입고되지 않은 발주건만 조회
     * - 입고항목 테이블에 존재하지 않는 발주항목만 필터링
     */
	@Override
	public List<UnreceivedOrderDTO> getUnreceivedOrdersPaging(SearchCriteria cri) throws Exception {
	    return sqlSession.selectList(NAMESPACE + "selectUnreceivedOrdersPaging", cri);
	}

	@Override
	public int getUnreceivedOrdersCount() throws Exception {
	    return sqlSession.selectOne(NAMESPACE + "countUnreceivedOrders");
	}


	// 미입고 DB 등록
	// DAOImpl
	@Override
	public List<MaterialOrderItemVO> getUnreceivedOrderItems() throws Exception {
	    // 아직 입고되지 않은 발주항목만 조회 (order_item_id 기준으로 join 체크)
	    return sqlSession.selectList(NAMESPACE + "selectUnreceivedOrderItems");
	}

	@Override
	public String generateInboundId() throws Exception {
	    // 오늘 날짜 기준으로 IN-RM-yyyyMMdd-001 형식의 입고ID 생성
	    return sqlSession.selectOne(NAMESPACE + "generateInboundId");
	}

	@Override
	public void insertMaterialInbound(MaterialInboundVO vo) throws Exception {
	    sqlSession.insert(NAMESPACE + "insertMaterialInbound", vo);
	}

	@Override
	public void insertMaterialInboundItem(MaterialInboundItemVO vo) throws Exception {
	    sqlSession.insert(NAMESPACE + "insertMaterialInboundItem", vo);
	}



	// MaterialInboundDAOImpl.java에 추가할 메서드
	@Override
	public List<MaterialOrderItemVO> getUnreceivedOrderItemsByOrderId(String orderId) throws Exception {
	    return sqlSession.selectList(NAMESPACE + "getUnreceivedOrderItemsByOrderId", orderId);
	}
	
	// inbound_item_id 자동 생성용
	@Override
	public String getLatestInboundItemId(String prefix) throws Exception {
	    return sqlSession.selectOne(NAMESPACE + "getLatestInboundItemId", prefix);
	}

	// 입고ID로 입고 항목 목록 조회
	@Override
	public List<MaterialInboundItemVO> getInboundItemsByInboundId(String inboundId) throws Exception {
	    return sqlSession.selectList(NAMESPACE + "getInboundItemsByInboundId", inboundId);
	}

	// 입고 완료 처리
	@Override
	public void updateInboundStatusCompleted(String inboundId) throws Exception {
	    sqlSession.update(NAMESPACE + "updateInboundStatusCompleted", inboundId);
	}

	
	
	
	// LOT, 유통기한, 수량, 창고코드 등 포함하여 입고 상세 항목 갱신
	@Override
	public void updateInboundItem(MaterialInboundItemDTO dto) throws Exception {
	    sqlSession.update(NAMESPACE + "updateInboundItem", dto);
	}

	// 자재 ID + 창고코드 기준으로 재고 존재 여부 확인
	@Override
	public boolean checkInventoryExists(String materialId, String warehouseCode) throws Exception {
	    Map<String, Object> param = new HashMap<>();
	    param.put("materialId", materialId);
	    param.put("warehouseCode", warehouseCode);

	    Integer result = sqlSession.selectOne(NAMESPACE + "checkInventoryExists", param);
	    return result != null && result > 0;
	}

	// 기존 재고에 입고 수량 추가
	@Override
	public void updateInventoryQuantity(String materialId, String warehouseCode, int quantity) throws Exception {
	    Map<String, Object> param = new HashMap<>();
	    param.put("materialId", materialId);
	    param.put("warehouseCode", warehouseCode);
	    param.put("quantity", quantity);

	    sqlSession.update(NAMESPACE + "updateInventoryQuantity", param);
	}

	// 재고 테이블 신규 등록
	@Override
	public void insertInventory(String materialId, String warehouseCode, int quantity) throws Exception {
	    Map<String, Object> param = new HashMap<>();
	    param.put("materialId", materialId);
	    param.put("warehouseCode", warehouseCode);
	    param.put("quantity", quantity);

	    sqlSession.insert(NAMESPACE + "insertInventory", param);
	}

	// 입고 마스터 상태 및 입고일자 갱신 (전체 항목 기준)
	@Override
	public void updateInboundMasterStatus(String inboundId) throws Exception {
	    sqlSession.update(NAMESPACE + "updateInboundMasterStatus", inboundId);
	}

	
	@Override
	public void markItemAsReceived(String inboundItemId) throws Exception {
	    sqlSession.update(NAMESPACE + "markItemAsReceived", inboundItemId);
	}

	
	// 입고 마스터 조회
	@Override
	public MaterialInboundVO getInboundMaster(String inboundId) {
	    return sqlSession.selectOne(NAMESPACE + "getInboundMaster", inboundId);
	}
	
	// 발주 정보 조회
	@Override
	public MaterialOrderVO getOrderInfoByOrderId(String orderId) {
	    return sqlSession.selectOne(NAMESPACE + "getOrderInfoByOrderId", orderId);
	}
	
	
	// 재고 ID 자동 생성 (형식: INV-RM-YYYYMMDD-001)
	@Override
	public void insertInventory(MaterialInventoryVO vo) {
	    sqlSession.insert(NAMESPACE + "insertInventory", vo);
	}

	@Override
	public int getTodayInventorySequence(String date) {
	    return sqlSession.selectOne(NAMESPACE + "getTodayInventorySequence", date);
	}

	
	// 발주 항목 단건 조회 (추가입고용)
	@Override
	public MaterialOrderItemVO getOrderItemById(String orderItemId) throws Exception {
		
	    return sqlSession.selectOne(NAMESPACE + "getOrderItemById", orderItemId);
	}
	
	// 누적 입고 수량 조회 (order_item_id 기준)
	@Override
	public int getTotalReceivedQtyByOrderItemId(String orderItemId) throws Exception {
		
	    Integer totalQty = sqlSession.selectOne(NAMESPACE + "getTotalReceivedQtyByOrderItemId", orderItemId);
	    return totalQty != null ? totalQty : 0;
	}


	// 특정 order_item_id의 누적 입고 수량 합계 반환
	@Override
	public int getTotalInboundQuantity(String orderItemId) {
	    return sqlSession.selectOne(NAMESPACE + "getTotalInboundQuantity", orderItemId);
	}
	
	@Override
	public int getReceivedQtyByInboundItemId(String inboundItemId) throws Exception {
		
	    return sqlSession.selectOne(NAMESPACE + "getReceivedQtyByInboundItemId", inboundItemId);
	}

	
	
}
