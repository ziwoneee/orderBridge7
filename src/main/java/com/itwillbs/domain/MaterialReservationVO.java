package com.itwillbs.domain;

import java.util.Date;

import lombok.Data;

/**
 * [자재 예약 VO]
 * - material_reservation 테이블과 매핑
 * - 예약은 "작업지시서 + 자재" 단위로 묶임
 */
@Data
public class MaterialReservationVO {
	
	private String workOrderId;			// 작업지시서 번호
	private String materialId;			// 자재 ID
	private int reservedQty;			// 예약 수량 (해당 작업지시서가 확보해둔 수량)
	private Date updatedAt;				// 수정 시간

}
