package com.itwillbs.persistence;

import java.util.List;
import java.util.Map;

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
    
    
	/* 부족분으로 발주 자동 생성 */
    // 1) 발주 항목의 다음 순번 조회
    public int selectNextOrderItemIndex(String orderId) throws Exception;
    
    // 2) 자재 ID 목록으로 거래처-자재 매핑 정보 조회
    public List<Map<String, Object>> selectSupplierItemMappings(List<String> materialIds) throws Exception;
    
    // 3) 발주 초안 헤더 등록
    public void insertOrderHeaderDraft(Map<String, Object> params) throws Exception;
    
    // 4) 발주 초안 항목 등록 (배치)
    public void insertOrderItemsBatch(List<Map<String, Object>> items) throws Exception;
    
    int insertOrderItem(Map<String, Object> item) throws Exception;
    
    // 내부사용(N) 자재 조회
    List<Map<String, Object>> selectNonPurchasableFromList(List<String> materialIds) throws Exception;

    
	/* 발주 초안에서 요청 */
    // 0) 초안 + 항목 로드 (검증용)
    List<Map<String,Object>> selectDraftWithItems(String orderId) throws Exception;
    
    // 0-1) 항목 총액 합계 (있으면 보여주기/로그용)
    Integer selectItemsTotal(String orderId) throws Exception;
    
    // 1) 상태 전이: 초안 -> 요청 (동시성 보호: 초안일 때만)
    int updateOrderToRequested(String orderId) throws Exception;
    
    
    /**
     * 발주 상세
     */
    // 주문 헤더
    Map<String,Object> selectOrderHeader(String orderId) throws Exception;
    
    // 주문 아이템 목록
    List<Map<String,Object>> selectOrderItems(String orderId) throws Exception;
    
    
    /**
     * 발주 상태별 탭기능 카운트
     */
    int getCountByStatus(String status);
    Map<String, Integer> getStatusCounts();
    
    //협력사 이메일
    MaterialOrderVO findById(String orderId);

    
    //협력사 발주상태 업데이트
	void updateOrderStatus(MaterialOrderVO vo);

	

    
}
