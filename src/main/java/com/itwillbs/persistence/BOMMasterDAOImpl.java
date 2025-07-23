package com.itwillbs.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.BOMDetailVO;
import com.itwillbs.domain.BOMMasterVO;
import com.itwillbs.domain.MaterialVO;
import com.itwillbs.domain.ProductVO;

@Repository
public class BOMMasterDAOImpl implements BOMMasterDAO {

    @Autowired
    private SqlSession sqlSession;

    private static final String NAMESPACE = "com.itwillbs.mapper.BOMMasterMapper";

    @Override
    public List<BOMMasterVO> getAllBOM() {
        return sqlSession.selectList(NAMESPACE + ".getAllBOM");
    }

    @Override
    public BOMMasterVO getBOMDetail(String bomId) {
        return sqlSession.selectOne(NAMESPACE + ".getBOMDetail", bomId);
    }
    
   //등록
    @Override
    public void insertBOM(BOMMasterVO bomMasterVO) {
        // BOM Master 저장
        sqlSession.insert(NAMESPACE + ".insertBOMMaster", bomMasterVO);
        // BOM 상세(원자재명세) 저장
        if (bomMasterVO.getDetails() != null) {
            for (BOMDetailVO detail : bomMasterVO.getDetails()) {
                detail.setBomId(bomMasterVO.getBomId());
                sqlSession.insert(NAMESPACE + ".insertBOMDetail", detail);
            }
        }
    }
    
   //아이디생성 
    @Override
    public String selectLastBOMId() {
        return sqlSession.selectOne(NAMESPACE + ".selectLastBOMId");
    }

  
   //상태 수정
    @Override
    public void updateBOMStatus(String bomId, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("bomId", bomId);
        param.put("status", status);
        sqlSession.update(NAMESPACE + ".updateBOMStatus", param);
    }
    
    //제품리스트
    @Override
    public List<ProductVO> getAllProducts() {
        return sqlSession.selectList(NAMESPACE + ".getAllProducts");
    }

    //자재리스트
    @Override
    public List<MaterialVO> getAllMaterials() {
        return sqlSession.selectList(NAMESPACE + ".getAllMaterials");
    }
    
    
    //상세조회
  
    @Override
    public BOMDetailVO getBOMDetailById(int bomDetailId) {
        return sqlSession.selectOne(NAMESPACE + ".getBOMDetailById", bomDetailId);
    }
    @Override
    public void updateBOMDetail(BOMDetailVO detail) {
        sqlSession.update(NAMESPACE + ".updateBOMDetail", detail);
    }
    @Override
    public void deleteBOMDetail(int bomDetailId) {
        sqlSession.delete(NAMESPACE + ".deleteBOMDetail", bomDetailId);
    }
    
}
