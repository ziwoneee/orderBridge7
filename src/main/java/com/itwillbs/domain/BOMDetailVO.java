package com.itwillbs.domain;

import java.sql.Date;

import lombok.Data;

@Data
public class BOMDetailVO {
    private String bomDetailId;
    private String bomId;
    private String materialId;
    private double qty;
    private String unit;
    private String remark;
    private String materialType; // "육수", "원료"
    private Date regDate; //등록일
    
    private String materialName;

    
}
