package com.itwillbs.persistence;

import java.util.List;
import com.itwillbs.domain.ClientOrderDetailVO;

public interface ClientOrderDetailDAO {
    // 등록
    void insertDetail(ClientOrderDetailVO detailVO);

    // 주문ID로 전체조회  // 수주 상세 보기
    List<ClientOrderDetailVO> getDetailListByOrderId(String clOrderId);

   

}
