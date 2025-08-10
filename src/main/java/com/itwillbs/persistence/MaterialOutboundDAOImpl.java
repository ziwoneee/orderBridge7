package com.itwillbs.persistence;

import java.util.Date;
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

/**
 * 출고 관리 DAO 구현체
 * - Mapper XML의 id와 매핑
 */
@Repository
public class MaterialOutboundDAOImpl implements MaterialOutboundDAO {
	
	@Inject
	private SqlSession sqlSession;
	
	private static final String NAMESPACE = "com.itwillbs.mapper.MaterialOutboundMapper.";

	 	@Override
	    public List<?> selectOutboundList(SearchCriteria cri) { 
	 		return sqlSession.selectList(NAMESPACE + "getOutboundList", cri);
	 		}
	 	
	    @Override
	    public int selectOutboundCount(SearchCriteria cri) { 
	    	return sqlSession.selectOne(NAMESPACE + "getMaterialOutboundCount", cri);
	    	}

	    @Override
	    public List<com.itwillbs.domain.WorkOrderVO> selectWaitingOrders() { 
	    	return sqlSession.selectList(NAMESPACE + "getWaitingOrders");
	    	}

	    @Override
	    public Map<String, Object> selectWorkOrderHeader(String id) { 
	    	return sqlSession.selectOne(NAMESPACE + "getWorkOrderWithStock", id);
	    	}
	    
	    @Override
	    public List<Map<String, Object>> selectWorkOrderItemsWithStock(String id) { 
	    	return sqlSession.selectList(NAMESPACE + "getWorkOrderItemsWithStock", id);
	    	}

	    @Override
	    public void insertOutboundHeader(Map<String,Object> header) {
	    	sqlSession.insert(NAMESPACE + "insertMaterialOutboundHeader", header);
	    	}
	   
	    @Override
	    public void insertOutboundItems(List<Map<String,Object>> items) {
	    	sqlSession.insert(NAMESPACE + "insertMaterialOutboundItems", items);
	    	}

	   
	    @Override
	    public Map<String, Object> selectOutboundHeader(String outboundId) {
	    	return sqlSession.selectOne(NAMESPACE + "getOutboundDetailFull", outboundId);
	    	}
	   
	    @Override
	    public List<Map<String, Object>> selectOutboundItems(String outboundId) {
	    	return sqlSession.selectList(NAMESPACE + "getOutboundItems", outboundId);
	    	}

	    @Override
	    public int decreaseInventoryByOutbound(String outboundId) {
	    	return sqlSession.update(NAMESPACE + "decreaseInventoryByOutbound", outboundId);
	    	}
	   
	    @Override
	    public void updateOutboundCompleted(String outboundId) {
	    	sqlSession.update(NAMESPACE + "updateOutboundAsCompleted", outboundId);
	    	}
	
	
	    @Override
	    public List<Map<String,Object>> getLotsByMaterial(String materialId) {
	        return sqlSession.selectList(NAMESPACE + "getLotsByMaterial", materialId);
	    }
	
	
	    @Override
	    public int countByStatus(String status) {
	        return sqlSession.selectOne(NAMESPACE + "countByStatus", status);
	    }

	    
	    @Override
	    public Date selectWorkOrderDueDate(String workOrderNo) {
	        return sqlSession.selectOne(NAMESPACE + "selectWorkOrderDueDate", workOrderNo);
	    }

	    @Override
	    public int updateWorkOrderShortageStatus(String workOrderId, String status) {
	        Map<String, Object> p = new HashMap<>();
	        p.put("workOrderId", workOrderId);
	        p.put("status", status);
	        return sqlSession.update(
	        		NAMESPACE + "updateWorkOrderShortageStatus", p);
	    }


	    // 해당 작업지시서가 출고 가능한 상태인지 여부 (1=가능, 0=불가능)
	    @Override
	    public int isWorkOrderReady(String workOrderId) throws Exception {
	        return sqlSession.selectOne(NAMESPACE + "isWorkOrderReady", workOrderId);
	    }

	    // 해당 작업지시서의 출고 레코드 존재 여부
	    @Override
	    public int existsOutboundByWorkOrder(String workOrderId) throws Exception {
	        return sqlSession.selectOne(NAMESPACE + "existsOutboundByWorkOrder", workOrderId);
	    }
	    
	    // 신규 출고 ID 생성
	    @Override
	    public String nextOutboundId() throws Exception {
	        return sqlSession.selectOne(NAMESPACE + "nextOutboundId");
	    }

	    // 출고 마스터 INSERT
	    @Override
	    public void insertMaterialOutbound(String outboundId, String workOrderId) throws Exception {
	        Map<String,Object> p = new HashMap<>();
	        p.put("outboundId", outboundId);
	        p.put("workOrderId", workOrderId);
	        sqlSession.insert(NAMESPACE + "insertMaterialOutbound", p);
	    }

	    // 출고 아이템 INSERT (작업지시서 자재 목록 복사)
	    @Override
	    public void insertOutboundItemsFromWOM(String outboundId, String workOrderId) throws Exception {
	        Map<String,Object> p = new HashMap<>();
	        p.put("outboundId", outboundId);
	        p.put("workOrderId", workOrderId);
	        sqlSession.insert(NAMESPACE + "insertOutboundItemsFromWOM", p);
	    }




}
