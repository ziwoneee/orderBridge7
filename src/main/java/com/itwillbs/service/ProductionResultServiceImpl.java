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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor // ✅ 이거 있으면 @Autowired 대신 final 사용
@Slf4j
@Service
public class ProductionResultServiceImpl implements ProductionResultService {

    @Autowired
    private ProductionResultDAO productionResultDAO;

    // ✅ WorkOrderMapper 직접 주입 (작업지시 상태 갱신용)
    @Autowired 
    private WorkOrderMapper workOrderMapper;
    
    @Autowired
    private ProductInboundService productInboundService;

    @Autowired
    private ProductInboundDAO productInboundDAO;

    private final Map<String, Integer> inboundSerialMap = new HashMap<>(); // 입고ID용만 메모리 사용
    
    private final ProductionResultMapper productionResultMapper;
    
    // -------------------- ✅ 생산 결과 등록 - DB 기반 ID 생성 --------------------------
    @Transactional
    @Override
    public void insertResult(ProductionResultVO vo) {
        // 1) 오늘 날짜(YYYYMMDD)
        String todayStr = new SimpleDateFormat("yyyyMMdd").format(new Date());

        // 2) result_id 발번 (DB에서 조회)
        String nextSeq = productionResultMapper.selectTodayResultSeq();
        if (nextSeq == null || nextSeq.isEmpty()) nextSeq = "001";
        String resultId = "PRS-" + todayStr + "-" + nextSeq;
        vo.setResultId(resultId);

        // 3) LOT 번호 생성 (DB에서 조회)
        String lotNo = generateLotNumber(vo.getProductId(), todayStr);
        vo.setLotNo(lotNo);

        // 4) 서버측 수량 검증 및 보정
        int actual = vo.getActualQty() == null ? 0 : vo.getActualQty();
        int defect = vo.getDefectQty() == null ? 0 : vo.getDefectQty();
        if (actual <= 0) {
            throw new IllegalArgumentException("생산수량은 0보다 커야 합니다.");
        }
        if (defect < 0 || defect > actual) {
            throw new IllegalArgumentException("불량품수량은 0 이상이고 생산수량 이하여야 합니다.");
        }

        // 5) 생산결과 INSERT
        productionResultDAO.insertResult(vo);
        log.info("생산결과 등록 완료: {}", resultId);

        // 6) 작업지시 누적/상태 반영 (같은 트랜잭션)
        // ✅ WorkOrderMapper의 applyResultToWorkOrder 직접 호출
        workOrderMapper.applyResultToWorkOrder(vo.getOrderId());
        log.info("작업지시 상태 갱신 완료: {}", vo.getOrderId());

        // 7) 자동입고 처리: 정상품 수량만 입고
        int netQty = actual - defect;
        if (netQty <= 0) {
            log.info("정상품 수량이 0 이하여서 입고를 건너뜁니다. LOT: {}", lotNo);
            return;
        }

        // 동일 LOT 중복입고 방지
        if (productInboundDAO.existsByLotNo(lotNo)) {
            log.warn("이미 입고된 LOT입니다: {}", lotNo);
            return;
        }

        // 8) 입고ID 발번 (메모리 기반 - 입고는 단순하므로)
        int serial = inboundSerialMap.getOrDefault(todayStr, 0) + 1;
        inboundSerialMap.put(todayStr, serial);
        String inboundId = String.format("IN-FG-%s-%03d", todayStr, serial);

        // 9) 입고 저장
        ProductInboundVO inbound = new ProductInboundVO();
        inbound.setInboundId(inboundId);
        inbound.setLotNo(lotNo);
        inbound.setProductId(vo.getProductId());
        inbound.setInboundQty(netQty);
        inbound.setInboundType("생산");
        inbound.setRemark("생산결과 자동입고");
        inbound.setManager(vo.getWorkerName());
        inbound.setRegDate(vo.getCreatedAt());
        inbound.setCreatedAt(vo.getCreatedAt());

        productInboundService.registerInbound(inbound);
        log.info("자동입고 완료: {} -> {}", lotNo, inboundId);
    }

