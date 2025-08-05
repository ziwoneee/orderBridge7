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
import com.itwillbs.domain.MaterialInventoryVO;
import com.itwillbs.domain.MaterialOrderItemVO;
import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialInboundDTO;
import com.itwillbs.dto.MaterialInboundItemDTO;
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
    public int getInboundListCount(SearchCriteria cri) throws Exception {

        return miDAO.getInboundListCount(cri);
    }

	// 입고되지 않은 발주건들만 조회 (inbound에 없는 order)
	@Override
    public List<MaterialOrderVO> getPendingInboundOrders() throws Exception {
        return miDAO.selectPendingInboundOrders();
    }

	/**
     * 아직 입고되지 않은 발주건 목록 조회
     * - 발주 항목 중 한 번도 입고처리된 적 없는 건만 조회
     */
	@Override
	public List<UnreceivedOrderDTO> getUnreceivedOrdersPaging(SearchCriteria cri) throws Exception {
	    return miDAO.getUnreceivedOrdersPaging(cri);
	}

	@Override
	public int getUnreceivedOrdersCount() throws Exception {
	    return miDAO.getUnreceivedOrdersCount();
	}


	/**
	 * 미입고 발주건을 material_inbound + material_inbound_item 테이블에 INSERT
	 * - 발주번호별로 입고ID(inbound_id) 생성 후 마스터 등록
	 * - 해당 발주에 속한 자재 항목들도 함께 입고 항목 테이블에 등록
	 */
	@Override
	public void insertUnreceivedOrders() throws Exception {
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
	public void insertSelectedUnreceivedOrders(String[] orderIds) throws Exception {
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

	                    // 단가 및 총금액 추가
	                    itemVO.setUnitPrice(item.getUnitPrice());
	                    itemVO.setTotalPrice(0); // 초기에는 입고수량이 0이라 총금액도 0
	                    
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
	
	
	// 입고처리
	/**
	 * 입고 ID에 해당하는 자재 항목들을 입고 처리
	 * - 수량 > 0 인 자재만 처리
	 * - LOT, 유통기한, 수량이 입력된 자재 기준으로만 처리
	 * - 처리 가능한 항목 1건 이상이면 전체 상태도 갱신
	 */
	@Override
	public void processInbound(String inboundId) throws Exception {
	    // 1. 해당 입고건의 모든 자재 항목 조회
	    List<MaterialInboundItemVO> itemList = miDAO.getInboundItemsByInboundId(inboundId);

	    boolean anyProcessed = false;

	    for (MaterialInboundItemVO item : itemList) {
	        // 2. 수량이 1 이상인 항목만 처리
	        if (item.getQuantity() > 0) {

	            // (1) 재고 존재 여부 확인
	            boolean exists = miDAO.checkInventoryExists(item.getMaterialId(), item.getWarehouseCode());

	            // (2) 재고 반영
	            if (exists) {
	                miDAO.updateInventoryQuantity(item.getMaterialId(), item.getWarehouseCode(), item.getQuantity());
	            } else {
	                miDAO.insertInventory(item.getMaterialId(), item.getWarehouseCode(), item.getQuantity());
	            }

	            // (3) 해당 자재 항목의 상태를 '입고완료'로 변경 + LOT 생성일 갱신
	            miDAO.markItemAsReceived(item.getInboundItemId());

	            anyProcessed = true;
	        }
	    }

	    // 3. 입고된 항목이 하나도 없다면 예외 발생
	    if (!anyProcessed) {
	        throw new Exception("입고 가능한 수량이 있는 자재가 없습니다.");
	    }

	    // 4. 입고 마스터 상태 및 입고일자(now) 갱신
	    miDAO.updateInboundMasterStatus(inboundId);
	}

	
	
	@Override
	public void processInboundItem(MaterialInboundItemDTO dto) throws Exception {
	    // 1. 유효성 검사
	    if (dto.getQuantity() <= 0) {
	        throw new Exception("입고 수량이 0 이하인 자재가 있습니다.");
	    }
	    if (dto.getLotNo() == null || dto.getLotNo().isEmpty()) {
	        throw new Exception("LOT 번호가 누락되었습니다.");
	    }
	    if (dto.getExpirationDate() == null) {
	        throw new Exception("유통기한이 누락되었습니다.");
	    }

	    // 2. 입고 상세 항목(inbound_item) 업데이트
	    // - lot_no, expiration_date, quantity, warehouse_code, inbound_status, lot_created_date(now)
	    
	    // 총금액 계산 추가
	    int totalPrice = dto.getUnitPrice() * dto.getQuantity();
	    dto.setTotalPrice(totalPrice);

	    // 단가, 총금액 포함하여 업데이트 (Mapper에 해당 필드 포함해야 함)
	    miDAO.updateInboundItem(dto);

	    // 3. 재고(material_inventory) 반영
	    // - 기존 재고 있으면 update, 없으면 insert
	    boolean exists = miDAO.checkInventoryExists(dto.getMaterialId(), dto.getWarehouseCode());

	    if (exists) {
	    	
	    	// 기존 재고 업데이트
	        miDAO.updateInventoryQuantity(dto.getMaterialId(), dto.getWarehouseCode(), dto.getQuantity());
	        
	    } else {
	    	
	    	// 신규 재고 등록 시 inventory_id 생성
	        MaterialInventoryVO vo = new MaterialInventoryVO();

	        String inventoryId = generateInventoryId(); // 아래 메서드 추가 필요
	        vo.setInventoryId(inventoryId);
	        vo.setMaterialId(dto.getMaterialId());
	        vo.setQuantity(dto.getQuantity());
	        vo.setLotNo(dto.getLotNo());
	        vo.setExpirationDate(dto.getExpirationDate());
	        vo.setReceivedDate(new Date());
	        vo.setWarehouseCode(dto.getWarehouseCode());
	        vo.setStatus("정상");
	        vo.setInventoryStatus("보관중");

	        miDAO.insertInventory(vo);
	    }

	    // 4. 입고일자(now) 입력 + 입고상태 갱신 (입고완료 or 부분입고)
	    miDAO.updateInboundMasterStatus(dto.getInboundId()); // 전체 입고수량 확인 후 상태 결정

	    // 참고: 필요 시 로그 또는 LOT 이력 저장 등 추가 가능
	}
	
	
	/**
	 * 재고 ID 자동 생성 (형식: INV-RM-YYYYMMDD-001)
	 */
	private String generateInventoryId() throws Exception {
	    String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // "yyyyMMdd"
	    int seq = miDAO.getTodayInventorySequence(date); // 오늘 날짜 기준 마지막 일련번호
	    return String.format("INV-RM-%s-%03d", date, seq + 1);
	}
	
	
	// 입고 상세 조회 (입고ID 기준)
	@Override
	public MaterialInboundDTO getInboundDetail(String inboundId) throws Exception {
	    MaterialInboundDTO dto = new MaterialInboundDTO();

	    // 1. 입고 마스터 정보 조회
	    MaterialInboundVO inbound = miDAO.getInboundMaster(inboundId);
	    if (inbound == null) return null;

	    // 2. 발주 정보 추가 (예상입고일, 주문일자, 거래처)
	    MaterialOrderVO order = miDAO.getOrderInfoByOrderId(inbound.getOrderId());
	    if (order != null) {
	        inbound.setExpectedArrivedDate(order.getExpectedArrivedDate()); // MaterialInboundVO에 필드 있으면
	        inbound.setOrderDate(order.getOrderDate());
	        inbound.setSupplierId(order.getSupplierId());
	    }

	    dto.setInbound(inbound); // ✅ 여기 핵심! VO 전체 주입

	    // 3. 자재 항목 목록
	    List<MaterialInboundItemVO> items = miDAO.getInboundItemsByInboundId(inboundId);
	    dto.setInboundItems(items);

	    return dto;
	}


	



	

}
