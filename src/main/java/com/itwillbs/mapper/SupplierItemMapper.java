package com.itwillbs.mapper;

import java.util.List;

import com.itwillbs.domain.SupplierItemVO;

public interface SupplierItemMapper {
	
	List<SupplierItemVO> getItemsBySupplier(String supplierId);
	

}
