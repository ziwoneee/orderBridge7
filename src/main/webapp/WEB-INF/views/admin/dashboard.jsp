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
        <div class="page-header">
          <h3 class="page-title">
            <span class="page-title-icon bg-gradient-primary text-white me-2">
              <i class="mdi mdi-view-dashboard"></i>
            </span>
            운영 대시보드
          </h3>
          <nav aria-label="breadcrumb">
            <ul class="breadcrumb">
              <li class="breadcrumb-item active" aria-current="page">
                <span></span>실시간 현황 
              </li>
            </ul>
          </nav>
          <!-- 마지막 업데이트 시간 표시 -->
          <div class="text-muted">
            <span id="last-refresh-time">-</span>
            <button id="btn-refresh" class="btn btn-sm btn-outline-primary ms-2" title="새로고침">
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
                  <!-- JavaScript에서 col-md-4 카드들로 채워짐 -->
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

<!-- ================= CSS 스타일 ================= -->
<style>
/* 전체 배경 */
.content-wrapper {
  background: #f8f9fa !important;
}

/* 메인 카드 스타일 */
.dashboard-card {
  background: #ffffff !important;
  border: 1px solid #e3e6f0 !important;
  border-radius: 15px !important;
  transition: all 0.3s ease !important;
  min-height: 180px;
}

.dashboard-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.1) !important;
}

/* 페이지 헤더 */
.page-header {
  background: #ffffff !important;
  border-radius: 15px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  border: 1px solid #e3e6f0;
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}

.page-title {
  color: #5a5c69 !important;
  font-weight: 600;
  font-size: 1.5rem !important;
}

.page-title-icon {
  background: linear-gradient(45deg, #4e73df, #224abe) !important;
  border-radius: 10px;
  padding: 10px;
}

/* 카드 기본 스타일 */
.card {
  background: #ffffff !important;
  border: 1px solid #e3e6f0 !important;
  border-radius: 15px !important;
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075) !important;
}

.card-title {
  color: #5a5c69 !important;
  font-weight: 600 !important;
  font-size: 1.2rem !important;
}

.card-description {
  color: #858796 !important;
  font-size: 1rem;
}

/* 숫자 강조 - 크게 */
h1, h2, h3 {
  font-weight: 700 !important;
}

h2 {
  font-size: 2.8rem !important;
}

h3 {
  font-size: 2.2rem !important;
}

/* 텍스트 크기 증가 */
p, .text-muted {
  font-size: 1rem !important;
}

.h6 {
  font-size: 1.1rem !important;
}

/* 라인 카드 스타일 - 가로 배치용 */
.line-card {
  background: #ffffff !important;
  border: 1px solid #e3e6f0 !important;
  border-radius: 12px !important;
  padding: 1.5rem !important;
  margin-bottom: 1rem;
  transition: all 0.3s ease !important;
  height: 100%;
  min-height: 200px;
}

.line-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 0.25rem 0.5rem rgba(0, 0, 0, 0.1) !important;
}

/* 라인 상태별 배경 */
.line-active {
  border-left: 5px solid #1cc88a !important;
  background: rgba(28, 200, 138, 0.05) !important;
}

.line-ready {
  border-left: 5px solid #36b9cc !important;
  background: rgba(54, 185, 204, 0.05) !important;
}

.line-waiting {
  border-left: 5px solid #f6c23e !important;
  background: rgba(246, 194, 62, 0.05) !important;
}

.line-idle {
  border-left: 5px solid #858796 !important;
  background: rgba(133, 135, 150, 0.05) !important;
}

/* 테이블 스타일 */
.table {
  color: #5a5c69 !important;
  background: #ffffff !important;
  font-size: 1rem !important;
}

.table th {
  background: #f8f9fc !important;
  color: #5a5c69 !important;
  border: none !important;
  font-weight: 600 !important;
  font-size: 1rem !important;
  padding: 1rem 0.75rem;
}

.table td {
  border: 1px solid #e3e6f0 !important;
  padding: 1rem 0.75rem;
  vertical-align: middle;
  font-size: 1rem !important;
}

.table-hover tbody tr:hover {
  background: #f8f9fc !important;
}

/* 배지 스타일 */
.badge {
  font-size: 0.875rem !important;
  padding: 0.5rem 1rem !important;
  border-radius: 20px;
  font-weight: 500;
}

.badge-primary { background: #4e73df !important; color: white !important; }
.badge-success { background: #1cc88a !important; color: white !important; }
.badge-info { background: #36b9cc !important; color: white !important; }
.badge-warning { background: #f6c23e !important; color: white !important; }
.badge-danger { background: #e74a3b !important; color: white !important; }
.badge-secondary { background: #858796 !important; color: white !important; }

.badge-outline-primary { background: transparent !important; border: 2px solid #4e73df !important; color: #4e73df !important; }
.badge-outline-success { background: transparent !important; border: 2px solid #1cc88a !important; color: #1cc88a !important; }

/* 프로그레스 바 */
.progress {
  background: #eaecf4 !important;
  border-radius: 10px !important;
  height: 12px !important;
}

.progress-bar {
  border-radius: 10px !important;
  background: linear-gradient(90deg, #4e73df 0%, #224abe 100%) !important;
}

.bg-success { background: linear-gradient(90deg, #1cc88a 0%, #13855c 100%) !important; }
.bg-info { background: linear-gradient(90deg, #36b9cc 0%, #258391 100%) !important; }
.bg-warning { background: linear-gradient(90deg, #f6c23e 0%, #d4a00a 100%) !important; }

/* 텍스트 색상 */
.text-primary { color: #4e73df !important; }
.text-success { color: #1cc88a !important; }
.text-info { color: #36b9cc !important; }
.text-warning { color: #f6c23e !important; }
.text-danger { color: #e74a3b !important; }
.text-muted { color: #858796 !important; }
.text-dark { color: #5a5c69 !important; }

/* 반응형 */
@media (max-width: 768px) {
  .dashboard-card {
    margin-bottom: 1rem;
  }
  
  h2 {
    font-size: 2rem !important;
  }
  
  h3 {
    font-size: 1.5rem !important;
  }
  
  .card-body {
    padding: 1.25rem;
  }
  
  .page-header {
    padding: 1rem;
  }
}

/* 애니메이션 */
.fade-in {
  animation: fadeInUp 0.5s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

<!-- ================= JavaScript 파일 ================= -->
<script>
// JSP에서 생성된 URL을 JavaScript로 전달
window.DASHBOARD_CONFIG = {
  API_URL: '/admin/dashboard/data',
  CONTEXT_PATH: '${pageContext.request.contextPath}'
};

console.log('Dashboard Config:', window.DASHBOARD_CONFIG);
</script>
<script src="${pageContext.request.contextPath}/resources/js/admin-dashboard.js"></script>