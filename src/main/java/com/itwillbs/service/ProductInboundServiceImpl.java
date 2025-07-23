package com.itwillbs.service;

import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itwillbs.domain.ProductInboundVO;
import com.itwillbs.domain.ProductionResultVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.persistence.ProductInboundDAO;
import com.itwillbs.persistence.ProductStockDAO;
import com.itwillbs.persistence.ProductionResultDAO;

@Service
public class ProductInboundServiceImpl implements ProductInboundService {

    @Autowired
    private ProductInboundDAO inboundDAO;

    @Autowired
    private ProductStockDAO stockDAO;

    @Autowired
    private ProductionResultDAO productionResultDAO;

    // ✅ 실제 입고 등록
    @Transactional
    @Override
    public void registerInbound(ProductInboundVO vo) {
        // 제품명 자동 추출
        String productName = extractProductNameFromProductId(vo.getProductId());
        vo.setProductName(productName);

        inboundDAO.insertInbound(vo);
        stockDAO.upsertStockQty(vo.getProductId(), vo.getLotNo(), vo.getInboundQty());
    }

    // ✅ 생산 결과 기반 임시 조회용 리스트
    @Override
    public List<ProductInboundVO> searchProductionInboundList(SearchCriteria cri) {
        List<ProductionResultVO> resultList = productionResultDAO.searchProductionResults(cri);
        List<ProductInboundVO> inboundList = new ArrayList<>();

        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
        int serial = cri.getPageStart();

        for (ProductionResultVO result : resultList) {
            int netQty = result.getActualQty() - result.getDefectQty();
            if (netQty <= 0) continue;

            serial++;
            String inboundId = String.format("IN-FG-%s-%03d", today, serial);
            String lotNo = result.getLotNo();
            String productId = extractProductIdFromLot(lotNo);

            ProductInboundVO vo = new ProductInboundVO();
            vo.setInboundId(inboundId);
            vo.setLotNo(lotNo);
            vo.setProductId(productId);
            vo.setProductName(extractProductNameFromProductId(productId)); // ✅ 제품명 세팅
            vo.setInboundQty(netQty);
            vo.setInboundType("생산");
            vo.setRemark("생산실적 기반 자동입고");
            vo.setManager(result.getWorkerName());
            vo.setRegDate(result.getCreatedAt());
            vo.setCreatedAt(result.getCreatedAt());

            inboundList.add(vo);
        }

        return inboundList;
    }

    @Override
    public int countProductionInboundList(SearchCriteria cri) {
        return inboundDAO.countProductionResults(cri);
    }

    // ✅ 생산 결과 기반 DB 저장 (조건부 자동입고)
    @Override
    @Transactional
    public void saveInboundFromProductionResults(SearchCriteria cri) {
        List<ProductionResultVO> resultList = productionResultDAO.searchProductionResults(cri);

        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
        int serial = 0;

        for (ProductionResultVO result : resultList) {
            int netQty = result.getActualQty() - result.getDefectQty();
            if (netQty <= 0) continue;

            serial++;
            String inboundId = String.format("IN-FG-%s-%03d", today, serial);
            String productId = extractProductIdFromLot(result.getLotNo());

            ProductInboundVO vo = new ProductInboundVO();
            vo.setInboundId(inboundId);
            vo.setLotNo(result.getLotNo());
            vo.setProductId(productId);
            vo.setInboundQty(netQty);
            vo.setInboundType("생산");
            vo.setRemark("생산결과 자동입고");
            vo.setManager(result.getWorkerName());
            vo.setRegDate(result.getCreatedAt());
            vo.setCreatedAt(result.getCreatedAt());

            try {
                registerInbound(vo); // ✅ 제품명 포함되도록 registerInbound 재사용
                System.out.println("✅ INSERT 성공: " + vo.getInboundId());
            } catch (Exception e) {
                System.err.println("❌ INSERT 실패: " + vo.getInboundId());
                e.printStackTrace();
            }
        }
    }

    // ✅ 기존 생산 결과 전체 자동입고 처리
    @Override
    @Transactional
    public void autoInboundFromExistingResults() {
        List<ProductionResultVO> allResults = productionResultDAO.selectAllResults();

        Map<String, Integer> serialMap = new HashMap<>();

        for (ProductionResultVO result : allResults) {
            int netQty = result.getActualQty() - result.getDefectQty();
            if (netQty <= 0) continue;

            if (inboundDAO.existsByLotNo(result.getLotNo())) continue;

            String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
            int serial = serialMap.getOrDefault(today, 0) + 1;
            serialMap.put(today, serial);
            String inboundId = String.format("IN-FG-%s-%03d", today, serial);
            String productId = extractProductIdFromLot(result.getLotNo());

            ProductInboundVO inbound = new ProductInboundVO();
            inbound.setInboundId(inboundId);
            inbound.setLotNo(result.getLotNo());
            inbound.setProductId(productId);
            inbound.setInboundQty(netQty);
            inbound.setInboundType("생산");
            inbound.setRemark("기존 실적 기반 자동입고");
            inbound.setManager(result.getWorkerName());
            inbound.setRegDate(result.getCreatedAt());
            inbound.setCreatedAt(result.getCreatedAt());

            registerInbound(inbound); // ✅ 제품명 포함됨
        }
    }

    // ✅ LOT에서 제품 ID 추출
    private String extractProductIdFromLot(String lotNo) {
        if (lotNo == null || lotNo.split("-").length < 2) return "UNKNOWN";

        String code = lotNo.split("-")[1]; // 예: SD, DG, HW
        switch (code) {
            case "DG": return "FG-001"; // 돼지국밥
            case "SD": return "FG-002"; // 순대국밥
            case "HW": return "FG-003"; // 한우곰탕
            default: return "FG-999";   // 기타
        }
    }

    // ✅ 제품 ID로 제품명 반환
    private String extractProductNameFromProductId(String productId) {
        switch (productId) {
            case "FG-0001": return "돼지국밥";
            case "FG-0002": return "순대국밥";
            case "FG-0003": return "한우곰탕";
            default: return "기타제품";
        }
    }
    
    
 // ✅ 입고 목록 검색 (실제 product_inbound 기준)
    @Override
    public List<ProductInboundVO> searchInboundList(SearchCriteria cri) {
        return inboundDAO.searchInboundList(cri);
    }

    // ✅ 검색 결과 개수 (페이징용)
    @Override
    public int countInboundList(SearchCriteria cri) {
        return inboundDAO.countInboundList(cri);
    }

}
