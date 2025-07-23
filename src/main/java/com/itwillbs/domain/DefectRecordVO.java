package com.itwillbs.domain;

import lombok.Data;

@Data
public class DefectRecordVO {

    private String defectId;        // 불량 번호
    private String resultId;        // 생산실적번호
    private String defectType;      // 불량 유형 (이물질/포장불량 등)
    private int quantity;           // 수량
    private String note;            // 비고 (특이사항 메모)

}
