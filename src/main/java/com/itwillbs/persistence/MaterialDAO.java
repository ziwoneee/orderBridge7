package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.domain.MaterialVO;
import com.itwillbs.domain.SearchCriteria;

public interface MaterialDAO {
	
	// 1. 자재 목록 조회
	public List<MaterialVO> getMaterialList(SearchCriteria cri) throws Exception;	// 페이징 + 검색
	int getMaterialCount(SearchCriteria cri)throws Exception;            			// 자재 총 개수 조회 (검색된 자재의 수)
	
	// 자재 등록
	public void insertMaterial(MaterialVO vo) throws Exception;
	
	// 자재 수정
	public MaterialVO getMaterial(String materialId) throws Exception;
	public void updateMaterial(MaterialVO vo) throws Exception;
	
	// 자재 존재 여부 확인 (COUNT 방식, 선택적으로 사용)
	public boolean checkMaterial(String materialId) throws Exception;
	
	// 가장 큰 자재ID 조회 (자동생성용)
	public String getMaxMaterialId() throws Exception;
	
    // 목록 조회 (자재 발주관리 등록 폼에서 필요)
    List<MaterialVO> selectAllMaterials();
    
    // 자재 논리 삭제 처리
    void deleteMaterial(String materialId) throws Exception;
    
}
