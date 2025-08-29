package com.itwillbs.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.BomItemDTO;
import com.itwillbs.dto.WorkOrderDTO;
import com.itwillbs.dto.WorkOrderMaterialDTO;
import com.itwillbs.dto.WorkOrderMergedDTO;

/**
 * 작업지시 Mapper
 * 상태 흐름: WAITING(대기) → READY(준비완료) → IN_PROGRESS(생산중) → COMPLETED(생산완료)
 * - READY→IN_PROGRESS : 버튼(수동) startProduction()
 * - IN_PROGRESS→COMPLETED : 양품 누적이 목표 도달 시 자동(applyResultToWorkOrder)
 */
@Mapper
public interface WorkOrderMapper {

    // ======================= 목록/상세 조회 =======================
    List<WorkOrderDTO> selectWorkOrderList(SearchCriteria cri);
    int selectWorkOrderTotalCount(SearchCriteria cri);
    WorkOrderDTO selectWorkOrderDetail(@Param("orderId") String orderId);
    List<WorkOrderDTO> selectWorkOrdersByLine(@Param("lineId") String lineId);

    // ========================= 통계/카운트 ========================
    int selectAllCount();
    int selectCountByStatus(@Param("status") String status);
    int selectInProgressCountByLine(@Param("lineId") String lineId);

    // ===================== 수주 조회(작업지시 등록) =====================
    List<WorkOrderDTO> selectConfirmedOrders(SearchCriteria cri);
    int selectConfirmedOrdersCount(SearchCriteria cri);
    WorkOrderDTO getOrderDetail(@Param("clOrderId") String clOrderId,
                                @Param("productId") String productId);

    // ============================ BOM =============================
    String getActiveBomIdByProductId(@Param("productId") String productId);
    List<BomItemDTO> getBomDetailByBomId(@Param("bomId") String bomId,
                                         @Param("orderQty") int orderQty);

    // ========================== 작업지시 CUD ==========================
    int selectTodayMaxSequence(@Param("today") String today);
    int insertWorkOrder(WorkOrderDTO workOrderDTO);
    int insertMergedOrder(WorkOrderMergedDTO mergedOrder);
    void insertWorkOrderMaterial(WorkOrderMaterialDTO item);
    int updateWorkOrder(WorkOrderDTO workOrderDTO);

    /** 상태 변경 공용 (필요 시 직접 호출) */
    int updateWorkOrderStatus(@Param("orderId") String orderId,
                              @Param("status") String status);

    /** 소프트 삭제 */
    int deleteWorkOrder(@Param("orderId") String orderId);

    // ======================= 생산실적 입력용 조회 =======================

    /** IN_PROGRESS만 조회 */
    List<WorkOrderDTO> selectInProgressOnlyOrders();

    // ======================== 생산실적 연동 =========================
    /** 완료 자동 반영 (양품 누적이 목표 도달 시 COMPLETED로 갱신) */
    int applyResultToWorkOrder(@Param("orderId") String orderId);

    /** 상태/라인 등 단건 확인용 */
    Map<String, Object> selectWorkOrderById(@Param("orderId") String orderId);

    /** READY → IN_PROGRESS : 생산 시작 버튼 전용 */
    int startProduction(@Param("orderId") String orderId);

    // ================== 병합 수주 연결 조회 ===================
    List<WorkOrderMergedDTO> selectMergedOrdersByWorkOrderId(
            @Param("workOrderId") String workOrderId);
    
    List<WorkOrderDTO> selectOrdersByIds(@Param("clOrderIds") List<String> clOrderIds,
            @Param("productId") String productId);
}
