package com.itwillbs.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.dto.WorkOrderDTO;

/**
 * 작업지시 MyBatis 매퍼 인터페이스 (네 XML과 일치)
 */
@Mapper
public interface WorkOrderMapper {
    

    // 작업지시 목록 조회 (네 XML 메서드명과 일치)
    List<WorkOrderDTO> getWorkOrderList(SearchCriteria cri);
    

    // 작업지시 총 개수 조회 (네 XML 메서드명과 일치)
    int getWorkOrderTotalCount(SearchCriteria cri);
    

    // 작업지시 상세 조회 (네 XML 메서드명과 일치)
    WorkOrderDTO getWorkOrderDetail(@Param("orderId") String orderId);
    
}