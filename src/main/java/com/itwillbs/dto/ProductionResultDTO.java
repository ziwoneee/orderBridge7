package com.itwillbs.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 생산실적 DTO
 * - production_result 기본 컬럼 + 조인/집계 컬럼 + 파생 컬럼
 */
@Data
public class ProductionResultDTO {

    // ====== DB 기본 컬럼 (production_result) ======
    private String  resultId;
    private String  orderId;
    private String  productId;
    private String  lotNo;
    private Integer actualQty;
    private Integer defectQty;
    private String  workerName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    // ====== JOIN 결과 컬럼 ======
    private String  productName;     // product.product_name
    private String  lineName;        // production_line.line_name
    private Integer orderQty;        // work_order.order_qty
    private String  orderManager;    // work_order.order_manager
    private String  status;          // work_order.status
    private String  priority;        // (필요시 매퍼에서 함께 내려줄 수 있음)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date    dueDate;         // (필요시 매퍼에서 함께 내려줄 수 있음)

    // ====== 집계(매퍼에서 내려주는 값과 이름 맞춤) ======
    /** 누적 양품 = SUM(GREATEST(actual_qty - COALESCE(defect_qty,0), 0)) */
    private Integer producedQty;
    /** 누적 불량 = SUM(defect_qty) */
    private Integer defectQtyTotal;
    /** 잔여 = orderQty - producedQty */
    private Integer remainingQty;

    // ====== 파생(계산) 컬럼 ======
    /** 불량률(%) = defectQtyTotal / (producedQty + defectQtyTotal) * 100 */
    private Double defectRate;
    /** 달성률(%) = producedQty / orderQty * 100 */
    private Double achievementRate;

    // ====== 보완생산 관련 ======
    private Boolean needSupplement;  // 보완생산 필요 여부
    private Integer shortageQty;     // 부족 수량

    // ---------------------------
    // 안전한 파생값 Getter (null 방지)
    // ---------------------------

    public Double getDefectRate() {
        if (defectRate != null) return clampRate(defectRate);
        int good = producedQty == null ? 0 : producedQty;
        int bad  = defectQtyTotal == null ? 0 : defectQtyTotal;
        int total = good + bad;
        if (total <= 0) return 0.0;
        return clampRate((bad * 100.0) / total);
    }

    public Double getAchievementRate() {
        if (achievementRate != null) return clampRate(achievementRate);
        int target = orderQty == null ? 0 : orderQty;
        int good   = producedQty == null ? 0 : producedQty;
        if (target <= 0) return 0.0;
        return clampRate((good * 100.0) / target);
    }

    public Integer getRemainingQty() {
        if (remainingQty != null) return Math.max(0, remainingQty);
        int target = orderQty == null ? 0 : orderQty;
        int good   = producedQty == null ? 0 : producedQty;
        return Math.max(0, target - good);
    }

    private Double clampRate(Double v) {
        if (v == null) return 0.0;
        if (v < 0) return 0.0;
        if (v > 100) return 100.0;
        return v;
    }
}
