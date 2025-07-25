package com.itwillbs.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itwillbs.domain.ClientOrderVO;
import com.itwillbs.domain.ClientOrderDetailVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.persistence.ClientOrderDAO;
import com.itwillbs.persistence.ClientOrderDetailDAO; // 상세 DAO 필요!

@Service
public class ClientOrderServiceImpl implements ClientOrderService {

    @Autowired
    private ClientOrderDAO clientOrderDAO;

    @Autowired
    private ClientOrderDetailDAO clientOrderDetailDAO; // 상세 DAO 주입

    // 수주(마스터) 목록
    @Override
    public List<ClientOrderVO> getOrderList(SearchCriteria cri) {
        return clientOrderDAO.getOrderList(cri);
    }

    @Override
    public int getOrderCount(SearchCriteria cri) {
        return clientOrderDAO.getOrderCount(cri);
    }

    // 수주(마스터) 등록
    @Override
    public void registerOrder(ClientOrderVO orderVO) {
        // 수주번호 자동 생성
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
        int maxSeq = clientOrderDAO.getTodayMaxSeq(today);
        String seqStr = String.format("%03d", maxSeq + 1);

        String newOrderId = "CLT-" + today + "-" + seqStr;
        orderVO.setClOrderId(newOrderId);

        String newOrderNum = "ORD-" + today + "-" + seqStr;
        orderVO.setClOrderNum(newOrderNum);

        if (orderVO.getClOrderDate() == null) {
            orderVO.setClOrderDate(new java.util.Date());
        }

        clientOrderDAO.insertOrder(orderVO);
        // ※ 상세 등록은 Controller에서 for문으로 별도 호출 (일반적)
    }

    // 상세(제품별) 등록
    @Override
    public void registerOrderDetail(ClientOrderDetailVO detailVO) {
        clientOrderDetailDAO.insertDetail(detailVO);
    }

    // 주문ID로 상세 리스트 조회 (선택)
    @Override
    public List<ClientOrderDetailVO> getOrderDetailList(String clOrderId) {
        return clientOrderDetailDAO.getDetailListByOrderId(clOrderId);
    }

    //수주번호 자동생성
    @Override
    public String generateNextOrderId() {
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
        int maxSeq = clientOrderDAO.getTodayMaxSeq(today);
        String seqStr = String.format("%03d", maxSeq + 1);
        return "CLT-" + today + "-" + seqStr;
    }
    
    //수주 상태 변경
    @Override
    public void bulkUpdateStatus(String[] orderIds, String newStatus) {
        clientOrderDAO.bulkUpdateStatus(orderIds, newStatus);
    }

    
    //수주 상세보기
    @Override
    public ClientOrderVO getOrderById(String clOrderId) {
        return clientOrderDAO.getOrderById(clOrderId);
    }
    
    
    //수주 입금확인
    @Override
    public void updateOrderStatus(String orderNum, String status) {
        clientOrderDAO.updateOrderStatus(orderNum, status);
    }
}
