package com.itwillbs.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.itwillbs.domain.ClientOrderDetailVO;
import com.itwillbs.persistence.ClientOrderDetailDAO;

@Service
public class ClientOrderDetailServiceImpl implements ClientOrderDetailService {

    @Autowired
    private ClientOrderDetailDAO clientOrderDetailDAO;

    @Override
    public void registerDetail(ClientOrderDetailVO detailVO) {
        clientOrderDetailDAO.insertDetail(detailVO);
    }
//수주 상세 등록
    @Override
    public List<ClientOrderDetailVO> getDetailsByOrderId(String clOrderId) {
        return clientOrderDetailDAO.getDetailListByOrderId(clOrderId);
    }
    
    
    //수주 상세 조회
    @Override
    public List<ClientOrderDetailVO> getDetailListByOrderId(String clOrderId) {
        return clientOrderDetailDAO.getDetailListByOrderId(clOrderId);
    }
}
