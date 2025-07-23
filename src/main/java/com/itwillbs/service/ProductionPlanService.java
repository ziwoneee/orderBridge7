package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.ClientOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.ProductionPlanDTO;

public interface ProductionPlanService {
    
    // 생산 계획 등록 시 수주번호(cl_order_id) 기준 제품 + 재고 정보 조회
    List<ProductionPlanDTO> getOrderDetailsForPlan(String clOrderId);

    // 확정된 상태 수주 목록 조회 (페이징 포함)
    List<ClientOrderVO> getConfirmedOrderList(SearchCriteria cri);

    // 확정된 상태 수주 총 개수 조회 (페이징 처리를 위한 전체 카운트)
    int getConfirmedOrderTotalCount(SearchCriteria cri);
    
    // 수주 상세 펼치기 클릭 시 제품 정보 + 재고 + 생산 필요 수량 조회
    List<ProductionPlanDTO> getOrderDetailItemsForPlan(String clOrderId);
    
    // 생산 계획 ID 생성 (예: PL-20250723-001 형식)
    String getNewPlanId();
    
    // 여러 생산 계획 한 번에 등록 처리
    void registerPlans(List<ProductionPlanDTO> planList);

    // 생산 계획 1건 등록 처리 
    void registerPlan(ProductionPlanDTO dto);
    
    // [중복 등록 체크] 이미 등록된 수주번호 + 제품 ID 조합인지 여부 확인
    boolean isDuplicatePlan(ProductionPlanDTO dto);
    
    
    // 생산 계획 목록 조회
    List<ProductionPlanDTO> getPlanList(SearchCriteria cri); 
    
    // 목록 총 개수 조회
    int getPlanListCount(SearchCriteria cri);    

}