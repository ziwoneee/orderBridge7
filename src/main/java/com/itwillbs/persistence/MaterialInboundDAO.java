package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.domain.MaterialInboundItemVO;
import com.itwillbs.domain.MaterialInboundVO;
import com.itwillbs.domain.MaterialOrderItemVO;
import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialInboundSummaryDTO;
import com.itwillbs.dto.UnreceivedOrderDTO;

public interface MaterialInboundDAO {
	
	// 입고 목록 조회
	List<MaterialInboundSummaryDTO> getInboundList(SearchCriteria cri) throws Exception;

	// 목록 전체 수 조회
	int getInboundListCount(SearchCriteria cri);  

	// 아직 입고되지 않은 발주건 리스트 조회
	List<MaterialOrderVO> selectPendingInboundOrders();

	// 미입고 발주건 목록 조회
	List<UnreceivedOrderDTO> getUnreceivedOrdersPaging(SearchCriteria cri);
	// 미입고 발주 전체 개수
	int getUnreceivedOrdersCount();


	// 미입고 발주건 DB 등록
	List<MaterialOrderItemVO> getUnreceivedOrderItems();
	String generateInboundId();
	void insertMaterialInbound(MaterialInboundVO vo);
	void insertMaterialInboundItem(MaterialInboundItemVO vo);

	/**
	 * 특정 발주 ID의 미입고 항목들만 조회
	 */
	List<MaterialOrderItemVO> getUnreceivedOrderItemsByOrderId(String orderId);

}
