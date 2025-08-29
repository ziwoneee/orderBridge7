<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>

  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

    <!-- 본문 시작 -->
    <div class="main-panel">
      <div class="content-wrapper">
        
        <!-- 페이지 헤더 -->
        <div class="page-header d-flex align-items-center justify-content-between flex-wrap">
		  <div class="d-flex align-items-center">
		    <span class="page-title-icon bg-gradient-primary text-white me-3">
		      <i class="mdi mdi-view-dashboard"></i>
		    </span>
		    <div>
		      <h3 class="page-title mb-0">운영 대시보드</h3>
		      <small class="text-muted">실시간 현황</small>
		    </div>
		  </div>
		
		  <div class="d-flex align-items-center">
		    <small id="last-refresh-time" class="text-muted me-2">-</small>
		    <button id="btn-refresh" class="btn btn-sm btn-outline-primary" title="새로고침">
		      <i class="mdi mdi-refresh"></i>
		    </button>
		  </div>
		</div>

        <!-- ================= 상단 메트릭 카드 ================= -->
        <div class="row mb-4">
          <!-- 오늘 수주 현황 -->
          <div class="col-md-3 stretch-card grid-margin">
            <div class="card dashboard-card shadow-sm">
              <div class="card-body text-center">
                <h5 class="card-title mb-4 text-dark font-weight-bold">오늘 수주 현황</h5>
                <div class="row">
                  <div class="col-6 border-right">
                    <p class="text-muted mb-3 h6">접수</p>
                    <h2 id="tile-today-req" class="mb-0 text-primary font-weight-bold">-</h2>
                  </div>
                  <div class="col-6">
                    <p class="text-muted mb-3 h6">확정</p>
                    <h2 id="tile-today-conf" class="mb-0 text-success font-weight-bold">-</h2>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 작업지시 현황 -->
          <div class="col-md-3 stretch-card grid-margin">
            <div class="card dashboard-card shadow-sm">
              <div class="card-body text-center">
                <h5 class="card-title mb-4 text-dark font-weight-bold">작업지시 현황</h5>
                <div class="row">
                  <div class="col-4">
                    <p class="text-muted mb-3 h6">대기</p>
                    <h2 id="wo-waiting" class="mb-0 text-warning font-weight-bold">-</h2>
                  </div>
                  <div class="col-4">
                    <p class="text-muted mb-3 h6">준비</p>
                    <h2 id="wo-ready" class="mb-0 text-info font-weight-bold">-</h2>
                  </div>
                  <div class="col-4">
                    <p class="text-muted mb-3 h6">진행</p>
                    <h2 id="wo-inprogress" class="mb-0 text-success font-weight-bold">-</h2>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 오늘 실적 -->
          <div class="col-md-3 stretch-card grid-margin">
            <div class="card dashboard-card shadow-sm">
              <div class="card-body text-center">
                <h5 class="card-title mb-4 text-dark font-weight-bold">오늘 실적</h5>
                <div class="row">
                  <div class="col-6 border-right">
                    <p class="text-muted mb-3 h6">완료 지시</p>
                    <h2 id="tile-completed-today" class="mb-0 text-success font-weight-bold">-</h2>
                  </div>
                  <div class="col-6">
                    <p class="text-muted mb-3 h6">생산 수량</p>
                    <h2 id="tile-prod-actual" class="mb-0 text-info font-weight-bold">-</h2>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 재고 경고 -->
          <div class="col-md-3 stretch-card grid-margin">
            <div class="card dashboard-card shadow-sm">
              <div class="card-body text-center">
                <h5 class="card-title mb-4 text-dark font-weight-bold">재고 경고</h5>
                <div class="row">
                  <div class="col-4">
                    <p class="text-muted mb-3 small">원자재부족</p>
                    <h3 id="rm-shortage" class="mb-0 text-danger font-weight-bold">-</h3>
                  </div>
                  <div class="col-4">
                    <p class="text-muted mb-3 small">원자재소진</p>
                    <h3 id="rm-exhausted" class="mb-0 text-secondary font-weight-bold">-</h3>
                  </div>
                  <div class="col-4">
                    <p class="text-muted mb-3 small">완제품부족</p>
                    <h3 id="fg-shortage" class="mb-0 text-warning font-weight-bold">-</h3>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ================= 생산라인 현황 (가로 배치) ================= -->
        <div class="row mb-4">
          <div class="col-12">
            <div class="card">
              <div class="card-body">
                <div class="d-flex justify-content-between align-items-center mb-4">
                  <h4 class="card-title mb-0 font-weight-bold">생산라인 현황</h4>
                  <span class="badge badge-outline-success">실시간</span>
                </div>
                <!-- 라인 카드들을 강제로 가로 배치 -->
                <div class="row" id="cards-lines">
                  <!-- JavaScript에서 col-lg-4 카드들로 채워짐 -->
                  <div class="col-12 text-center text-muted py-4">
                    <div class="spinner-border spinner-border-sm me-2" role="status"></div>
                    라인 정보를 불러오는 중...
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ================= 오늘 수주 현황 ================= -->
        <div class="row mb-4">
          <div class="col-12">
            <div class="card">
              <div class="card-body">
                <div class="d-flex justify-content-between align-items-center mb-4">
                  <h4 class="card-title mb-0 font-weight-bold">오늘 수주 현황</h4>
                  <span class="badge badge-outline-primary">실시간</span>
                </div>
                <div class="table-responsive">
                  <table class="table table-hover">
                    <thead class="thead-light">
                      <tr>
                        <th class="border-bottom-0">수주번호</th>
                        <th class="border-bottom-0">거래처</th>
                        <th class="border-bottom-0">주문 품목</th>
                        <th class="border-bottom-0">상태</th>
                        <th class="border-bottom-0">접수시간</th>
                      </tr>
                    </thead>
                    <tbody id="tbl-today-orders">
                      <tr>
                        <td colspan="5" class="text-center text-muted py-4">
                          <div class="spinner-border spinner-border-sm me-2" role="status"></div>
                          데이터를 불러오는 중...
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ================= 재고 관리 섹션 ================= -->
        <div class="row">
          <!-- 원자재 부족 -->
          <div class="col-lg-6 grid-margin stretch-card">
            <div class="card">
              <div class="card-body">
                <h4 class="card-title">
                  <i class="mdi mdi-alert-circle text-danger me-2"></i>원자재 부족 현황
                </h4>
                <p class="card-description">안전재고 이하 자재 목록</p>
                <div class="table-responsive">
                  <table class="table table-striped">
                    <thead>
                      <tr>
                        <th>자재코드</th>
                        <th>자재명</th>
                        <th class="text-end">현재고</th>
                        <th class="text-end">안전재고</th>
                        <th>유통기한</th>
                      </tr>
                    </thead>
                    <tbody id="tbl-rm-shortage">
                      <tr>
                        <td colspan="5" class="text-center text-muted py-3">
                          <i class="mdi mdi-loading mdi-spin me-2"></i>불러오는 중...
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>

          <!-- 원자재 소진 -->
          <div class="col-lg-6 grid-margin stretch-card">
            <div class="card">
              <div class="card-body">
                <h4 class="card-title">
                  <i class="mdi mdi-close-circle text-secondary me-2"></i>원자재 소진 현황
                </h4>
                <p class="card-description">재고가 0인 자재 목록</p>
                <div class="table-responsive">
                  <table class="table table-striped">
                    <thead>
                      <tr>
                        <th>자재코드</th>
                        <th>자재명</th>
                        <th class="text-end">현재고</th>
                        <th class="text-end">안전재고</th>
                        <th>유통기한</th>
                      </tr>
                    </thead>
                    <tbody id="tbl-rm-exhausted">
                      <tr>
                        <td colspan="5" class="text-center text-muted py-3">
                          <i class="mdi mdi-loading mdi-spin me-2"></i>불러오는 중...
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ================= 완제품 재고 ================= -->
        <div class="row">
          <div class="col-lg-12 grid-margin stretch-card">
            <div class="card">
              <div class="card-body">
                <h4 class="card-title">
                  <i class="mdi mdi-package-variant text-warning me-2"></i>완제품 부족 현황
                </h4>
                <p class="card-description">가용재고가 부족한 완제품 목록</p>
                <div class="table-responsive">
                  <table class="table table-bordered">
                    <thead class="thead-light">
                      <tr>
                        <th class="border-bottom-0">제품코드</th>
                        <th class="border-bottom-0">제품명</th>
                        <th class="border-bottom-0 text-end">가용재고</th>
                        <th class="border-bottom-0 text-end">예약수량</th>
                      </tr>
                    </thead>
                    <tbody id="tbl-fg-shortage">
                      <tr>
                        <td colspan="4" class="text-center text-muted py-3">
                          <i class="mdi mdi-loading mdi-spin me-2"></i>불러오는 중...
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>

      </div><!-- content-wrapper -->
      <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
    </div><!-- main-panel -->
  </div><!-- container-fluid -->
</div><!-- container-scroller -->

<!-- ================= 외부 CSS 파일 연결 ================= -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/admin-dashboard.css">

<!-- ================= JavaScript 설정 및 파일 연결 ================= -->
<script>
// JSP에서 생성된 URL을 JavaScript로 전달
window.DASHBOARD_CONFIG = {
  API_URL: '/admin/dashboard/data',
  CONTEXT_PATH: '${pageContext.request.contextPath}'
};

console.log('Dashboard Config:', window.DASHBOARD_CONFIG);
</script>
<script src="${pageContext.request.contextPath}/resources/js/admin-dashboard.js"></script>