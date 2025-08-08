package com.itwillbs.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itwillbs.domain.MaterialOrderItemVO;
import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialOrderDTO;
import com.itwillbs.dto.PurchaseDraftRequest;
import com.itwillbs.dto.PurchaseDraftRequest.ShortageItem;
import com.itwillbs.dto.PurchaseDraftResult;
import com.itwillbs.dto.SupplierItemDTO;
import com.itwillbs.persistence.MaterialOrderDAO;

/**
 * 자재 발주 서비스 구현체
 * - DAO를 호출하여 비즈니스 로직 처리
 */
@Service
public class MaterialOrderServiceImpl implements MaterialOrderService {
	
	@Inject
	private MaterialOrderDAO mOrderDAO;

	// 발주 목록 조회
	@Override
    public List<MaterialOrderVO> getOrderList(SearchCriteria cri) throws Exception {
        return mOrderDAO.getOrderList(cri);
    }

	// 총 건수 조회 (페이징)
    @Override
    public int getTotalCount(SearchCriteria cri) throws Exception {
        return mOrderDAO.getTotalCount(cri);
    }

    
    // 발주 등록
    @Override
    public void insertOrder(MaterialOrderDTO orderDTO) throws Exception {
    	
    	// 1. 납기일 유효성 검사
    	java.util.Date today = new java.util.Date();
    	java.util.Date expectedDate = orderDTO.getOrder().getExpectedArrivedDate();
    	
        if (expectedDate.before(today)) {
            throw new IllegalArgumentException("납기일은 오늘 이후여야 합니다.");
        }

        
        // 1. 발주번호 생성
        String newOrderId = mOrderDAO.generateOrderId();
        orderDTO.getOrder().setOrderId(newOrderId);

        // 2. order 테이블 insert
        mOrderDAO.insertOrder(orderDTO.getOrder());

        // 3. order_item 테이블 insert (for each)
        int index = 1;
        for (MaterialOrderItemVO item : orderDTO.getOrderItems()) {
            item.setOrderId(newOrderId); // 외래키 설정

            // ✅ 자동 생성되는 order_item_id
            String itemId = newOrderId + "-" + index;
            item.setOrderItemId(itemId);
            index++;

            mOrderDAO.insertOrderItem(item);
        }

    }
	
    
    // 자재명으로 거래처 검색
    @Override
    public List<SupplierItemDTO> searchSuppliersByMaterial(String keyword) {
        return mOrderDAO.searchSuppliersByMaterial(keyword);
    }

	
	
	
    @Transactional(rollbackFor = Exception.class)
    @Override
    public PurchaseDraftResult createDraftFromShortages(PurchaseDraftRequest req) throws Exception {
        PurchaseDraftResult result = new PurchaseDraftResult();

        if (req.getItems() == null || req.getItems().isEmpty()) {
            result.setOrderId(null);
            result.setUnmappedMaterials(Collections.emptyList());
            return result;
        }

        // 1) 자재→거래처/단가 매핑
        List<String> materialIds = req.getItems().stream()
                .map(ShortageItem::getMaterialId).distinct().collect(Collectors.toList());

        List<Map<String, Object>> mappings = mOrderDAO.selectSupplierItemMappings(materialIds);

        Map<String, Map<String, Object>> byMaterial = new HashMap<>();
        for (Map<String, Object> m : mappings) {
            byMaterial.put((String)m.get("material_id"), m);
        }

        // 매핑 없는 자재
        List<String> unmapped = new ArrayList<>();
        for (String mid : materialIds) {
            if (!byMaterial.containsKey(mid)) unmapped.add(mid);
        }
        result.setUnmappedMaterials(unmapped);

        // 발주 대상만
        List<ShortageItem> mappable = req.getItems().stream()
                .filter(it -> byMaterial.containsKey(it.getMaterialId()) && it.getLackQty() > 0)
                .collect(Collectors.toList());
        if (mappable.isEmpty()) {
            result.setOrderId(null);
            return result;
        }

        // 3) 거래처별 그룹
        Map<String, List<ShortageItem>> bySupplier = new LinkedHashMap<>();
        for (ShortageItem it : mappable) {
            String sup = (String) byMaterial.get(it.getMaterialId()).get("supplier_id");
            bySupplier.computeIfAbsent(sup, k -> new ArrayList<>()).add(it);
        }

        // 4) 거래처별 초안 생성
        String lastOrderId = null;
        for (Map.Entry<String, List<ShortageItem>> e : bySupplier.entrySet()) {
            String supplierId = e.getKey();
            Date now = new Date();

            Map<String, Object> header = new HashMap<>();
            // ⚠️ orderId는 넣지 마! selectKey가 채움
            header.put("supplierId", supplierId);          // ✅ camelCase
            header.put("orderStatus", "DRAFT");
            header.put("expectedArrivedDate", null);
            header.put("createdBy", "SYSTEM");
            header.put("note", "[AUTO] 작업지시 " + req.getWorkOrderId() + " 부족분 초안");

            mOrderDAO.insertOrderHeaderDraft(header);      // 여기서 selectKey가 header.orderId 세팅

            String orderId = (String) header.get("orderId");

	         // ✅ 이 주문의 시작 인덱스를 한 번만 조회
	         int idx = mOrderDAO.selectNextOrderItemIndex(orderId);
	
	         for (ShortageItem item : e.getValue()) {
	             Map<String, Object> map = byMaterial.get(item.getMaterialId());
	
	             String warehouse = (String) map.getOrDefault("warehouse_code", "WH001");
	             int unit  = ((Number) map.getOrDefault("unit_price", 0)).intValue();
	             int qty   = item.getLackQty();
	             int total = unit * qty;
	
	             String orderItemId = orderId + "-" + (idx++); // ✅ 계속 증가
	
	             Map<String, Object> row = new HashMap<>();
	             row.put("orderItemId", orderItemId);
	             row.put("orderId", orderId);
	             row.put("materialId", item.getMaterialId());
	             row.put("orderQuantity", qty);
	             row.put("unitPrice", unit);
	             row.put("totalPrice", total);
	             row.put("warehouseCode", warehouse);
	
	             mOrderDAO.insertOrderItem(row);
	         }


            lastOrderId = orderId;
        }
        result.setOrderId(lastOrderId);
        return result;
    }

    
}
