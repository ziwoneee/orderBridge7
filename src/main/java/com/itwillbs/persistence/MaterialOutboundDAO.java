package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.domain.MaterialOutboundVO;

// 자재 출고 DAO 인터페이스 
public interface MaterialOutboundDAO {
	
	// 출고 목록 조회
	List<MaterialOutboundVO> getOutboundList() throws Exception;

}
