// com.itwillbs.dto.DashboardDTO
package com.itwillbs.dto;
import java.util.List;
import lombok.Data;

@Data
public class DashboardDTO {
  // KPI
  private int todayOrders;
  private int todayDeliveries;
  private int todayWoInProgress;
  private int todayWoCompleted;
  private int todayPlanQty;
  private int todayActualQty;
  private int materialShortageCount;

  // 리스트
  private List<MaterialShortageItem> topShortages;
  private List<DueRiskItem> topDueRisks;

  // 차트
  private List<DailyQty> weeklyPlanSeries;
  private List<DailyQty> weeklyActualSeries;
}

@Data public class DailyQty { private String ymd; private int qty; }
@Data public class MaterialShortageItem {
  private String materialId, materialName, unit;
  private int requiredQty, availableQty, shortageQty;
}
@Data public class DueRiskItem {
  private String orderId, clientName, productName, dueDate;
  private int progressPct; // 0~100
}
