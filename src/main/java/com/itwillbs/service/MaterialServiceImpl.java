package com.itwillbs.service;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.itwillbs.domain.MaterialVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.persistence.MaterialDAO;

@Service
public class MaterialServiceImpl implements MaterialService {
	
	@Inject
	private MaterialDAO mDAO;

	// 1. 자재 목록 조회
	@Override
	public List<MaterialVO> getMaterialList(SearchCriteria cri) throws Exception {

		return mDAO.getMaterialList(cri);
	}
	
	
	// 페이징용 전체 개수
    @Override
    public int getMaterialCount(SearchCriteria cri) throws Exception {
        return mDAO.getMaterialCount(cri);
    }

	// 자재 등록
    @Override
    public String getMaxMaterialId() throws Exception {
        return mDAO.getMaxMaterialId();
    }
	@Override
	public void insertMaterial(MaterialVO vo) throws Exception {
		
		// 자재 ID가 비어 있으면 자동 생성
	    if(vo.getMaterialId() == null || vo.getMaterialId().isEmpty()) {
	        String maxId = mDAO.getMaxMaterialId(); // 가장 큰 자재ID 조회 (예: RM-0012)
	        String newId = createNextId(maxId);		// 다음 자재ID 생성 (예: RM-0013)
	        vo.setMaterialId(newId);
	    }
		
	    // DB에 자재 등록
		mDAO.insertMaterial(vo);
	}

	
	// 자재ID 생성 메서드 (ex: RM-0001 → RM-0002)
	private String createNextId(String maxId) {
	    String prefix = "RM-";
	    int nextNum = 1; // 기본값 (최초등록)

	    if (maxId != null) {
	        // RM-0001 -> 0001만 잘라서 숫자 변환 후 +1
	        String numPart = maxId.substring(3);
	        nextNum = Integer.parseInt(numPart) + 1;
	    }

	    // RM-0002 형식으로 리턴
	    return String.format(prefix + "%04d", nextNum);
	}

	
	// 자재 수정
	@Override
	public void updateMaterial(MaterialVO vo) throws Exception {
		
		mDAO.updateMaterial(vo);
	}

	// 자재 존재 여부 확인 (수정인지 등록인지 구분용)
	@Override
	public boolean checkMaterial(String materialId) throws Exception {
		
		// 해당 자재ID로 자재가 존재하는지 확인 (null 아니면 존재함)
		return mDAO.selectMaterialById(materialId) != null;
	}
	
	
    // 목록 조회 (자재 발주관리 등록 폼에서 필요)
    @Override
    public List<MaterialVO> getAllMaterials() throws Exception {
        return mDAO.selectAllMaterials(); // DAO 메서드 필요
    }
	

}
