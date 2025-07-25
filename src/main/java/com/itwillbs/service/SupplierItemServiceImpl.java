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
	
	

}
