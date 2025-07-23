package com.itwillbs.service;

import com.itwillbs.domain.BOMDetailVO;
import com.itwillbs.domain.BOMMasterVO;
import com.itwillbs.domain.MaterialVO;
import com.itwillbs.domain.ProductVO;
import com.itwillbs.persistence.BOMMasterDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BOMMasterServiceImpl implements BOMMasterService {

    @Autowired
    private BOMMasterDAO bomMasterDAO;

    // 목록
    @Override
    public List<BOMMasterVO> getAllBOM() {
        return bomMasterDAO.getAllBOM();
    }

    // 단건 상세
    @Override
    public BOMMasterVO getBOMDetail(String bomId) {
        return bomMasterDAO.getBOMDetail(bomId);
    }

    // 등록
    @Override
    public String createNextBOMId() {
        String lastId = bomMasterDAO.selectLastBOMId(); // "BM-003"
        int nextNo = 1;
        if (lastId != null && lastId.length() >= 6) {
            try {
                nextNo = Integer.parseInt(lastId.substring(3)) + 1;
            } catch (Exception e) {
                nextNo = 1;
            }
        }
        return String.format("BM-%03d", nextNo); // 결과: BM-001, BM-002, ...
    }

   
    public void insertBOM(BOMMasterVO bomMasterVO) {
        bomMasterDAO.insertBOM(bomMasterVO);
    }
    
    @Override
    public List<ProductVO> getAllProducts() {
        return bomMasterDAO.getAllProducts();
    }

    @Override
    public List<MaterialVO> getAllMaterials() {
        return bomMasterDAO.getAllMaterials();
    }

   
    // 상태변경
    @Override
    public void updateBOMStatus(String bomId, String status) {
        bomMasterDAO.updateBOMStatus(bomId, status);
    }
    
    //상세조회
    
  
    @Override
    public BOMDetailVO getBOMDetailById(int bomDetailId) {
        return bomMasterDAO.getBOMDetailById(bomDetailId);
    }
    @Override
    public void updateBOMDetail(BOMDetailVO detail) {
        bomMasterDAO.updateBOMDetail(detail);
    }
    @Override
    public void deleteBOMDetail(int bomDetailId) {
        bomMasterDAO.deleteBOMDetail(bomDetailId);
    }
}
