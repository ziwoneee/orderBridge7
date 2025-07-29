package com.itwillbs.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.WorkOrderDTO;

/**
 * 작업지시 MyBatis 매퍼 인터페이스
 */
@Mapper
public interface WorkOrderMapper {
    

    // 작업지시 목록 조회
    List<WorkOrderDTO> getWorkOrderList(SearchCriteria cri);
    

    // 작업지시 총 개수 조회
    int getWorkOrderTotalCount(SearchCriteria cri);
    

    // 작업지시 상세 조회 
    WorkOrderDTO getWorkOrderDetail(@Param("orderId") String orderId);
    
    // 확정된 수주 목록 조회 (작업지시 등록 대상)
    List<WorkOrderDTO> getConfirmedOrders(SearchCriteria cri);

    // 목록 총 개수
    int getConfirmedOrdersCount(SearchCriteria cri);
    

    // 수주번호 기준 상세 제품 목록 조회(재고 수량, 생산 필요 수량 포함)

    List<WorkOrderDTO> getOrderDetailList(String clOrderId);

}