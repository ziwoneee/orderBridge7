package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.domain.MaterialOutboundItemVO;
import com.itwillbs.domain.MaterialOutboundVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.WorkOrderVO;
import com.itwillbs.dto.MaterialOutboundDetailDTO;
import com.itwillbs.dto.MaterialOutboundItemDTO;
import com.itwillbs.dto.MaterialOutboundSummaryDTO;

// 자재 출고 DAO 인터페이스 
public interface MaterialOutboundDAO {
	
	// 출고 목록 조회
	List<MaterialOutboundSummaryDTO> getOutboundList(SearchCriteria cri) throws Exception;

	// 전체 건수 조회
    int getMaterialOutboundCount(SearchCriteria cri) throws Exception;
    
    // 출고 상세 기본 정보 조회
    MaterialOutboundDetailDTO getOutboundDetail(String outboundId) throws Exception;

    // 출고 자재 항목 리스트 조회
    List<MaterialOutboundItemDTO> getOutboundItemList(String outboundId) throws Exception;
    
    // 출고 자재 목록 조회
    List<MaterialOutboundItemVO> getOutboundItems(String outboundId) throws Exception;

    // 출고 완료 처리 (출고일자 + 상태 변경)
    void updateOutboundAsCompleted(String outboundId) throws Exception;
    
    // 자재 재고 차감
    void decreaseMaterialStock(String materialId, int qty) throws Exception;
    void updateOutboundItemStock(String outboundId, String materialId, int qty) throws Exception;
    
    
    // 작업지시서 목록 DAO 인터페이스
    List<WorkOrderVO> getWaitingOrders() throws Exception;
    
    
    // 작업지시 기본 정보 조회
    MaterialOutboundDetailDTO getWorkOrderInfo(String workOrderNo);

    // 작업지시 기반 필요 자재 목록 조회
    List<MaterialOutboundItemDTO> getRequiredMaterialsByWorkOrder(String workOrderNo);

    // 출고 마스터 저장
    void insertMaterialOutbound(MaterialOutboundDetailDTO dto);

    // 출고 자재 항목 저장
    void insertMaterialOutboundItem(MaterialOutboundItemDTO item);

    // 출고 ID 중 가장 마지막 값 조회
    String getLastOutboundId(String prefix);




    
}
