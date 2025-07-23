package com.itwillbs.persistence;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.itwillbs.domain.SupplierVO;

public interface SupplierDAO {
	
	// 검색 조건 및 키워드, 정렬에 따른 협력사 목록 조회
	List<SupplierVO> selectSupplierList(@Param("keyword") String keyword,
										@Param("condition") String condition,
										@Param("sort") String sort,
										@Param("order") String order) throws Exception;
	
	// ✅ 페이징 포함된 리스트 조회
    List<SupplierVO> getSupplierListPaged(int offset, int size, String keyword, String condition, String sort, String order) throws Exception;

    // ✅ 전체 건수 조회
    int getSupplierCount(String keyword, String condition) throws Exception;
    
	
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

}
