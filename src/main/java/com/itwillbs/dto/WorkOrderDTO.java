package com.itwillbs.dto;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 작업지시 + 제품 + 라인 + 거래처 + 재고 정보를 담는 통합 DTO
 */
@Data
public class WorkOrderDTO {

	 // 작업 지시 기본 정보
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date clOrderDate; 	  // 수주일
	private String clOrderId;     // 수주번호 (select-order-popup용)
	private String clientName;
    private String orderId;
    private String productId;
    private String lineId;
    private int orderQty;
    private String priority;
    private String status;
    private String remarks;
    private String orderManager;  // 작업지시자 추가
    private Date createdAt;
    private Date updatedAt;
    private Boolean isDeleted;
    
    // 제품 정보 (조회용)
    private String productName;
    private String unit;
    
    // 생산 라인 정보 (조회용)
    private String lineName;
    
    // 병합 수주 관련
    private List<String> mergedOrders;
    
    // BOM 정보
    private List<BomItemDTO> bomList;
    
    // 집계 정보 (조회용)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
	private Date dueDate;	 // 병합된 수주 중 가장 빠른 납기일 
    private String clientNames;   // 병합된 거래처명들 (콤마 구분)
    private int totalOrderQty;    // 병합된 수주 총 수량
    private int requiredQty; // 추가 필요 (수주 수량 - 가용 재고로 계산한 필요 수량)
    private int availableQty;   // 가용 수량
    
    // 저장용 자재 소요량 리스트 (work_order_material 테이블 insert용)
    private List<WorkOrderMaterialDTO> materialList;

}
