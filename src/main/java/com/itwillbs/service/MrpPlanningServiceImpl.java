package com.itwillbs.service;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

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

}
