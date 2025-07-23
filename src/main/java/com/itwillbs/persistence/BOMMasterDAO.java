package com.itwillbs.persistence;

import java.util.List;
import java.util.Map;

import com.itwillbs.domain.BOMDetailVO;
import com.itwillbs.domain.BOMMasterVO;
import com.itwillbs.domain.MaterialVO;
import com.itwillbs.domain.ProductVO;

public interface BOMMasterDAO {
    List<BOMMasterVO> getAllBOM();          // 상태가 ACTIVE인 BOM 전체 조회
    BOMMasterVO getBOMDetail(String bomId); // 단일 BOM 상세
    void insertBOM(BOMMasterVO bomMasterVO);      // 신규 등록
    String selectLastBOMId();  //BOM ID 생성
   
    void updateBOMStatus(String bomId, String status); // 상태변경
   
    
    List<ProductVO> getAllProducts();//제품리스트
    List<MaterialVO> getAllMaterials(); //자재리스트
	
    //상세조회 
    BOMDetailVO getBOMDetailById(int bomDetailId);
    void updateBOMDetail(BOMDetailVO detail);
    void deleteBOMDetail(int bomDetailId);
    
    
}
