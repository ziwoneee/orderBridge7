package com.itwillbs.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class DashboardDTO {

    // ===== 상단 타일 =====
    private int todayOrdersRequested;   // 오늘 접수
    private int todayOrdersConfirmed;   // 오늘 확정
    private int woWaiting;
    private int woReady;
    private int woInProgress;
    private int woCompletedToday;       // 오늘 완료
    private int productionActualToday;  // 오늘 실적
    private int rmShortageCount;        // 원자재 부족
    private int rmExhaustedCount;       // 원자재 소진
    private int fgShortageCount;        // 완제품 부족(가용<=0)

    // ===== 목록 섹션 =====
    private List<TodayOrderRow> todayOrders;   // 오늘 접수 Top10
    private List<LineCard> lines;              // 라인 현황(3개)
    private List<RmRow> rmShortage;            // 원자재 부족
    private List<RmRow> rmExhausted;           // 원자재 소진
    private List<FgRow> fgShortage;            // 완제품 부족

    // ---- 내부 행 타입들 (한 파일에 몰아넣기) ----
    @Data
    public static class TodayOrderRow {
        private String clOrderId;
        private String clientName;
        private String productNames;   // GROUP_CONCAT
        private Integer totalQty;
        private String status;         // REQUESTED/CONFIRMED
        private Date createdAt;
    }

    @Data
    public static class LineCard {
        // 라인 + 대표 지시
        private String lineId;
        private String lineName;

        private String state;          // IN_PROGRESS / READY / WAITING / IDLE
        private String workOrderId;    // 대표 지시
        private String productId;
        private String productName;
        private Integer orderQty;
        private Date dueDate;

        private Integer producedQty;   // 누적 양품
        private Double progressRate;   // 0~100

        private Integer readyCount;    // READY 대기 수
        private Integer waitingCount;  // WAITING 대기 수
    }

    @Data
    public static class RmRow {
        private String materialId;
        private String materialName;
        private String unit;
        private BigDecimal quantity;      // 합계 재고
        private Integer safetyStock;
        private Date nextExpireDate;   // 남은 LOT 중 최솟값
        private String status;         // '부족' / '소진'
    }

    @Data
    public static class FgRow {
        private String productId;
        private String productName;
        private String unit;
        private Integer reservedQty;
        private Integer availableQty;  // 입고-출고-예약
    }
}
