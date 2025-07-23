package com.itwillbs.service;

import java.util.List;


/**
 * 관리자 대시보드에서 필요한 통계 데이터를 제공하는 인터페이스
 */

public interface DashboardService {

	// 오늘 생산 계획 수 조회
    int getTodayPlanCount();

    // 자재 부족 건수 조회
    int getShortageMaterialCount();

    // 납기 지연 건수 조회
    int getDelayedDeliveryCount();

    // 월별 라벨 (예: ["1월", "2월", ...])
    List<String> getMonthLabels();

    // 월별 생산 계획 수 (예: [30, 45, 60, ...])
    List<Integer> getMonthlyPlanCounts();
    
}