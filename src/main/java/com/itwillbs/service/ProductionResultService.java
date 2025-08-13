package com.itwillbs.service;
import java.util.List;
import com.itwillbs.domain.ProductionResultVO;   
import com.itwillbs.domain.SearchCriteria;     
import com.itwillbs.dto.ProductionResultDTO;

/**
 * 생산 실적 서비스 (비즈니스 로직 인터페이스)
 */
public interface ProductionResultService {
    
    // ===========================
    // 아름님 흐름: 완제품 입고 연동
    // ===========================
    void insertResult(ProductionResultVO vo);
    void saveAllToInbound();
    
    // ===========================
    // 태현 흐름: 목록/조회
    // ===========================
    List<ProductionResultDTO> getList(SearchCriteria cri);
    int getTotalCount(SearchCriteria cri);
    ProductionResultDTO getDetail(String resultId);
    
    // ===========================
    // ✅ 보완생산 관련 추가
    // ===========================
    
    /**
     * 보완생산 필요 여부 체크
     */
    boolean checkNeedSupplement(String orderId);
    
    /**
     * 부족 수량 계산
     */
    int getShortageQty(String orderId);
}