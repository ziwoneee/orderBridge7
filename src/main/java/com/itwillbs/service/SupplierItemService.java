package com.itwillbs.service;

import java.util.List;
import java.util.Map;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.SupplierItemVO;

/**
 * 공급 품목 관련 서비스 인터페이스
 */
public interface SupplierItemService {
	
    // 특정 거래처의 공급 품목 JSON 목록 반환
	List<SupplierItemVO> getItemsBySupplier(String supplierId) throws Exception;
    
    // 페이징
    int getItemCountBySupplier(String supplierId) throws Exception;

    List<SupplierItemVO> getItemListBySupplierWithPaging(String supplierId, SearchCriteria cri) throws Exception;
    
    // 공급 품목 등록
    void registerItem(SupplierItemVO item) throws Exception;
    
    // 공급 품목 중복 확인
    boolean isDuplicateItem(String supplierId, String materialId, String itemId) throws Exception;
    
    // 공급 품목 수정
    void updateItem(SupplierItemVO item) throws Exception;
    
    // 공급 품목 단건 조회 (수정폼용)
    SupplierItemVO getItemById(String itemId) throws Exception;

}
