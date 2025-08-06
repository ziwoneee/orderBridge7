package com.itwillbs.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.MaterialOutboundItemVO;
import com.itwillbs.domain.MaterialOutboundVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.WorkOrderVO;
import com.itwillbs.dto.MaterialOutboundDetailDTO;
import com.itwillbs.dto.MaterialOutboundItemDTO;
import com.itwillbs.dto.MaterialOutboundSummaryDTO;

// 자재 출고 DAO 구현채
@Repository
public class MaterialOutboundDAOImpl implements MaterialOutboundDAO {
	
	@Inject
	private SqlSession sqlSession;
	
	private static final String NAMESPACE = "com.itwillbs.mapper.MaterialOutboundMapper.";

	
	// 출고 목록 조회 (페이징, 검색 포함)
	@Override
	public List<MaterialOutboundSummaryDTO> getOutboundList(SearchCriteria cri) throws Exception {

		return sqlSession.selectList(NAMESPACE + "getOutboundList", cri);
	}

	// 전체 출고 건수 조회 (페이징 계산용)
	@Override
	public int getMaterialOutboundCount(SearchCriteria cri) throws Exception {

		return  sqlSession.selectOne(NAMESPACE + "getMaterialOutboundCount", cri);
	}
	
	
	// 출고 기본 정보 조회 (상세 Ajax)
    @Override
    public MaterialOutboundDetailDTO getOutboundDetail(String outboundId) throws Exception {
        return sqlSession.selectOne(NAMESPACE + "getOutboundDetail", outboundId);
    }

    // 출고 자재 항목 리스트 조회 (상세 Ajax)
    @Override
    public List<MaterialOutboundItemDTO> getOutboundItemList(String outboundId) throws Exception {
        return sqlSession.selectList(NAMESPACE + "getOutboundItemList", outboundId);
    }

    // 출고 자재 목록 조회
    @Override
    public List<MaterialOutboundItemVO> getOutboundItems(String outboundId) throws Exception {
        return sqlSession.selectList(NAMESPACE + "getOutboundItemListRaw", outboundId);
    }

    // 출고 완료 처리 (출고일자 + 상태 변경)
    @Override
    public void updateOutboundAsCompleted(String outboundId) throws Exception {
        sqlSession.update(NAMESPACE + "updateOutboundAsCompleted", outboundId);
    }

    // 자재 재고 차감
    @Override
    public void decreaseMaterialStock(String materialId, int qty) throws Exception {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("materialId", materialId);
        paramMap.put("qty", qty);

        sqlSession.update(NAMESPACE + "decreaseMaterialStock", paramMap);
    }
    @Override
    public void updateOutboundItemStock(String outboundId, String materialId, int qty) throws Exception {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("outboundId", outboundId);
        paramMap.put("materialId", materialId);
        paramMap.put("qty", qty);

        sqlSession.update(NAMESPACE + "updateOutboundItemStock", paramMap);
    }

	
	
    // 작업지시서 목록 DAO 인터페이스
    @Override
    public List<WorkOrderVO> getWaitingOrders() throws Exception {
    	
        return sqlSession.selectList(NAMESPACE + "getWaitingOrders");
    }

    // 작업지시 기본 정보 조회
    @Override
    public MaterialOutboundDetailDTO getWorkOrderInfo(String workOrderNo) {
        return sqlSession.selectOne(NAMESPACE + "getWorkOrderInfo", workOrderNo);
    }

    // 작업지시 기반 필요 자재 목록 조회
    @Override
    public List<MaterialOutboundItemDTO> getRequiredMaterialsByWorkOrder(String workOrderNo) {
        return sqlSession.selectList(NAMESPACE + "getRequiredMaterialsByWorkOrder", workOrderNo);
    }

    // 출고 마스터 저장
    @Override
    public void insertMaterialOutbound(MaterialOutboundDetailDTO dto) {
        sqlSession.insert(NAMESPACE + "insertMaterialOutbound", dto);
    }

    // 출고 자재 항목 저장
    @Override
    public void insertMaterialOutboundItem(MaterialOutboundItemDTO item) {
        sqlSession.insert(NAMESPACE + "insertMaterialOutboundItem", item);
    }

    // 출고 ID 중 가장 마지막 값 조회
    @Override
    public String getLastOutboundId(String prefix) {
        return sqlSession.selectOne(NAMESPACE + "getLastOutboundId", prefix);
    }

	
	
	
	
	
	
	
	
	
	
	
	

}
