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
 * 작업지시 관련 매퍼 인터페이스
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024
 */
@Mapper
public interface WorkOrderMapper {
    
    // ========================================================================
    // 작업지시 목록 조회
    // ========================================================================
    
    /**
     * 작업지시 목록 조회 (페이징, 검색, 정렬 포함)
     * 
     * @param cri 검색 조건 (키워드, 상태, 날짜범위, 페이징 정보)
     * @return 작업지시 목록
     */
    List<WorkOrderDTO> selectWorkOrderList(SearchCriteria cri);
    
    /**
     * 작업지시 전체 개수 조회 (검색 조건 포함)
     * 
     * @param cri 검색 조건
     * @return 검색 조건에 맞는 전체 개수
     */
    int selectWorkOrderTotalCount(SearchCriteria cri);
    
    /**
     * 작업지시 상세 조회
     * 
     * @param orderId 작업지시번호 (예: WO-20241201-001)
     * @return 작업지시 상세 정보 (재고, 우선순위 등 포함)
     */
    WorkOrderDTO selectWorkOrderDetail(@Param("orderId") String orderId);
    
    /**
     * 생산라인별 작업지시 목록 조회
     * 
     * @param lineId 생산라인 ID (예: L-01, L-02)
     * @return 해당 라인의 대기/진행중인 작업지시 목록
     */
    List<WorkOrderDTO> selectWorkOrdersByLine(@Param("lineId") String lineId);
    
    // ========================================================================
    // 통계 및 카운트 조회
    // ========================================================================
    
    /**
     * 전체 작업지시 개수 조회
     * 
     * @return 전체 작업지시 개수
     */
    int selectAllCount();
    
    /**
     * 상태별 작업지시 개수 조회
     * 
     * @param status 상태 (WAITING, READY, IN_PROGRESS, COMPLETED, CANCELLED)
     * @return 해당 상태의 작업지시 개수
     */
    int selectCountByStatus(@Param("status") String status);
    
    /**
     * 라인별 진행중인 작업지시 개수 조회
     * 
     * @param lineId 생산라인 ID
     * @return 해당 라인의 진행중인 작업지시 개수
     */
    int selectInProgressCountByLine(@Param("lineId") String lineId);
    
    // ========================================================================
    // 수주 관련 조회 (작업지시 등록용)
    // ========================================================================
    
    /**
     * 확정 수주 목록 조회 (작업지시 등록용)
     * - 작업지시가 아직 등록되지 않은 확정 수주만 조회
     * - 재고 정보 포함 (수주수량 - 재고수량 = 생산필요수량)
     * 
     * @param cri 검색 조건 (키워드, 페이징 정보)
     * @return 확정 수주 목록
     */
    List<WorkOrderDTO> selectConfirmedOrders(SearchCriteria cri);
    
    /**
     * 확정 수주 개수 조회 (페이징용)
     * 
     * @param cri 검색 조건
     * @return 확정 수주 개수
     */
    int selectConfirmedOrdersCount(SearchCriteria cri);
    
    /**
     * 수주번호 + 제품ID로 상세 조회
     * 
     * @param clOrderId 수주번호 (예: CL-20241201-001)
     * @param productId 제품ID (예: P-001)
     * @return 수주 상세 정보 (재고, 생산필요수량 등 포함)
     */
    WorkOrderDTO getOrderDetail(@Param("clOrderId") String clOrderId,
                               @Param("productId") String productId);
    
    // ========================================================================
    // BOM 조회
    // ========================================================================
    
    /**
     * 제품ID로 활성화된 BOM ID 조회
     * 
     * @param productId 제품ID
     * @return 활성화된 BOM ID (가장 최신 버전)
     */
    String getActiveBomIdByProductId(@Param("productId") String productId);
    
