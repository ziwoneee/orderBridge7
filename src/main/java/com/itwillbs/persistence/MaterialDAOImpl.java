package com.itwillbs.persistence;

import java.util.List;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.MaterialVO;
import com.itwillbs.domain.SearchCriteria;

@Repository
public class MaterialDAOImpl implements MaterialDAO {
	
	// mylog
	private static final Logger logger = LoggerFactory.getLogger(MaterialDAOImpl.class);

	private static final String NAMESPACE = "com.itwillbs.mapper.MaterialMapper.";
	
	@Inject
	private SqlSession sqlSession;
	
	// 1. 자재 목록 조회
	@Override
	public List<MaterialVO> getMaterialList(SearchCriteria cri) throws Exception {
		
		return sqlSession.selectList(NAMESPACE + "getMaterialList", cri);
	}
	
	@Override
	public int getMaterialCount(SearchCriteria cri) throws Exception {
	    // 매퍼에서 제공하는 쿼리 실행, 조건에 맞는 자재 개수 조회
	    return sqlSession.selectOne(NAMESPACE + "getMaterialCount", cri);
	}

	// 자재 등록
	@Override
	public void insertMaterial(MaterialVO vo) throws Exception {
		
		sqlSession.insert(NAMESPACE + "insertMaterial", vo);
	}

	// 자재 수정
	@Override
	public void updateMaterial(MaterialVO vo) throws Exception {

		sqlSession.update(NAMESPACE + "updateMaterial", vo);
	}
	
	// 자재ID 기준으로 해당 자재 조회 (단건)
	@Override
	public MaterialVO selectMaterialById(String materialId) throws Exception {
		
		// 해당 자재ID로 자재 1건 조회 (없으면 null 반환)
		return sqlSession.selectOne(NAMESPACE + "selectMaterialById", materialId);
	}


	// 자재 존재 여부 확인
	@Override
	public boolean checkMaterial(String materialId) throws Exception {
		
		Integer cnt = sqlSession.selectOne(NAMESPACE + "checkMaterial", materialId);
		return cnt != null && cnt > 0;
	}
	
	// 자재ID 자동 부여
	@Override
	public String getMaxMaterialId() throws Exception {
		
	    return sqlSession.selectOne(NAMESPACE + "getMaxMaterialId");
	}

    // 목록 조회 (자재 발주관리 등록 폼에서 필요)
	@Override
	public List<MaterialVO> selectAllMaterials() {
		
		return sqlSession.selectList(NAMESPACE + "selectAllMaterials");
	}
    
    
	

}
