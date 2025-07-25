package com.itwillbs.service;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.itwillbs.domain.MaterialOutboundVO;
import com.itwillbs.persistence.MaterialOutboundDAO;

@Service
public class MaterialOutboundServiceImpl implements MaterialOutboundService {

	@Inject
	private MaterialOutboundDAO moDAO;
	
	@Override
	public List<MaterialOutboundVO> getOutboundList() throws Exception {
		return null;
	}
	
	

}
