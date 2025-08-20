<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>
  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

    <div class="main-panel">
      <div class="content-wrapper">
        <!-- 제목/브레드크럼 -->
        <div class="page-header">
          <h3 class="page-title"> 종합 홈 대시보드 </h3>
        </div>

        <!-- Row 1: KPI 6개 -->
        <div class="row">
          <!-- 오늘 수주 -->
          <div class="col-md-2 grid-margin stretch-card">
            <a href="${pageContext.request.contextPath}/clientOrder/list?date=today" class="text-decoration-none w-100">
              <div class="card">
                <div class="card-body">
                  <p class="card-title mb-2">오늘 수주</p>
                  <h3 class="mb-0 text-primary"><c:out value="${dash.todayOrders}" default="0"/></h3>
                </div>
              </div>
            </a>
          </div>
          <!-- 오늘 출하 예정 -->
          <div class="col-md-2 grid-margin stretch-card">
            <a href="${pageContext.request.contextPath}/clientDelivery/list?date=today" class="text-decoration-none w-100">
              <div class="card">
                <div class="card-body">
                  <p class="card-title mb-2">오늘 출하 예정</p>
                  <h3 class="mb-0 text-primary"><c:out value="${dash.todayDeliveries}" default="0"/></h3>
                </div>
              </div>
            </a>
          </div>
          <!-- 작업지시 진행중 -->
          <div class="col-md-2 grid-margin stretch-card">
            <a href="${pageContext.request.contextPath}/work-order/list?status=IN_PROGRESS" class="text-decoration-none w-100">
              <div class="card">
                <div class="card-body">
                  <p class="card-title mb-2">진행중 지시</p>
                  <h3 class="mb-0"><c:out value="${dash.todayWoInProgress}" default="0"/></h3>
                </div>
              </div>
            </a>
          </div>
          <!-- 작업지시 완료 -->
          <div class="col-md-2 grid-margin stretch-card">
            <a href="${pageContext.request.contextPath}/work-order/list?status=COMPLETED" class="text-decoration-none w-100">
              <div class="card">
                <div class="card-body">
                  <p class="card-title mb-2">완료 지시</p>
                  <h3 class="mb-0"><c:out value="${dash.todayWoCompleted}" default="0"/></h3>
                </div>
              </div>
            </a>
          </div>
          <!-- 오늘 생산(실적/계획) -->
          <div class="col-md-2 grid-margin stretch-card">
            <a href="${pageContext.request.contextPath}/production/result?date=today" class="text-decoration-none w-100">
              <div class="card">
                <div class="card-body">
                  <p class="card-title mb-2">오늘 생산 (실적/계획)</p>
                  <h4 class="mb-0">
                    <c:out value="${dash.todayActualQty}" default="0"/> /
                    <c:out value="${dash.todayPlanQty}" default="0"/>
                  </h4>
                </div>
              </div>
            </a>
          </div>
          <!-- 자재 부족 품목 -->
          <div class="col-md-2 grid-margin stretch-card">
            <a href="${pageContext.request.contextPath}/material/inventory?filter=shortage" class="text-decoration-none w-100">
              <div class="card">
                <div class="card-body">
                  <p class="card-title mb-2">자재 부족 품목</p>
                  <h3 class="mb-0 text-danger"><c:out value="${dash.materialShortageCount}" default="0"/></h3>
                </div>
              </div>
            </a>
          </div>
        </div>

        <!-- Row 2: 테이블 2개 -->
        <div class="row">
          <!-- 자재 부족 TOP5 -->
          <div class="col-md-6 grid-margin stretch-card">
            <div class="card">
              <div class="card-body">
                <h4 class="card-title">자재 부족 TOP5</h4>
                <div class="table-responsive">
                  <table class="table">
                    <thead>
                      <tr><th>자재</th><th class="text-right">필요</th><th class="text-right">가용</th><th class="text-right">부족</th></tr>
                    </thead>
                    <tbody>
                      <c:choose>
                        <c:when test="${not empty dash.topShortages}">
                          <c:forEach var="it" items="${dash.topShortages}">
                            <tr>
                              <td>${it.materialName} (${it.unit})</td>
                              <td class="text-right">${it.requiredQty}</td>
                              <td class="text-right">${it.availableQty}</td>
                              <td class="text-right text-danger font-weight-bold">${it.shortageQty}</td>
                            </tr>
                          </c:forEach>
                        </c:when>
                        <c:otherwise>
                          <tr><td colspan="4" class="text-center text-muted">부족 품목이 없습니다.</td></tr>
                        </c:otherwise>
                      </c:choose>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>

          <!-- 납기 위험 TOP5 -->
          <div class="col-md-6 grid-margin stretch-card">
            <div class="card">
              <div class="card-body">
                <h4 class="card-title">납기 위험 TOP5</h4>
                <div class="table-responsive">
                  <table class="table">
                    <thead>
                      <tr><th>주문/거래처</th><th>제품</th><th>납기</th><th>진행</th></tr>
                    </thead>
                    <tbody>
                      <c:choose>
                        <c:when test="${not empty dash.topDueRisks}">
                          <c:forEach var="it" items="${dash.topDueRisks}">
                            <tr onclick="location.href='${pageContext.request.contextPath}/work-order/list?orderId=${it.orderId}'" style="cursor:pointer">
                              <td>${it.orderId} / ${it.clientName}</td>
                              <td>${it.productName}</td>
                              <td><span class="badge badge-outline-primary">${it.dueDate}</span></td>
                              <td><span class="badge badge-danger">${it.progressPct}%</span></td>
                            </tr>
                          </c:forEach>
                        </c:when>
                        <c:otherwise>
                          <tr><td colspan="4" class="text-center text-muted">위험 주문이 없습니다.</td></tr>
                        </c:otherwise>
                      </c:choose>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Row 3: 7일 계획 vs 실적 차트 -->
        <div class="row">
          <div class="col-md-12 grid-margin stretch-card">
            <div class="card">
              <div class="card-body">
                <h4 class="card-title">최근 7일 계획 vs 실적</h4>
                <canvas id="weeklyChart" height="90"></canvas>
              </div>
            </div>
          </div>
        </div>

      </div>
      <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

<script>
(function() {
  // 서버 바인딩 데이터 → 차트용
  const plan = [
    <c:forEach var="d" items="${dash.weeklyPlanSeries}" varStatus="s">
      {x:'${d.ymd}', y:${d.qty}}<c:if test="${!s.last}">,</c:if>
    </c:forEach>
  ];
  const actual = [
    <c:forEach var="d" items="${dash.weeklyActualSeries}" varStatus="s">
      {x:'${d.ymd}', y:${d.qty}}<c:if test="${!s.last}">,</c:if>
    </c:forEach>
  ];

  const ctx = document.getElementById('weeklyChart').getContext('2d');
  new Chart(ctx, {
    type: 'line',
    data: { datasets: [
      { label: '계획', parsing:{xAxisKey:'x', yAxisKey:'y'}, data: plan },
      { label: '실적', parsing:{xAxisKey:'x', yAxisKey:'y'}, data: actual }
    ]},
    options: {
      responsive: true,
      interaction: { intersect:false, mode:'index' },
      scales: { x: { type:'time', time:{ unit:'day' } }, y: { beginAtZero:true } }
    }
  });
})();
</script>
