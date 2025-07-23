package com.itwillbs.domain;

import java.util.Date;
import lombok.Data;

@Data
public class SupplementOrderVO {

    private String supplementId;    // 보완번호
    private String resultId;        // 실적번호
    private String reason;          // 사유 (보완 필요 사유)
    private int supplementQty;      // 추가 수량
    private Date dueDate;           // 보완 납기일
    private Date createdAt;         // 등록일

}
