package com.itwillbs.service;

import java.util.List;
import com.itwillbs.domain.ClientOrderVO;
import com.itwillbs.domain.ClientOrderDetailVO;
import com.itwillbs.domain.SearchCriteria;

public interface ClientOrderService {
    // 수주(마스터) 등록
    void registerOrder(ClientOrderVO orderVO);
    String generateNextOrderId(); // 수주번호 자동생성

    // 수주(마스터) 목록/건수
    List<ClientOrderVO> getOrderList(SearchCriteria cri);
    int getOrderCount(SearchCriteria cri);

    // *** [추가] 수주상세(제품별) 등록 ***
    void registerOrderDetail(ClientOrderDetailVO detailVO);

    // 필요시: 주문ID로 상세 리스트 조회 등
    List<ClientOrderDetailVO> getOrderDetailList(String clOrderId);
    
    //수주 상태 변경
    void bulkUpdateStatus(String[] orderIds, String newStatus);
    
    //수주 상세 보기
    ClientOrderVO getOrderById(String clOrderId);
    
    
    //수주 입금확인
    void updateOrderStatus(String orderNum, String status);
    
    //수주 삭제하기
    void deleteOrder(String clOrderId);

    //상태별 카운트
    int countOrdersByStatus(String status);
    int countAllOrders();

    
    
}
