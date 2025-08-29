package com.itwillbs.service;

import com.itwillbs.domain.ClientOrderDetailVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.StockReservationVO;
import com.itwillbs.dto.LotStockDTO;
import com.itwillbs.dto.ReservationDetailDTO;
import com.itwillbs.persistence.ClientOrderDAO;
import com.itwillbs.persistence.ProductStockDAO;
import com.itwillbs.persistence.StockReservationDAO;
import com.itwillbs.util.AlarmMsg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.itwillbs.domain.ProductStockVO;          // [ALARM] fallback용
import org.mybatis.spring.SqlSessionTemplate;       // [ALARM]

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class StockReservationServiceImpl implements StockReservationService {

    @Autowired
    private StockReservationDAO reservationDAO;

    @Autowired
    private ProductStockDAO stockDAO;

    @Autowired
    private ClientOrderDAO orderDAO;
    
    @Autowired
    private ProductStockService productStockService; 
    
    // [ALARM] 추가: 알림 서비스 & MyBatis 세션
    @Autowired private AlarmService alarmService;
    @Autowired private SqlSessionTemplate sql;
    
    // [ALARM] 완제품 임계값 (기본 50)
    private static final int FG_THRESHOLD = 50;

  

    /**
     * 단일 예약 등록
     */
    @Transactional
    @Override
    public void reserveStock(StockReservationVO vo) {
        // ✅ qty 검증 (primitive int이면 null 체크 불가)
        int reservedQty = vo.getReservedQty();
        if (reservedQty <= 0) {
            throw new IllegalArgumentException("예약 수량이 0 이하입니다.");
        }

        // ✅ productId 검증
        String productId = vo.getProductId();
        if (productId == null || productId.isEmpty()) {
            throw new IllegalArgumentException("productId가 필요합니다.");
        }

        // ✅ LOT 미지정 → 임박 LOT부터 자동 배정
        if (vo.getLotNo() == null || vo.getLotNo().isEmpty()) {   // ← '||' 사용
            int remaining = reservedQty;

            // 임박순 + 만료 제외 + FOR UPDATE (Mapper에 준비된 메서드로 교체)
            List<LotStockDTO> lots = stockDAO.getAvailableLotsOrdered(productId);
            if (lots == null || lots.isEmpty()) {
                throw new IllegalStateException("가용 LOT 없음: " + productId);
            }

            for (LotStockDTO lot : lots) {
                if (remaining <= 0) break;

                int alloc = Math.min(remaining, Math.max(lot.getAvailableQty(), 0));
                if (alloc <= 0) continue;

                StockReservationVO r = new StockReservationVO();
                r.setClOrderId(vo.getClOrderId());
                r.setDetailId(vo.getDetailId());
                r.setProductId(productId);
                r.setClientId(vo.getClientId());
                r.setLotNo(lot.getLotNo());
                r.setReservedQty(alloc);
                r.setManager(vo.getManager() == null ? "SYSTEM" : vo.getManager());
                r.setCreatedAt(new java.util.Date());

                reservationDAO.insertReservation(r);
                stockDAO.increaseReservedQty(productId, lot.getLotNo(), alloc);

                productStockService.insertTransaction(
                    "RESERVE", r.getLotNo(), alloc, productId,
                    r.getClientId(), r.getManager(), null, null, r.getClOrderId()
                );

                remaining -= alloc;
            }

            if (remaining > 0) {
                // 보상 롤백
                List<StockReservationVO> rollback = reservationDAO.getReservationsByOrderId(vo.getClOrderId());
                for (StockReservationVO r : rollback) {
                    if (!productId.equals(r.getProductId())) continue;
                    reservationDAO.deleteReservation(r.getClOrderId(), r.getProductId());
                    stockDAO.decreaseReservedQty(r.getProductId(), r.getLotNo(), r.getReservedQty());
                    productStockService.insertTransaction(
                        "CANCEL_RESERVE", r.getLotNo(), r.getReservedQty(), r.getProductId(),
                        r.getClientId(), r.getManager(), null, null, r.getClOrderId()
                    );
                }
                throw new IllegalStateException("자재 부족: " + productId + ", 부족=" + remaining);
            }
            
            // [ALARM] 성공적으로 배정 끝났을 때 1회 체크
            checkAndNotifyProduct(productId);
            return;
        }

        // ✅ LOT 지정된 경우: 기존 단일 LOT 예약
        reservationDAO.insertReservation(vo);
        stockDAO.increaseReservedQty(productId, vo.getLotNo(), reservedQty);
        productStockService.insertTransaction(
            "RESERVE", vo.getLotNo(), reservedQty, productId,
            vo.getClientId(), vo.getManager(), null, null, vo.getClOrderId()
        );
        
        // [ALARM]
        checkAndNotifyProduct(productId);
    }


    /**
     * 단일 예약 해제 (제품 기준)
     */
    @Override
    public void releaseStock(String clOrderId, String productId) {
        // 수주번호 + 제품ID 기준 예약 목록 조회
        List<StockReservationVO> reservations = reservationDAO.getReservationsByOrderId(clOrderId);
        
        for (StockReservationVO r : reservations) {
            if (r.getProductId().equals(productId)) {
                // 예약 삭제
                reservationDAO.deleteReservation(clOrderId, productId);

                // 재고 복원 (LOT 단위)
                stockDAO.decreaseReservedQty(r.getProductId(), r.getLotNo(), r.getReservedQty());
               
                // ✅ 예약 해제 이력 기록
                productStockService.insertTransaction(
                    "CANCEL_RESERVE",
                    r.getLotNo(),
                    r.getReservedQty(),
                    r.getProductId(),
                    r.getClientId(),
                    r.getManager(),
                    null,
                    null,
                    r.getClOrderId()
                );
            
            
            }
        }
        // [ALARM] 해제 후 1회 체크
        checkAndNotifyProduct(productId);
    }


    /**
     * 예약 수량 조회 (수주번호 + 제품 기준)
     */
    @Override
    public int getReservedQty(String clOrderId, String productId) {
        return reservationDAO.getReservedQty(clOrderId, productId);
    }

    /**
     * 수주 전체 예약 목록 조회
     */
    @Override
    public List<StockReservationVO> getReservationsByOrderId(String clOrderId) {
        return reservationDAO.getReservationsByOrderId(clOrderId);
    }

    /**
     * 수주 전체 예약 등록 및 재고 반영
     */
    @Transactional
    @Override
    public boolean reserveStockByOrderId(String clOrderId,String manager) {
        // 기존 예약 삭제
        reservationDAO.deleteByOrderId(clOrderId);

        List<ClientOrderDetailVO> orderDetails = orderDAO.getOrderDetailsByOrderId(clOrderId);
        boolean success = true;

        // ✅ 임시 저장 리스트 (rollback 시 사용)
        List<StockReservationVO> tempReservations = new java.util.ArrayList<>();
        Set<String> changedProducts = new HashSet<>(); // [ALARM]
        
        for (ClientOrderDetailVO detail : orderDetails) {
            String productId = detail.getProductId();
            int requiredQty = detail.getOrderQty();

            List<LotStockDTO> lotStocks = stockDAO.getAvailableLotsOrdered(productId);
            boolean reserved = false;

            for (LotStockDTO lot : lotStocks) {
                if (requiredQty <= 0) break;

                int allocQty = Math.min(requiredQty, lot.getAvailableQty());
                if (allocQty <= 0) continue;

                StockReservationVO reservation = new StockReservationVO();
                reservation.setClOrderId(clOrderId);
                reservation.setDetailId(detail.getDetailId());
                reservation.setProductId(productId);
                reservation.setClientId(detail.getClientId());
                reservation.setLotNo(lot.getLotNo());
                reservation.setReservedQty(allocQty);
                reservation.setManager(manager);
                reservation.setCreatedAt(new Date());

                reservationDAO.insertReservation(reservation);
                stockDAO.increaseReservedQty(productId, lot.getLotNo(), allocQty);

                tempReservations.add(reservation); 
                
                productStockService.insertTransaction(
                        "RESERVE",
                        lot.getLotNo(),
                        allocQty,
                        productId,
                        detail.getClientId(),
                        manager,         
                        null,
                        null,
                        clOrderId
                    );

                requiredQty -= allocQty;
                reserved = true;
                changedProducts.add(productId); // [ALARM]
            }

            if (!reserved || requiredQty > 0) {
                System.out.println("⚠ 예약 실패: 제품 " + productId + " - 부족 수량: " + requiredQty);
                success = false;
                break; // 더 이상 반복할 필요 없음
            }
        }

        // ✅ 실패 시 롤백 수행
        if (!success) {
            for (StockReservationVO r : tempReservations) {
                reservationDAO.deleteReservation(r.getClOrderId(), r.getProductId());
                stockDAO.decreaseReservedQty(r.getProductId(), r.getLotNo(), r.getReservedQty());
            }
        }

        // [ALARM] 성공 시 변경된 제품별 1회씩 가용수량 확인 → 부족이면 MATERIAL 알림
        if (success) {
            for (String pid : changedProducts) {
                checkAndNotifyProduct(pid);
            }
        }
        
        return success;
    }



    /**
     * 수주 전체 예약 해제 및 재고 복원
     */
    @Override
    public void deleteReservation(String clOrderId) {
        // 예약 내역 조회
        List<StockReservationVO> reservations = reservationDAO.getReservationsByOrderId(clOrderId);
        Set<String> changedProducts = new HashSet<>(); // [ALARM]
        
        for (StockReservationVO r : reservations) {
            // 예약 해제
            reservationDAO.deleteReservation(r.getClOrderId(), r.getProductId());

            // 예약 수량 만큼 재고에서 복원
            stockDAO.decreaseReservedQty(r.getProductId(), r.getLotNo(), r.getReservedQty());
        
         // ✅ 예약 전체 해제 이력 기록
            productStockService.insertTransaction(
                "CANCEL_RESERVE",
                r.getLotNo(),
                r.getReservedQty(),
                r.getProductId(),
                r.getClientId(),
                r.getManager(),
                null,
                null,
                r.getClOrderId()
            );
        
            changedProducts.add(r.getProductId()); // [ALARM]
        
        }
        
        // [ALARM] 변경된 제품별 1회씩 체크
        for (String pid : changedProducts) {
            checkAndNotifyProduct(pid);
        }
    }

    /**
     * 제품 기준 LOT별 가용 수량 조회
     */
    @Override
    public List<LotStockDTO> getAvailableLotsOrdered(String productId) {
        return reservationDAO.getAvailableLotsOrdered(productId);
    }

    /**
     * 예약 중인 수주번호 목록 조회
     */
    @Override
    public List<String> getReservedOrderIds() {
        return reservationDAO.getAllReservedOrderIds();
    }

    /**
     * 해당 수주번호가 예약 상태인지 여부 확인
     */
    @Override
    public boolean isReserved(String clOrderId) {
        return reservationDAO.countReservationsByOrderId(clOrderId) > 0;
    }
    
    //예약내역확인
    @Override
    public List<StockReservationVO> getFilteredReservationList(SearchCriteria cri) {
        return reservationDAO.getFilteredReservationList(cri);
    }

    @Override
    public int countFilteredReservationList(SearchCriteria cri) {
        return reservationDAO.countFilteredReservationList(cri);
    }
    
    //예약상세 모달
    @Override
    public ReservationDetailDTO getReservationDetail(String lotNo, String clOrderId) {
        return reservationDAO.selectReservationDetailForModal(lotNo, clOrderId);
    }



// ==========================
// [ALARM] 내부 헬퍼
// ==========================
/** 제품 가용수량 조회 + 임계치 비교 후 MATERIAL에게 알림 */
    private void checkAndNotifyProduct(String productId) {
        int available = getAvailableByProduct(productId);
        if (available < FG_THRESHOLD) {
            String name = productId;   // 필요하면 제품명으로 교체
            String unit = "개";        // 필요시 "BOX" 등으로 변경

            alarmService.createAlarm(
                "FG_SHORTAGE",
                AlarmMsg.finishedShortage(name, available, FG_THRESHOLD, unit), // ← 이 줄만 변경
                "MATERIAL",
                null
            );
        }
    }

/** selectAvailableByProduct가 없으면 getProductStockSummaryList로 대체 조회 */
private int getAvailableByProduct(String productId) {
    try {
        Integer a = sql.selectOne(
            "com.itwillbs.mapper.ProductStockMapper.selectAvailableByProduct", productId);
        if (a != null) return a;
    } catch (Exception ignore) {}

    List<ProductStockVO> list =
        sql.selectList("com.itwillbs.mapper.ProductStockMapper.getProductStockSummaryList");
    if (list == null) return 0;

    return list.stream()
        .filter(v -> productId.equals(v.getProductId()))
        .map(ProductStockVO::getAvailableQty)   // Integer일 수 있음
        .filter(Objects::nonNull)               // null이면 제외
        .mapToInt(Integer::intValue)            // int 스트림으로
        .findFirst()
        .orElse(0);
}
}
