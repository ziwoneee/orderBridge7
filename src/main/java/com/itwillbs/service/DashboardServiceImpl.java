// com.itwillbs.service.DashboardServiceImpl
package com.itwillbs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.itwillbs.dto.DashboardDTO;
import com.itwillbs.mapper.DashboardMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

  @Autowired private DashboardMapper mapper;

  @Override
  public DashboardDTO getDashboard() {
    DashboardDTO d = new DashboardDTO();
    // KPI
    d.setTodayOrders(mapper.countTodayOrders());
    d.setTodayDeliveries(mapper.countTodayDeliveries());
    d.setTodayWoInProgress(mapper.countTodayWoInProgress());
    d.setTodayWoCompleted(mapper.countTodayWoCompleted());
    d.setTodayPlanQty(mapper.sumTodayPlanQty());
    d.setTodayActualQty(mapper.sumTodayActualQty());
    d.setMaterialShortageCount(mapper.countMaterialShortageToday());

    // 리스트
    d.setTopShortages(mapper.selectTopShortages(5));
    d.setTopDueRisks(mapper.selectTopDueRisks(5));

    // 차트
    d.setWeeklyPlanSeries(mapper.selectWeeklyPlanSeries());
    d.setWeeklyActualSeries(mapper.selectWeeklyActualSeries());
    return d;
  }
}
