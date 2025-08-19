package com.itwillbs.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.itwillbs.dto.AiPredictionLogDTO;

public interface AiPredictionLogMapper {
	
	void insert(AiPredictionLogDTO log);

    List<AiPredictionLogDTO> search(
        @Param("q") String q,                 // workOrderId / requestType like
        @Param("from") Date from,             // 시작일
        @Param("to") Date to,                 // 종료일
        @Param("limit") int limit             // 최대 N건
    );

}
