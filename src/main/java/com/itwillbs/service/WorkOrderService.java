package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.BomItemDTO;
import com.itwillbs.dto.WorkOrderDTO;

/**
 * 작업지시 관련 서비스 인터페이스
 */
public interface WorkOrderService {
    
    /**
     * 작업지시 목록 조회 (검색, 페이징 포함)
     * @param cri 검색 조건
     * @return 작업지시 목록
     */
    List<WorkOrderDTO> getWorkOrderList(SearchCriteria cri);
    
    /**
     * 작업지시 전체 개수 조회 (검색 조건 포함)
     * @param cri 검색 조건
     * @return 전체 개수
     */
    int getWorkOrderTotalCount(SearchCriteria cri);
    
    /**
     * 전체 작업지시 개수 조회 (검색 조건 없음)
     * @return 전체 개수
     */
    int getAllCount();
    
    /**
     * 상태별 작업지시 개수 조회
     * @param status 상태 (WAITING, IN_PROGRESS, COMPLETED)
     * @return 해당 상태의 개수
     */
    int getCountByStatus(String status);
    
    /**
     * 작업지시 상세 조회
     * @param orderId 작업지시번호
     * @return 작업지시 상세 정보
     */
    WorkOrderDTO getWorkOrderDetail(String orderId);
    
    /**
     * 확정 수주 목록 조회 (작업지시 등록용)
     * @param cri 검색 조건
     * @return 확정 수주 목록
     */
    List<WorkOrderDTO> getConfirmedOrders(SearchCriteria cri);
    
    /**
     * 확정 수주 개수 조회
     * @param cri 검색 조건
     * @return 확정 수주 개수
     */
    int getConfirmedOrdersCount(SearchCriteria cri);
    
    /**
     * 작업지시 등록
     * @param workOrderDTO 작업지시 정보
     * @return 등록 성공 여부 (1: 성공, 0: 실패)
     */
    int registerWorkOrder(WorkOrderDTO workOrderDTO);
    
    /**
     * 작업지시 상태 변경
     * @param orderId 작업지시번호
     * @param status 변경할 상태
     * @return 변경 성공 여부 (1: 성공, 0: 실패)
     */
    int updateWorkOrderStatus(String orderId, String status);
    
    /**
     * 작업지시 수정
     * @param dto 수정할 작업지시 정보
     */
    void updateWorkOrder(WorkOrderDTO dto);
    
    /**
     * 작업지시 삭제
     * @param orderId 작업지시번호
     */
    void deleteWorkOrder(String orderId);

    // 수주 상세 정보 조회 (작업지시 등록용)
    WorkOrderDTO getOrderDetail(String clOrderId, String productId);
    
    /**
     * 작업지시 등록 시, BOM 기준 자재 소요량 계산
     * @param productId 제품 ID
     * @param orderQty 지시 수량
     * @return 자재 소요량 목록 (자재명, 단위, 수량 포함)
     */
    List<BomItemDTO> calculateMaterialUsage(String productId, int orderQty);
    
    
    // 자재 출고관리에 필요
    /**
     * 대기 상태의 작업지시 목록 조회
	 * - 출고 등록 시 작업지시 선택용
	 * @return 작업지시 목록
     */
    List<WorkOrderDTO> getWaitingWorkOrders();


}