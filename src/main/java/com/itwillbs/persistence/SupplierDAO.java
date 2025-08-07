package com.itwillbs.persistence;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.itwillbs.domain.MaterialVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.SupplierVO;

public interface SupplierDAO {
	
	
	// 페이징 포함된 리스트 조회
    List<SupplierVO> getSupplierList(SearchCriteria cri) throws Exception;

    // 전체 건수 조회
    int getSupplierCount(SearchCriteria cri) throws Exception;
    
	
	// 협력사 ID로 협력사 상세 조회
    SupplierVO getSupplierById(String supplierId) throws Exception;
    
    
    // 오늘 날짜 기준 가장 큰 supplier_id 조회
    public String getMaxSupplierIdToday() throws Exception;

    
    // 협력사 신규 등록
    void insertSupplier(SupplierVO vo) throws Exception;
    
    
    // 사업자번호 중복확인용
    int countByBusinessNumber(String businessNumber) throws Exception;
    
    
    // 협력사 정보 수정 기능
 	void updateSupplier(SupplierVO vo) throws Exception;
 	
 	
 	// 목록 조회 (자재 발주관리 등록 폼에서 필요)
 	List<SupplierVO> selectAllSuppliers() throws Exception;
 	
 	// 거래처 ID로 공급 자재 목록 조회 (자재 발주관리)
 	List<MaterialVO> getMaterialsBySupplier(String supplierId, String keyword) throws Exception;


}
