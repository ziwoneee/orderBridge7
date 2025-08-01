package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.MaterialVO;
import com.itwillbs.domain.SearchCriteria;

public interface MaterialService {
	
	// 1. 자재 목록 조회
	public List<MaterialVO> getMaterialList(SearchCriteria cri) throws Exception;		 // (페이징 + 검색)
	int getMaterialCount(SearchCriteria cri) throws Exception;		 // 자재 총 개수 조회 (검색된 자재의 수)

	// 자재 등록
	String getMaxMaterialId() throws Exception;
	public void insertMaterial(MaterialVO vo) throws Exception;
	
	// 자재 수정
	public void updateMaterial(MaterialVO vo) throws Exception;
	
	// 자재 존재 여부 확인 (수정인지 등록인지 구분용)
	public boolean checkMaterial(String materialId) throws Exception;
	
	// 목록 조회 (자재 발주관리 등록 폼에서 필요)
	List<MaterialVO> getAllMaterials() throws Exception;


}
