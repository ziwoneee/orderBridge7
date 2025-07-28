package com.itwillbs.domain;

import java.util.Date;
import lombok.Data;

/**
 * 작업 지시 정보를 담는 VO 클래스
 */
@Data
public class WorkOrderVO {

    private String orderId;         // 작업 지시 번호
    private String clOrderId;  		// 수주 번호
    private String productId;       // 제품 ID
    private String lineId;          // 생산 라인 ID
    private int orderQty;           // 지시 수량
    private String priority;        // 우선순위 (EMERGENCY, HIGH, NORMAL, LOW)
    private String isFollowup;      // 보완 생산 여부 (Y/N)
    private Date createdAt;         // 등록일
    private Date updatedAt;         // 수정일
    private String status;          // 상태 (대기 / 진행중 / 완료)

}
