package com.itwillbs.domain;

import java.util.Date;
import lombok.Data;

@Data
public class ProductOutboundVO {
    private String outboundId;     // 출고ID
    private String productId;      // 제품ID
    private int outboundQty;       // 출고수량
    private Date outboundDate;     // 출고일자
    private String outboundType;   // 출고유형 (납품/폐기/샘플 등)
    private String clientId;       // 거래처ID
    private String lotNo;          // LOT번호
    private String remark;         // 비고
    private Date regDate;          // 등록일
    private Date updDate;          // 수정일
    
    
    private String productName;   // 제품명 (조인용)
    private String manager;       // 담당자명 (등록자, 조인용)
    private String clientName;    // 거래처명 (조인용)
    
    private String trackingNumber; // 운송장번호


}
