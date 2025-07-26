package com.itwillbs.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itwillbs.domain.ClientOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.ProductionPlanDTO;
import com.itwillbs.mapper.ProductionPlanMapper;
import com.itwillbs.persistence.ProductionPlanDAO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductionPlanServiceImpl implements ProductionPlanService {

	private final ProductionPlanMapper productionPlanMapper;
	private final ProductionPlanDAO productionPlanDAO;

	// 계획 등록 시 사용할 수주 + 제품 + 재고 정보 조회 (단순 조회)
	@Override
	public List<ProductionPlanDTO> getOrderDetailsForPlan(String clOrderId) {
		return productionPlanMapper.getOrderDetailsForPlan(clOrderId);
	}

	// 수주 상세 펼치기 시 부족 수량 포함 조회
	@Override
	public List<ProductionPlanDTO> getOrderDetailItemsForPlan(String clOrderId) {
		List<ProductionPlanDTO> list = productionPlanMapper.getOrderDetailItemsForPlan(clOrderId);

		for (ProductionPlanDTO dto : list) {
			int availableQty = dto.getStockQty() - dto.getReservedQty();
			if (availableQty < 0)
				availableQty = 0;
			dto.setAvailableQty(availableQty); // 가용 재고 계산 후 저장

			int requiredQty = dto.getOrderQty() - availableQty;
			if (requiredQty < 0)
				requiredQty = 0;
			dto.setRequiredQty(requiredQty); // 생산 필요 수량 계산 후 저장
		}

		return list;
	}

	// 생산 계획 ID 생성 (예: PL-20250723-001 형식)
	@Override
	public String getNewPlanId() {
		String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String prefix = "PL-" + today + "-";

		String maxId = productionPlanDAO.getMaxPlanIdLike(prefix);

		int nextSeq = 1;
		if (maxId != null) {
			String lastSeq = maxId.substring(prefix.length());
			nextSeq = Integer.parseInt(lastSeq) + 1;
		}

		return prefix + String.format("%03d", nextSeq);
	}

	// 생산 계획 1건 등록 처리
	@Override
	public void registerPlan(ProductionPlanDTO dto) {
		String newPlanId = getNewPlanId();
		dto.setPlanId(newPlanId);
		// 2. 상태 초기값 설정
		dto.setStatus("WAITING");

		// 3. DB insert
		productionPlanDAO.insertPlan(dto);
	}

	// 여러 생산 계획 한 번에 등록 처리
	@Override
	@Transactional
	public void registerPlans(List<ProductionPlanDTO> planList) {
		String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")); // 예: 20250726
		String prefix = "PL-" + today + "-";

		for (ProductionPlanDTO dto : planList) {
			// 1. 날짜별 max plan_id 조회
			String maxId = productionPlanDAO.getMaxPlanIdLike(prefix); // 예: PL-20250726-003

			// 2. 다음 순번 계산
			int nextSeq = 1;
			if (maxId != null) {
				String lastSeq = maxId.substring(prefix.length()); // "003"
				nextSeq = Integer.parseInt(lastSeq) + 1;
			}

			// 3. plan_id 생성
			String planId = prefix + String.format("%03d", nextSeq);
			dto.setPlanId(planId);

			// 4. 상태 초기화
			dto.setStatus("WAITING");

			// 5. 제품 ID 기준 라인 ID 자동 조회 → 세팅
			String lineId = productionPlanDAO.findLineIdByProduct(dto.getProductId());
			dto.setLineId(lineId); // 이거 없으면 line_id는 무조건 NULL로 들어감!

			// 6. DB에 저장
			productionPlanDAO.insertPlan(dto);
		}
	}

	// [중복 등록 체크] DAO 통해 조회
	@Override
	public boolean isDuplicatePlan(ProductionPlanDTO dto) {
		return productionPlanDAO.isDuplicatePlan(dto);
	}

	// CONFIRMED 수주 총 개수 조회 (페이징)
	@Override
	public int getConfirmedOrderTotalCount(SearchCriteria cri) {
		return productionPlanDAO.getConfirmedOrderTotalCount(cri);
	}

	// CONFIRMED 수주 목록 조회 (페이징)
	@Override
	public List<ClientOrderVO> getConfirmedOrderList(SearchCriteria cri) {
		return productionPlanDAO.getConfirmedOrderList(cri);
	}

	// [생산 계획 목록 조회] 검색 조건(상태, 키워드, 정렬 등)에 따라 생산 계획 목록 조회 (페이징 포함)
	@Override
	public List<ProductionPlanDTO> getPlanList(SearchCriteria cri) {
		return productionPlanDAO.getPlanList(cri); // DAO로 위임
	}

	// [생산 계획 총 개수 조회] 조건에 맞는 전체 생산 계획 개수 조회 (페이징 계산에 사용)
	@Override
	public int getPlanListCount(SearchCriteria cri) {
		return productionPlanDAO.getPlanListCount(cri); // DAO로 위임
	}

	// 제품 ID를 기준으로 전용 생산 라인 ID를 조회한다.
	// ex) 돼지국밥 → L-01, 순대국밥 → L-02, 곰탕 → L-03
	@Override
	public String getLineIdByProduct(String productId) {
		return productionPlanDAO.findLineIdByProduct(productId);
	}
}
