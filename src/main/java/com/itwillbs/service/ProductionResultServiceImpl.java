package com.itwillbs.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itwillbs.domain.ProductInboundVO;
import com.itwillbs.domain.ProductionResultVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.ProductionResultDTO;
import com.itwillbs.mapper.ProductionResultMapper;
import com.itwillbs.mapper.WorkOrderMapper;
import com.itwillbs.persistence.ProductInboundDAO;
import com.itwillbs.persistence.ProductionResultDAO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductionResultServiceImpl implements ProductionResultService {

    @Autowired
    private ProductionResultDAO productionResultDAO;

    @Autowired
    private ProductionResultMapper productionResultMapper;

    // 작업지시 상태/정보 조회
    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Autowired
    private ProductInboundService productInboundService;

    @Autowired
    private ProductInboundDAO productInboundDAO;

    /** 당일 입고ID 시퀀스(메모리) */
    private final Map<String, Integer> inboundSerialMap = new HashMap<>();

    // ==================== 생산 결과 등록 (ID/LOT 자동 발번) ====================
    @Transactional
    @Override
    public void insertResult(ProductionResultVO vo) {
        // 0) 작업지시 존재/상태 확인 (IN_PROGRESS만 등록 허용)
        Map<String, Object> wo = workOrderMapper.selectWorkOrderById(vo.getOrderId());
        if (wo == null || wo.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 작업지시입니다: " + vo.getOrderId());
        }
        String status = asString(wo.get("status"));
        if (!"IN_PROGRESS".equals(status)) {
            throw new IllegalStateException("생산중(IN_PROGRESS) 작업지시에만 실적을 등록할 수 있습니다.");
        }

        // productId가 폼에서 안 넘어올 가능성 대비(있으면 그대로 사용)
        if (vo.getProductId() == null || vo.getProductId().isEmpty()) {
            vo.setProductId(asString(wo.get("product_id"))); // WorkOrder에서 보강
        }

        // 1) 오늘 날짜(YYYYMMDD)
        String todayStr = new SimpleDateFormat("yyyyMMdd").format(new Date());

        // 2) result_id 발번 (PRS-YYYYMMDD-001)
        String nextSeq = productionResultMapper.selectTodayResultSeq();
        if (nextSeq == null || nextSeq.isEmpty()) nextSeq = "001";
        String resultId = "PRS-" + todayStr + "-" + nextSeq;
        vo.setResultId(resultId);

        // 3) LOT 자동 생성 (productId 접두어 + 일자 + 시퀀스)
        String lotNo = generateLotNumber(vo.getProductId(), todayStr);
        vo.setLotNo(lotNo);

        // 4) 서버측 수량 검증
        int actual = nvlInt(vo.getActualQty());
        int defect = nvlInt(vo.getDefectQty());
        if (actual <= 0) throw new IllegalArgumentException("생산수량은 0보다 커야 합니다.");
        if (defect < 0 || defect > actual) throw new IllegalArgumentException("불량수량은 0 이상이고 생산수량 이하여야 합니다.");

        //  추가: endedAt이 null인 경우 현재시간으로 설정
        if (vo.getEndedAt() == null) {
            vo.setEndedAt(new Date());
        }
        
        // 4-1)  등록 전 현재 누적량 조회
        int currentProduced = productionResultMapper.selectTotalProducedQty(vo.getOrderId());
        
        // 4-2)  이번 실적으로 인한 새로운 누적량 계산
        int thisNetQty = nvlInt(vo.getActualQty()) - nvlInt(vo.getDefectQty());
        int newCumulativeQty = currentProduced + thisNetQty;
        
        // 4-3)  진행률 계산
        int orderQty = Integer.parseInt(asString(wo.get("order_qty")));
        double progressRate = orderQty > 0 ? (double) newCumulativeQty / orderQty * 100.0 : 0.0;
        
        // 4-4)  VO에 계산된 값 설정
        vo.setProgressRate(progressRate);
        vo.setCumulativeQty(newCumulativeQty);
        
        
        // 5) 생산결과 INSERT
        productionResultDAO.insertResult(vo);
        log.info("생산결과 등록 완료: resultId={}, lotNo={}", resultId, lotNo);

        // 6) 작업지시 상태 자동 반영(양품 누적 기준: 목표 달성 시 COMPLETED)
        workOrderMapper.applyResultToWorkOrder(vo.getOrderId());

        // 7) 자동입고(정상품만, 동일 LOT 중복 방지)
        int netQty = actual - defect;
        if (netQty <= 0) {
            log.info("정상품 0 → 자동입고 생략. LOT: {}", lotNo);
            return;
        }
        if (productInboundDAO.existsByLotNo(lotNo)) {
            log.warn("이미 입고된 LOT → 자동입고 생략: {}", lotNo);
            return;
        }
        String inboundId = nextInboundId(todayStr);

        ProductInboundVO inbound = new ProductInboundVO();
        inbound.setInboundId(inboundId);
        inbound.setLotNo(lotNo);
        inbound.setProductId(vo.getProductId()); // 이미 확보됨
        inbound.setInboundQty(netQty);
        inbound.setInboundType("생산");
        inbound.setRemark("생산결과 자동입고");
        inbound.setManager(vo.getWorkerName());
        inbound.setRegDate(vo.getCreatedAt());
        inbound.setCreatedAt(vo.getCreatedAt());

        productInboundService.registerInbound(inbound);
        log.info("자동입고 완료: LOT {} -> inboundId {}", lotNo, inboundId);
    }

    /** LOT 번호 생성 (기존 Mapper 시그니처 사용)
     *  형식: LOT-<prefix>-YYYYMMDD-XXX  (prefix: DG/SD/HW/ETC)
     */
    private String generateLotNumber(String productId, String yyyymmdd) {
        String productCode = toPrefix(productId);
        // 기존 XML: selectTodayLotSeq(productCode, dateStr)
        String lotSeq = productionResultMapper.selectTodayLotSeq(productCode, yyyymmdd);
        if (lotSeq == null || lotSeq.isEmpty()) lotSeq = "001";
        return String.format("LOT-%s-%s-%s", productCode, yyyymmdd, lotSeq);
    }

    /** 제품ID → 접두어 */
    private String toPrefix(String productId) {
        if (productId == null) return "ETC";
        switch (productId) {
            case "FG-001": return "DG";  // 돼지국밥
            case "FG-002": return "SD";  // 순대국밥
            case "FG-003": return "HW";  // 한우곰탕
            default:       return "ETC"; // 기타
        }
    }

    /** 당일 inbound_id 채번: IN-FG-YYYYMMDD-001 (메모리 시리얼) */
    private String nextInboundId(String yyyymmdd) {
        int serial = inboundSerialMap.getOrDefault(yyyymmdd, 0) + 1;
        inboundSerialMap.put(yyyymmdd, serial);
        return String.format("IN-FG-%s-%03d", yyyymmdd, serial);
    }

    private int nvlInt(Integer v) { return v == null ? 0 : v; }
    private String asString(Object v) { return v == null ? null : String.valueOf(v); }

    // ==================== 자동입고 일괄 처리(옵션) ====================
    @Override
    public void saveAllToInbound() {
        List<ProductionResultVO> resultList = productionResultDAO.selectAllResults();
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
        int serial = 0;

        for (ProductionResultVO result : resultList) {
            try {
                if (productInboundDAO.existsByLotNo(result.getLotNo())) continue;

                int actual = nvlInt(result.getActualQty());
                int defect = nvlInt(result.getDefectQty());
                int netQty = actual - defect;
                if (netQty <= 0) continue;

                serial++;
                String inboundId = String.format("IN-FG-%s-%03d", today, serial);

                ProductInboundVO inbound = new ProductInboundVO();
                inbound.setInboundId(inboundId);
                inbound.setLotNo(result.getLotNo());
                inbound.setProductId(result.getProductId());
                inbound.setInboundQty(netQty);
                inbound.setInboundType("자동입고");
                inbound.setRemark("생산결과 자동입고");
                inbound.setManager(result.getWorkerName());
                inbound.setRegDate(result.getCreatedAt());
                inbound.setCreatedAt(result.getCreatedAt());

                productInboundService.registerInbound(inbound);
                log.info("일괄 자동입고 완료: {}", result.getLotNo());
            } catch (Exception e) {
                log.error("자동입고 실패 LOT: {}, 오류: {}", result.getLotNo(), e.getMessage());
            }
        }
    }

    // ==================== 목록/카운트/상세 ====================
    @Override
    public List<ProductionResultDTO> getList(SearchCriteria cri) {
        log.debug("[RESULT][LIST] criteria={}", cri);
        if (cri.getSortColumn() == null || cri.getSortColumn().isEmpty()) {
            cri.setSortColumn("created_at");
            cri.setSortOrder("desc");
        }
        return productionResultMapper.selectResultList(cri);
    }

    @Override
    public int getTotalCount(SearchCriteria cri) {
        int total = productionResultMapper.selectResultCount(cri);
        cri.setTotalCount(total);
        log.debug("[RESULT][COUNT] total={}", total);
        return total;
    }

    @Override
    public ProductionResultDTO getDetail(String resultId) {
        log.debug("[RESULT][DETAIL] resultId={}", resultId);
        ProductionResultDTO result = productionResultMapper.selectResultDetail(resultId);
        if (result == null) {
            throw new IllegalArgumentException("존재하지 않는 생산실적입니다: " + resultId);
        }
        return result;
    }
}
