package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.domain.SupplierItemVO;

/**
 * 공급 품목 DAO 인터페이스
 */
public interface SupplierItemDAO {
	
	/**
     * 특정 협력사의 공급 품목 조회
     */
    List<SupplierItemVO> selectSuppliedItemsBySupplierId(String supplierId);
    
    
    // 특정 거래처의 공급 품목 JSON 목록 반환
    public List<SupplierItemVO> getItemsBySupplier(String supplierId) throws Exception;
    
    
    // 공급 품목 등록
    void insertItem(SupplierItemVO item) throws Exception;

}
