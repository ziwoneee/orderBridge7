package com.itwillbs.domain;

import java.sql.Date;
import java.util.List;

import lombok.Data;

@Data
public class BOMMasterVO {
    private String bomId;
    private String productId;
    private String productName;
    private String bomName;
    private Date bomDate;
    private String bomEtc;
    private String status; 
    private List<BOMDetailVO> details; // BOM 상세 리스트
   
}


