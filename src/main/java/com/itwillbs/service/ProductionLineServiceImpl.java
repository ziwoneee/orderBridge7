package com.itwillbs.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itwillbs.domain.ProductionLineVO;
import com.itwillbs.dto.WorkOrderDTO;
import com.itwillbs.mapper.ProductionLineMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProductionLineServiceImpl implements ProductionLineService {

    @Autowired
    private ProductionLineMapper productionLineMapper;

    @Override
    public List<ProductionLineVO> getAvailableLines() {
        log.debug("사용 가능한 생산라인 조회 (ACTIVE만)");
        return productionLineMapper.selectAvailableLines();
    }

    @Override
    public List<ProductionLineVO> getAllLines() {
        log.debug("모든 생산라인 조회 (ACTIVE + INACTIVE)");
        return productionLineMapper.selectAllLines();
    }

    @Override
    public int getCountByStatus(String status) {
        log.debug("상태별 생산라인 개수 조회 - 상태: {}", status);
        return productionLineMapper.selectCountByStatus(status);
    }

    @Override
    public ProductionLineVO getProductionLineDetail(String lineId) {
        log.debug("생산라인 상세 조회 - ID: {}", lineId);
        return productionLineMapper.selectProductionLineDetail(lineId);
    }

    @Override
    @Transactional
    public int updateStatus(String lineId, String status) {
        log.info("생산라인 상태 변경 - ID: {}, 상태: {}", lineId, status);

        if (!"ACTIVE".equalsIgnoreCase(status) && !"INACTIVE".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("유효하지 않은 상태값입니다. (ACTIVE/INACTIVE)");
        }

        int updated;
        if ("INACTIVE".equalsIgnoreCase(status)) {
            // 진행중 작업이 있으면 DB 레벨에서 차단
            updated = productionLineMapper.updateStatusIfNoRunning(lineId, status);
            if (updated == 0) {
                // 친절한 메시지를 위해 원인 추정
                WorkOrderDTO running = productionLineMapper.selectCurrentWorkByLineId(lineId);
                if (running != null) {
                    throw new IllegalStateException("해당 라인에 진행중 작업이 있어 비활성화할 수 없습니다.");
                }
                ProductionLineVO line = productionLineMapper.selectProductionLineDetail(lineId);
                if (line == null) {
                    throw new IllegalArgumentException("해당 생산라인을 찾을 수 없습니다: " + lineId);
                }
                throw new IllegalStateException("이미 비활성 상태이거나 동시성으로 변경에 실패했습니다.");
            }
        } else {
            // ACTIVE로 전환은 일반 업데이트
            updated = productionLineMapper.updateStatus(lineId, status);
            if (updated == 0) {
                ProductionLineVO line = productionLineMapper.selectProductionLineDetail(lineId);
                if (line == null) {
                    throw new IllegalArgumentException("해당 생산라인을 찾을 수 없습니다: " + lineId);
                }
                throw new IllegalStateException("이미 활성 상태이거나 동시성으로 변경에 실패했습니다.");
            }
        }
        return updated;
    }

    @Override
    public WorkOrderDTO getCurrentWorkByLine(String lineId) {
        log.debug("라인 진행중 작업 조회 - lineId: {}", lineId);
        return productionLineMapper.selectCurrentWorkByLineId(lineId);
    }

    @Override
    public List<WorkOrderDTO> getWaitingWorksByLine(String lineId) {
        log.debug("라인 대기중 작업 조회 - lineId: {}", lineId);
        return productionLineMapper.selectWaitingWorksByLineId(lineId);
    }
}