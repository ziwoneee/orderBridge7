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
	    public String nextOutboundId() { 
	    	return sqlSession.selectOne(NAMESPACE + "getNextOutboundId");
	    	}
	    
	    @Override
	    public void insertOutboundHeader(Map<String,Object> header) {
	    	sqlSession.insert(NAMESPACE + "insertMaterialOutbound", header);
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

}
