package com.itwillbs.service;

import com.itwillbs.domain.BOMDetailVO;
import com.itwillbs.domain.BOMMasterVO;
import com.itwillbs.domain.MaterialVO;
import com.itwillbs.domain.ProductVO;

import java.util.List;

public interface BOMMasterService {
	//목록
    List<BOMMasterVO> getAllBOM();    
    BOMMasterVO getBOMDetail(String bomId);
    
    //등록
    String createNextBOMId();
    
    // 제품, 원자재 목록 조회 (폼 렌더링용)
    List<ProductVO> getAllProducts();
    List<MaterialVO> getAllMaterials();

    // BOM 저장
    void insertBOM(BOMMasterVO bomMasterVO);
   
    //상태변경
    void updateBOMStatus(String bomId, String status);
    
    //상세조회하기
     BOMDetailVO getBOMDetailById(int bomDetailId);     
    void updateBOMDetail(BOMDetailVO detail);
    void deleteBOMDetail(int bomDetailId);
    
    
    

}
