package com.itwillbs.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itwillbs.domain.MaterialOutboundVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.WorkOrderVO;
import com.itwillbs.mapper.MaterialOutboundMapper;
import com.itwillbs.persistence.MaterialOutboundDAO;
import com.itwillbs.persistence.MaterialReservationDAO;

/**
 * 출고 관리 서비스 구현체
 * - DB 접근은 DAO 호출로만 수행
 * - 출고 등록/처리 트랜잭션 관리
 */
@Service
public class MaterialOutboundServiceImpl implements MaterialOutboundService {

	@Inject
	private MaterialOutboundDAO moDAO;
	
	@Inject
	private MaterialOutboundMapper mapper;
	
	@Inject
	private MaterialReservationDAO reservationDAO;
	
	
	private static final Logger logger = LoggerFactory.getLogger(MaterialOutboundServiceImpl.class);
	
	 	@Override
	    public List<?> getOutboundList(SearchCriteria cri) { 
		 	return moDAO.selectOutboundList(cri);
	 	}

	    @Override
	    public int getOutboundCount(SearchCriteria cri) { 
	    	return moDAO.selectOutboundCount(cri); 
    	}

	    @Override
	    public List<WorkOrderVO> getWaitingOrders() { 
	    	return moDAO.selectWaitingOrders();
    	}

	    @Override
	    public Map<String, Object> getWorkOrderWithStockMap(String workOrderId) {
	        // 1. 작업지시서 기본 정보 1건 조회
	        Map<String,Object> header = mapper.selectWorkOrderHeader(workOrderId);

	        // 결과가 없으면 빈 Map 반환
	        if (header == null) return Collections.emptyMap();

	        // 2. 작업지시서에 포함된 자재 목록 조회
	        List<Map<String,Object>> mats = mapper.selectWorkOrderMaterials(workOrderId);

	        // 3. header Map에 materialList 키로 자재 목록을 넣음
	        header.put("materialList", mats != null ? mats : Collections.emptyList());

	        // 4. 컨트롤러로 반환 → model.addAttribute("wo", header) 해서 JSP에서 바로 사용
	        return header;
	    }




