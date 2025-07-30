package com.itwillbs.domain;

import java.util.Date;
import lombok.Data;

@Data
public class MaterialInboundItemVO {

    private String inboundItemId;      // 입고 상세 ID (입고 항목 고유 식별자)
    private String inboundId;          // 입고 ID (연결된 입고 ID)
    private String materialId;         // 자재 ID (입고된 자재 ID)
    private int quantity;              // 수량 (입고 수량)
    private String lotNo;              // LOT 번호 (입고된 자재의 LOT 번호)
    private Date lotCreatedDate;       // LOT 생성일자 (생성 시점)
    private Date expirationDate;       // 유통기한 (해당 자재의 유통기한)
    private String storageLocation;    // 입고 위치 (보관될 위치)
    private String unit;               // 입고 단위 (예: kg, 개 등)
    private String warehouseCode;      // 보관 창고 코드 (예: WH001)
    private Date supplyDueDate;        // 납기일 (실제 납품 예정일)
    private String inboundStatus;      // 진행상태 (미입고, 입고완료 등)

    
    // 화면 출력용 필드
    private String orderItemId;
    private int receivedQuantity;
    private int orderQuantity;
    
}