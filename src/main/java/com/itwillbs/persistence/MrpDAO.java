package com.itwillbs.persistence;

import java.util.List;
import java.util.Map;

/**
 * MRP 관련 데이터 접근(DAO).
 * - MyBatis Mapper XML의 SQL을 여기서 호출한다.
 * - Service는 DAO만 바라보고, 컨트롤러는 Service만 바라보는 3계층 구조.
 */
public interface MrpDAO {
	
	/**
     * 제품/수량을 입력 받아 자재별 "총소요량(gross requirement)"을 조회한다.
     *
     * @param productId 제품 ID (예: "FG-001")
     * @param orderQty  주문 수량 (예: 100)
     * @return List of Map: materialId, materialName, unit, gross_req
     */
    List<Map<String, Object>> selectGrossRequirements(String productId, double orderQty);

    
    /**
     * 제품/수량 기준으로 자재별 "가용/순소요"를 조회한다.
     * 반환 컬럼: materialId, materialName, unit, gross_req, on_hand, reserved, available, net_req
     */
    List<Map<String, Object>> selectNetting(String productId, double orderQty);
    
}
