package com.itwillbs.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.BomItemDTO;
import com.itwillbs.dto.WorkOrderDTO;
import com.itwillbs.dto.WorkOrderMaterialDTO;
import com.itwillbs.dto.WorkOrderMergedDTO;
import com.itwillbs.mapper.WorkOrderMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 작업지시 서비스 구현
 * 상태 흐름: WAITING → READY → IN_PROGRESS → COMPLETED
 * - READY→IN_PROGRESS : startProduction() (버튼)
 * - COMPLETED : refreshStatusByResults(orderId) 호출 시 자동 반영(양품 누적 기준)
 */
@Service
@Slf4j
public class WorkOrderServiceImpl implements WorkOrderService {

    @Autowired
    private WorkOrderMapper workOrderMapper;

    // ================= 목록/카운트 =================
    @Override
    public List<WorkOrderDTO> getWorkOrderList(SearchCriteria cri) {
        log.debug("작업지시 목록 조회 - 조건: {}", cri);
        return workOrderMapper.selectWorkOrderList(cri);
    }

    @Override
    public int getWorkOrderTotalCount(SearchCriteria cri) {
        log.debug("작업지시 전체 개수 조회 - 조건: {}", cri);
        return workOrderMapper.selectWorkOrderTotalCount(cri);
    }

    @Override
    public int getAllCount() {
        log.debug("전체 작업지시 개수 조회");
        return workOrderMapper.selectAllCount();
    }

    @Override
    public int getCountByStatus(String status) {
        log.debug("상태별 작업지시 개수 조회 - 상태: {}", status);
        return workOrderMapper.selectCountByStatus(status);
    }

    // ================= 상세/등록/수정/삭제 =================
    @Override
    public WorkOrderDTO getWorkOrderDetail(String orderId) {
        log.debug("작업지시 상세 조회 - ID: {}", orderId);
        return workOrderMapper.selectWorkOrderDetail(orderId);
    }

