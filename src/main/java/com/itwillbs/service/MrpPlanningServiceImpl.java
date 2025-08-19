package com.itwillbs.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itwillbs.persistence.MrpDAO;

@Service
public class MrpPlanningServiceImpl implements MrpPlanningService {
	
	@Inject
    private MrpDAO mrpDAO;
	
	// 제품/수량 기준으로 자재별 "총소요량"을 조회
	@Override
    public List<Map<String, Object>> getGrossRequirements(String productId, double orderQty) throws Exception {
        // (예시) 간단한 입력 검증
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId는 필수입니다.");
        }
        if (orderQty <= 0) {
            throw new IllegalArgumentException("orderQty는 0보다 커야 합니다.");
        }

        // DAO 호출 → 결과 그대로 리턴 (다음 단계에서 가공/라운딩/단위변환 등 추가 가능)
        return mrpDAO.selectGrossRequirements(productId, orderQty);
    }
	
	
	// 가용/순소요(Netting) 결과를 조회
	@Override
    public List<Map<String, Object>> getNetting(String productId, double orderQty) throws Exception {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId는 필수입니다.");
        }
        if (orderQty <= 0) {
            throw new IllegalArgumentException("orderQty는 0보다 커야 합니다.");
        }
        return mrpDAO.selectNetting(productId, orderQty);
    }
	
	
	// 3-1) 부족분 리스트 (net_req > 0)
    @Override
    public List<Map<String, Object>> getShortages(String productId, double orderQty) throws Exception {
        if (productId == null || productId.isBlank()) throw new IllegalArgumentException("productId는 필수");
        if (orderQty <= 0) throw new IllegalArgumentException("orderQty는 0보다 커야 함");
        return mrpDAO.selectShortages(productId, orderQty);
    }

    // 3-2) 발주 추천 초안
    @Override
    public List<Map<String, Object>> recommendPO(String productId, double orderQty) throws Exception {
        if (productId == null || productId.isBlank()) throw new IllegalArgumentException("productId는 필수");
        if (orderQty <= 0) throw new IllegalArgumentException("orderQty는 0보다 커야 함");

        // ⚠️ MOQ/LOT 규칙이 DB에 없으면 DAO 결과를 그대로 반환.
        //    앞으로 material_policy( moq, lot_size )가 생기면 여기서 라운딩/보정 로직 추가.
        return mrpDAO.selectRecommendPO(productId, orderQty);
    }
    
    
    /**
     * 발주 초안 생성 (공급사별 1건씩 끊어서 저장)
     * - 트랜잭션으로 헤더/아이템 함께 커밋
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> createPoDraft(List<Map<String, Object>> items) throws Exception {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("발주 생성 대상 아이템이 없습니다.");
        }

        // 1) supplierId 누락/수량 0 이하 데이터 필터링
        List<Map<String, Object>> valid = items.stream()
          .filter(it -> it.get("supplierId") != null)
          .filter(it -> new BigDecimal(String.valueOf(it.get("qty"))).compareTo(BigDecimal.ZERO) > 0)
          .collect(Collectors.toList());
        if (valid.isEmpty()) throw new IllegalArgumentException("유효한 아이템이 없습니다.");

        // 2) 공급사별 그룹핑
        Map<String, List<Map<String, Object>>> bySupplier = valid.stream()
          .collect(Collectors.groupingBy(it -> String.valueOf(it.get("supplierId"))));

        List<Map<String,Object>> results = new ArrayList<>();

        // 3) 각 공급사 그룹마다 PO 1건 생성
        for (Map.Entry<String, List<Map<String, Object>>> e : bySupplier.entrySet()) {
            String supplierId = e.getKey();
            List<Map<String, Object>> groupItems = e.getValue();

            // 3-1) 채번
            String orderId = mrpDAO.selectNextPoId();

            // 3-2) 헤더 예상입고일(대표값): 그룹 아이템 중 가장 늦은 expectedInboundDate
            String headerInbound = groupItems.stream()
              .map(it -> (String) it.get("expectedInboundDate"))
              .filter(Objects::nonNull)
              .max(String::compareTo) // 문자열 YYYY-MM-DD 비교 OK
              .orElse(null);

            // 3-3) 헤더 INSERT
            mrpDAO.insertMaterialOrderHeader(orderId, supplierId, headerInbound);

            // 3-4) 아이템 INSERT (필요 키 표준화)
            // 각 it: {materialId, qty, unitPrice, expectedInboundDate}
            mrpDAO.insertMaterialOrderItems(orderId, groupItems);

            // 3-5) 응답 요약(총금액)
            BigDecimal total = groupItems.stream().map(it -> {
                BigDecimal q = new BigDecimal(String.valueOf(it.get("qty")));
                BigDecimal p = new BigDecimal(String.valueOf(it.get("unitPrice") == null ? "0" : it.get("unitPrice")));
                return q.multiply(p);
            }).reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String,Object> one = new HashMap<>();
            one.put("orderId", orderId);
            one.put("supplierId", supplierId);
            one.put("itemCount", groupItems.size());
            one.put("totalAmount", total);
            results.add(one);
        }
        return results;
    }

}
