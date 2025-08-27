package com.itwillbs.domain;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class MaterialInventoryVO {
	

    private String inventoryId;         // 재고 ID (재고 고유 식별자)
    private String materialId;          // 자재 ID (해당 자재의 자재 ID)
    private BigDecimal quantity;        // 보유 수량 (현재 보유하고 있는 수량)
    private String lotNo;               // LOT 번호 (자재의 LOT 구분값)
    private Date expirationDate;        // 유통기한 (해당 자재의 유통기한)
    private Date receivedDate;          // 입고일 (입고된 날짜)
    private String warehouseCode;       // 보관 창고 (보관 장소 코드 또는 위치)
    private String status;              // 상태 (정상, 폐기예정 등 상태값)
    private String inventoryStatus;     // 재고 상태 판단 값 (현재고와 실제 재고 비교 결과)
    private Date lastUpdated;           // 최종 수정일 (재고 상태 수정 시각)
    private Date lastMovementDate;      // 최근 이동일 (입고/출고 등 마지막 트랜잭션)
    private BigDecimal actualQuantity;  // 실사 수량 (실사 조사한 수량)
    private Boolean isDiscarded;        // 폐기 여부 (폐기 처리 여부 Y/N)
    private Date discardedDate;         // 폐기일자 (실제 폐기 처리된 일자)
    private String discardReason;       // 폐기 사유 (예: 유통기한 초과, 변질, 파손 등)
    
    // material 테이블에서 가져온 정보
    private String materialName;    // 자재명 (material 테이블에서 join)
    private String unit;            // 단위
    private int safetyStock;        // 안전재고 기준 (material 테이블에서 가져옴)
    private String materialType;
    private String stockStatus;

}