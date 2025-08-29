package com.itwillbs.service;

import java.util.List;
import com.itwillbs.domain.ClientOrderDetailVO;

public interface ClientOrderDetailService {
	//수주 상세 등록
    void registerDetail(ClientOrderDetailVO detailVO);
    List<ClientOrderDetailVO> getDetailsByOrderId(String clOrderId);


// 수주 상세 조회
    List<ClientOrderDetailVO> getDetailListByOrderId(String clOrderId);
    
 // 총합 계산 메서드 추가
    int calculateTotalPrice(List<ClientOrderDetailVO> detailList);

    
}