    /**
     * BOM ID로 자재 목록 조회
     * 
     * @param bomId BOM ID
     * @param orderQty 생산 지시 수량 (총 소요량 계산용)
     * @return BOM 자재 목록 (단위 소요량, 총 소요량 포함)
     */
    List<BomItemDTO> getBomDetailByBomId(@Param("bomId") String bomId,
                                        @Param("orderQty") int orderQty);
    
    // ========================================================================
    // 작업지시 CUD 작업
    // ========================================================================
    
    /**
     * 오늘 날짜의 작업지시 최대 순번 조회 (자동번호 생성용)
     * 
     * @param today 오늘 날짜 (yyyyMMdd 형식)
     * @return 오늘의 최대 순번 (예: 5 → 다음 번호는 WO-20241201-006)
     */
    int selectTodayMaxSequence(@Param("today") String today);
    
    /**
     * 작업지시 등록
     * 
     * @param workOrderDTO 작업지시 정보 (수주번호, 제품ID, 라인ID, 수량, 우선순위 등)
     * @return 등록 성공 여부 (1: 성공, 0: 실패)
     */
    int insertWorkOrder(WorkOrderDTO workOrderDTO);
    
    /**
     * 병합된 수주 정보 등록
     * 
     * @param mergedOrder 병합 수주 정보 DTO
     * @return 등록 성공 여부 (1: 성공, 0: 실패)
     */
    int insertMergedOrder(WorkOrderMergedDTO mergedOrder);
    
    /**
     * 작업지시별 자재 소요량 저장 (workorder_material)
     * 
     * @param item 자재 BOM 정보 DTO
     * @return 등록 성공 여부 (1: 성공, 0: 실패)
     */
    void insertWorkOrderMaterial(WorkOrderMaterialDTO item);
    
    /**
     * 작업지시 정보 수정
     * 
     * @param workOrderDTO 수정할 작업지시 정보 (라인ID, 수량, 우선순위)
     * @return 수정 성공 여부 (1: 성공, 0: 실패)
     */
    int updateWorkOrder(WorkOrderDTO workOrderDTO);
    
    /**
     * 작업지시 상태 변경
     * 
     * @param orderId 작업지시번호
     * @param status 변경할 상태 (WAITING → READY → IN_PROGRESS → COMPLETED)
     * @return 변경 성공 여부 (1: 성공, 0: 실패)
     */
    int updateWorkOrderStatus(@Param("orderId") String orderId, 
                             @Param("status") String status);
    
    /**
     * 작업지시 삭제
     * 
     * @param orderId 작업지시번호
     * @return 삭제 성공 여부 (1: 성공, 0: 실패)
     */
    int deleteWorkOrder(@Param("orderId") String orderId);
    
    // ========================================================================
    // ✅ 생산실적 등록용 작업지시 조회
    // ========================================================================

    /**
     * 생산실적 등록 가능한 작업지시 목록 조회 (READY + IN_PROGRESS)
     * - 보완생산용: READY와 IN_PROGRESS 상태 모두 포함
     * 
     * @return READY, IN_PROGRESS 상태의 작업지시 목록
     */
    List<WorkOrderDTO> selectInProgressOrders();
    
    /**
     * ✅ 일반 생산실적 등록용 작업지시 조회 (IN_PROGRESS만)
     * - 일반 등록용: IN_PROGRESS 상태만 조회
     * 
     * @return IN_PROGRESS 상태의 작업지시 목록만
     */
    List<WorkOrderDTO> selectInProgressOnlyOrders();
    
    // ========================================================================
    // 생산실적 연동
    // ========================================================================
    
    /**
     * 실적 반영 (누적/상태 업데이트)
     * 
     * @param orderId 작업지시번호
     * @return 업데이트 성공 여부
     */
    int applyResultToWorkOrder(@Param("orderId") String orderId);
    
    /**
     * 작업지시 기본 정보 조회 (Map 형태)
     * 
     * @param orderId 작업지시번호
     * @return 작업지시 기본 정보
     */
    Map<String, Object> selectWorkOrderById(String orderId);

}