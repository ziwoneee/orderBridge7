package com.itwillbs.service;

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

import com.itwillbs.domain.MaterialOutboundItemVO;
import com.itwillbs.domain.MaterialOutboundVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.WorkOrderVO;
import com.itwillbs.dto.MaterialOutboundDetailDTO;
import com.itwillbs.dto.MaterialOutboundItemDTO;
import com.itwillbs.dto.MaterialOutboundSummaryDTO;
import com.itwillbs.mapper.MaterialOutboundMapper;
import com.itwillbs.persistence.MaterialOutboundDAO;

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
	    public void registerOutbound(MaterialOutboundVO vo) {

	        // ── 0) NPE 방지: 리스트 null이면 빈 리스트로
	        List<String> mIds    = (vo.getMaterialIdList()    != null) ? vo.getMaterialIdList()    : java.util.Collections.<String>emptyList();
	        List<Integer> reqs   = (vo.getReqQtyList()        != null) ? vo.getReqQtyList()        : java.util.Collections.<Integer>emptyList();
	        List<String> lotMIds = (vo.getLotMaterialIdList() != null) ? vo.getLotMaterialIdList() : java.util.Collections.<String>emptyList();
	        List<String> lotNos  = (vo.getLotNoList()         != null) ? vo.getLotNoList()         : java.util.Collections.<String>emptyList();
	        List<Integer> qtys   = (vo.getQtyList()           != null) ? vo.getQtyList()           : java.util.Collections.<Integer>emptyList();

	        // ── 1) 자재별 필요수량 맵
	        Map<String,Integer> reqMap = new HashMap<String,Integer>();
	        for (int i = 0; i < mIds.size(); i++) {
	            String mid = mIds.get(i);
	            Integer rq = (i < reqs.size() && reqs.get(i) != null) ? reqs.get(i) : 0;
	            reqMap.put(mid, rq);
	        }

	        // ── 2) LOT 선택을 자재별로 묶기
	        Map<String, List<Map<String,Object>>> picks = new HashMap<String, List<Map<String,Object>>>();
	        for (int i = 0; i < lotMIds.size(); i++) {
	            Integer qObj = (i < qtys.size()) ? qtys.get(i) : 0;
	            int q = (qObj != null) ? qObj.intValue() : 0;
	            if (q <= 0) continue;

	            String mid = lotMIds.get(i);
	            String lot = (i < lotNos.size()) ? lotNos.get(i) : null;

	            Map<String,Object> one = new HashMap<String,Object>();
	            one.put("material_id", mid);
	            one.put("lot_no", lot);
	            one.put("qty", q);

	            // computeIfAbsent 대신 기존 방식
	            List<Map<String,Object>> list = picks.get(mid);
	            if (list == null) {
	                list = new ArrayList<Map<String,Object>>();
	                picks.put(mid, list);
	            }
	            list.add(one);
	        }

	        // ── 3) 검증: ΣLOT == 필요수량
	        for (String mid : reqMap.keySet()) {
	            int req = reqMap.get(mid) != null ? reqMap.get(mid).intValue() : 0;
	            int sum = 0;
	            List<Map<String,Object>> list = picks.get(mid);
	            if (list != null) {
	                for (Map<String,Object> p : list) {
	                    Object v = p.get("qty");
	                    if (v instanceof Number) sum += ((Number) v).intValue();
	                }
	            }
	            if (req != sum) {
	                throw new IllegalArgumentException("필요수량 불일치: " + mid + " (필요:" + req + ", 선택:" + sum + ")");
	            }
	        }

	        // ── 4) ID 발급 + 헤더 저장
	        String outboundId = moDAO.nextOutboundId();
	        Map<String,Object> header = new HashMap<String,Object>();
	        header.put("outbound_id", outboundId);
	        header.put("work_order_no", vo.getWorkOrderNo());
	        header.put("handled_by",   vo.getHandledBy());
	        moDAO.insertOutboundHeader(header);

	        // ── 5) 항목 저장(LOT별)
	        List<Map<String,Object>> rows = new ArrayList<Map<String,Object>>();
	        for (String mid : picks.keySet()) {
	            List<Map<String,Object>> list = picks.get(mid);
	            if (list == null) continue;
	            for (Map<String,Object> p : list) {
	                Map<String,Object> row = new HashMap<String,Object>();
	                row.put("outbound_id", outboundId);
	                row.put("material_id", mid);
	                row.put("quantity",   p.get("qty"));
	                row.put("lot_no",     p.get("lot_no"));
	                rows.add(row);
	            }
	        }
	        if (!rows.isEmpty()) {
	            moDAO.insertOutboundItems(rows);
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
	    public void processOutbound(String outboundId) {
	        int affected = moDAO.decreaseInventoryByOutbound(outboundId);
	        if (affected <= 0) throw new IllegalStateException("재고 차감 실패");
	        moDAO.updateOutboundCompleted(outboundId);
	    }
    
	    
	    @Override
	    public List<Map<String,Object>> getLotsByMaterial(String materialId) throws Exception {
	        return moDAO.getLotsByMaterial(materialId);
	    }

	    @Override
	    public int getOutboundCountByStatus(String status) throws Exception {
	        return moDAO.countByStatus(status);
	    }
}
