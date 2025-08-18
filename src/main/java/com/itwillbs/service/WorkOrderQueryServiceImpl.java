package com.itwillbs.service;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.itwillbs.dto.WorkOrderLiteDTO;
import com.itwillbs.mapper.WorkOrderQueryMapper;

@Service
public class WorkOrderQueryServiceImpl implements WorkOrderQueryService {
	
	@Inject
    private WorkOrderQueryMapper mapper;

    @Override
    public List<WorkOrderLiteDTO> findEligibleForEta(String q, int limit) {
        // q가 공백이면 null로 넘겨서 동적 where에서 무시
        String keyword = (q != null && q.trim().length() > 0) ? q.trim() : null;
        int cap = (limit <= 0 || limit > 500) ? 200 : limit;
        return mapper.selectEligibleForEta(keyword, cap);
    }

    @Override
    public Optional<WorkOrderLiteDTO> findOne(String orderId) {
        return Optional.ofNullable(mapper.selectLiteById(orderId));
    }

}
