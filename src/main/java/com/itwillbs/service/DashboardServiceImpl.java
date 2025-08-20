package com.itwillbs.service;

import java.util.*;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.itwillbs.dto.DashboardDTO;
import com.itwillbs.domain.MaterialInventoryVO;
import com.itwillbs.domain.ProductStockVO;
import com.itwillbs.domain.SearchCriteria;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final SqlSession sql; // Mapper.java 없이 XML 직접 호출

    // XML namespace (파일 상단 <mapper namespace="...">와 반드시 동일)
    private static final String NS_DASH   = "com.itwillbs.mapper.DashboardMapper";
    private static final String NS_RM_INV = "com.itwillbs.mapper.MaterialInventoryMapper";
    private static final String NS_FG_STK = "com.itwillbs.mapper.ProductStockMapper";

    @Override
    public DashboardDTO getDashboardData() {
        DashboardDTO dto = new DashboardDTO();

        // ===== 상단 타일 =====
        // 1) 오늘 수주(접수/확정)
        dto.setTodayOrdersRequested( nz( (Integer) sql.selectOne(NS_DASH + ".countOrdersRequestedToday") ) );
        dto.setTodayOrdersConfirmed( nz( (Integer) sql.selectOne(NS_DASH + ".countOrdersConfirmedToday") ) );

        // 2) 작업지시 현황
        dto.setWoWaiting   ( nz( (Integer) sql.selectOne(NS_DASH + ".countWoWaiting") ) );
        dto.setWoReady     ( nz( (Integer) sql.selectOne(NS_DASH + ".countWoReady") ) );
        dto.setWoInProgress( nz( (Integer) sql.selectOne(NS_DASH + ".countWoInProgress") ) );

        // 3) 오늘 완료 지시 & 오늘 생산 실적
        dto.setWoCompletedToday     ( nz( (Integer) sql.selectOne(NS_DASH + ".countWoCompletedToday") ) );
        dto.setProductionActualToday( nz( (Integer) sql.selectOne(NS_DASH + ".sumProductionActualToday") ) );

        // 4) 원자재 현황(부족/소진) — 네가 준 MaterialInventoryMapper.selectStatusCounts 그대로 사용
        Map<String, Object> rmCounts = sql.selectOne(
            NS_RM_INV + ".selectStatusCounts", buildMaterialInvCriteria(null, null, null, null)
        );
        dto.setRmShortageCount ( nzMap(rmCounts, "shortage") );
        dto.setRmExhaustedCount( nzMap(rmCounts, "exhausted") );

        // 5) 완제품 부족(가용 <= 0) 개수
        List<ProductStockVO> fgSummary = sql.selectList(NS_FG_STK + ".getProductStockSummaryList");
        int fgShortage = 0;
        if (fgSummary != null) {
            for (ProductStockVO v : fgSummary) {
                // availableQty가 int든 Integer든 상관없이 안전하게 처리
                int available = v.getAvailableQty();
                if (available <= 0) fgShortage++;
            }
        }
        dto.setFgShortageCount(fgShortage);

        // ===== 하단 카드 =====
        // 🔥 개선: 품목 정보까지 포함한 새 쿼리 사용
        dto.setTodayOrders( fetchTodayOrdersWithProducts() ); 
        dto.setLines      ( fetchLineCards() );        
        dto.setRmShortage ( fetchRmListByStatus("부족", 10) );  
        dto.setRmExhausted( fetchRmListByStatus("소진", 10) );  
        dto.setFgShortage ( fetchFgShortageTop10(fgSummary) ); 

        return dto;
    }

    // -----------------------------
    // 하단 카드 빌더
    // -----------------------------

    /** 🔥 신규: 품목 정보까지 포함한 오늘 수주 Top10 */
    private List<DashboardDTO.TodayOrderRow> fetchTodayOrdersWithProducts() {
        return sql.selectList(NS_DASH + ".selectTodayOrdersWithProducts");
    }

    /** 라인 3개 카드(대표 지시/진행률/대기 건수) */
    private List<DashboardDTO.LineCard> fetchLineCards() {
        List<Map<String, Object>> rows = sql.selectList(NS_DASH + ".selectLineCards");
        List<DashboardDTO.LineCard> out = new ArrayList<>();
        if (rows == null) return out;

        for (Map<String, Object> m : rows) {
            DashboardDTO.LineCard c = new DashboardDTO.LineCard();
            c.setLineId      ( str(m.get("lineId")) );
            c.setLineName    ( str(m.get("lineName")) );
            c.setState       ( str(m.get("state")) ); // IN_PROGRESS/READY/WAITING/IDLE
            c.setWorkOrderId ( str(m.get("workOrderId")) );
            c.setProductId   ( str(m.get("productId")) );
            c.setProductName ( str(m.get("productName")) );
            c.setOrderQty    ( toInt(m.get("orderQty")) );
            c.setDueDate     ( (Date) m.get("dueDate") );
            c.setProducedQty ( toInt(m.get("producedQty")) );
            c.setProgressRate( toDouble(m.get("progressRate")) );
            c.setReadyCount  ( toInt(m.get("readyCount")) );
            c.setWaitingCount( toInt(m.get("waitingCount")) );
            out.add(c);
        }
        return out;
    }

    /** 원자재 부족/소진 목록 */
    private List<DashboardDTO.RmRow> fetchRmListByStatus(String status, int limit) {
        SearchCriteria sc = buildMaterialInvCriteria(null, null, status, limit);
        List<MaterialInventoryVO> list = sql.selectList(NS_RM_INV + ".selectInventorySummaryList", sc);

        List<DashboardDTO.RmRow> out = new ArrayList<>();
        if (list == null) return out;

        for (MaterialInventoryVO v : list) {
            DashboardDTO.RmRow r = new DashboardDTO.RmRow();
            r.setMaterialId   ( v.getMaterialId() );
            r.setMaterialName ( v.getMaterialName() );
            r.setUnit         ( v.getUnit() );
            r.setQuantity     ( nz(v.getQuantity()) );
            r.setSafetyStock  ( nz(v.getSafetyStock()) );
            r.setNextExpireDate( v.getExpirationDate() );
            r.setStatus       ( status );
            out.add(r);
            if (out.size() >= limit) break;
        }
        return out;
    }

    /** 완제품 가용<=0 TOP10 */
    private List<DashboardDTO.FgRow> fetchFgShortageTop10(List<ProductStockVO> fgSummaryAll) {
        List<ProductStockVO> src = (fgSummaryAll != null)
                ? fgSummaryAll
                : sql.selectList(NS_FG_STK + ".getProductStockSummaryList");

        List<DashboardDTO.FgRow> out = new ArrayList<>();
        if (src == null) return out;

        for (ProductStockVO v : src) {
            int available = v.getAvailableQty();
            if (available <= 0) {
                DashboardDTO.FgRow row = new DashboardDTO.FgRow();
                row.setProductId   ( v.getProductId() );
                row.setProductName ( v.getProductName() );
                row.setUnit        ( v.getUnit() );
                row.setReservedQty ( v.getReservedQty() );
                row.setAvailableQty( available );
                out.add(row);
            }
        }
        // 보기 좋게 제품명 정렬 후 상위 10개
        out.sort(Comparator.comparing(
            DashboardDTO.FgRow::getProductName,
            Comparator.nullsLast(String::compareTo)
        ));
        if (out.size() > 10) return out.subList(0, 10);
        return out;
    }

    // -----------------------------
    // 유틸
    // -----------------------------
    private SearchCriteria buildMaterialInvCriteria(String keyword, String materialType, String status, Integer limit) {
        SearchCriteria sc = new SearchCriteria();
        sc.setKeyword(keyword);
        sc.setMaterialType(materialType);
        sc.setStatus(status);
        sc.setPage(1); // ✔ setPageStart 대신 setPage 사용
        sc.setPerPageNum( (limit == null) ? 10 : limit );
        sc.setSortColumn("material_id");
        sc.setSortOrder("asc");
        return sc;
    }

    private static int nz(Integer v) { return (v == null) ? 0 : v; }

    private static int nzMap(Map<String, ?> m, String key) {
        if (m == null) return 0;
        Object o = m.get(key);
        return (o instanceof Number) ? ((Number) o).intValue() : 0;
    }

    private static String str(Object o) {
        return (o == null) ? null : String.valueOf(o);
    }

    private static Integer toInt(Object o) {
        return (o instanceof Number) ? ((Number) o).intValue() : null;
    }

    private static Double toDouble(Object o) {
        return (o instanceof Number) ? ((Number) o).doubleValue() : null;
    }
}