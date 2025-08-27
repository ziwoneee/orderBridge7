package com.itwillbs.service;

import java.math.BigDecimal;
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
	        // cap = min(required, a4wo);  a4wo는 기존 int 로직 유지
	        final BigDecimal EPS = new BigDecimal("0.01"); // 1/100 허용
	        for (String mid : reqMap.keySet()) {
	            if ("RM-0015".equals(mid)) {
	                // 물은 LOT 불필요/직접출고 → 검증 패스
	                continue;
	            }

	            BigDecimal requiredBD = reqMap.get(mid) != null ? reqMap.get(mid) : BigDecimal.ZERO;

	            // LOT 선택합계(BigDecimal)
	            BigDecimal sum = BigDecimal.ZERO;
	            java.util.List<java.util.Map<String,Object>> list = picks.get(mid);
	            if (list != null) {
	                for (java.util.Map<String,Object> p : list) {
	                    Object v = p.get("qty");
	                    if (v instanceof BigDecimal) sum = sum.add((BigDecimal) v);
	                    else if (v instanceof Number) sum = sum.add(new BigDecimal(((Number) v).toString()));
	                }
	            }

	            // a4wo 계산 (int 기반)
	            int onhand        = reservationDAO.selectOnhand(mid);
	            int reservedAll   = reservationDAO.sumReservedByMaterial(mid);
	            int reservedThis  = reservationDAO.selectWoReserved(vo.getWorkOrderId(), mid);
	            int reservedOthers= Math.max(reservedAll - reservedThis, 0);
	            int a4wo          = Math.max(0, onhand - reservedOthers) + reservedThis;

	            // cap/target 을 BigDecimal로
	            BigDecimal capBD    = requiredBD.min(new BigDecimal(a4wo));
	            BigDecimal targetBD = capBD;

	            // 허용 오차 내 비교
	            if (sum.subtract(targetBD).abs().compareTo(EPS) > 0) {
	                throw new IllegalArgumentException(
	                    "필요수량 불일치: " + mid + " (필요:" + requiredBD + ", target:" + targetBD + ", 선택:" + sum + ")"
	                );
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
	    
	    
	    
	    
	    
	    
}
