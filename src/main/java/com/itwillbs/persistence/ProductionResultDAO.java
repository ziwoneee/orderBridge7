package com.itwillbs.persistence;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.itwillbs.domain.ProductionResultVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.ProductionResultDTO;

public interface ProductionResultDAO {
	
	//입고목록가져오기 (아름 시작)
	 List<ProductionResultVO> searchProductionResults(SearchCriteria cri);
	 
	    int countProductionResults(SearchCriteria cri);
	    
	    void insertResult(ProductionResultVO vo);
	    
	    List<ProductionResultVO> selectAllResults();

	  /** LOT 번호에서 최신 result_id 1건 조회 */
	    String getLatestResultIdByLot(String lotNo);

	    /** result_id로 생산 실적 상세 조회 (조인/파생 전 원본 필드) */
	    ProductionResultDTO getDetailByResultId(String resultId);

	    	  
	//입고목록가져오기 (아름 끝)
	  
	  
}
