package com.itwillbs.service;

import com.itwillbs.domain.ClientDeliveryVO;
import com.itwillbs.domain.ProductOutboundVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.StockReservationVO;
import com.itwillbs.dto.DeliveryHistoryDTO;
import com.itwillbs.dto.LotStockDTO;
import com.itwillbs.dto.ShipmentCompletedDTO;
import com.itwillbs.dto.ShipmentCompletedGroupDTO;
import com.itwillbs.dto.ShipmentPendingDTO;
import com.itwillbs.dto.ShipmentPendingGroupDTO;
import com.itwillbs.persistence.ClientDeliveryDAO;
import com.itwillbs.persistence.ProductStockDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        List<ShipmentCompletedDTO> list = deliveryDAO.searchCompletedShipmentList(cri);

        for (ShipmentCompletedDTO dto : list) {
            boolean canCancel = canCancelShipment(dto.getDeliveryDate(), dto.getCreatedAt());
            dto.setCancelAvailable(canCancel);
        }

        return list;
    }

    private boolean canCancelShipment(LocalDateTime deliveryDate, LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();

        // 출하일이 오늘이고, 현재 시간이 14시 이전
        if (deliveryDate.toLocalDate().isEqual(now.toLocalDate()) && now.getHour() < 14) {
            return true;
        }

        // 출하일이 내일이고, 출하처리 시각이 오늘 14시 이후, 그리고 지금이 내일 14시 전
        if (deliveryDate.toLocalDate().isEqual(now.toLocalDate().plusDays(1))) {
            if (createdAt.toLocalDate().isEqual(now.toLocalDate())
                && createdAt.getHour() >= 14
                && now.getHour() < 14) {
                return true;
            }
        }

        return false;
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

        // 2. 재고 복원
        productStockDAO.increaseLotStock(vo.getProductId(), vo.getLotNo(), vo.getDeliveryQty());

        // 3. 수주 상태 복원
        deliveryDAO.revertClientOrderStatus(vo.getClOrderId());

        // 4. 출하 상태 변경
        System.out.println(">> delivery_status → CANCELLED 처리: " + deliveryId);
        deliveryDAO.updateDeliveryStatus(deliveryId, "CANCELLED"); // ✅ 확인!
   
     // 5. 출고 이력 삭제
        outboundService.deleteOutboundByOrderId(vo.getClOrderId());
     
       
    
    }

    
    //출하상태 변경
    @Override
    public void updateDeliveryStatus(String deliveryId, String status) {
        deliveryDAO.updateDeliveryStatus(deliveryId, status);
    }

    
    
    //출하취소 가능 시간 
    
    public boolean canCancelShipment(Date deliveryDate, Date createdAt) {
        Date now = new Date();
        long nowMillis = now.getTime();

        // 오늘 날짜 문자열
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String todayStr = sdf.format(now);
        String deliveryDayStr = sdf.format(deliveryDate);

        // 오후 2시 기준 시간 설정
        String limitStr = deliveryDayStr + " 14:00:00";
        Date limitTime;
        try {
            limitTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(limitStr);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // [1] 오늘 출하이고, 현재 시간이 오후 2시 전이면 취소 가능
        if (todayStr.equals(deliveryDayStr) && now.before(limitTime)) {
            return true;
        }

        // [2] 출하가 어제 오후 2시 이후에 처리되었고, 지금이 오늘 오후 2시 전이면 취소 가능
        long diff = nowMillis - createdAt.getTime(); // 차이 밀리초
        long oneDayMillis = 24 * 60 * 60 * 1000;

        if (!todayStr.equals(deliveryDayStr) && createdAt.after(limitTime) && diff < oneDayMillis) {
            return true;
        }

        return false;
    }


}
