package com.itwillbs.service;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.itwillbs.domain.MaterialOutboundItemVO;
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
	
	
	private static final Logger logger = LoggerFactory.getLogger(MaterialOutboundServiceImpl.class);
	
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
	
    
    
    @Override
    public boolean processOutbound(String outboundId) throws Exception {
        // 1. 해당 출고 건의 자재 목록 조회
        List<MaterialOutboundItemVO> itemList = moDAO.getOutboundItems(outboundId);
        
        logger.info("출고처리 로직 실행됨 - outboundId: {}", outboundId);

        // 2. 재고 확인
        for (MaterialOutboundItemVO item : itemList) {
            if (item.getStockQty() < item.getRequiredQty()) {
                return false; // 하나라도 부족하면 출고 불가
            }
        }
        
        // 3. 재고 차감 처리
        for (MaterialOutboundItemVO item : itemList) {
            String materialId = item.getMaterialId();
            int qty = item.getRequiredQty();

            // 자재 재고 차감 (material_inventory 테이블)
            moDAO.decreaseMaterialStock(materialId, qty);

            // ✅ 출고 상세 테이블의 stock_qty 차감
            moDAO.updateOutboundItemStock(outboundId, materialId, qty);
        }

        // 4. 출고일자 등록 + 상태 변경 (출고완료)
        moDAO.updateOutboundAsCompleted(outboundId);

        return true;
    }

    
    // 자재 재고 차감
    @Override
    public void updateOutboundItemStock(String outboundId, String materialId, int qty) throws Exception {
    	moDAO.updateOutboundItemStock(outboundId, materialId, qty);
    }

    
    

}
