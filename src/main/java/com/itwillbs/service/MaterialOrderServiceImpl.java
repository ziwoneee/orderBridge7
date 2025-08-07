package com.itwillbs.service;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.itwillbs.domain.MaterialOrderItemVO;
import com.itwillbs.domain.MaterialOrderVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.MaterialOrderDTO;
import com.itwillbs.dto.SupplierItemDTO;
import com.itwillbs.persistence.MaterialOrderDAO;

/**
 * 자재 발주 서비스 구현체
 * - DAO를 호출하여 비즈니스 로직 처리
 */
@Service
public class MaterialOrderServiceImpl implements MaterialOrderService {
	
	@Inject
	private MaterialOrderDAO mOrderDAO;

	// 발주 목록 조회
	@Override
    public List<MaterialOrderVO> getOrderList(SearchCriteria cri) throws Exception {
        return mOrderDAO.getOrderList(cri);
    }

	// 총 건수 조회 (페이징)
    @Override
    public int getTotalCount(SearchCriteria cri) throws Exception {
        return mOrderDAO.getTotalCount(cri);
    }

    
    // 발주 등록
    @Override
    public void insertOrder(MaterialOrderDTO orderDTO) throws Exception {
    	
    	// 1. 납기일 유효성 검사
    	java.util.Date today = new java.util.Date();
    	java.util.Date expectedDate = orderDTO.getOrder().getExpectedArrivedDate();
    	
        if (expectedDate.before(today)) {
            throw new IllegalArgumentException("납기일은 오늘 이후여야 합니다.");
        }

        
        // 1. 발주번호 생성
        String newOrderId = mOrderDAO.generateOrderId();
        orderDTO.getOrder().setOrderId(newOrderId);

        // 2. order 테이블 insert
        mOrderDAO.insertOrder(orderDTO.getOrder());

        // 3. order_item 테이블 insert (for each)
        int index = 1;
        for (MaterialOrderItemVO item : orderDTO.getOrderItems()) {
            item.setOrderId(newOrderId); // 외래키 설정

            // ✅ 자동 생성되는 order_item_id
            String itemId = newOrderId + "-" + index;
            item.setOrderItemId(itemId);
            index++;

            mOrderDAO.insertOrderItem(item);
        }

    }
	
    
    // 자재명으로 거래처 검색
    @Override
    public List<SupplierItemDTO> searchSuppliersByMaterial(String keyword) {
        return mOrderDAO.searchSuppliersByMaterial(keyword);
    }

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
