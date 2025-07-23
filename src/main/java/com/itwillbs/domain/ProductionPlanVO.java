package com.itwillbs.domain;

import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.Data;

@Data
public class ProductionPlanVO {

    private String planId;          // 생산계획번호 (생산 계획 고유 식별자)
    private String productId;       // 제품 ID (제품 마스터 참조)
    private int plannedQty;         // 제품 수량 (생산 예정 수량)
    private Date dueDate;           // 납기 예정일
    private String priority;        // 우선순위 (LOW, MID, HIGH 중 선택)
    private Date createdAt;         // 등록일
    private String status;  		// 상태: WAITING / IN_PROGRESS / COMPLETED
    private boolean isFollowup;		// // 보완 계획 여부 (true: 보완 생산)
    
    // 날짜 포맷용 getter (서버단에서 포맷 처리)
    public String getDueDateStr() {
        return formatDate(dueDate);
    }

    public String getCreatedAtStr() {
        return formatDate(createdAt);
    }

    private String formatDate(Date date) {
        if (date == null) return "-";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

}
