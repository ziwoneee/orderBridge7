package com.itwillbs.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.SupplierVO;

@Repository
public class SupplierDAOImpl implements SupplierDAO {
	
	@Inject
	private SqlSession sqlSession;
	
	private static final String NAMESPACE = "com.itwillbs.mapper.SupplierMapper.";

	// 검색 조건 및 키워드, 정렬에 따른 협력사 목록 조회
	@Override
	public List<SupplierVO> selectSupplierList(@Param("keyword") String keyword,
											   @Param("condition") String condition,
											   @Param("sort") String sort,
											   @Param("order") String order) throws Exception {
		
		// 파라미터를 Map으로 묶어 전달 (MyBatis에서 다중 파라미터 처리 용이)
		Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("keyword", keyword);
        paramMap.put("condition", condition);
        paramMap.put("sort", sort);
        paramMap.put("order", order);
        
        return sqlSession.selectList(NAMESPACE + "getSupplierList", paramMap);
	}
	
	// ✅ 페이징 포함된 리스트 조회
    @Override
    public List<SupplierVO> getSupplierListPaged(int offset, int size, String keyword, String condition, String sort, String order) throws Exception {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("offset", offset);
        paramMap.put("size", size);
        paramMap.put("keyword", keyword);
        paramMap.put("condition", condition);
        paramMap.put("sort", sort);
        paramMap.put("order", order);
        return sqlSession.selectList(NAMESPACE + "getSupplierListPaged", paramMap);
    }

    // ✅ 전체 건수 조회
    @Override
    public int getSupplierCount(String keyword, String condition) throws Exception {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("keyword", keyword);
        paramMap.put("condition", condition);
        return sqlSession.selectOne(NAMESPACE + "getSupplierCount", paramMap);
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
	
}
