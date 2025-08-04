package com.itwillbs.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class MaterialInboundServiceImpl implements MaterialInboundService {
	
	@Inject
	private MaterialInboundDAO miDAO;
	
	private static final Logger logger = LoggerFactory.getLogger(MaterialInboundServiceImpl.class);

	
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
	    Map<String, Boolean> uniqueOrderMap = new HashMap<>();
	    for (String orderId : orderIds) {
	        if (orderId != null && !orderId.trim().isEmpty()) {
	            uniqueOrderMap.put(orderId.trim(), true);
	        }
	    }

	    for (String orderId : uniqueOrderMap.keySet()) {
	        try {
	            List<MaterialOrderItemVO> orderItems = miDAO.getUnreceivedOrderItemsByOrderId(orderId);
	            if (!orderItems.isEmpty()) {
	                String inboundId = miDAO.generateInboundId();

	                // 입고 마스터 등록
	                MaterialInboundVO inbound = new MaterialInboundVO();
	                inbound.setInboundId(inboundId);
	                inbound.setOrderId(orderId);
	                inbound.setInboundStatus("미입고");
	                inbound.setInboundDate(null);
	                inbound.setHandledBy("system");
	                miDAO.insertMaterialInbound(inbound);

	                // 📌 inbound_item_id 자동 생성 prefix
	                String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
	                String prefix = "IN-RM-ITEM-" + today + "-";

	                int seq = 1;
	                String latestId = miDAO.getLatestInboundItemId(prefix);
	                if (latestId != null) {
	                    String[] parts = latestId.split("-");
	                    seq = Integer.parseInt(parts[4]) + 1;
	                }

	                // 각 항목 insert
	                for (MaterialOrderItemVO item : orderItems) {
	                    String inboundItemId = String.format("%s%03d", prefix, seq++);

	                    MaterialInboundItemVO itemVO = new MaterialInboundItemVO();
	                    itemVO.setInboundItemId(inboundItemId);
	                    itemVO.setInboundId(inboundId);
	                    itemVO.setOrderItemId(item.getOrderItemId());
	                    itemVO.setMaterialId(item.getMaterialId());
	                    itemVO.setOrderQuantity(item.getOrderQuantity());
	                    itemVO.setQuantity(0); // receivedQuantity → quantity
	                    itemVO.setInboundStatus("미입고");

	                    itemVO.setCreatedDate(new Date());
	                    itemVO.setUpdatedDate(new Date());

	                    miDAO.insertMaterialInboundItem(itemVO);
	                }
	            }
	        } catch (Exception e) {
	            System.out.println("발주 ID " + orderId + " 처리 중 오류 발생: " + e.getMessage());
	            throw new RuntimeException("발주 ID " + orderId + " 처리 실패: " + e.getMessage());
	        }
	    }
	}

	

}
