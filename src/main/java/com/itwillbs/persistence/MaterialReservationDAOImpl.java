package com.itwillbs.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

/**
 * [자재 예약 DAO 구현]
 * - sqlSession으로 Mapper XML 호출
 * - 네임스페이스 + id 조합으로 실행
 */
@Repository
public class MaterialReservationDAOImpl implements MaterialReservationDAO {
	
	@Inject
    private SqlSession sqlSession;
	
	private static final String NAMESPACE = "com.itwillbs.mapper.MaterialReservationMapper.";
	
	
	// 예약 upsert: 같은 (WO, 자재)이면 수량 누적
	@Override
    public int upsertReservation(String workOrderId, String materialId, int qty) throws Exception {
        Map<String,Object> p = new HashMap<>();
        p.put("workOrderId", workOrderId);
        p.put("materialId",  materialId);
        p.put("qty",         qty);
        // <insert id="upsertReservation">
        return sqlSession.insert(NAMESPACE + "upsertReservation", p);
    }

	
	// 자재별 총 예약합(모든 작업지시서 합계)
    @Override
    public int sumReservedByMaterial(String materialId) throws Exception {
        // <select id="sumReservedByMaterial">
        Integer n = sqlSession.selectOne(NAMESPACE + "sumReservedByMaterial", materialId);
        return (n == null) ? 0 : n; // NPE 방지
    }

    
    // 해당 작업지시서가 이미 예약한 수량(없으면 0)
    @Override
    public int selectWoReserved(String workOrderId, String materialId) throws Exception {
        Map<String,Object> p = new HashMap<>();
        p.put("workOrderId", workOrderId);
        p.put("materialId",  materialId);
        // <select id="selectWoReserved">
        Integer n = sqlSession.selectOne(NAMESPACE + "selectWoReserved", p);
        return (n == null) ? 0 : n;
    }
    

    // 자재 onhand(고정창고 기준 합계)
    @Override
    public int selectOnhand(String materialId) throws Exception {
        // <select id="selectOnhand">
        Integer n = sqlSession.selectOne(NAMESPACE + "selectOnhand", materialId);
        return (n == null) ? 0 : n;
    }
    
    
    // 작업지시서 필요자재 목록(materialId, requiredQty)
    @Override
    public List<Map<String, Object>> selectWoMaterials(String workOrderId) throws Exception {
        // <select id="selectWoMaterials">
        return sqlSession.selectList(NAMESPACE + "selectWoMaterials", workOrderId);
    }

    
    // 모든 자재 예약 충족 시 work_order.shortage_status = RESOLVED
    @Override
    public int resolveIfAllReserved(String workOrderId) throws Exception {
        // <update id="resolveIfAllReserved">
        return sqlSession.update(NAMESPACE + "resolveIfAllReserved", workOrderId);
    }

    
    // 출고 확정(ISSUED) 시 예약 수량 차감
    @Override
    public int releaseReservation(String workOrderId, String materialId, int qty) throws Exception {
        Map<String,Object> p = new HashMap<>();
        p.put("workOrderId", workOrderId);
        p.put("materialId",  materialId);
        p.put("qty",         qty);
        // <update id="releaseReservation">
        return sqlSession.update(NAMESPACE + "releaseReservation", p);
    }
    
    
    @Override
    public int markWorkOrderShortageStatus(String wo, String status, String uid) throws Exception {
        Map<String,Object> p = new HashMap<>();
        p.put("workOrderId", wo);
        p.put("status", status);
        p.put("userId", uid);
        return sqlSession.update(NAMESPACE + "markWorkOrderShortageStatus", p);
    }


    @Override
    public int transitionShortageStatus(String workOrderId, String from, String to, String userId) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("workOrderId", workOrderId);
        params.put("from", from);
        params.put("to", to);
        params.put("userId", userId);
        return sqlSession.update(NAMESPACE + "transitionShortageStatus", params);
    }

    
    // 이번 출고(outboundId)로 사용된 수량만큼 예약 차감
    @Override
    public void consumeReservationByOutbound(String outboundId) throws Exception {
        sqlSession.update(NAMESPACE + "consumeReservationByOutbound", outboundId);
    }
    
 	// 0이 된 예약행 정리
    @Override
    public void deleteZeroReservationsByOutbound(String outboundId) throws Exception {
        sqlSession.delete(NAMESPACE + "deleteZeroReservationsByOutbound", outboundId);
    }
    
    
    

}
