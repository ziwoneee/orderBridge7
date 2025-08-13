package com.itwillbs.service;

import java.util.List;
import java.util.Map;

/**
 * MRP 도메인 서비스.
 * - 비즈니스 로직(검증/가공/트랜잭션)을 담당.
 * - 현재 단계는 단순 위임(DAO → Controller) 구조로 시작하고,
 *   다음 단계(가용/순소요/부족분)에서 계산/검증을 여기에 추가한다.
 */
public interface MrpPlanningService {
	
	/**
     * 제품/수량 기준으로 자재별 "총소요량"을 조회한다.
     */
    List<Map<String, Object>> getGrossRequirements(String productId, double orderQty) throws Exception;
    
    
    /**
     * 가용/순소요(Netting) 결과를 조회한다.
     */
    List<Map<String, Object>> getNetting(String productId, double orderQty) throws Exception;

}