    @Override
    @Transactional
    public int registerWorkOrder(WorkOrderDTO workOrderDTO) {
        try {
            // 1) 작업지시번호 생성
            String orderId = generateOrderId();
            workOrderDTO.setOrderId(orderId);

            // 2) 기본 상태/생성일
            workOrderDTO.setStatus("WAITING");
            workOrderDTO.setCreatedAt(new Date());

            // 3) 작업지시 등록
            int result = workOrderMapper.insertWorkOrder(workOrderDTO);
            if (result <= 0) throw new RuntimeException("작업지시 등록 실패");

            // 4) 병합 수주 저장
            List<String> mergedOrderIds = workOrderDTO.getMergedOrders();
            String productId = workOrderDTO.getProductId();
            int orderQty = workOrderDTO.getOrderQty();

            if (mergedOrderIds != null && !mergedOrderIds.isEmpty()) {
                for (String clOrderId : mergedOrderIds) {
                    WorkOrderMergedDTO merged = new WorkOrderMergedDTO();
                    merged.setWorkOrderId(orderId);
                    merged.setClOrderId(clOrderId);
                    merged.setProductId(productId);
                    merged.setOrderQty(orderQty);
                    workOrderMapper.insertMergedOrder(merged);
                }
            } else {
                log.warn("병합 수주 정보 없음 - 저장 생략");
            }

            // 5) 자재 소요량 저장(중복 합산)
            List<WorkOrderMaterialDTO> materialList = workOrderDTO.getMaterialList();
            if (materialList != null && !materialList.isEmpty()) {
                Map<String, Integer> materialMap = new HashMap<>();
                for (WorkOrderMaterialDTO item : materialList) {
                    int roundedQty = (int) Math.round(item.getRequiredQty());
                    materialMap.merge(item.getMaterialId(), roundedQty, Integer::sum);
                }
                for (Map.Entry<String, Integer> e : materialMap.entrySet()) {
                    WorkOrderMaterialDTO material = new WorkOrderMaterialDTO();
                    material.setWorkOrderId(orderId);
                    material.setMaterialId(e.getKey());
                    material.setRequiredQty(e.getValue());
                    workOrderMapper.insertWorkOrderMaterial(material);
                }
            }

            return result;
        } catch (Exception e) {
            log.error("작업지시 등록 중 오류", e);
            throw new RuntimeException("작업지시 등록 중 오류: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateWorkOrder(WorkOrderDTO dto) {
        log.info("작업지시 수정 - ID: {}", dto.getOrderId());
        int updated = workOrderMapper.updateWorkOrder(dto);
        if (updated == 0) {
            log.warn("작업지시 수정 실패 - 해당 ID 없음: {}", dto.getOrderId());
            throw new RuntimeException("해당 작업지시가 존재하지 않습니다.");
        }
    }

    @Override
    @Transactional
    public void deleteWorkOrder(String orderId) {
        log.info("작업지시 소프트 삭제 - ID: {}", orderId);
        int updated = workOrderMapper.deleteWorkOrder(orderId);
        if (updated == 0) {
            log.warn("작업지시 삭제 실패 - ID 없음: {}", orderId);
            throw new RuntimeException("해당 작업지시가 존재하지 않거나 이미 삭제되었습니다.");
        }
    }

    // ================= 수주/자재/BOM =================
    @Override
    public List<WorkOrderDTO> getConfirmedOrders(SearchCriteria cri) {
        return workOrderMapper.selectConfirmedOrders(cri);
    }

    @Override
    public int getConfirmedOrdersCount(SearchCriteria cri) {
        return workOrderMapper.selectConfirmedOrdersCount(cri);
    }

    @Override
    public WorkOrderDTO getOrderDetail(String clOrderId, String productId) {
        return workOrderMapper.getOrderDetail(clOrderId, productId);
    }

    @Override
    public List<BomItemDTO> calculateMaterialUsage(String productId, int orderQty) {
        String bomId = workOrderMapper.getActiveBomIdByProductId(productId);
        if (bomId == null) {
            log.warn("활성 BOM 없음 - productId={}", productId);
            return List.of();
        }
        return workOrderMapper.getBomDetailByBomId(bomId, orderQty);
    }

    // ================= 상태 변경 =================
    @Override
    @Transactional
    public int updateWorkOrderStatus(String orderId, String status) {
        log.info("상태 변경 - ID: {}, 상태: {}", orderId, status);
        return workOrderMapper.updateWorkOrderStatus(orderId, status);
    }

    /** READY→IN_PROGRESS : 생산 시작 버튼 */
    @Override
    @Transactional
    public int startProduction(String orderId) {
        log.info("=== 생산 시작 요청 시작 === orderId: {}", orderId);

        // 1. 해당 작업지시 상세 조회
        WorkOrderDTO workOrder = workOrderMapper.selectWorkOrderDetail(orderId);
        if (workOrder == null) {
            log.error("작업지시를 찾을 수 없음: {}", orderId);
            throw new IllegalArgumentException("작업지시를 찾을 수 없음: " + orderId);
        }

        String lineId = workOrder.getLineId();

        // 2. 같은 라인에 이미 생산중(IN_PROGRESS) 작업이 있는지 체크
        int activeCount = workOrderMapper.selectInProgressCountByLine(lineId);
        log.info("라인 [{}]의 진행중 작업 개수: {}", lineId, activeCount);

        if (activeCount > 0) {
            List<WorkOrderDTO> inProgressOrders = workOrderMapper.selectWorkOrdersByLine(lineId);
            for (WorkOrderDTO order : inProgressOrders) {
                if ("IN_PROGRESS".equals(order.getStatus())) {
                    log.warn("- 진행중 작업: {} (상태: {})", order.getOrderId(), order.getStatus());
                }
            }
            throw new IllegalStateException("라인 [" + lineId + "] 은 이미 생산 중입니다.");
        }

        log.info("라인 [{}]에 진행중인 작업 없음. 생산 시작 진행", lineId);

        // 3. 상태 변경 실행 (READY → IN_PROGRESS)
        int updated = workOrderMapper.startProduction(orderId);
        log.info("상태 변경 결과: {} (0이면 실패)", updated);

        if (updated == 0) {
            log.error("상태 변경 실패 - READY 상태가 아니거나 해당 작업지시 없음: {}", orderId);
            throw new IllegalStateException("READY 상태에서만 생산 시작 가능: " + orderId);
        }

        log.info("=== 생산 시작 완료 === orderId: {}", orderId);
        return updated;
    }

    // ================= 실적 입력용 조회 =================
    @Override
    public List<WorkOrderDTO> getInProgressOnlyOrders() {
        log.debug("실적 입력용 목록 조회 (IN_PROGRESS)");
        return workOrderMapper.selectInProgressOnlyOrders();
    }

    // ================= 실적 연동 =================
    @Override
    @Transactional
    public void refreshStatusByResults(String orderId) {
        int updated = workOrderMapper.applyResultToWorkOrder(orderId);
        if (updated == 0) {
            log.warn("applyResultToWorkOrder: 갱신 없음(목표 미달 or 대상 없음) - orderId={}", orderId);
        } else {
            log.info("작업지시 상태 자동완료 반영 - orderId={}", orderId);
        }
    }

    // ================= 내부 유틸 =================
    /** 작업지시번호: WO-YYYYMMDD-XXX */
    private String generateOrderId() {
        try {
            String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
            int maxSeq = workOrderMapper.selectTodayMaxSequence(today);
            String seq = String.format("%03d", maxSeq + 1);
            String orderId = "WO-" + today + "-" + seq;
            log.debug("생성된 작업지시번호: {}", orderId);
            return orderId;
        } catch (Exception e) {
            log.error("작업지시번호 생성 실패", e);
            throw new RuntimeException("작업지시번호 생성 실패: " + e.getMessage());
        }
    }

}
