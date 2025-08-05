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

import com.itwillbs.domain.*;
import com.itwillbs.dto.*;
import com.itwillbs.persistence.MaterialInboundDAO;

@Service
public class MaterialInboundServiceImpl implements MaterialInboundService {

    @Inject
    private MaterialInboundDAO miDAO;

    private static final Logger logger = LoggerFactory.getLogger(MaterialInboundServiceImpl.class);

    /**
     * 입고 목록 조회 (조건 기반 페이징)
     */
    @Override
    public List<MaterialInboundSummaryDTO> getInboundList(SearchCriteria cri) throws Exception {
        return miDAO.getInboundList(cri);
    }

    /**
     * 입고 목록 전체 건수 조회
     */
    @Override
    public int getInboundListCount(SearchCriteria cri) throws Exception {
        return miDAO.getInboundListCount(cri);
    }

    /**
     * 아직 입고 테이블에 등록되지 않은 발주건들 조회
     */
    @Override
    public List<MaterialOrderVO> getPendingInboundOrders() throws Exception {
        return miDAO.selectPendingInboundOrders();
    }

    /**
     * 한 번도 입고된 적 없는 발주 항목 기준 - 미입고 목록 조회 (페이징 포함)
     */
    @Override
    public List<UnreceivedOrderDTO> getUnreceivedOrdersPaging(SearchCriteria cri) throws Exception {
        return miDAO.getUnreceivedOrdersPaging(cri);
    }

    /**
     * 미입고 발주건 목록 전체 건수
     */
    @Override
    public int getUnreceivedOrdersCount() throws Exception {
        return miDAO.getUnreceivedOrdersCount();
    }

