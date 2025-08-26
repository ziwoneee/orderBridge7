package com.itwillbs.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.itwillbs.domain.SupplierItemVO;

public interface SupplierItemMapper {
	
	// SupplierItemMapper.java
	List<Map<String, Object>> getItemsBySupplier(@Param("supplierId") String supplierId);

	

}
