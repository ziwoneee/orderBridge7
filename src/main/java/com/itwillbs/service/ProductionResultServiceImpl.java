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
import com.itwillbs.persistence.ProductInboundDAO;
import com.itwillbs.persistence.ProductionResultDAO;

@Service
public class ProductionResultServiceImpl implements ProductionResultService {

    @Autowired
    private ProductionResultDAO productionResultDAO;

    @Autowired
    private ProductInboundService productInboundService;

    @Autowired
    private ProductInboundDAO productInboundDAO;

    private final Map<String, Integer> serialMap = new HashMap<>(); // 날짜별 시리얼 관리
    
    
    // -------------------- ✅ 생산 결과 등록  - 아름 시작--------------------------
    // ✅ 생산 결과 등록 (단일) 
    @Transactional
    @Override
    public void insertResult(ProductionResultVO vo) {
        productionResultDAO.insertResult(vo);

        int netQty = vo.getActualQty() - vo.getDefectQty();
        if (netQty <= 0) return;

        String lotNo = vo.getLotNo();
        String productId = extractProductIdFromLot(lotNo);
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());

        // 시리얼 번호 증가
        int serial = serialMap.getOrDefault(today, 0) + 1;
        serialMap.put(today, serial);

        String inboundId = String.format("IN-FG-%s-%03d", today, serial);

        ProductInboundVO inbound = new ProductInboundVO();
        inbound.setInboundId(inboundId);
        inbound.setLotNo(lotNo);
        inbound.setProductId(productId);
        inbound.setInboundQty(netQty);
        inbound.setInboundType("생산");
        inbound.setRemark("생산결과 자동입고");
        inbound.setManager(vo.getWorkerName());
        inbound.setRegDate(vo.getCreatedAt());
        inbound.setCreatedAt(vo.getCreatedAt());

        productInboundService.registerInbound(inbound);
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

                String productId = extractProductIdFromLot(result.getLotNo());
                String productName = extractProductNameFromProductId(productId);
               
                
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
            } catch (Exception e) {
                System.err.println("자동입고 실패 LOT: " + result.getLotNo());
                e.printStackTrace();
            }
        }
    }

    // ✅ LOT에서 제품코드 추출 로직
    private String extractProductIdFromLot(String lotNo) {
        if (lotNo == null || lotNo.split("-").length < 2) return "UNKNOWN";
        String code = lotNo.split("-")[1];
        switch (code) {
            case "DG": return "FG-001";
            case "SD": return "FG-002";
            case "HW": return "FG-003";
            default: return "FG-999";
        }
    }
    //제품 ID → 제품명
    private String extractProductNameFromProductId(String productId) {
        switch (productId) {
            case "FG-001": return "돼지국밥";
            case "FG-002": return "순대국밥";
            case "FG-003": return "한우곰탕";
            default: return "기타제품";
        }
    }
        
    // -------------------- ✅ 생산 결과 등록  - 아름 끝--------------------------

}
