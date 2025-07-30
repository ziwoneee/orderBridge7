package com.itwillbs.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.itwillbs.domain.MaterialInboundItemVO;
import com.itwillbs.domain.MaterialInboundVO;
import com.itwillbs.domain.MaterialOrderItemVO;
import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialInboundSummaryDTO;
import com.itwillbs.dto.UnreceivedOrderDTO;
import com.itwillbs.persistence.MaterialInboundDAO;

@Service
public class MaterialInboundServiceImpl implements MaterialInboundService{
	
	@Inject
	private MaterialInboundDAO miDAO;

	// 입고 목록 조회
	@Override
	public List<MaterialInboundSummaryDTO> getInboundList(SearchCriteria cri) throws Exception {
		
		return miDAO.getInboundList(cri);
	}
	
	// 목록 전체 수 조회
	@Override
    public int getInboundListCount(SearchCriteria cri) {
		
        return miDAO.getInboundListCount(cri);
    }
	
	// 입고되지 않은 발주건들만 조회 (inbound에 없는 order)
	@Override
    public List<MaterialOrderVO> getPendingInboundOrders() {
        return miDAO.selectPendingInboundOrders();
    }
	
	/**
     * 아직 입고되지 않은 발주건 목록 조회
     * - 발주 항목 중 한 번도 입고처리된 적 없는 건만 조회
     */
	@Override
	public List<UnreceivedOrderDTO> getUnreceivedOrdersPaging(SearchCriteria cri) {
	    return miDAO.getUnreceivedOrdersPaging(cri);
	}

	@Override
	public int getUnreceivedOrdersCount() {
	    return miDAO.getUnreceivedOrdersCount();
	}
    
	
	/**
	 * 미입고 발주건을 material_inbound + material_inbound_item 테이블에 INSERT
	 * - 발주번호별로 입고ID(inbound_id) 생성 후 마스터 등록
	 * - 해당 발주에 속한 자재 항목들도 함께 입고 항목 테이블에 등록
	 */
	@Override
	public void insertUnreceivedOrders() {
	    // 1. 미입고 발주 항목 전체 조회
	    List<MaterialOrderItemVO> unreceivedItems = miDAO.getUnreceivedOrderItems();

	    // 2. 발주번호 기준으로 그룹핑 (입고 마스터 1건 + 항목 N건 구조)
	    Map<String, List<MaterialOrderItemVO>> grouped = unreceivedItems.stream()
	        .collect(Collectors.groupingBy(MaterialOrderItemVO::getOrderId));

	    // 3. 각 발주번호별로 입고 데이터 등록
	    for (String orderId : grouped.keySet()) {
	        // 입고관리번호 생성
	        String inboundId = miDAO.generateInboundId();

	        // 입고 마스터 테이블 등록
	        MaterialInboundVO inbound = new MaterialInboundVO();
	        inbound.setInboundId(inboundId);
	        inbound.setOrderId(orderId);
	        inbound.setInboundStatus("미입고"); // 초기 상태
	        inbound.setInboundDate(null);     // 아직 입고일 없음
	        miDAO.insertMaterialInbound(inbound);

	        // 입고 항목 테이블 등록
	        for (MaterialOrderItemVO item : grouped.get(orderId)) {
	            MaterialInboundItemVO itemVO = new MaterialInboundItemVO();
	            itemVO.setInboundId(inboundId);
	            itemVO.setOrderItemId(item.getOrderItemId());
	            itemVO.setMaterialId(item.getMaterialId());
	            itemVO.setOrderQuantity(item.getOrderQuantity());
	            itemVO.setReceivedQuantity(0); // 초기 수량
	            miDAO.insertMaterialInboundItem(itemVO);
	        }
	    }
	}
	
	// MaterialInboundServiceImpl.java에 추가할 메서드
	/**
	 * 선택된 발주 ID 목록의 미입고건만 입고 등록
	 */
	@Override
	public void insertSelectedUnreceivedOrders(String[] orderIds) {
	    for (String orderId : orderIds) {
	        // 해당 발주의 미입고 항목들만 조회
	        List<MaterialOrderItemVO> orderItems = miDAO.getUnreceivedOrderItemsByOrderId(orderId);
	        
	        if (!orderItems.isEmpty()) {
	            // 입고관리번호 생성
	            String inboundId = miDAO.generateInboundId();
	            
	            // 입고 마스터 테이블 등록
	            MaterialInboundVO inbound = new MaterialInboundVO();
	            inbound.setInboundId(inboundId);
	            inbound.setOrderId(orderId);
	            inbound.setInboundStatus("미입고");
	            inbound.setInboundDate(null);
	            miDAO.insertMaterialInbound(inbound);
	            
	            // 입고 항목 테이블 등록
	            for (MaterialOrderItemVO item : orderItems) {
	                MaterialInboundItemVO itemVO = new MaterialInboundItemVO();
	                itemVO.setInboundId(inboundId);
	                itemVO.setOrderItemId(item.getOrderItemId());
	                itemVO.setMaterialId(item.getMaterialId());
	                itemVO.setOrderQuantity(item.getOrderQuantity());
	                itemVO.setReceivedQuantity(0);
	                miDAO.insertMaterialInboundItem(itemVO);
	            }
	        }
	    }
	}

	

}