    /**
     * 전체 미입고 발주건을 입고 테이블(material_inbound / material_inbound_item)에 등록
     * - 발주ID별로 입고ID 자동 생성
     * - 항목은 0건 입고상태로 등록
     */
    @Override
    public void insertUnreceivedOrders() throws Exception {
        List<MaterialOrderItemVO> unreceivedItems = miDAO.getUnreceivedOrderItems();

        // 발주ID 기준으로 그룹핑
        Map<String, List<MaterialOrderItemVO>> grouped = unreceivedItems.stream()
            .collect(Collectors.groupingBy(MaterialOrderItemVO::getOrderId));

        for (String orderId : grouped.keySet()) {
            String inboundId = miDAO.generateInboundId();

            // 입고 마스터 등록
            MaterialInboundVO inbound = new MaterialInboundVO();
            inbound.setInboundId(inboundId);
            inbound.setOrderId(orderId);
            inbound.setInboundStatus("미입고");
            inbound.setInboundDate(null);
            miDAO.insertMaterialInbound(inbound);

            // 각 발주 항목을 입고 항목으로 등록 (초기 수량 0)
            for (MaterialOrderItemVO item : grouped.get(orderId)) {
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

    /**
     * 선택한 발주ID 배열만 입고 데이터 등록
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

                    // 마스터 등록
                    MaterialInboundVO inbound = new MaterialInboundVO();
                    inbound.setInboundId(inboundId);
                    inbound.setOrderId(orderId);
                    inbound.setInboundStatus("미입고");
                    inbound.setInboundDate(null);
                    inbound.setHandledBy("system");
                    miDAO.insertMaterialInbound(inbound);

                    // 항목 등록 (ID 자동 생성)
                    String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
                    String prefix = "IN-RM-ITEM-" + today + "-";
                    int seq = 1;

                    String latestId = miDAO.getLatestInboundItemId(prefix);
                    if (latestId != null) {
                        String[] parts = latestId.split("-");
                        seq = Integer.parseInt(parts[4]) + 1;
                    }

                    for (MaterialOrderItemVO item : orderItems) {
                        String inboundItemId = String.format("%s%03d", prefix, seq++);

                        MaterialInboundItemVO itemVO = new MaterialInboundItemVO();
                        itemVO.setInboundItemId(inboundItemId);
                        itemVO.setInboundId(inboundId);
                        itemVO.setOrderItemId(item.getOrderItemId());
                        itemVO.setMaterialId(item.getMaterialId());
                        itemVO.setOrderQuantity(item.getOrderQuantity());
                        itemVO.setQuantity(0);
                        itemVO.setInboundStatus("미입고");
                        itemVO.setUnitPrice(item.getUnitPrice());
                        itemVO.setTotalPrice(0);
                        itemVO.setCreatedDate(new Date());
                        itemVO.setUpdatedDate(new Date());

                        miDAO.insertMaterialInboundItem(itemVO);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("발주 ID " + orderId + " 처리 실패: " + e.getMessage());
            }
        }
    }

    /**
     * 특정 입고건에 포함된 자재들 전체 입고 처리
     * - 수량 > 0 인 자재만 처리
     * - 재고 반영 (기존 있으면 update, 없으면 insert)
     * - 상태 변경, 입고일자 처리 포함
     */
    @Override
    public void processInbound(String inboundId) throws Exception {
        List<MaterialInboundItemVO> itemList = miDAO.getInboundItemsByInboundId(inboundId);

        boolean anyProcessed = false;

        for (MaterialInboundItemVO item : itemList) {
            if (item.getQuantity() > 0) {
                boolean exists = miDAO.checkInventoryExists(item.getMaterialId(), item.getWarehouseCode());

                if (exists) {
                    miDAO.updateInventoryQuantity(item.getMaterialId(), item.getWarehouseCode(), item.getQuantity());
                } else {
                    miDAO.insertInventory(item.getMaterialId(), item.getWarehouseCode(), item.getQuantity());
                }

                miDAO.markItemAsReceived(item.getInboundItemId());
                anyProcessed = true;
            }
        }

        if (!anyProcessed) {
            throw new Exception("입고 가능한 수량이 있는 자재가 없습니다.");
        }

        miDAO.updateInboundMasterStatus(inboundId);
    }

    /**
     * 개별 자재 항목 입고 처리 (입고 수량, LOT 정보, 유통기한 등)
     * - 유효성 검사
     * - 입고 항목 테이블 업데이트
     * - 재고 테이블 반영 (신규면 ID 생성)
     * - 마스터 상태 갱신
     */
    @Override
    public void processInboundItem(MaterialInboundItemDTO dto) throws Exception {
        if (dto.getQuantity() <= 0) throw new Exception("입고 수량이 0 이하인 자재가 있습니다.");
        if (dto.getLotNo() == null || dto.getLotNo().isEmpty()) throw new Exception("LOT 번호가 누락되었습니다.");
        if (dto.getExpirationDate() == null) throw new Exception("유통기한이 누락되었습니다.");

        // 총 금액 계산
        dto.setTotalPrice(dto.getUnitPrice() * dto.getQuantity());

        // === 1. 기존 입고 수량 가져오기 ===
        int prevQty = miDAO.getReceivedQtyByInboundItemId(dto.getInboundItemId()); // 기존 DB 수량
        int deltaQty = dto.getQuantity();  // 이번에 추가로 입고할 수량
        int newTotalQty = prevQty + deltaQty;  // 누적 수량

        // === 2. 발주 수량과 비교하여 상태 판단 ===
        MaterialOrderItemVO orderItem = miDAO.getOrderItemById(dto.getOrderItemId());
        if (orderItem == null) throw new RuntimeException("orderItem 조회 결과가 null입니다: " + dto.getOrderItemId());

        int orderQty = orderItem.getOrderQuantity();
        String newStatus = newTotalQty >= orderQty ? "입고완료" : "부분입고";

        // === 3. 입고 항목 테이블 업데이트 ===
        dto.setQuantity(newTotalQty);  // 누적 수량으로 업데이트
        dto.setInboundStatus(newStatus);
        miDAO.updateInboundItem(dto);

        // === 4. 재고 반영 ===
        boolean exists = miDAO.checkInventoryExists(dto.getMaterialId(), dto.getWarehouseCode());

        if (exists) {
            // 재고는 deltaQty 만큼만 증가
            miDAO.updateInventoryQuantity(dto.getMaterialId(), dto.getWarehouseCode(), deltaQty);
        } else {
            // 신규 재고 등록
            MaterialInventoryVO vo = new MaterialInventoryVO();
            String inventoryId = generateInventoryId();
            vo.setInventoryId(inventoryId);
            vo.setMaterialId(dto.getMaterialId());
            vo.setQuantity(deltaQty);  // 여기에는 실제 입고 수량만
            vo.setLotNo(dto.getLotNo());
            vo.setExpirationDate(dto.getExpirationDate());
            vo.setReceivedDate(new Date());
            vo.setWarehouseCode(dto.getWarehouseCode());
            vo.setStatus("정상");
            vo.setInventoryStatus("보관중");

            miDAO.insertInventory(vo);
        }

        // === 5. 입고 마스터 상태 갱신 ===
        miDAO.updateInboundMasterStatus(dto.getInboundId());
    }


    /**
     * 입고 마스터 및 자재 목록 상세 조회
     */
    @Override
    public MaterialInboundDTO getInboundDetail(String inboundId) throws Exception {
        MaterialInboundDTO dto = new MaterialInboundDTO();
        MaterialInboundVO inbound = miDAO.getInboundMaster(inboundId);
        if (inbound == null) return null;

        MaterialOrderVO order = miDAO.getOrderInfoByOrderId(inbound.getOrderId());
        if (order != null) {
            inbound.setExpectedArrivedDate(order.getExpectedArrivedDate());
            inbound.setOrderDate(order.getOrderDate());
            inbound.setSupplierId(order.getSupplierId());
        }

        dto.setInbound(inbound);
        dto.setInboundItems(miDAO.getInboundItemsByInboundId(inboundId));
        return dto;
    }

    /**
     * 부분입고 상태의 항목에 대해 추가입고건 생성
     * - 남은 수량만큼 새로운 입고ID + 항목 등록
     */
    @Override
    public void createAdditionalInbound(String orderItemId) throws Exception {
        MaterialOrderItemVO orderItem = miDAO.getOrderItemById(orderItemId);

        int orderedQty = orderItem.getOrderQuantity();
        int receivedQty = miDAO.getTotalReceivedQtyByOrderItemId(orderItemId);
        int remainQty = orderedQty - receivedQty;

        if (remainQty <= 0) throw new Exception("남은 입고 수량이 없습니다.");

        String inboundId = generateInboundId();

        MaterialInboundVO inbound = new MaterialInboundVO();
        inbound.setInboundId(inboundId);
        inbound.setOrderId(orderItem.getOrderId());
        inbound.setInboundStatus("미입고");
        inbound.setHandledBy("system");
        inbound.setInboundDate(null);
        miDAO.insertMaterialInbound(inbound);

        MaterialInboundItemVO item = new MaterialInboundItemVO();
        item.setInboundId(inboundId);
        item.setMaterialId(orderItem.getMaterialId());
        item.setQuantity(remainQty);
        item.setInboundStatus("미입고");
        item.setOrderItemId(orderItemId);
        item.setWarehouseCode(orderItem.getWarehouseCode());

        String inboundItemId = generateInboundItemId();
        item.setInboundItemId(inboundItemId);

        miDAO.insertMaterialInboundItem(item);
    }

    /** 입고ID 자동 생성 (형식: IN-RM-YYYYMMDD-001) */
    private String generateInboundId() throws Exception {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String prefix = "IN-RM-" + date + "-";
        int seq = 1;

        String latestId = miDAO.generateInboundId();
        if (latestId != null && latestId.startsWith(prefix)) {
            String[] parts = latestId.split("-");
            seq = Integer.parseInt(parts[3]) + 1;
        }

        return String.format("%s%03d", prefix, seq);
    }

    /** 입고항목ID 자동 생성 (형식: IN-RM-ITEM-YYYYMMDD-001) */
    private String generateInboundItemId() throws Exception {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String prefix = "IN-RM-ITEM-" + date + "-";
        int seq = 1;

        String latestId = miDAO.getLatestInboundItemId(prefix);
        if (latestId != null && latestId.startsWith(prefix)) {
            String[] parts = latestId.split("-");
            seq = Integer.parseInt(parts[4]) + 1;
        }

        return String.format("%s%03d", prefix, seq);
    }

    /** 재고ID 자동 생성 (형식: INV-RM-YYYYMMDD-001) */
    private String generateInventoryId() throws Exception {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        int seq = miDAO.getTodayInventorySequence(date);
        return String.format("INV-RM-%s-%03d", date, seq + 1);
    }

    
    // 특정 발주 항목의 누적 입고 수량 조회
    @Override
    public int getTotalInboundQuantity(String orderItemId) throws Exception {
        return miDAO.getTotalInboundQuantity(orderItemId);
    }
    
}
