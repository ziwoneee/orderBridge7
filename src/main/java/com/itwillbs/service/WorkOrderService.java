package com.itwillbs.service;

import java.util.List;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.WorkOrderDTO;

/**
 * 작업지시 서비스 인터페이스
 */
public interface WorkOrderService {
    
    // 작업지시 목록 조회 
    List<WorkOrderDTO> getWorkOrderList(SearchCriteria cri);
    
    // 작업지시 총 개수 조회
    int getWorkOrderTotalCount(SearchCriteria cri);
    
    // 작업지시 상세 조회
    WorkOrderDTO getWorkOrderDetail(String orderId);
    

    //전체 작업지시 개수 조회 (탭용)
    int getAllCount();
    
    //상태별 작업지시 개수 조회 (탭용)
    int getCountByStatus(String status);
    
    // 작업지시 등록 가능한 확정 수주 목록 조회
    List<WorkOrderDTO> getConfirmedOrders(SearchCriteria cri);
    // 해당 목록의 전체 건수 조회 (페이징용)
    int getConfirmedOrdersCount(SearchCriteria cri);
    
    // 수주번호로 제품 상세 목록 조회
    List<WorkOrderDTO> getOrderDetailList(String clOrderId);
}