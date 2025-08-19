package com.itwillbs.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.AiPredictionLogDTO;

public interface AiPredictionLogMapper {
	
	void insert(AiPredictionLogDTO log);

    // 페이징용 새 메서드들
    int getLogCount(
        @Param("q") String q,
        @Param("from") Date from,
        @Param("to") Date to
    );
    
    List<AiPredictionLogDTO> searchWithPaging(
        @Param("q") String q,
        @Param("from") Date from,
        @Param("to") Date to,
        @Param("cri") SearchCriteria cri
    );

}
