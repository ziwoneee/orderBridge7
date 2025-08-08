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

    // 특정 거래처의 공급 품목 JSON 목록 반환
    public List<SupplierItemVO> getItemsBySupplier(String supplierId) throws Exception;
    
    // 공급 품목 등록
    void registerItem(SupplierItemVO item) throws Exception;
    
    // 공급 품목 수정
    void updateItem(SupplierItemVO item) throws Exception;

}
