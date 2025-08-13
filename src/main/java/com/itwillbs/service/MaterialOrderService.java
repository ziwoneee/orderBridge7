package com.itwillbs.service;

import java.util.List;
import java.util.Map;

import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialOrderDTO;
import com.itwillbs.dto.PurchaseDraftRequest;
import com.itwillbs.dto.PurchaseDraftResult;
import com.itwillbs.dto.SupplierItemDTO;

/**
 * 자재 발주 서비스 인터페이스
 * - 발주 목록 및 개수 조회 등의 비즈니스 로직 정의
 */
public interface MaterialOrderService {
	
	// 발주 목록 조회
    List<MaterialOrderVO> getOrderList(SearchCriteria cri) throws Exception;

    // 총 건수 조회 (페이징)
    int getTotalCount(SearchCriteria cri) throws Exception;

    // 발주 등록
    void insertOrder(MaterialOrderDTO orderDTO) throws Exception;
    
    // 자재명으로 거래처 검색
    List<SupplierItemDTO> searchSuppliersByMaterial(String keyword);
    
    
    // 부족분으로 발주 초안 생성	
    PurchaseDraftResult createDraftFromShortages(PurchaseDraftRequest req) throws Exception;

    /* 발주 초안에서 요청 */
    void submitOrderRequest(String orderId) throws Exception;
    
    
    /**
     * 발주 상세
     */
    // 주문 헤더
    Map<String,Object> getOrderHeader(String orderId) throws Exception;
    
    // 주문 아이템 목록
    List<Map<String,Object>> getOrderItems(String orderId) throws Exception;
    
    
    /**
     * 발주 상태별 탭기능 카운트
     */
    int getCountByStatus(String status);
    Map<String, Integer> getStatusCounts();
    
    
    //협력사 승인 요청 이메일
    void sendApprovalRequest(String orderId);
    
    //협력사 승인 상태변경
	MaterialOrderVO findByOrderId(String orderId);

    
    
}
