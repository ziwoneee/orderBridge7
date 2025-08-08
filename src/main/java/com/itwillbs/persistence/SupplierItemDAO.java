package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.SupplierItemVO;

/**
 * 공급 품목 DAO 인터페이스
 */
public interface SupplierItemDAO {
	
    
    // 특정 거래처의 공급 품목 JSON 목록 반환
    public List<SupplierItemVO> getItemsBySupplier(String supplierId) throws Exception;
    
    // 페이징
    int getItemCountBySupplier(String supplierId) throws Exception;

    List<SupplierItemVO> getItemListBySupplierWithPaging(String supplierId, SearchCriteria cri) throws Exception;
    
    
    // 공급 품목 등록
    void insertItem(SupplierItemVO item) throws Exception;
    
    
    // 공급 품목 중복 확인
    int countBySupplierAndMaterial(String supplierId, String materialId) throws Exception;
    
    // 공급 품목 수정
    void updateItem(SupplierItemVO item) throws Exception;
   
    
    // 공급 품목 단건 조회 (수정폼용)
    SupplierItemVO getItemById(String itemId) throws Exception;
    

}
