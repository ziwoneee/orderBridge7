package com.itwillbs.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.MaterialVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.SupplierVO;
import com.itwillbs.mapper.SupplierPackRuleMapper;

@Repository
public class SupplierDAOImpl implements SupplierDAO {
	
	@Inject
	private SqlSession sqlSession;
	
	@Inject
    private SupplierPackRuleMapper packRuleMapper; 
	
	private static final String NAMESPACE = "com.itwillbs.mapper.SupplierMapper.";

	
	// ✅ 페이징 포함된 리스트 조회
    @Override
    public List<SupplierVO> getSupplierList(SearchCriteria cri) throws Exception {

    	return sqlSession.selectList(NAMESPACE + "getSupplierList", cri);
    }

    // ✅ 전체 건수 조회
    @Override
    public int getSupplierCount(SearchCriteria cri) throws Exception {
    	
        return sqlSession.selectOne(NAMESPACE + "getSupplierCount", cri);
    }
	
    /** 자재별 기본 발주 포장단위(pack_qty). 없으면 1 */
    @Override
    public Double getPackQtyByMaterial(String materialId) throws Exception {
        Double v = null;
        try {
            // ✅ 애노테이션 기반 Mapper 직접 호출
            v = packRuleMapper.getPackQtyByMaterial(materialId);
        } catch (Exception ignore) {
            // (선택) 스캔 설정 문제 시, 안전망: MyBatis 프록시로도 시도
            try {
                v = sqlSession.getMapper(SupplierPackRuleMapper.class)
                              .getPackQtyByMaterial(materialId);
            } catch (Exception e2) { /* 무시하고 아래 보정 */ }
        }
        return (v == null || v <= 0) ? 1d : v;
    }
	
    
	// 협력사 ID로 협력사 상세 조회
	@Override
	public SupplierVO getSupplierById(String supplierId) throws Exception {
		
		// MyBatis Mapper 호출
		return sqlSession.selectOne(NAMESPACE + "getSupplierById", supplierId);
	}
	
	
	// 오늘 날짜 기준 가장 큰 supplier_id 조회
	@Override
	public String getMaxSupplierIdToday() throws Exception {
	    return sqlSession.selectOne(NAMESPACE + "getMaxSupplierIdToday");
	}

	
	// 협력사 신규 등록
	@Override
	public void insertSupplier(SupplierVO vo) throws Exception {
	    sqlSession.insert(NAMESPACE + "insertSupplier", vo);
	}

	
	// 사업자번호 중복확인용
	@Override
	public int countByBusinessNumber(String businessNumber) throws Exception {
	    return sqlSession.selectOne(NAMESPACE + "countByBusinessNumber", businessNumber);
	}
	
	
	// 협력사 정보 수정 기능
	@Override
    public void updateSupplier(SupplierVO vo) throws Exception {
        sqlSession.update(NAMESPACE + "updateSupplier", vo);
    }
	
	
	// 목록 조회 (자재 발주관리 등록 폼에서 필요)
	@Override
	public List<SupplierVO> selectAllSuppliers() throws Exception {
	    return sqlSession.selectList(NAMESPACE + "selectAllSuppliers");
	}
	
	// 거래처 ID로 공급 자재 목록 조회 (자재 발주관리)
	@Override
	public List<MaterialVO> getMaterialsBySupplier(String supplierId, String keyword) throws Exception {
	    
		Map<String, Object> params = new HashMap<>();
	    params.put("supplierId", supplierId);
	    params.put("keyword", keyword);
		
		return sqlSession.selectList(NAMESPACE + "getMaterialsBySupplier", params);
	}

	
	
	// 협력사 비활성화 처리 (소프트 삭제)
	@Override
	public void updateSupplierStatus(String supplierId, String status) throws Exception {
	    Map<String, String> paramMap = new HashMap<>();
	    paramMap.put("supplierId", supplierId);
	    paramMap.put("status", status);
	    sqlSession.update(NAMESPACE + "updateSupplierStatus", paramMap);
	}
	

	//협력사 이메일
	@Override
    public String findEmailById(String supplierId) {
        return sqlSession.selectOne(NAMESPACE + "findEmailById", supplierId);
    }
	
	
}
