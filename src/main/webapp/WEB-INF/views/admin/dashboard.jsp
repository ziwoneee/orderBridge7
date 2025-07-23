<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">

  <%@ include file="/WEB-INF/views/main/top.jsp" %>      

  <div class="container-fluid page-body-wrapper">

    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

	<!-- 대시보드 시작 -->
	<div class="main-panel">
	  <div class="content-wrapper">
	   <div class="row">

		  <div class="col-12 mb-4">
		    <h3 class="font-weight-bold">홈 대시보드</h3>
		  </div>
	
		    <!-- 오늘의 생산 계획 -->
		    <div class="col-md-4 grid-margin stretch-card">
		      <div class="card text-white bg-primary">
		        <div class="card-body">
		          <h4 class="card-title">오늘의 생산 계획</h4>
		          <p class="mb-0">총 ${todayPlanCount}건</p>
		        </div>
		      </div>
		    </div>
		
		    <!-- 자재 재고 요약 -->
		    <div class="col-md-4 grid-margin stretch-card">
		      <div class="card text-dark bg-warning">
		        <div class="card-body">
		          <h4 class="card-title">자재 재고 요약</h4>
		          <p class="mb-0">부족 자재 ${shortageCount}건</p>
		        </div>
		      </div>
		    </div>
		
		    <!-- 납기 예측 결과 -->
		    <div class="col-md-4 grid-margin stretch-card">
		      <div class="card text-white bg-danger">
		        <div class="card-body">
		          <h4 class="card-title">납기 예측 결과</h4>
		          <p class="mb-0">지연 예상 ${delayedCount}건</p>
		        </div>
		      </div>
		    </div>
		  </div>
		
		  <!-- 차트 영역 -->
		  <div class="row">
		    <div class="col-md-12 grid-margin stretch-card">
		      <div class="card">
		        <div class="card-body">
		          <h4 class="card-title">월별 생산 계획 차트</h4>
		          <canvas id="productionChart"></canvas>
		        </div>
		      </div>
		    </div>
		  </div>
	   </div>

	<!-- Chart.js 로딩 -->
	<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
	<script>
	  // 월별 라벨 배열 생성
	  const labels = [
	    <c:forEach var="label" items="${monthLabels}" varStatus="status">
	      '${label}'<c:if test="${!status.last}">,</c:if>
	    </c:forEach>
	  ];
	
	  // 월별 생산 계획 수 배열 생성
	  const data = [
	    <c:forEach var="count" items="${monthlyPlanCounts}" varStatus="status">
	      ${count}<c:if test="${!status.last}">,</c:if>
	    </c:forEach>
	  ];
	
	  const ctx = document.getElementById('productionChart').getContext('2d');
	  const productionChart = new Chart(ctx, {
	    type: 'bar',
	    data: {
	      labels: labels,
	      datasets: [{
	        label: '생산 계획 수',
	        data: data,
	        backgroundColor: 'rgba(54, 162, 235, 0.6)',
	      }]
	    },
	    options: {
	      responsive: true,
	      scales: {
	        y: { beginAtZero: true }
	      }
	    }
	  });
	</script>

        <!-- content-wrapper 끝 -->
	  <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
     </div>
     <!-- 본문.jsp main-panel ends -->
  </div>   
  <!-- container-fluid page-body-wrapper 끝 -->
</div>
<!-- container-scroller 끝-->