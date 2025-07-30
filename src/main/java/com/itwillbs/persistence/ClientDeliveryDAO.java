package com.itwillbs.persistence;

import com.itwillbs.domain.ClientDeliveryVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.LotStockDTO;
import com.itwillbs.dto.ShipmentCompletedDTO;
import com.itwillbs.dto.ShipmentPendingDTO;
import com.itwillbs.dto.ShipmentPendingGroupDTO;
import com.itwillbs.dto.ShipmentProductDTO;

import java.util.List;

public interface ClientDeliveryDAO {

        
    // 출하 대기 목록 - 수주번호 기준 그룹형 전체 조회 (페이징 없이 전체)
    List<ShipmentPendingGroupDTO> getPendingShipmentGroupedList();

    // 출하 대기 목록 - 납품 제품 단건 리스트 형태
    List<ShipmentProductDTO> selectPendingShipmentFlatList();

    // 수주 상세 항목 단건 조회
    ShipmentPendingDTO selectShipmentItem(Long orderDetailId);

    // 출하 등록
    void insertDelivery(ClientDeliveryVO vo);

    // LOT별 재고 차감
    void decreaseLotStock(String productId, String lotNo, int qty);

    // 제품 전체 재고 차감 (기존방식)
    void decreaseStock(String productId, int qty);

    // 수주 상세 상태 변경
    void updateOrderDetailStatus(Long orderDetailId, String status);
    void updateClientOrderStatus(String clOrderId, String status);

    // LOT별 가용재고 조회
    List<LotStockDTO> getAvailableLots(String productId);

    // 수주번호 기준 출하대기 항목 조회
    List<ShipmentPendingDTO> selectItemsByOrderId(String clOrderId);

    // 출하 미완료 상세 개수
    int countUnshippedDetails(String clOrderId);

    // 수주 상태 업데이트
    void updateOrderStatus(String clOrderId, String status);

    // 출하완료 목록 + 총개수
    List<ShipmentCompletedDTO> searchCompletedShipmentList(SearchCriteria cri);
    int countCompletedShipmentList(SearchCriteria cri);

    // ✅ 출하대기 목록 (그룹형) - 검색 + 페이징용
    List<ShipmentPendingGroupDTO> searchPendingGroupedList(SearchCriteria cri);

    // ✅ 출하대기 총 개수 조회 (그룹형, 페이징용)
    int countPendingGroupedList(SearchCriteria cri);
    
    // 수주 상세 상태 업데이트 (int형 오버로드 대응)
    void updateOrderDetailStatus(int detailId, String status);
}
