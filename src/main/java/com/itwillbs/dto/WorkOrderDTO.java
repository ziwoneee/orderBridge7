package com.itwillbs.dto;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 작업지시 + 제품 + 라인 + 거래처 + 재고 정보를 담는 통합 DTO
 * 상태 흐름: WAITING → READY → IN_PROGRESS → COMPLETED
 */
@Data
public class WorkOrderDTO {
    // ---------------------------
    // 작업 지시 기본 정보
    // ---------------------------
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date clOrderDate;       // 수주일

    private String clOrderId;       // 수주번호 (select-order-popup용)
    private String clientName;      // 단건 조회용

    private String orderId;         // 작업지시번호
    private String productId;
    private String lineId;
    private int    orderQty;
    private String priority;        // EMERGENCY/HIGH/NORMAL/LOW
    private String status;          // WAITING/READY/IN_PROGRESS/COMPLETED
    private String remarks;
    private String orderManager;    // 작업지시자

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    private Boolean isDeleted;

    // ---------------------------
    // 집계/진행 정보 (조회용)
    // ---------------------------
    /** 누적 양품 수량: SUM(actual_qty - defect_qty) */
    private Integer producedQty;

    /** 누적 불량 수량: SUM(defect_qty) (있으면 편함) */
    private Integer defectQtyTotal;

    /** 잔여 수량: orderQty - producedQty (쿼리에서 내려오면 매핑, 없으면 getter에서 계산) */
    private Integer remainingQty;

    // 편의계산: remainingQty가 세팅되어 있지 않으면 producedQty로 계산
    public Integer getRemainingQty() {
        if (remainingQty != null) return Math.max(0, remainingQty);
        if (producedQty == null)   return orderQty;
        return Math.max(0, orderQty - producedQty);
    }

    /** 진행률(%) = producedQty / orderQty * 100 */
    public Double getProgressRate() {
        if (orderQty <= 0) return 0.0;
        int good = (producedQty == null ? 0 : producedQty);
        double rate = (good * 100.0) / orderQty;
        return Math.min(100.0, Math.max(0.0, rate));
    }

    // ---------------------------
    // 제품/라인 정보 (조회용)
    // ---------------------------
    private String productName;
    private String unit;

    private String lineName;

    // ---------------------------
    // 병합 수주 관련
    // ---------------------------
    private List<String> mergedOrders; // 병합 수주 ID 리스트
    private String clientNames;        // 병합된 거래처명들 (콤마 구분)
    private int    totalOrderQty;      // 병합 총 수량

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;              // 병합된 수주 중 가장 빠른 납기일

    // ---------------------------
    // 재고/필요량
    // ---------------------------
    private int requiredQty;           // 필요 수량 (수주 수량 - 가용 재고)
    private int availableQty;          // 가용 재고

    // ---------------------------
    // BOM/자재 소요 (저장용)
    // ---------------------------
    private List<WorkOrderMaterialDTO> materialList;

    // ---------------------------
    // BOM 상세 리스트 (조회용)
    // ---------------------------
    private List<BomItemDTO> bomList;
    
 // ---------------------------
 // 진행 시간 관련 (조회용)
 // ---------------------------
 @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
 private Date startedAt;   // production_result.started_at (진행중인 실적 시작시간)
}
