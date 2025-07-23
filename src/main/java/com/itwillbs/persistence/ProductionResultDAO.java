package com.itwillbs.persistence;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.itwillbs.domain.ProductionResultVO;
import com.itwillbs.domain.SearchCriteria;

public interface ProductionResultDAO {
	
	//입고목록가져오기 (아름 시작)
	 List<ProductionResultVO> searchProductionResults(SearchCriteria cri);
	    int countProductionResults(SearchCriteria cri);
	    void insertResult(ProductionResultVO vo);
	  List<ProductionResultVO> selectAllResults();	  
	//입고목록가져오기 (아름 끝)
	  
	  
}