    // ✅ LOT 번호 생성 (DB 기반 - 안전함)
    private String generateLotNumber(String productId, String todayStr) {
        // 제품 코드 매핑
        String productCode = getProductCodeFromId(productId);
        
        // DB에서 오늘 날짜의 해당 제품 LOT 시퀀스 조회
        String lotSeq = productionResultMapper.selectTodayLotSeq(productCode, todayStr);
        if (lotSeq == null || lotSeq.isEmpty()) {
            lotSeq = "001";
        }
        
        String lotNo = String.format("LOT-%s-%s-%s", productCode, todayStr, lotSeq);
        log.debug("LOT 번호 생성: {} -> {}", productId, lotNo);
        return lotNo;
    }

    // ✅ 제품ID → 제품코드 매핑
    private String getProductCodeFromId(String productId) {
        switch (productId) {
            case "FG-001": return "DG";  // 돼지국밥
            case "FG-002": return "SD";  // 순대국밥  
            case "FG-003": return "HW";  // 한우곰탕
            default: return "ETC";       // 기타
        }
    }

    // ✅ 자동입고 전체 처리 (버튼용)
    @Override
    public void saveAllToInbound() {
        List<ProductionResultVO> resultList = productionResultDAO.selectAllResults();
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
        int serial = 0;

        for (ProductionResultVO result : resultList) {
            try {
                boolean alreadyInbound = productInboundDAO.existsByLotNo(result.getLotNo());
                if (alreadyInbound) continue;

                int netQty = result.getActualQty() - result.getDefectQty();
                if (netQty <= 0) continue;

                String productId = result.getProductId();
                String productName = getProductNameFromId(productId);
               
                serial++;
                String inboundId = String.format("IN-FG-%s-%03d", today, serial);

                ProductInboundVO inbound = new ProductInboundVO();
                inbound.setInboundId(inboundId);
                inbound.setLotNo(result.getLotNo());
                inbound.setProductId(productId);
                inbound.setProductName(productName);
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

    // 제품 ID → 제품명
    private String getProductNameFromId(String productId) {
        switch (productId) {
            case "FG-001": return "돼지국밥";
            case "FG-002": return "순대국밥";
            case "FG-003": return "한우곰탕";
            default: return "기타제품";
        }
    }
        
    // -------------------- 목록/카운트 (JOIN 조회) --------------------
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

    // ✅ 상세 조회 메서드 추가 (Controller에서 사용)
    @Override
    public ProductionResultDTO getDetail(String resultId) {
        log.debug("[RESULT][DETAIL] resultId={}", resultId);
        ProductionResultDTO result = productionResultMapper.selectResultDetail(resultId);
        if (result == null) {
            throw new IllegalArgumentException("존재하지 않는 생산실적입니다: " + resultId);
        }
        return result;
    }
    
 // 기존 ServiceImpl 클래스에 추가

    @Override
    public boolean checkNeedSupplement(String orderId) {
        try {
            // ✅ 기존 selectWorkOrderProgress 사용
            Map<String, Object> workOrderInfo = productionResultMapper.selectWorkOrderProgress(orderId);
            if (workOrderInfo == null) {
                return false;
            }
            
            Integer orderQty = (Integer) workOrderInfo.get("order_qty");
            if (orderQty == null) {
                return false;
            }
            
            // 해당 작업지시의 총 양품 수량 계산
            int totalProduced = productionResultMapper.selectTotalProducedQty(orderId);
            int totalDefect = productionResultMapper.selectTotalDefectQty(orderId);
            int goodQty = totalProduced - totalDefect;
            
            boolean needSupplement = goodQty < orderQty;
            
                  return needSupplement;
            
        } catch (Exception e) {
            log.error("보완생산 체크 중 오류: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public int getShortageQty(String orderId) {
        try {
            // ✅ 기존 selectWorkOrderProgress 사용
            Map<String, Object> workOrderInfo = productionResultMapper.selectWorkOrderProgress(orderId);
            if (workOrderInfo == null) {
                return 0;
            }
            
            Integer orderQty = (Integer) workOrderInfo.get("order_qty");
            if (orderQty == null) {
                return 0;
            }
            
            int totalProduced = productionResultMapper.selectTotalProducedQty(orderId);
            int totalDefect = productionResultMapper.selectTotalDefectQty(orderId);
            int goodQty = totalProduced - totalDefect;
            
            return Math.max(0, orderQty - goodQty);
            
        } catch (Exception e) {
            log.error("부족수량 계산 중 오류: {}", e.getMessage());
            return 0;
        }
    }
}