	    @Transactional
	    @Override
	    public void registerOutbound(MaterialOutboundVO vo) throws Exception {

	        // ── 0) NPE 방지
	        List<String> mIds    = (vo.getMaterialIdList()    != null) ? vo.getMaterialIdList()    : java.util.Collections.<String>emptyList();
	        List<BigDecimal> reqs= (vo.getReqQtyList()        != null) ? vo.getReqQtyList()        : java.util.Collections.<BigDecimal>emptyList();
	        List<String> lotMIds = (vo.getLotMaterialIdList() != null) ? vo.getLotMaterialIdList() : java.util.Collections.<String>emptyList();
	        List<String> lotNos  = (vo.getLotNoList()         != null) ? vo.getLotNoList()         : java.util.Collections.<String>emptyList();
	        List<BigDecimal> qtys= (vo.getQtyList()           != null) ? vo.getQtyList()           : java.util.Collections.<BigDecimal>emptyList();

	        // ── 1) 자재별 필요수량 맵 (정수 cap 계산에 쓰일 값은 소수 반올림)
	        java.util.Map<String, BigDecimal> reqMap = new java.util.HashMap<>();
	        for (int i = 0; i < mIds.size(); i++) {
	            String mid = mIds.get(i);
	            BigDecimal rq = (i < reqs.size() && reqs.get(i) != null) ? reqs.get(i) : BigDecimal.ZERO;
	            reqMap.put(mid, rq);
	        }

	        // ── 2) LOT 선택을 자재별로 묶기 (qty는 BigDecimal 보존)
	        java.util.Map<String, java.util.List<java.util.Map<String,Object>>> picks = new java.util.HashMap<>();
	        for (int i = 0; i < lotMIds.size(); i++) {
	            BigDecimal q = (i < qtys.size() && qtys.get(i) != null) ? qtys.get(i) : BigDecimal.ZERO;
	            if (q.compareTo(BigDecimal.ZERO) <= 0) continue;

	            String mid = lotMIds.get(i);
	            String lot = (i < lotNos.size()) ? lotNos.get(i) : null;
	            lot = (lot != null && lot.trim().isEmpty()) ? null : lot; // "" → null

	            java.util.Map<String,Object> one = new java.util.HashMap<>();
	            one.put("material_id", mid);
	            one.put("lot_no", lot);
	            one.put("qty", q);

	            java.util.List<java.util.Map<String,Object>> list = picks.get(mid);
	            if (list == null) {
	                list = new java.util.ArrayList<>();
	                picks.put(mid, list);
	            }
	            list.add(one);
	        }

	        // ── 3) 검증: ΣLOT == target(= min(required, cap))
	        // ── 3) 검증: ΣLOT == target(= min(required, cap))  [소수 3자리, BigDecimal]
	        for (String mid : reqMap.keySet()) {
	            if ("RM-0015".equals(mid)) continue; // 물은 패스

	            // required
	            Object val = reqMap.get(mid);
	            
	            BigDecimal required = (val == null)
            	    ? BigDecimal.ZERO
            	    : new BigDecimal(val.toString());

            	required = required.setScale(3, RoundingMode.HALF_UP);



	            // LOT 선택합계
	            java.math.BigDecimal sum = java.math.BigDecimal.ZERO;
	            List<Map<String,Object>> list = picks.get(mid);
	            if (list != null) {
	                for (Map<String,Object> p : list) {
	                    Object v = p.get("qty");
	                    if (v instanceof java.math.BigDecimal) {
	                        sum = sum.add(((java.math.BigDecimal) v).setScale(3, java.math.RoundingMode.HALF_UP));
	                    } else if (v instanceof Number) {
	                        sum = sum.add(java.math.BigDecimal.valueOf(((Number) v).doubleValue())
	                                .setScale(3, java.math.RoundingMode.HALF_UP));
	                    }
	                }
	            }

	            // 가용 계산: onhand/reserved 모두 BigDecimal 사용
	            java.math.BigDecimal onhand =
	                toBD(reservationDAO.selectOnhandDecimal(mid));         // ← DAO가 BigDecimal 반환하도록
	            java.math.BigDecimal reservedAll =
	                toBD(reservationDAO.sumReservedByMaterialDecimal(mid)); // ← BigDecimal
	            java.math.BigDecimal reservedThis =
	                toBD(reservationDAO.selectWoReservedDecimal(vo.getWorkOrderId(), mid));

	            java.math.BigDecimal reservedOthers = reservedAll.subtract(reservedThis);
	            if (reservedOthers.signum() < 0) reservedOthers = java.math.BigDecimal.ZERO;

	            // onhand가 0(혹은 null)로 오면 LOT 합계로 보정 (프론트 effOnhand와 동일 아이디어)
	            if (onhand.signum() == 0) {
	                java.math.BigDecimal lotOnhand = java.math.BigDecimal.ZERO;
	                List<Map<String,Object>> lots = moDAO.getLotsByMaterial(mid);
	                if (lots != null) {
	                    for (Map<String,Object> lot : lots) {
	                        Object q = lot.get("quantity");
	                        if (q instanceof java.math.BigDecimal) {
	                            lotOnhand = lotOnhand.add(((java.math.BigDecimal) q)
	                                .setScale(3, java.math.RoundingMode.HALF_UP));
	                        } else if (q instanceof Number) {
	                            lotOnhand = lotOnhand.add(java.math.BigDecimal
	                                .valueOf(((Number) q).doubleValue()).setScale(3, java.math.RoundingMode.HALF_UP));
	                        }
	                    }
	                }
	                if (lotOnhand.signum() > 0) onhand = lotOnhand;
	            }

	            java.math.BigDecimal a4wo = onhand.subtract(reservedOthers);
	            if (a4wo.signum() < 0) a4wo = java.math.BigDecimal.ZERO;
	            a4wo = a4wo.add(reservedThis);

	            // cap/target
	            java.math.BigDecimal cap    = minBD(required, a4wo);
	            java.math.BigDecimal target = cap; // = min(required, a4wo)

	            // 허용 오차(±0.0005)로 비교
	            java.math.BigDecimal diff = sum.subtract(target).abs();
	            if (diff.compareTo(new java.math.BigDecimal("0.0005")) > 0) {
	                throw new IllegalArgumentException("필요수량 불일치: " + mid
	                    + " (필요:" + required + ", target:" + target + ", 선택:" + sum + ")");
	            }
	        }


	        // ── 4) ID 발급 + 헤더 저장 (기존 동일)
	        String outboundId = moDAO.nextOutboundId();
	        logger.info("nextOutboundId={}", outboundId);

	        java.util.Date dueDate = vo.getDueDate();
	        if (dueDate == null) {
	            if (vo.getWorkOrderId() == null || vo.getWorkOrderId().isEmpty()) {
	                throw new IllegalStateException("workOrderId가 없습니다.(폼 바인딩 확인)");
	            }
	            dueDate = moDAO.selectWorkOrderDueDate(vo.getWorkOrderId());
	        }
	        if (dueDate == null) {
	            throw new IllegalStateException("작업지시 due_date 없음: " + vo.getWorkOrderId());
	        }

	        java.util.Map<String,Object> header = new java.util.HashMap<>();
	        header.put("outbound_id",  outboundId);
	        header.put("work_order_id",vo.getWorkOrderId());
	        header.put("handled_by",   vo.getHandledBy());
	        header.put("status",       vo.getStatus());
	        header.put("due_date",     dueDate);
	        moDAO.insertOutboundHeader(header);

	        // ── 5) 항목 저장 (qty/required_qty 는 BigDecimal로 그대로 저장)
	        java.util.List<java.util.Map<String,Object>> rows = new java.util.ArrayList<>();
	        int idx = 1;
	        for (String mid : picks.keySet()) {
	            java.util.List<java.util.Map<String,Object>> list = picks.get(mid);
	            if (list == null) continue;

	            for (java.util.Map<String,Object> p : list) {
	                java.util.Map<String,Object> row = new java.util.HashMap<>();
	                row.put("outbound_item_id", String.format("%s-%03d", outboundId, idx++));
	                row.put("outbound_id",  outboundId);
	                row.put("material_id",  mid);
	                row.put("quantity",     p.get("qty"));    // BigDecimal
	                row.put("lot_no",       p.get("lot_no"));
	                row.put("required_qty", p.get("qty"));    // DRAFT에선 선택수량과 동일하게
	                rows.add(row);
	            }
	        }
	        if (!rows.isEmpty()) {
	            moDAO.insertOutboundItems(rows);
	        }

	        // ── 6) 상태 갱신(기존 동일)
	        int remain = moDAO.countRemainByWorkOrder(vo.getWorkOrderId());
	        if (remain == 0) {
	            moDAO.updateWorkOrderShortageResolved(vo.getWorkOrderId());
	        } else {
	            moDAO.updateWorkOrderShortageStatus(vo.getWorkOrderId(), "CHECKED");
	        }
	    }




