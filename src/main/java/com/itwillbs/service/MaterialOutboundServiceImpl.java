package com.itwillbs.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.itwillbs.domain.MaterialOutboundItemVO;
import com.itwillbs.domain.MaterialOutboundVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.WorkOrderVO;
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
    
    
    
    // 작업지시서 목록 조회 서비스
    @Override
    public List<WorkOrderVO> getWaitingOrders() throws Exception {
    	
        // DAO 통해 status = 'WAITING'인 작업지시서 반환
        return moDAO.getWaitingOrders();
    }


    // 작업지시서 기반 출고 상세정보(자재 목록 포함) 조회
    @Override
    public MaterialOutboundDetailDTO getOutboundDetailByWorkOrder(String workOrderNo) throws Exception {
        // 1. 작업지시 기본 정보 조회
        MaterialOutboundDetailDTO detail = moDAO.getWorkOrderInfo(workOrderNo);

        // 2. 작업지시 자재 목록 조회
        List<MaterialOutboundItemDTO> materialList = moDAO.getRequiredMaterialsByWorkOrder(workOrderNo);

        // 3. 자재 목록 세팅
        detail.setMaterialList(materialList);

        return detail;
    }

    
    // 출고 등록 서비스 메서드
    @Override
    public void registerOutbound(MaterialOutboundDetailDTO dto) throws Exception {
        // [1] 출고 ID 생성
        String outboundId = generateOutboundId(); // 예: "OUT-RM-20250806-001"
        dto.setOutboundId(outboundId);

        // [2] 출고 마스터 저장
        moDAO.insertMaterialOutbound(dto);

        // [3] 출고 자재 항목 저장
        for (MaterialOutboundItemDTO item : dto.getMaterialList()) {
            // 출고 ID 설정
            item.setOutboundId(outboundId);
            moDAO.insertMaterialOutboundItem(item);
        }
    }

    // 출고 ID 생성 로직
    private String generateOutboundId() {
        String prefix = "OUT-RM-" + new SimpleDateFormat("yyyyMMdd").format(new Date());
        String lastId = moDAO.getLastOutboundId(prefix); // 예: "OUT-RM-20250806-002"
        int nextNum = 1;

        if (lastId != null) {
            String numStr = lastId.substring(prefix.length() + 1);
            nextNum = Integer.parseInt(numStr) + 1;
        }

        return String.format("%s-%03d", prefix, nextNum);
    }

    

}
