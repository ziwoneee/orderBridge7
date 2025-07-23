package com.itwillbs.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {


    @Override
    public int getTodayPlanCount() {
        // 실제로는 DB 조회 필요
        return 3; // 예시: 오늘 생산 계획 3건
    }

    @Override
    public int getShortageMaterialCount() {
        // 실제 자재 재고 테이블에서 부족 수량 계산 필요
        return 2; // 예시: 2종 자재 부족
    }

    @Override
    public int getDelayedDeliveryCount() {
        // 납기 지연 예상 건수 조회 필요
        return 1; // 예시: 1건 지연 예상
    }

    @Override
    public List<String> getMonthLabels() {
        // 예시: 최근 6개월
        return Arrays.asList("3월", "4월", "5월", "6월", "7월", "8월");
    }

    @Override
    public List<Integer> getMonthlyPlanCounts() {
        // 예시: 각 월별 생산 계획 수량
        return Arrays.asList(20, 25, 18, 30, 22, 27);
    }
    
}