	    @Override
	    public Map<String, Object> getOutboundDetailMap(String outboundId) {
	        Map<String,Object> header = moDAO.selectOutboundHeader(outboundId);
	        List<Map<String,Object>> items = moDAO.selectOutboundItems(outboundId);
	        
	        Map<String,Object> res = new HashMap<>();
	        if (header != null) res.putAll(header);
	        res.put("items", items);
	        return res;
	    }

	    @Transactional
	    @Override
	    public void processOutbound(String outboundId) throws Exception {
	        // 1) 재고 차감
	        int affected = moDAO.decreaseInventoryByOutbound(outboundId);
	        if (affected <= 0) throw new IllegalStateException("재고 차감 실패");

	        // 2) 예약 차감 + 0행 정리
	        reservationDAO.consumeReservationByOutbound(outboundId);
	        reservationDAO.deleteZeroReservationsByOutbound(outboundId);

	        // 3) 출고 완료
	        moDAO.updateOutboundCompleted(outboundId);

	        // 🔹 4) 이번 출고로 사용된 LOT들의 원(原)입고건 usage_status 재계산
	        //     - outbound_item(lot_no) → inbound_item(lot_no) → inbound_id 추출
	        List<String> inboundIds = moDAO.findInboundIdsByOutbound(outboundId);
	        for (String inboundId : inboundIds) {
	            moDAO.updateInboundUsageStatus(inboundId); // 이미 가지고 있는 쿼리 재사용
	        }

	        // 🔹 5) 작업지시서 남은 필요 0이면 마감(부족상태 해제)
	        String workOrderId = moDAO.getWorkOrderIdByOutbound(outboundId);
	        moDAO.updateWorkOrderStatus(workOrderId, "READY");
	        
	        int remain = moDAO.countRemainByWorkOrder(workOrderId);
	        if (remain == 0) {
	            moDAO.updateWorkOrderShortageResolved(workOrderId);
	        }
	    }

    
	    
	    @Override
	    public List<Map<String,Object>> getLotsByMaterial(String materialId) throws Exception {
	        return moDAO.getLotsByMaterial(materialId);
	    }

