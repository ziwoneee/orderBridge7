package com.itwillbs.domain;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class MaterialOutboundVO {

    private String outboundId;        // 출고 ID (출고 고유 식별자)
    private Date outboundDate;        // 출고일자 (출고 처리된 날짜)
    private String handledBy;         // 출고 담당자 (출고 처리자 이름 또는 ID)
    private String status;            // 출고 상태 (출고 대기 / 완료 여부 등)
    private String workOrderId;       // 작업지시 ID
    private Date workOrderDate;       // 작업지시일자 (지시가 생성된 날짜)
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;             // 납기일자 (출고 대상 예정 납기일)
    private String note;              // 비고 (출고 비고 또는 특이사항)
    private String sourceInboundId;	  // 주 연결 입고ID
    
    
    private String materialId;
    
    // ====== 출고 등록용: 폼에서 반복 name으로 들어오는 값들 받는 그릇 ======
    // 자재 행 (자재별 필요수량)
    private List<String> materialIdList; // name="materialId"
    private List<Integer> reqQtyList;    // name="reqQty"

    // LOT 피킹 (평평한 배열)
    private List<String> lotMaterialIdList; // name="lotMaterialId"
    private List<String> lotNoList;         // name="lotNo"
    private List<Integer> qtyList;          // name="qty"

}
