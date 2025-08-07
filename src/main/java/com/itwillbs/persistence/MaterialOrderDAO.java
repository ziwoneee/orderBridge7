package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.domain.MaterialOrderItemVO;
import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.SupplierItemDTO;

/**
 * 자재 발주 DAO 인터페이스
 * - DB와 직접 연결되는 쿼리 메서드 정의
 */
public interface MaterialOrderDAO {
	
	 // 발주 목록 조회
    List<MaterialOrderVO> getOrderList(SearchCriteria cri) throws Exception;

    // 전체 건수 조회
    int getTotalCount(SearchCriteria cri) throws Exception;

    // 발주 등록
    String generateOrderId() throws Exception;
    void insertOrder(MaterialOrderVO order) throws Exception;
    void insertOrderItem(MaterialOrderItemVO item) throws Exception;
    
    // 자재명으로 거래처 검색
    public List<SupplierItemDTO> searchSuppliersByMaterial(String keyword);

    
}
