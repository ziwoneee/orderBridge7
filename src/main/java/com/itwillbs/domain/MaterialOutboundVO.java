package com.itwillbs.domain;

import java.util.Date;
import lombok.Data;

@Data
public class MaterialOutboundVO {

    private String outboundId;        // 출고 ID (출고 고유 식별자)
    private Date outboundDate;        // 출고일자 (출고 처리된 날짜)
    private String handledBy;         // 출고 담당자 (출고 처리자 이름 또는 ID)
    private String status;            // 출고 상태 (출고 대기 / 완료 여부 등)
    private String workOrderNo;       // 작업지시 ID
    private Date workOrderDate;       // 작업지시일자 (지시가 생성된 날짜)
    private Date dueDate;             // 납기일자 (출고 대상 예정 납기일)
    private String note;              // 비고 (출고 비고 또는 특이사항)

}
