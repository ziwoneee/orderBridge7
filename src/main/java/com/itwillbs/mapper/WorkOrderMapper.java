package com.itwillbs.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.WorkOrderDTO;

/**
 * 작업지시 관련 매퍼 인터페이스
 */
@Mapper
public interface WorkOrderMapper {
    
    /**
     * 작업지시 목록 조회 (페이징, 검색, 정렬 포함)
     * @param cri 검색 조건
     * @return 작업지시 목록
     */
    List<WorkOrderDTO> selectWorkOrderList(SearchCriteria cri);
    
    /**
     * 작업지시 전체 개수 조회 (검색 조건 포함)
     * @param cri 검색 조건
     * @return 전체 개수
     */
    int selectWorkOrderTotalCount(SearchCriteria cri);
    
    /**
     * 전체 작업지시 개수 조회 (검색 조건 없음)
     * @return 전체 개수
     */
    int selectAllCount();
    
    /**
     * 상태별 작업지시 개수 조회
     * @param status 상태 (WAITING, IN_PROGRESS, COMPLETED)
     * @return 해당 상태의 개수
     */
    int selectCountByStatus(@Param("status") String status);
    
    /**
     * 작업지시 상세 조회
     * @param orderId 작업지시번호
     * @return 작업지시 상세 정보
     */
    WorkOrderDTO selectWorkOrderDetail(@Param("orderId") String orderId);
    
    /**
     * 확정 수주 목록 조회 (작업지시 등록용)
     * - 재고 계산 포함 (수주수량 - 재고수량 = 생산필요수량)
     * @param cri 검색 조건
     * @return 확정 수주 목록
     */
    List<WorkOrderDTO> selectConfirmedOrders(SearchCriteria cri);
    
    /**
     * 확정 수주 개수 조회
     * @param cri 검색 조건
     * @return 확정 수주 개수
     */
    int selectConfirmedOrdersCount(SearchCriteria cri);
    
    /**
     * 오늘 날짜의 작업지시 최대 순번 조회 (자동번호 생성용)
     * @param today 오늘 날짜 (yyyyMMdd 형식)
     * @return 최대 순번
     */
    int selectTodayMaxSequence(@Param("today") String today);
    
    /**
     * 작업지시 등록
     * @param workOrderDTO 작업지시 정보
     * @return 등록 성공 여부 (1: 성공, 0: 실패)
     */
    int insertWorkOrder(WorkOrderDTO workOrderDTO);
    
    /**
     * 작업지시 상태 변경
     * @param orderId 작업지시번호
     * @param status 변경할 상태
     * @return 변경 성공 여부 (1: 성공, 0: 실패)
     */
    int updateWorkOrderStatus(@Param("orderId") String orderId, @Param("status") String status);
    
    /**
     * 작업지시 정보 수정 (필요시 추가)
     * @param workOrderDTO 작업지시 정보
     * @return 수정 성공 여부
     */
    int updateWorkOrder(WorkOrderDTO workOrderDTO);
    
    /**
     * 작업지시 삭제 (필요시 추가)
     * @param orderId 작업지시번호
     * @return 삭제 성공 여부
     */
    int deleteWorkOrder(@Param("orderId") String orderId);
    
    /**
     * 라인별 진행중인 작업지시 개수 조회 (부하 확인용, 필요시 추가)
     * @param lineId 라인ID
     * @return 진행중인 작업지시 개수
     */
    int selectInProgressCountByLine(@Param("lineId") String lineId);
    
    // 수주번호 + 제품ID로 상세 조회
    WorkOrderDTO getOrderDetail(@Param("clOrderId") String clOrderId,
                                @Param("productId") String productId);
}