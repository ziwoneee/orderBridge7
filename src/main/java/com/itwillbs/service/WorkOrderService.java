package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.BomItemDTO;
import com.itwillbs.dto.WorkOrderDTO;

/**
 * 작업지시 서비스
 * 상태: WAITING → READY → IN_PROGRESS → COMPLETED
 * - READY→IN_PROGRESS : startProduction() (버튼)
 * - COMPLETED : applyResultToWorkOrder(orderId)로 자동 반영
 */
public interface WorkOrderService {

    // ================= 목록/카운트 =================
    List<WorkOrderDTO> getWorkOrderList(SearchCriteria cri);
    int getWorkOrderTotalCount(SearchCriteria cri);
    int getAllCount();
    int getCountByStatus(String status);

    // ================= 작업지시 상세/등록/수정/삭제 ==============
    WorkOrderDTO getWorkOrderDetail(String orderId);
    int registerWorkOrder(WorkOrderDTO workOrderDTO);
    void updateWorkOrder(WorkOrderDTO dto);
    void deleteWorkOrder(String orderId);

    // ================= 수주/자재/BOM =================
    List<WorkOrderDTO> getConfirmedOrders(SearchCriteria cri);
    int getConfirmedOrdersCount(SearchCriteria cri);
    WorkOrderDTO getOrderDetail(String clOrderId, String productId);
    List<BomItemDTO> calculateMaterialUsage(String productId, int orderQty);

    // ================= 상태 변경 =================
    /** 공용 상태 변경 (필요 시 직접 사용) */
    int updateWorkOrderStatus(String orderId, String status);

    /** READY → IN_PROGRESS : 생산 시작 버튼 */
    int startProduction(String orderId);

    // ================= 실적 입력용 조회 =================

    /** IN_PROGRESS만 (일반 등록용) */
    List<WorkOrderDTO> getInProgressOnlyOrders();

    // ================= 실적 연동 =================
    /** 실적 집계로 완료 자동 반영 (양품 누적 >= 목표) */
    void refreshStatusByResults(String orderId);
}
