package com.itwillbs.domain;

import lombok.Data;

@Data
public class MaterialOutboundItemVO {

    private String outboundItemId;    // 출고 상세 ID (출고 항목 고유 식별자)
    private String outboundId;        // 출고 ID (연결된 출고 ID)
    private String materialId;        // 자재 ID (출고된 자재 ID)
    private int plannedQty;
    private int issuedQty;
    private int quantity;             // 출고 수량
    private String lotNo;             // LOT 번호 (출고 자재의 LOT 번호 - 선택사항)
    private String inventoryId;
    private int requiredQty;          // 필요 수량 (출고 요청 수량)
    private int stockQty;             // 재고 수량 (출고 직전 기준 재고)
    private String stockStatus;       // 재고 상태 (적정 / 정상 / 부족 등)
    private int note;                 // 출고 사유/메모 (선택적 메모: 출고지연 사유 등)
    

}
