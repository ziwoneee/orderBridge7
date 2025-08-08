package com.itwillbs.service;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import com.itwillbs.domain.SupplierItemVO;
import com.itwillbs.persistence.SupplierItemDAO;


/**
 * 공급 품목 서비스 구현체
 */
@Service
public class SupplierItemServiceImpl implements SupplierItemService {
	
	
	@Inject
    private SupplierItemDAO siDAO;


	// 특정 협력사의 공급 품목 전체 조회
	@Override
	public List<SupplierItemVO> getSuppliedItemsBySupplierId(String supplierId) {
		
		return siDAO.selectSuppliedItemsBySupplierId(supplierId);
	}
	
	// 특정 거래처의 공급 품목 JSON 목록 반환
	@Override
	public List<SupplierItemVO> getItemsBySupplier(String supplierId) throws Exception {
		
	    return siDAO.getItemsBySupplier(supplierId);
	}

	
	 // 공급 품목 등록
	@Override
    public void registerItem(SupplierItemVO item) throws Exception {
        // DAO를 통해 DB에 저장
		siDAO.insertItem(item);
    }
	
	
	// 공급 품목 수정
	@Override
	public void updateItem(SupplierItemVO item) throws Exception {
		siDAO.updateItem(item);
	}
	
	// 공급 품목 단건 조회 (수정폼용)
	@Override
	public SupplierItemVO getItemById(String itemId) throws Exception {
	    return siDAO.getItemById(itemId);
	}
	
	
}
