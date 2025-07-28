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
    
}