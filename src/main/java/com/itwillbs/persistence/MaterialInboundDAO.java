package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.domain.MaterialInboundItemVO;
import com.itwillbs.domain.MaterialInboundVO;
import com.itwillbs.domain.MaterialInventoryVO;
import com.itwillbs.domain.MaterialOrderItemVO;
import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialInboundItemDTO;
import com.itwillbs.dto.MaterialInboundSummaryDTO;
import com.itwillbs.dto.UnreceivedOrderDTO;

public interface MaterialInboundDAO {
	
	// 입고 목록 조회
	List<MaterialInboundSummaryDTO> getInboundList(SearchCriteria cri) throws Exception;

	// 목록 전체 수 조회
	int getInboundListCount(SearchCriteria cri) throws Exception;  

	// 아직 입고되지 않은 발주건 리스트 조회
	List<MaterialOrderVO> selectPendingInboundOrders() throws Exception;

	// 미입고 발주건 목록 조회
	List<UnreceivedOrderDTO> getUnreceivedOrdersPaging(SearchCriteria cri) throws Exception;
	// 미입고 발주 전체 개수
	int getUnreceivedOrdersCount() throws Exception;


	// 미입고 발주건 DB 등록
	List<MaterialOrderItemVO> getUnreceivedOrderItems() throws Exception;
	String generateInboundId() throws Exception;
	
	// 특정 발주 ID의 미입고 항목들만 조회
	List<MaterialOrderItemVO> getUnreceivedOrderItemsByOrderId(String orderId) throws Exception;
	
	// inbound_item_id 자동 생성용
	String getLatestInboundItemId(String prefix) throws Exception;
	
	// 입고 완료 처리
	void updateInboundStatusCompleted(String inboundId) throws Exception;
	
	// LOT, 유통기한, 수량, 창고코드 등 포함하여 입고 상세 항목 갱신
	void updateInboundItem(MaterialInboundItemDTO dto) throws Exception;

	// 자재 ID + 창고코드 기준으로 재고 존재 여부 확인
	boolean checkInventoryExists(String materialId, String warehouseCode) throws Exception;

	// 기존 재고에 입고 수량 추가
	void updateInventoryQuantity(String materialId, String warehouseCode, int quantity) throws Exception;

	// 재고 테이블 신규 등록
	void insertInventory(String materialId, String warehouseCode, int quantity) throws Exception;

	// 입고 마스터 상태 및 입고일자 갱신 (전체 항목 기준)
	void updateInboundMasterStatus(String inboundId) throws Exception;
	
	// 입고 상세 항목 조회
	List<MaterialInboundItemVO> getInboundItemsByInboundId(String inboundId) throws Exception;

	// 개별 항목 입고 상태 '입고완료' + LOT 생성일자 갱신
	void markItemAsReceived(String inboundItemId) throws Exception;
	
	// 입고 마스터 조회
	MaterialInboundVO getInboundMaster(String inboundId) throws Exception;

	// 발주 정보 조회
	MaterialOrderVO getOrderInfoByOrderId(String orderId) throws Exception;
	
	// 재고 ID 자동 생성 (형식: INV-RM-YYYYMMDD-001)
	void insertInventory(MaterialInventoryVO vo) throws Exception;
	int getTodayInventorySequence(String date) throws Exception;
	

	// 입고 마스터 등록
	void insertMaterialInbound(MaterialInboundVO inbound) throws Exception;

	// 입고 상세 항목 등록
	void insertMaterialInboundItem(MaterialInboundItemVO item) throws Exception;
	
	// 발주 항목 단건 조회 (추가입고용)
	MaterialOrderItemVO getOrderItemById(String orderItemId) throws Exception;
	
	// 누적 입고 수량 조회
	int getTotalReceivedQtyByOrderItemId(String orderItemId) throws Exception;

	// 특정 order_item_id의 누적 입고 수량 조회 (부분입고/입고완료 여부 판단용)
	public int getTotalInboundQuantity(String orderItemId) throws Exception;
	
	int getReceivedQtyByInboundItemId(String inboundItemId) throws Exception;

	// 입고완료시 발주쪽 상태값 업데이트
	// inboundId로 orderId 조회
	String selectOrderIdByInboundId(String inboundId) throws Exception;

	// 모든 입고건이 '입고완료'일 때만 발주 상태 '입고완료'로 업데이트
	void updateOrderStatusToCompletedIfAllInboundDone(String orderId) throws Exception;
	
	// 창고 자동 매칭
	String getDefaultWarehouseByMaterialId(String materialId) throws Exception;


	int recalcUsageStatusByOutboundId(String outboundId);
    int recalcUsageStatusByInboundId(String inboundId);


}
