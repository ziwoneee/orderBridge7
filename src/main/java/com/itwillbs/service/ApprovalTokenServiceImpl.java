package com.itwillbs.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itwillbs.domain.ApprovalTokenVO;
import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.persistence.ApprovalTokenDAO;
import com.itwillbs.persistence.MaterialOrderDAO;

@Service
public class ApprovalTokenServiceImpl implements ApprovalTokenService {

    @Autowired
    private ApprovalTokenDAO approvalTokenDAO;  

    @Autowired
    private MaterialOrderDAO orderDAO;

    @Override
    public ApprovalTokenVO findByTokenId(String tokenId) {
        return approvalTokenDAO.findByTokenId(tokenId);
    }

    @Override
    public boolean approve(String tokenId) {
        ApprovalTokenVO token = approvalTokenDAO.findByTokenId(tokenId);

        if (token == null || token.isUsed() || token.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            return false;
        }

       
        MaterialOrderVO vo = new MaterialOrderVO();
        vo.setOrderId(token.getOrderId());
        vo.setOrderStatus("승인"); 

        approvalTokenDAO.markTokenUsed(tokenId);
        orderDAO.updateOrderStatus(vo);  
        return true;
    }

    @Override
    public boolean reject(String tokenId) {
        ApprovalTokenVO token = approvalTokenDAO.findByTokenId(tokenId);

        if (token == null || token.isUsed() || token.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            return false;
        }

        // 거절 처리
        approvalTokenDAO.markTokenUsed(tokenId);

        
        MaterialOrderVO vo = new MaterialOrderVO();
        vo.setOrderId(token.getOrderId());
        vo.setOrderStatus("거절");

        orderDAO.updateOrderStatus(vo);

        return true;
    }

}


