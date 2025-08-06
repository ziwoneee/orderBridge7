package com.itwillbs.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.BomItemDTO;
import com.itwillbs.dto.WorkOrderDTO;
import com.itwillbs.dto.WorkOrderMergedDTO;
import com.itwillbs.mapper.WorkOrderMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 작업지시 관련 서비스 구현체
 */
@Service
@Slf4j
public class WorkOrderServiceImpl implements WorkOrderService {
	
    @Autowired
    private WorkOrderMapper workOrderMapper;


    /**
     * 작업지시 목록 조회 (검색, 페이징 포함)
     */
    @Override
    public List<WorkOrderDTO> getWorkOrderList(SearchCriteria cri) {
        log.debug(" 작업지시 목록 조회 - 조건: {}", cri);
        return workOrderMapper.selectWorkOrderList(cri);
    }

    /**
     * 작업지시 전체 개수 조회 (검색 조건 포함)
     */
    @Override
    public int getWorkOrderTotalCount(SearchCriteria cri) {
        log.debug(" 작업지시 전체 개수 조회 - 조건: {}", cri);
        return workOrderMapper.selectWorkOrderTotalCount(cri);
    }

    /**
     * 전체 작업지시 개수 조회 (검색 조건 없음)
     */
    @Override
    public int getAllCount() {
        log.debug(" 전체 작업지시 개수 조회");
        return workOrderMapper.selectAllCount();
    }

    /**
     * 상태별 작업지시 개수 조회
     */
    @Override
    public int getCountByStatus(String status) {
        log.debug(" 상태별 작업지시 개수 조회 - 상태: {}", status);
        return workOrderMapper.selectCountByStatus(status);
    }

    /**
     * 확정 수주 목록 조회 (작업지시 등록용)
     */
    @Override
    public List<WorkOrderDTO> getConfirmedOrders(SearchCriteria cri) {
        log.debug(" 확정 수주 목록 조회 - 조건: {}", cri);
        return workOrderMapper.selectConfirmedOrders(cri);
    }

    /**
     * 확정 수주 개수 조회
     */
    @Override
    public int getConfirmedOrdersCount(SearchCriteria cri) {
        log.debug(" 확정 수주 개수 조회 - 조건: {}", cri);
        return workOrderMapper.selectConfirmedOrdersCount(cri);
    }
    
    /**
     * 작업지시 상세 조회
     */
    @Override
    public WorkOrderDTO getWorkOrderDetail(String orderId) {
        log.debug(" 작업지시 상세 조회 - ID: {}", orderId);
        return workOrderMapper.selectWorkOrderDetail(orderId); // ✅ 변경 완료
    }

    /**
     * 작업지시 등록
     */
    @Override
    @Transactional  // 트랜잭션 관리 - 작업지시 등록 전체가 하나의 트랜잭션으로 처리됨
    public int registerWorkOrder(WorkOrderDTO workOrderDTO) {
        try {
            // 1. 작업지시번호 자동 생성 (예: WO-20250807-001)
            String orderId = generateOrderId();
            workOrderDTO.setOrderId(orderId);

            // 2. 작업지시 상태 초기화 및 등록일 세팅
            workOrderDTO.setStatus("WAITING");           // 기본 상태: 대기
            workOrderDTO.setCreatedAt(new Date());       // 현재 시각으로 등록일 설정

            // 3. work_order 테이블에 기본 작업지시 정보 저장
            int result = workOrderMapper.insertWorkOrder(workOrderDTO);
            if (result <= 0) {
                // 저장 실패 시 예외 발생
                throw new RuntimeException("작업지시 등록 실패");
            }

            // 4. 병합된 수주 정보(work_order_client_order 테이블)에 저장
            List<WorkOrderMergedDTO> mergedOrders = workOrderDTO.getMergedOrders();
            if (mergedOrders != null) {
                for (WorkOrderMergedDTO merged : mergedOrders) {
                    merged.setWorkOrderId(orderId);          // 방금 생성한 작업지시번호 주입
                    workOrderMapper.insertMergedOrder(merged); // 병합 수주 데이터 삽입
                }
            }

            // 5. BOM 기준 자재 소요량 계산 (제품ID와 작업지시 수량 기반)
            List<BomItemDTO> bomList = calculateMaterialUsage(workOrderDTO.getProductId(), workOrderDTO.getOrderQty());

            // 6. 자재 소요량을 workorder_material 테이블에 저장
            for (BomItemDTO item : bomList) {
                item.setWorkOrderId(orderId);                 // 작업지시 번호 FK 세팅
                workOrderMapper.insertWorkOrderMaterial(item); // 자재 소요량 저장
            }

            // 7. 성공적으로 등록됐으면 결과 반환 (1 이상)
            return result;

        } catch (Exception e) {
            // 에러 발생 시 로그 기록 및 RuntimeException으로 재발생
            log.error("작업지시 등록 중 오류", e);
            throw new RuntimeException("작업지시 등록 중 오류: " + e.getMessage());
        }
    }

