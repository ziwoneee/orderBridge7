package com.itwillbs.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.WorkOrderDTO;
import com.itwillbs.mapper.WorkOrderMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 작업지시 서비스 구현체
 */
@Service
@Slf4j
public class WorkOrderServiceImpl implements WorkOrderService {
    
    @Autowired
    private WorkOrderMapper workOrderMapper;
    
    @Override
    public List<WorkOrderDTO> getWorkOrderList(SearchCriteria cri) {
        log.info("작업지시 목록 조회 - 조건: {}", cri);
        
        // 기본 정렬 설정
        if (cri.getSortColumn() == null) {
            cri.setSortColumn("w.created_at");  
            cri.setSortOrder("desc");
        }
        
        return workOrderMapper.getWorkOrderList(cri);  
    }
    
    @Override
    public int getWorkOrderTotalCount(SearchCriteria cri) {
        log.info("작업지시 총 개수 조회 - 조건: {}", cri);
        return workOrderMapper.getWorkOrderTotalCount(cri);  
    }
    
    @Override
    public WorkOrderDTO getWorkOrderDetail(String orderId) {
        log.info("작업지시 상세 조회 - ID: {}", orderId);
        return workOrderMapper.getWorkOrderDetail(orderId);  
    }
    
    @Override
    public int getAllCount() {
        log.info("전체 작업지시 개수 조회");
        SearchCriteria cri = new SearchCriteria();
        return workOrderMapper.getWorkOrderTotalCount(cri);
    }
    
    @Override
    public int getCountByStatus(String status) {
        log.info("상태별 작업지시 개수 조회 - 상태: {}", status);
        SearchCriteria cri = new SearchCriteria();
        cri.setStatus(status);
        return workOrderMapper.getWorkOrderTotalCount(cri);
    }
    

    // [1] 작업지시 등록 가능한 확정 수주 목록 조회
    @Override
    public List<WorkOrderDTO> getConfirmedOrders(SearchCriteria cri) {
        log.debug(" getConfirmedOrders() 호출");
        return workOrderMapper.getConfirmedOrders(cri);
    }


    // 해당 목록의 전체 건수 조회
    @Override
    public int getConfirmedOrdersCount(SearchCriteria cri) {
        log.debug(" getConfirmedOrdersCount() 호출");
        return workOrderMapper.getConfirmedOrdersCount(cri);
    }
 // 수주번호로 제품 상세 목록 조회
    @Override
    public List<WorkOrderDTO> getOrderDetailList(String clOrderId) {
        log.debug("▶ getOrderDetailList() 실행 - clOrderId: {}", clOrderId);
        return workOrderMapper.getOrderDetailList(clOrderId);
    }

}