// 경로: com.itwillbs.service.ProductionLineServiceImpl.java
package com.itwillbs.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itwillbs.domain.ProductionLineVO;
import com.itwillbs.mapper.ProductionLineMapper;

@Service
public class ProductionLineServiceImpl implements ProductionLineService {

    @Autowired
    private ProductionLineMapper productionLineMapper;

    /**
     * 사용 가능한 생산 라인 전체 조회
     */
    @Override
    public List<ProductionLineVO> getAvailableLines() {
        return productionLineMapper.selectAvailableLines();
    }

}
