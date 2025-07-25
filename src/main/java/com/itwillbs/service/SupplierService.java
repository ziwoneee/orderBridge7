package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.SupplierVO;

public interface SupplierService {
	
	// 1-1. 페이징이 포함된 리스트 조회
	List<SupplierVO> getSupplierList(SearchCriteria cri) throws Exception;
		
	// 1-2. 전체 협력사 수 조회 (페이징 계산용)
	int getSupplierCount(SearchCriteria cri) throws Exception;
	
	// 2. 특정 협력사 ID에 해당하는 상세 정보 반환
    SupplierVO getSupplierById(String supplierId) throws Exception;
    
    
    // 3. 협력사 신규 등록
    void insertSupplier(SupplierVO vo) throws Exception;
    void registerSupplier(SupplierVO vo) throws Exception;

    
    // 4. 사업자번호 중복확인용
    boolean isBusinessNumberExists(String businessNumber) throws Exception;
    
    
    // 5. 협력사 정보 수정 기능
    void updateSupplier(SupplierVO vo) throws Exception;
    

}