    /**
     * 작업지시 상태 변경
     */
    @Override
    @Transactional
    public int updateWorkOrderStatus(String orderId, String status) {
        log.info(" 작업지시 상태 변경 - ID: {}, 상태: {}", orderId, status);
        
        try {
            int result = workOrderMapper.updateWorkOrderStatus(orderId, status);
            
            if (result > 0) {
                log.info(" 작업지시 상태 변경 완료 - ID: {}, 새상태: {}", orderId, status);
            } else {
                log.warn(" 작업지시 상태 변경 실패 - 해당 ID가 존재하지 않음: {}", orderId);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error(" 작업지시 상태 변경 중 오류 발생", e);
            throw new RuntimeException("작업지시 상태 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 작업지시 수정
     * - 수량, 우선순위만 수정 가능
     */
    @Override
    @Transactional
    public void updateWorkOrder(WorkOrderDTO dto) {
        log.info("작업지시 수정 요청 - ID: {}", dto.getOrderId());
        try {
            int result = workOrderMapper.updateWorkOrder(dto);
            if (result > 0) {
                log.info("작업지시 수정 성공 - ID: {}", dto.getOrderId());
            } else {
                log.warn("작업지시 수정 실패 - 해당 ID 없음: {}", dto.getOrderId());
                throw new RuntimeException("해당 작업지시가 존재하지 않습니다.");
            }
        } catch (Exception e) {
            log.error("작업지시 수정 중 오류", e);
            throw new RuntimeException("작업지시 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 작업지시 소프트 삭제
     */
    @Override
    @Transactional
    public void deleteWorkOrder(String orderId) {
        log.info("작업지시 소프트 삭제 요청 - ID: {}", orderId);
        try {
            int result = workOrderMapper.deleteWorkOrder(orderId); // ← 이름 되돌림
            if (result > 0) {
                log.info("작업지시 삭제 성공 (is_deleted = true) - ID: {}", orderId);
            } else {
                log.warn("작업지시 삭제 실패 - ID 없음: {}", orderId);
                throw new RuntimeException("해당 작업지시가 존재하지 않거나 이미 삭제되었습니다.");
            }
        } catch (Exception e) {
            log.error("작업지시 삭제 중 오류", e);
            throw new RuntimeException("작업지시 삭제 중 오류: " + e.getMessage());
        }
    }

    /**
     * 작업지시번호 자동 생성
     * 형식: WO-YYYYMMDD-XXX (예: WO-20250729-001)
     */
    private String generateOrderId() {
        try {
            // 오늘 날짜
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String today = sdf.format(new Date());
            
            // 오늘 날짜의 최대 순번 조회
            int maxSequence = workOrderMapper.selectTodayMaxSequence(today);
            
            // 다음 순번 계산 (3자리로 포맷)
            int nextSequence = maxSequence + 1;
            String sequenceStr = String.format("%03d", nextSequence);
            
            // 작업지시번호 생성
            String orderId = "WO-" + today + "-" + sequenceStr;
            
            log.debug(" 작업지시번호 생성: {}", orderId);
            return orderId;
            
        } catch (Exception e) {
            log.error(" 작업지시번호 생성 실패", e);
            throw new RuntimeException("작업지시번호 생성에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 수주번호 + 제품ID로 상세 조회
     */
    @Override
    public WorkOrderDTO getOrderDetail(String clOrderId, String productId) {
        return workOrderMapper.getOrderDetail(clOrderId, productId);
    }

    /**
     * BOM 기준 자재 소요량 계산
     * - 제품 ID와 지시 수량을 기반으로
     * - 1팩당 BOM 사용량 * 지시 수량 계산
     * @param productId 제품 ID
     * @param orderQty 지시 수량
     * @return 자재ID, 자재명, 단위, 사용량 포함된 리스트
     */
    @Override
    public List<BomItemDTO> calculateMaterialUsage(String productId, int orderQty) {
        log.debug(" BOM 자재 계산 - productId={}, orderQty={}", productId, orderQty);

        // 1단계: 제품 ID로 활성화된 BOM ID 조회
        String bomId = workOrderMapper.getActiveBomIdByProductId(productId);
        log.debug(" 조회된 활성 BOM ID: {}", bomId);

        if (bomId == null) {
            log.warn(" 활성 BOM 없음 - productId={}", productId);
            return List.of(); // 빈 리스트 반환
        }

        // 2단계: BOM ID로 자재 목록 조회
        List<BomItemDTO> bomList = workOrderMapper.getBomDetailByBomId(bomId, orderQty);

        return bomList;
    }
    
    
    // 자재 출고관리에 필요
    /**
     * 대기(WAITING) 상태의 작업지시 목록 조회
     */
    @Override
    public List<WorkOrderDTO> getWaitingWorkOrders() {
        return workOrderMapper.selectWaitingWorkOrders();
    }


    
}