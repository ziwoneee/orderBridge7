package com.itwillbs.service;

import java.util.List;
import java.util.Optional;

import com.itwillbs.dto.WorkOrderLiteDTO;

public interface WorkOrderQueryService {
	
	/**
     * ETA 예측 대상 작업지시서 목록 조회
     * - COMPLETED/취소 등은 제외
     * - 기본 대상: WAITING, READY (원하면 확장 가능)
     * @param q     검색어 (order_id / product_id like), null/blank 허용
     * @param limit 최대 개수 (예: 200)
     */
    List<WorkOrderLiteDTO> findEligibleForEta(String q, int limit);

    /**
     * 단건 라이트 조회 (화면에서 보정 필요할 때)
     */
    Optional<WorkOrderLiteDTO> findOne(String orderId);

}
