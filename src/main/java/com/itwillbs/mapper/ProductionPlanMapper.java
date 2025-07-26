package com.itwillbs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.itwillbs.domain.ClientOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.ProductionPlanDTO;

@Mapper
public interface ProductionPlanMapper {

	// 생산 계획 등록 시 사용할 수주 기본 정보 + 제품 재고 조회
	// 수주 번호 기준으로 납기일, 제품명, 제품ID, 단위, 재고 포함
	List<ProductionPlanDTO> getOrderDetailsForPlan(String clOrderId);

	// 확정된 상태 수주 총 개수 조회 (페이징 처리를 위한 전체 개수 카운트)
	int getConfirmedOrderTotalCount(SearchCriteria cri);

	// 확정된 상태 수주 목록 조회 (페이징 기준으로 수주 목록을 조회함)
	List<ClientOrderVO> getConfirmedOrderList(SearchCriteria cri);

	// 수주 상세 펼치기 시 해당 수주의 제품 + 재고 정보 조회
	// 생산 필요 수량 계산 포함 (order_qty - stock_qty)
	List<ProductionPlanDTO> getOrderDetailItemsForPlan(String clOrderId);

	// 생산 계획 ID 생성 (예: PL-20250723-001 형식)
	String getNewPlanId();

	// [plan_id 자동생성용] 오늘 날짜 기준으로 가장 큰 plan_id 조회 (예: PL-20250723-007)
	String getMaxPlanIdLike(String prefix);

	// 생산 계획 1건 등록
	void insertPlan(ProductionPlanDTO dto);

	// [중복 등록 체크] 해당 수주번호 + 제품 ID 조합이 이미 등록된 생산 계획에 존재하는지 여부 확인
	boolean isDuplicatePlan(ProductionPlanDTO dto);

	// 생산 계획 목록 조회
	List<ProductionPlanDTO> getPlanList(SearchCriteria cri);

	// 생산 계획 총 개수 조회
	int getPlanListCount(SearchCriteria cri);

	// 제품 ID로 라인 ID 조회 (자동 지정용)
	String findLineIdByProduct(String productId);

}