package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.ProductionResultVO;   
import com.itwillbs.domain.SearchCriteria;     
import com.itwillbs.dto.ProductionResultDTO;     

/**
 * 생산 실적 서비스 (비즈니스 로직 인터페이스)
 * - VO는 DB 테이블과 1:1로 INSERT/UPDATE할 때 사용
 * - DTO는 목록/상세 조회처럼 JOIN/파생 필드가 필요한 경우 사용
 */
public interface ProductionResultService {
    
    // ===========================
    // 아름님 흐름: 완제품 입고 연동
    // ===========================
    
    /**
     * 생산결과 1건 등록 (완제품 입고에서 저장할 때 사용)
     * - 파라미터는 테이블과 1:1 매핑된 VO 사용
     * - 실적번호(ResultId) 생성/LOT발번은 구현체(ServiceImpl)에서 처리
     */
    void insertResult(ProductionResultVO vo);
    
    /**
     * 생산결과를 기준으로 '완제품 입고' 테이블에 일괄 반영
     * - 배치/버튼 액션 등에서 호출되는 메서드
     * - 구현체에서 트랜잭션 처리 권장
     */
    void saveAllToInbound();
    
    // ===========================
    // 태현 흐름: 목록/조회
    // ===========================
    
    /**
     * 생산 실적 목록 조회
     * - 검색/정렬/페이징 조건은 SearchCriteria로 전달
     * - JOIN/파생 컬럼까지 포함된 DTO로 반환
     */
    List<ProductionResultDTO> getList(SearchCriteria cri);
    
    /**
     * 생산 실적 총 건수 조회 (페이징 계산용)
     * - getList와 동일한 검색 조건을 사용해야 함
     */
    int getTotalCount(SearchCriteria cri);
    
    /**
     * 생산 실적 상세 조회
     * - Controller의 detail 메서드에서 사용
     * - JOIN된 상세 정보를 DTO로 반환
     */
    ProductionResultDTO getDetail(String resultId);
}