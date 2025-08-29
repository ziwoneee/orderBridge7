package com.itwillbs.service;

import com.itwillbs.domain.ClientDeliveryVO;
import com.itwillbs.domain.MaterialInventoryVO;
import com.itwillbs.domain.ProductOutboundVO;
import com.itwillbs.domain.ProductStockTransactionVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.StockReservationVO;
import com.itwillbs.dto.DeliveryHistoryDTO;
import com.itwillbs.dto.LotStockDTO;
import com.itwillbs.dto.ShipmentCompletedDTO;
import com.itwillbs.dto.ShipmentCompletedGroupDTO;
import com.itwillbs.dto.ShipmentPendingDTO;
import com.itwillbs.dto.ShipmentPendingGroupDTO;
import com.itwillbs.persistence.ClientDeliveryDAO;
import com.itwillbs.persistence.MaterialInventoryDAO;
import com.itwillbs.persistence.ProductStockDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClientDeliveryServiceImpl implements ClientDeliveryService {

    @Autowired
    private ClientDeliveryDAO deliveryDAO;

    @Autowired
    private ProductOutboundService outboundService;

    @Autowired
    private StockReservationService reservationService;
    
    @Autowired
    private ProductStockDAO productStockDAO;
    
    
    @Autowired
    private MaterialInventoryDAO materialInventoryDAO;    
    


 // 출하대기 목록 (그룹형) - 검색 + 페이징 지원
    @Override
    public List<ShipmentPendingGroupDTO> searchPendingGroupedList(SearchCriteria cri) {
        return deliveryDAO.searchPendingGroupedList(cri);
    }

    // 출하대기 총 개수 조회 (그룹형, 페이징용)
    @Override
    public int countPendingGroupedList(SearchCriteria cri) {
        return deliveryDAO.countPendingGroupedList(cri);
    }


    @Override
    public List<ShipmentPendingGroupDTO> getPendingShipmentGroupedList() {
        return deliveryDAO.getPendingShipmentGroupedList();
    }

    /**
     * ✅ 수주번호 단위 출하 처리 (예약 기반)
     */
    @Transactional
    @Override
    public void processShipmentByOrderId(String clOrderId) {
        List<StockReservationVO> reservations = reservationService.getReservationsByOrderId(clOrderId);

        if (reservations == null || reservations.isEmpty()) return;

        boolean allShipped = true;
        String trackingNumber = generateTrackingNumber();
        
        // ✅ 주문수량 합계 조회
        int totalOrderQty = deliveryDAO.getTotalOrderQtyByOrderId(clOrderId);

        for (StockReservationVO r : reservations) {
            int reservedQty = r.getReservedQty();
            

            // ✅ 출하 ID 생성
            String deliveryId = generateDeliveryId();

            // ✅ 출하 등록
            ClientDeliveryVO delivery = new ClientDeliveryVO();
            delivery.setDeliveryId(deliveryId);
            delivery.setClOrderId(r.getClOrderId());
            delivery.setProductId(r.getProductId());
            delivery.setLotNo(r.getLotNo());
            delivery.setDeliveryQty(reservedQty);
            delivery.setDeliveryDate(new Date());
            delivery.setDeliveryStatus("배송준비");
            delivery.setClientId(r.getClientId());
            delivery.setTrackingNumber(trackingNumber);
            delivery.setCreatedAt(new Date());
            delivery.setUpdatedAt(new Date());

            deliveryDAO.insertDelivery(delivery);

            // ✅ 출고 등록
            ProductOutboundVO outbound = new ProductOutboundVO();
            outbound.setProductId(r.getProductId());
            outbound.setLotNo(r.getLotNo());
            outbound.setOutboundQty(reservedQty);
            outbound.setOutboundDate(new Date());
            outbound.setClientId(r.getClientId());
            outbound.setManager(r.getManager());
            outbound.setTrackingNumber(trackingNumber);

            outboundService.registerOutbound(outbound);

            
            
            // ✅ 상세 상태 SHIPPED 처리
            deliveryDAO.updateOrderDetailStatus(r.getDetailId(), "SHIPPED");
        }
        
     // ✅ 자재 소진 규칙 적용
        BigDecimal rm0018Qty = BigDecimal.ZERO; // 대박스(100개)
        int rm0017Qty = 0;                      // 소박스(≤30개)

        if (totalOrderQty <= 30) {
            rm0017Qty = 1;
        } else {
            rm0018Qty = BigDecimal.valueOf(totalOrderQty / 100); // 정수 나눗셈
            int remainder = totalOrderQty % 100;
            if (remainder > 0) {
                if (remainder <= 30) rm0017Qty = 1;
                else rm0018Qty = rm0018Qty.add(BigDecimal.ONE);
            }
        }

        // ✅ 실제 차감 (FEFO)
        if (rm0018Qty.compareTo(BigDecimal.ZERO) > 0) {
            // consumeMaterialFefo(String, BigDecimal) 버전이 있으면 그대로
            consumeMaterialFefo("RM-0018", rm0018Qty);

            // 만약 메서드 시그니처가 int면 ↓로 호출
            // consumeMaterialFefo("RM-0018", rm0018Qty.intValue());
        }
        if (rm0017Qty > 0) {
            // 타입 맞추기 (BigDecimal 버전 쓰면 변환)
            consumeMaterialFefo("RM-0017", BigDecimal.valueOf(rm0017Qty));
            // 또는 메서드가 int면 그대로: consumeMaterialFefo("RM-0017", rm0017Qty);
        }

        

        // ✅ 마스터 상태도 SHIPPED 처리
        deliveryDAO.updateClientOrderStatus(clOrderId, "SHIPPED");

        // ✅ 예약 해제
        reservationService.deleteReservation(clOrderId);
    }

    @Override
    public void updateClientOrderStatus(String clOrderId, String status) {
        deliveryDAO.updateClientOrderStatus(clOrderId, status);
    }
    
   //출하 완료 항목
    @Override
    public List<ShipmentCompletedDTO> searchCompletedShipmentList(SearchCriteria cri) {
    	deliveryDAO.updateDeliveriesToCompletedAfter3Days(); 
    	return deliveryDAO.searchCompletedShipmentList(cri);
    }
   

    @Override
    public int countCompletedShipmentList(SearchCriteria cri) {
        return deliveryDAO.countCompletedShipmentList(cri);
    }

    /**
     * ✅ 송장번호 생성 메서드: 예) 20250725-123456
     */
    private String generateTrackingNumber() {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        int random = (int) (Math.random() * 1000000);
        return date + "-" + String.format("%06d", random);
    }
    
 // ✅ 출하 완료 목록 (수주번호 기준 그룹형 구조)
    @Override
    public List<ShipmentCompletedGroupDTO> getCompletedGroupedList(SearchCriteria cri) {
        List<ShipmentCompletedDTO> flatList = deliveryDAO.searchCompletedShipmentList(cri);

        Map<String, ShipmentCompletedGroupDTO> groupedMap = new LinkedHashMap<>();

        for (ShipmentCompletedDTO dto : flatList) {
            String clOrderId = dto.getClOrderId();
            ShipmentCompletedGroupDTO group = groupedMap.get(clOrderId);
            if (group == null) {
                group = new ShipmentCompletedGroupDTO();
                group.setClOrderId(clOrderId);
                group.setClientName(dto.getClientName());
                group.setDeliveryDate(dto.getDeliveryDate());
                group.setTrackingNumber(dto.getTrackingNumber());
                group.setProductList(new ArrayList<>());
                groupedMap.put(clOrderId, group);
            }
            group.getProductList().add(dto);
        }

        return new ArrayList<>(groupedMap.values());
    }

//출하 아이디 생성
    @Override
    public String generateDeliveryId() {
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
        Integer maxSeq = deliveryDAO.getMaxDeliverySeqToday(today);
        int nextSeq = (maxSeq == null) ? 1 : maxSeq + 1;
        return String.format("DLV-%s-%03d", today, nextSeq);
    
}
 //수주관리 출하이력 조회   
    @Override
    public List<DeliveryHistoryDTO> getDeliveriesByOrderId(String clOrderId) {
        return deliveryDAO.getDeliveriesByOrderId(clOrderId); 
    
   
    }
    @Override
    @Transactional
    public void cancelDelivery(String deliveryId) {
        // 1. 출하 상세 정보 조회
        ClientDeliveryVO vo = deliveryDAO.getDeliveryById(deliveryId);
        System.out.println(">> 출하 상세 정보: " + vo);

        // ✅ 이미 취소된 출하건이면 중단
        if ("CANCELLED".equalsIgnoreCase(vo.getDeliveryStatus())) {
            System.out.println("⚠️ 이미 취소된 출하건입니다: " + deliveryId);
            return; // 또는 throw new IllegalStateException("이미 취소된 건입니다.");
        }

        // 2. 재고 복원
        productStockDAO.increaseLotStock(vo.getProductId(), vo.getLotNo(), vo.getDeliveryQty());

        // ✅ 2-1. 취소 이력 기록
        ProductStockTransactionVO tx = new ProductStockTransactionVO();
        tx.setProductId(vo.getProductId());
        tx.setLotNo(vo.getLotNo());
        tx.setQty(vo.getDeliveryQty());
        tx.setType("취소");
        tx.setManager(vo.getManager());
        tx.setRemark("출하 취소");
        tx.setClientId(vo.getClientId());
        tx.setClOrderId(vo.getClOrderId()); 
        tx.setRegDate(new Date());

        productStockDAO.insertTransaction(tx);

        // 3. 수주 상태 복원
        deliveryDAO.revertClientOrderStatus(vo.getClOrderId());

        // 4. 출하 상태 변경
        System.out.println(">> delivery_status → CANCELLED 처리: " + deliveryId);
        deliveryDAO.updateDeliveryStatus(deliveryId, "CANCELLED");

        // 5. 출고 이력 삭제
        outboundService.deleteOutboundByOrderId(vo.getClOrderId());
    }



    
    //출하상태 변경
    @Override
    public void updateDeliveryStatus(String deliveryId, String status) {
        deliveryDAO.updateDeliveryStatus(deliveryId, status);
    }

   
    //자재 박스 선입선출
    private void consumeMaterialFefo(String materialId, java.math.BigDecimal needQty) {
        java.util.List<MaterialInventoryVO> lots = materialInventoryDAO.selectAvailableLotsForMaterial(materialId);

        java.math.BigDecimal remain = (needQty == null ? java.math.BigDecimal.ZERO
                : needQty).setScale(4, java.math.RoundingMode.HALF_UP);

        for (MaterialInventoryVO lot : lots) {
            if (remain.compareTo(java.math.BigDecimal.ZERO) <= 0) break;

            java.math.BigDecimal available = (lot.getQuantity() == null ? java.math.BigDecimal.ZERO
                    : lot.getQuantity());

            if (available.compareTo(java.math.BigDecimal.ZERO) <= 0) continue;

            java.math.BigDecimal deduct = available.min(remain); // Math.min 대신
            materialInventoryDAO.decreaseLotQuantity(lot.getInventoryId(), deduct); // BigDecimal로
            remain = remain.subtract(deduct);
        }

        if (remain.compareTo(java.math.BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("자재 부족: " + materialId + ", 부족=" + remain.stripTrailingZeros().toPlainString());
        }
    }


    
  

}
