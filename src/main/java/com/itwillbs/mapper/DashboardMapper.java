// com.itwillbs.mapper.DashboardMapper
package com.itwillbs.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.itwillbs.dto.*;

public interface DashboardMapper {
  // KPI
  int countTodayOrders();
  int countTodayDeliveries();
  int countTodayWoInProgress();
  int countTodayWoCompleted();
  int sumTodayPlanQty();
  int sumTodayActualQty();
  int countMaterialShortageToday();

  // 리스트
  List<MaterialShortageItem> selectTopShortages(@Param("limit") int limit);
  List<DueRiskItem> selectTopDueRisks(@Param("limit") int limit);

  // 차트
  List<DailyQty> selectWeeklyPlanSeries();
  List<DailyQty> selectWeeklyActualSeries();
}
