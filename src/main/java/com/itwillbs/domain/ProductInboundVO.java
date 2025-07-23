package com.itwillbs.domain;

import java.util.Date;
import lombok.Data;

@Data
public class ProductInboundVO {
    private String inboundId;     // 입고ID
    private String productId;     // 제품ID
    private int inboundQty;       // 입고수량
    private Date createdAt;     // 입고일자
    private String inboundType;   // 입고유형 (생산/외주/반품 등)
    private String manager;       // 담당자
    private String lotNo;         // LOT번호
    private String remark;        // 비고
    private Date regDate;         // 등록일
    private Date updDate;         // 수정일
    
    private String productName; // 제품명 (조인용)
	
  

}
