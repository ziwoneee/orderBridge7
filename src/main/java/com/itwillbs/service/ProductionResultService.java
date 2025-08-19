package com.itwillbs.service;
import java.util.List;
import com.itwillbs.domain.ProductionResultVO;   
import com.itwillbs.domain.SearchCriteria;     
import com.itwillbs.dto.ProductionResultDTO;

/**
 * 생산 실적 서비스 (비즈니스 로직 인터페이스)
 */
public interface ProductionResultService {
    
    // 아름 흐름: 완제품 입고 연동
	// 등록(LOT 자동생성 + 실적저장 + 상태자동반영 + 자동입고)
    void insertResult(ProductionResultVO vo);
    
	// 일괄 자동입고
    void saveAllToInbound();

    // 태현 흐름: 목록/조회
    List<ProductionResultDTO> getList(SearchCriteria cri);
    int getTotalCount(SearchCriteria cri);
    ProductionResultDTO getDetail(String resultId);
    
}