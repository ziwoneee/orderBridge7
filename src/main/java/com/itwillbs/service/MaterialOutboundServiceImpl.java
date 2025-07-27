package com.itwillbs.service;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.itwillbs.domain.MaterialOutboundVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialOutboundDetailDTO;
import com.itwillbs.dto.MaterialOutboundItemDTO;
import com.itwillbs.dto.MaterialOutboundSummaryDTO;
import com.itwillbs.persistence.MaterialOutboundDAO;

@Service
public class MaterialOutboundServiceImpl implements MaterialOutboundService {

	@Inject
	private MaterialOutboundDAO moDAO;
	
	// 출고 목록 조회 (페이징, 검색 포함)
	@Override
	public List<MaterialOutboundSummaryDTO> getOutboundList(SearchCriteria cri) throws Exception {
		return moDAO.getOutboundList(cri);
	}

	// 전체 출고 건수 조회 (페이징용)
	@Override
	public int getMaterialOutboundCount(SearchCriteria cri) throws Exception {

		return moDAO.getMaterialOutboundCount(cri);
	}
	
	
	/**
     * ✅ 출고 상세 정보 조회 (Ajax)
     */
    @Override
    public MaterialOutboundDetailDTO getOutboundDetail(String outboundId) throws Exception {
        // 출고 기본 정보 가져오기
        MaterialOutboundDetailDTO detail = moDAO.getOutboundDetail(outboundId);

        // 출고 자재 리스트 가져오기
        List<MaterialOutboundItemDTO> materialList = moDAO.getOutboundItemList(outboundId);

        // DTO에 세팅
        detail.setMaterialList(materialList);

        return detail;
    }
	

}
