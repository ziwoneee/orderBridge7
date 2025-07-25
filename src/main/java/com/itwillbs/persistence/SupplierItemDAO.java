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

}
