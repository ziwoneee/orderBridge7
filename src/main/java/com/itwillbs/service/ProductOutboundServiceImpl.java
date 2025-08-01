package com.itwillbs.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itwillbs.domain.ProductOutboundVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.persistence.ProductOutboundDAO;
import com.itwillbs.persistence.ProductStockDAO;

@Service
public class ProductOutboundServiceImpl implements ProductOutboundService {

    @Autowired
    private ProductOutboundDAO outboundDAO;

    @Autowired
    private ProductStockDAO stockDAO;
    
    @Autowired
    private ProductStockService productStockService;

    /**
     * ✅ 출고 등록 서비스
     * - 출고ID 자동 생성 (형식: OUT-FG-YYYYMMDD-001)
     * - 출고 데이터 DB에 등록
     * - 해당 LOT의 재고 수량 차감
     */
    @Transactional
    @Override
    public void registerOutbound(ProductOutboundVO vo) {
        // 1. 오늘 날짜를 yyyyMMdd 형식으로 문자열 생성
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());

        // 2. 금일 출고된 내역 중 가장 큰 일련번호 조회
        //    예: OUT-FG-20250728-003 이 있다면 -> 3 반환됨
        Integer maxSeq = outboundDAO.getMaxOutboundSeqToday(today);

        // 3. 다음 일련번호 생성 (없으면 1부터 시작)
        int nextSeq = (maxSeq != null) ? maxSeq + 1 : 1;

        // 4. 출고ID 구성: OUT-FG-YYYYMMDD-XXX
        String outboundId = String.format("OUT-FG-%s-%03d", today, nextSeq);
        vo.setOutboundId(outboundId); // VO에 ID 설정

        // 5. 출고 등록 (INSERT INTO product_outbound ...)
        outboundDAO.insertOutbound(vo);

        // 6. 재고 차감 처리 (해당 제품 + LOT 기준)
        stockDAO.decreaseStockQty(vo.getProductId(), vo.getLotNo(), vo.getOutboundQty());
   
     // ✅ 출고 이력 기록
        productStockService.insertTransaction("출고", vo.getLotNo(), vo.getOutboundQty(), vo.getProductId(), vo.getClientId(), "시스템");

    
    }

    /**
     * ✅ 출고 목록 검색
     */
    @Override
    public List<ProductOutboundVO> searchOutboundList(SearchCriteria cri) {
        return outboundDAO.searchOutboundList(cri);
    }

    /**
     * ✅ 출고 목록 총 개수 조회 (페이징용)
     */
    @Override
    public int countOutboundList(SearchCriteria cri) {
        return outboundDAO.countOutboundList(cri);
    }

    
    public String generateOutboundId() {
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
        Integer maxSeq = outboundDAO.getMaxOutboundSeqToday(today);
        int nextSeq = (maxSeq != null) ? maxSeq + 1 : 1;
        return String.format("OUT-FG-%s-%03d", today, nextSeq);
    }
    
    //상세보기
    @Override
    public ProductOutboundVO getOutboundDetail(String outboundId) {
        return outboundDAO.getOutboundDetail(outboundId);
    }
}
