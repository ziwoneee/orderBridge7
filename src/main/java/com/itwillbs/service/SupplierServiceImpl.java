package com.itwillbs.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.SupplierVO;
import com.itwillbs.persistence.SupplierDAO;

@Service
public class SupplierServiceImpl implements SupplierService{

	@Inject
	private SupplierDAO sDAO;
	
	// 페이징 포함된 리스트 조회
    @Override
    public List<SupplierVO> getSupplierList(SearchCriteria cri) throws Exception {
        return sDAO.getSupplierList(cri);
    }

    // 전체 건수 조회
    @Override
    public int getSupplierCount(SearchCriteria cri) throws Exception {
        return sDAO.getSupplierCount(cri);
    }

	
	// 특정 협력사 ID에 해당하는 상세 정보 반환
	@Override
	public SupplierVO getSupplierById(String supplierId) throws Exception {
		
		return sDAO.getSupplierById(supplierId);
	}
	
	
	// supplier_id
	@Override
	public void insertSupplier(SupplierVO vo) throws Exception {
		
		// 1. 서버 단 중복 확인 (꼭 해야 함)
	    if (isBusinessNumberExists(vo.getBusinessNumber())) {
	        throw new IllegalArgumentException("중복된 사업자등록번호입니다.");
	    }

	    // 2. 거래처 ID가 비어있다면 자동 생성
	    if (vo.getSupplierId() == null || vo.getSupplierId().isEmpty()) {
	        
	        // 1) 오늘 날짜 기준 가장 마지막으로 등록된 ID 조회 (예: SUP-20250723-005)
	        String maxId = sDAO.getMaxSupplierIdToday();
	        
	        // 2) 가장 마지막 ID를 기반으로 다음 ID 생성 (예: SUP-20250723-006)
	        String newId = createNextSupplierId(maxId);
	        
	        // 3) 새로 생성한 ID를 VO에 설정
	        vo.setSupplierId(newId);
	    }
	    
	    // 3. created_at, updated_at 세팅
	    Timestamp now = new Timestamp(System.currentTimeMillis());
	    vo.setCreatedAt(now);
	    vo.setUpdatedAt(now);

	    // 4. DB에 최종 등록
	    sDAO.insertSupplier(vo);
	}
	
	
	// 오늘 날짜 기준 다음 ID 생성 메서드
	private String createNextSupplierId(String maxId) {
	    // 날짜 포맷: yyyyMMdd (예: 20250723)
	    String today = new SimpleDateFormat("yyyyMMdd").format(new Date());

	    int nextSeq = 1; // 기본 시작 번호

	    if (maxId != null && maxId.startsWith("SUP-" + today)) {
	        // 예: SUP-20250723-005 → 숫자 부분만 잘라내기
	        String seqStr = maxId.substring(14); // "005"
	        nextSeq = Integer.parseInt(seqStr) + 1;
	    }

	    // 새 ID 생성: SUP-20250723-00X
	    return String.format("SUP-%s-%03d", today, nextSeq);
	}

	
	// 협력사 신규 등록
	@Override
	public void registerSupplier(SupplierVO vo) throws Exception {
	    // 생성일자, 수정일자 세팅 (필요하면)
	    vo.setCreatedAt(new Timestamp(System.currentTimeMillis()));
	    vo.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

	    insertSupplier(vo);
	}
	
	
	// 사업자번호 중복확인용
	@Override
	public boolean isBusinessNumberExists(String businessNumber) throws Exception {
	    return sDAO.countByBusinessNumber(businessNumber) > 0;
	}


	// 협력사 정보 수정 기능
	@Override
	public void updateSupplier(SupplierVO vo) throws Exception {
		
		sDAO.updateSupplier(vo);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
















