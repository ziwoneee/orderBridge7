package com.itwillbs.dto;

import java.util.Date;
import lombok.Data;

/**
 * 생산 실적(Production Result) 조회/등록용 DTO
 * - DB: production_result 컬럼 + 조인 결과 + 계산 필드 포함
 * - 주의: MyBatis mapUnderscoreToCamelCase=true 이므로
 *         result_id -> resultId 처럼 자동 매핑됨
 */
@Data
public class ProductionResultDTO {
    
    // ====== DB 기본 컬럼 (production_result) ======
    private String resultId;     // 실적번호(PR S-YYYYMMDD-XXX) PK, 자동 생성/조회
    private String orderId;      // 작업지시 ID (FK: work_order.order_id)
    private String productId;    // 제품 ID (FK: product.product_id) - 테이블에 존재
    private String lotNo;        // LOT 번호 (예: LOT-SD-YYYYMMDD-XXX)
    private Integer actualQty;   // 정상품 수량(불량 제외)
    private Integer defectQty;   // 불량 수량
    private String workerName;   // 작업자 이름
    private Date startedAt;      // 작업 시작 일시
    private Date endedAt;        // 작업 종료 일시
    private Date createdAt;      // 등록 일시(DB default now)
    
    // ====== JOIN 결과 컬럼 ======
    private String productName;  // 제품명 (product.product_name)
    private String lineName;     // 라인명 (production_line.line_name via work_order.line_id)
    private Integer orderQty;    // 계획수량 (work_order.order_qty)
    private String orderManager; // 작업지시자 (work_order.order_manager)
    private String status;       // 지시 상태 (work_order.status: WAITING/IN_PROGRESS/DONE 등)
    
    // ====== 파생(계산) 컬럼 ======
    private Double defectRate;      // 불량률 = (defect / (actual+defect)) * 100
    private Double achievementRate; // 달성률 = (actual / order_qty) * 100
    
    // ====== 추가 필요시 ======
    private String lineId;       // 라인 ID (work_order.line_id) - 필요시
    private String priority;     // 우선순위 (work_order.priority) - 필요시
    private Date dueDate;        // 납기일 (work_order.due_date) - 필요시
}