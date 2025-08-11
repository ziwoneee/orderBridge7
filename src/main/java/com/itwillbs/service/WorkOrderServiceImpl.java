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
    @Transactional
    public int registerWorkOrder(WorkOrderDTO workOrderDTO) {
        try {
            // 1. 작업지시번호 자동 생성
            String orderId = generateOrderId();
            workOrderDTO.setOrderId(orderId);

            // 2. 상태 및 생성일 설정
            workOrderDTO.setStatus("WAITING");
            workOrderDTO.setCreatedAt(new Date());

            // 3. 작업지시 등록
            int result = workOrderMapper.insertWorkOrder(workOrderDTO);
            if (result <= 0) {
                throw new RuntimeException("작업지시 등록 실패");
            }

            // 4. 병합된 수주정보 저장
            List<String> mergedOrderIds = workOrderDTO.getMergedOrders();
            String productId = workOrderDTO.getProductId();   // ✅ 추가
            int orderQty = workOrderDTO.getOrderQty();        // ✅ 추가

            if (mergedOrderIds != null && !mergedOrderIds.isEmpty()) {
                for (String clOrderId : mergedOrderIds) {
                    WorkOrderMergedDTO merged = new WorkOrderMergedDTO();
                    merged.setWorkOrderId(orderId);
                    merged.setClOrderId(clOrderId);

                    // ✅ 누락된 필드 채워주기
                    merged.setProductId(productId);
                    merged.setOrderQty(orderQty);

                    workOrderMapper.insertMergedOrder(merged);
                }
            } else {
                log.warn("병합 수주 정보가 없음 - 저장 생략");
            }

            //  5. 자재 소요량 저장 (중복 자재 합산 처리)
            List<WorkOrderMaterialDTO> materialList = workOrderDTO.getMaterialList();
            if (materialList != null && !materialList.isEmpty()) {
                
                // 자재ID 기준으로 소요량 합산
                Map<String, Integer> materialMap = new HashMap<>();
                
                for (WorkOrderMaterialDTO item : materialList) {
                    int roundedQty = (int) Math.round(item.getRequiredQty());  // 🔧 소수점 반올림
                    materialMap.merge(item.getMaterialId(), roundedQty, Integer::sum);
                }

                // 중복 제거된 자재 소요량 리스트로 저장
                for (Map.Entry<String, Integer> entry : materialMap.entrySet()) {
                    WorkOrderMaterialDTO material = new WorkOrderMaterialDTO();
                    material.setWorkOrderId(orderId);
                    material.setMaterialId(entry.getKey());
                    material.setRequiredQty(entry.getValue());

                    workOrderMapper.insertWorkOrderMaterial(material);
                }
            }

            // 6. 성공적으로 등록됐으면 result 반환
            return result;

        } catch (Exception e) {
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
    
    


    /**
     * 생산실적 등록 가능한 작업지시 목록 조회 (진행중 + 완료)
     */
    @Override
    public List<WorkOrderDTO> getInProgressOrders() {
        return workOrderMapper.selectInProgressOrders();
    }
    
 // 작업지시 실적 반영(누적 합산 후 상태 자동 갱신)
    @Override
    @Transactional
    public void refreshStatusByResults(String orderId) {
        int updated = workOrderMapper.applyResultToWorkOrder(orderId);
        if (updated == 0) {
            log.warn("applyResultToWorkOrder: 대상 없음 or 갱신 없음 - orderId={}", orderId);
        } else {
            log.info("작업지시 상태/진행률 갱신 완료 - orderId={}", orderId);
        }
    }
    
    
}