	    @Override
	    public int getOutboundCountByStatus(String status) throws Exception {
	        return moDAO.countByStatus(status);
	    }
	    
	    
	    
	    @Override
	    @Transactional
	    public CreateOutboundResult createOutboundIfReady(String workOrderId) throws Exception {
	        CreateOutboundResult r = new CreateOutboundResult();

	        // 1. 이미 생성돼 있으면 스킵
	        if (moDAO.existsOutboundByWorkOrder(workOrderId) > 0) {
	            r.created = false;
	            r.reason = "already-exists";
	            return r;
	        }

	        // 2. 출고 가능 여부 확인
	        if (moDAO.isWorkOrderReady(workOrderId) != 1) {
	            r.created = false;
	            r.reason = "not-ready";
	            return r;
	        }

	        // 3. 출고 생성
	        String outboundId = moDAO.nextOutboundId();
	        moDAO.insertMaterialOutbound(outboundId, workOrderId);
	        moDAO.insertOutboundItemsFromWOM(outboundId, workOrderId);

	        r.created = true;
	        r.outboundId = outboundId;
	        r.reason = "created";
	        return r;
	    }
	    
	    
	    
	 // MaterialOutboundServiceImpl.java에 추가할 메서드 구현들
    @Override
    public List<Map<String, Object>> getAvailableMaterialsByInbound(String inboundId, String workOrderId) throws Exception {
        // logger 자리표시자 수정 (두 값 모두 찍으려면 {} 2개)
        logger.info("getAvailableMaterialsByInbound() called with inboundId: {}, workOrderId: {}", inboundId, workOrderId);

        if (inboundId == null || inboundId.trim().isEmpty()) {
            throw new IllegalArgumentException("입고ID는 필수입니다.");
        }
        if (workOrderId == null || workOrderId.trim().isEmpty()) {
            // 필요수량을 계산하려면 작업지시도 필요
            throw new IllegalArgumentException("작업지시ID는 필수입니다.");
        }

        try {
            Map<String,Object> params = new HashMap<>();
            params.put("inboundId", inboundId);
            params.put("workOrderId", workOrderId);

            List<Map<String, Object>> materials = moDAO.getAvailableMaterialsByInbound(params);
            logger.info("getAvailableMaterialsByInbound() completed - found {} materials",
                    (materials != null ? materials.size() : 0));
            return (materials != null ? materials : new ArrayList<>());
        } catch (Exception e) {
            throw new Exception("가용 자재 목록 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
	    
	    @Override
	    @Transactional
	    public void updateInboundUsageStatus(String inboundId) throws Exception {
	        logger.info("updateInboundUsageStatus() called with inboundId: {}", inboundId);
	        
	        if (inboundId == null || inboundId.trim().isEmpty()) {
	            throw new IllegalArgumentException("입고ID는 필수입니다.");
	        }
	        
	        try {
	            int updatedRows = moDAO.updateInboundUsageStatus(inboundId);
	            logger.info("updateInboundUsageStatus() completed - updated {} rows", updatedRows);
	            
	            if (updatedRows == 0) {
	                logger.warn("updateInboundUsageStatus() - No rows updated for inboundId: {}", inboundId);
	            }
	        } catch (Exception e) {
	            logger.error("updateInboundUsageStatus() error for inboundId: {}", inboundId, e);
	            throw new Exception("입고건 사용 상태 업데이트 중 오류가 발생했습니다: " + e.getMessage(), e);
	        }
	    }
	    
	    
       // helpers
        private static java.math.BigDecimal toBD(Object v) {
            if (v == null) return java.math.BigDecimal.ZERO.setScale(3, java.math.RoundingMode.HALF_UP);
            if (v instanceof java.math.BigDecimal) return ((java.math.BigDecimal) v).setScale(3, java.math.RoundingMode.HALF_UP);
            if (v instanceof Number) return java.math.BigDecimal.valueOf(((Number) v).doubleValue())
                    .setScale(3, java.math.RoundingMode.HALF_UP);
            return new java.math.BigDecimal(v.toString()).setScale(3, java.math.RoundingMode.HALF_UP);
        }
        private static java.math.BigDecimal minBD(java.math.BigDecimal a, java.math.BigDecimal b) {
            return (a.compareTo(b) <= 0 ? a : b);
        }
	    
	    
	    
	    
	    
}
