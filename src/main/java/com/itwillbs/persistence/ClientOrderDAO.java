package com.itwillbs.persistence;

import java.util.List;

import com.itwillbs.domain.ClientOrderVO;
import com.itwillbs.domain.SearchCriteria;



public interface ClientOrderDAO {
	//수주등록
    void insertOrder(ClientOrderVO orderVO);
    int getTodayMaxSeq(String today); //수주번호자동생성
    
    //수주목록
    List<ClientOrderVO> getOrderList(SearchCriteria cri);
    int getOrderCount(SearchCriteria cri);
    
    //수주 상태 변경
    void bulkUpdateStatus(String[] orderIds, String newStatus);

    //수주 상세 보기
    ClientOrderVO getOrderById(String clOrderId);
    
    //입금확인
    void updateOrderStatus(String orderNum, String status);
    
  
 // 수주 삭제
    void deleteOrder(String clOrderId);



    
}
