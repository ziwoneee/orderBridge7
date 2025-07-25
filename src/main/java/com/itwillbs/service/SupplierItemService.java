package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.SupplierItemVO;

/**
 * 공급 품목 관련 서비스 인터페이스
 */
public interface SupplierItemService {
	
	/**
     * 특정 협력사의 공급 품목 전체 조회
     * @param supplierId 협력사 ID
     * @return 공급 품목 리스트
     */
    List<SupplierItemVO> getSuppliedItemsBySupplierId(String supplierId);

}
