<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- ✅ 최소한의 보조 스타일 -->
<style>
  .kv { display:flex; gap:.75rem; align-items:center; }
  .kv .key { min-width: 84px; color:#6c757d; font-size:.9rem; }
  .kv .val { flex:1; }
  
  

  .modal-content { opacity: 1 !important; filter: none !important; }  

/* 모달 내부 전용 스코프 */
.production-modal * { box-sizing: border-box; }

/* 제목 바(모달 타이틀 영역이 아니라 본문 첫 카드/섹션 타이틀용) */
.production-modal .section-title {
  display:flex; align-items:center; gap:.5rem;
  font-weight:700; font-size:1rem; color:#0f172a;
  margin:0 0 .75rem 0;
}
.production-modal .section-title::before{
  content:""; width:6px; height:18px; border-radius:4px; 
  background:#0d6efd; /* 포인트 색 */
}

/* 카드 대비 강화 */
.production-modal .card {
  border:1px solid #cbd5e1 !important;            /* 선명한 테두리 */
  box-shadow:0 6px 18px rgba(2,6,23,.10) !important; /* 강한 음영 */
  border-radius:14px;
}

/* 헤더 카드(요약 바)는 진한 배경 + 흰 글씨 */
.production-modal .card.card--header .card-body {
  background:#1f3a5f; color:#fff;
  border-radius:12px;
}

/* 섹션 헤더 바 (작업 시간 등) */
.production-modal .card-header {
  background:#1f3a5f !important; color:#fff !important;
  border-bottom:none; font-weight:700;
}

/* 레이블/값 가독성 */
.production-modal .kv .key { color:#334155; font-weight:600; min-width:84px; }
.production-modal .kv .val { color:#0f172a; }

/* 구분선 */
.production-modal .divider { height:1px; background:#d1d5db; margin:12px 0; }

/* 혹시 상위 투명도 있으면 제거 */
.modal-content { opacity:1 !important; filter:none !important; }
</style>
  


<% java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
    String today = sdf.format(new java.util.Date());%>

<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">

  <%@ include file="/WEB-INF/views/main/top.jsp" %>      

  <div class="container-fluid page-body-wrapper">

    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

      <!-- 본문 시작 -->
      <div class="main-panel">
        <div class="content-wrapper">
          <div class="row">
			
			<!-- 제목 -->
			<div class="col-12 mb-4">
			  <h3 class="font-weight-bold">입고 내역 리스트</h3>
			</div>


    <!-- ✅ 검색 & 필터 -->
    <div class="d-flex justify-content-between mb-3">
    <form method="get" class="form-inline mb-4">
    
    <select name="sortColumn" class="form-control mr-2">
            <option value="all" ${cri.sortColumn eq 'all' ? 'selected' : ''}>전체</option>
            <option value="product_id" ${cri.sortColumn eq 'product_id' ? 'selected' : ''}>제품ID</option>
            <option value="product_name" ${cri.sortColumn eq 'product_name' ? 'selected' : ''}>제품명</option>
            <option value="lot_no" ${cri.sortColumn eq 'lot_no' ? 'selected' : ''}>LOT번호</option>
        </select>
        <input type="text" name="keyword" value="${cri.keyword}" class="form-control mr-2" placeholder="제품명 ,ID 또는 LOT 검색">
	<input type="date" name="startDate" value="${cri.startDate}" class="form-control mr-2" max="<%= today %>">
	<span class="mx-1">~</span>
	<input type="date" name="endDate" value="${cri.endDate}" class="form-control mr-2" max="<%= today %>">
        
               
        
       <button type="submit" class="btn btn-primary me-2" >
                      <i class="ti-search"></i> 검색
                    </button>
        <a href="/product/inbound/list" class="btn btn-light">
          <i class="ti-reload"></i> 초기화
        </a>
    </form>
    </div>
    
      <!-- 자동입고 버튼 -->
    <form method="post" action="${pageContext.request.contextPath}/product/inbound/saveFromProduction">
        <!-- Hidden 필드 전달 -->
        <input type="hidden" name="keyword" value="${cri.keyword}">
        <input type="hidden" name="startDate" value="${cri.startDate}">
        <input type="hidden" name="endDate" value="${cri.endDate}">
        <input type="hidden" name="sortColumn" value="${cri.sortColumn}">
        <input type="hidden" name="sortOrder" value="${cri.sortOrder}">
        <input type="hidden" name="page" value="${cri.page}">
        
        <button type="submit" class="btn btn-success ml-5">+ 자동입고 업데이트</button>
    </form>

   <div row>
    <c:if test="${not empty msg}">
    <div class="alert alert-warning text-center">${msg}</div>
</c:if>
    </div>

    <!-- ✅ 테이블 -->
     <div class="table-responsive mt-4">
    <table id="inboundTable" class="table table-hover text-center">
       <thead>
<tr>
  <th>입고ID</th>
  <th>제품ID</th>
  
  <!-- ✅ 제품명 정렬 -->
  <th>
  <a href="?page=1&sortColumn=productname&sortOrder=${cri.sortColumn eq 'productname' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}">
    제품명
    <c:choose>
      <c:when test="${cri.sortColumn eq 'productname'}">
        <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
      </c:when>
      <c:otherwise>
        <span class="neutral-arrow">⇅</span>
      </c:otherwise>
    </c:choose>
  </a>
</th>


  <th>LOT번호</th>
  <th>수량</th>

  <!-- ✅ 입고일자 정렬 -->
<th>
  <a href="?page=1&sortColumn=createdat&sortOrder=${cri.sortColumn eq 'createdat' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}">
    입고일자
    <c:choose>
      <c:when test="${cri.sortColumn eq 'createdat'}">
        <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
      </c:when>
      <c:otherwise>
        <span class="neutral-arrow">⇅</span>
      </c:otherwise>
    </c:choose>
  </a>
</th>


  <th>입고유형</th>

  <!-- ✅ 담당자 정렬 -->
<th>
  <a href="?page=1&sortColumn=manager&sortOrder=${cri.sortColumn eq 'manager' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}">
    담당자
    <c:choose>
      <c:when test="${cri.sortColumn eq 'manager'}">
        <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
      </c:when>
      <c:otherwise>
        <span class="neutral-arrow">⇅</span>
      </c:otherwise>
    </c:choose>
  </a>
</th>

  <th>비고</th>
  <th>상세내역</th> <!-- 출고내역과 동일하게 상세내역 컬럼 추가 -->
</tr>
</thead>

        <tbody>
        <c:forEach var="vo" items="${inboundList}">
            <tr>
                <td>${vo.inboundId}</td>
                <td>${vo.productId}</td>
                <td>${vo.productName}</td>                
                <td>${vo.lotNo}</td>
                <td><fmt:formatNumber value="${vo.inboundQty}" pattern="#,###"/></td>
                <td><fmt:formatDate value="${vo.createdAt}" pattern="yyyy-MM-dd"/></td>
                <td>${vo.inboundType}</td>
                <td>${vo.manager}</td>
                <td>${vo.remark}</td>
                <td>
                    <!-- 입고 상세보기 버튼 (출고내역과 동일한 스타일) -->
                  <button type="button"
        class="btn btn-sm btn-outline-info open-result-modal"
        data-resultid="${vo.resultId}"
        data-lot="${vo.lotNo}">
  상세
</button>



                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>


      <!--입고 상세 모달 영역 -->
<div class="modal fade" id="resultDetailModal" tabindex="-1" aria-hidden="true">
 <div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">

     <div class="modal-header bg-primary text-white">
        <h5 class="modal-title">생산 실적 상세</h5>
               <button type="button" class="close text-white" data-dismiss="modal">&times;</button>

      </div>
<div class="modal-body">

 <div class="modal-body production-modal"><!-- ← 스코프 클래스 추가 -->

  <!-- ✅ 1) 헤더 카드: 기본 정보 요약 (진한 바 느낌) -->
  <div class="card card--header shadow-lg mb-3">
    <div class="card-body py-3">
      <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
        <div class="d-flex align-items-center flex-wrap gap-3">       

          <div class="d-flex align-items-center gap-2 text-white">
            <span class="fw-semibold">실적번호</span>
            <strong id="rd-resultId" class="ms-1"></strong>
            <span class="vr mx-2 d-none d-md-inline bg-white opacity-50" style="width:1px;height:16px;"></span>
            <span class="fw-semibold">작업지시</span>
            <strong id="rd-orderId" class="ms-1"></strong>
          </div>
        </div>

        <div class="text-end">
          <div class="small text-white-50">등록일시</div>
          <div id="rd-createdAt" class="fw-semibold text-white"></div>
        </div>
      </div>
    </div>
  </div>

  <!-- ✅ 2) 그리드: 제품/라인/작업자/LOT -->
  <div class="row g-3 mb-3">
    <div class="col-12 col-md-6">
      <div class="card shadow-sm h-100">
        <div class="card-body py-3">
          <div class="section-title">기본 정보</div>
          <div class="kv"><div class="key">제품명</div><div id="rd-productName" class="val fw-semibold"></div></div>
          <div class="kv mt-2"><div class="key">라인명</div><div id="rd-lineName" class="val"></div></div>
        </div>
      </div>
    </div>

    <div class="col-12 col-md-6">
      <div class="card shadow-sm h-100">
        <div class="card-body py-3">
          <div class="section-title">작업/LOT</div>
          <div class="kv"><div class="key">작업자명</div><div id="rd-workerName" class="val"></div></div>
          <div class="kv mt-2"><div class="key">LOT번호</div><div id="rd-lotNo" class="val"></div></div>
        </div>
      </div>
    </div>
  </div>

  <!-- ✅ 3) KPI 카드 + 달성률 -->
  <div class="row g-3 mb-3">
    <div class="col-6 col-lg-3">
      <div class="card text-center shadow-sm h-100">
        <div class="card-body py-3">
          <div class="small fw-semibold text-dark">계획수량</div>
          <div id="rd-orderQty" class="display-6 fw-bold text-dark"></div>
        </div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="card text-center shadow-sm h-100">
        <div class="card-body py-3">
          <div class="small fw-semibold text-dark">생산수량</div>
          <div id="rd-actualQty" class="display-6 fw-bold text-dark"></div>
        </div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="card text-center shadow-sm h-100">
        <div class="card-body py-3">
          <div class="small fw-semibold text-dark">불량수량</div>
          <div id="rd-defectQty" class="display-6 fw-bold text-dark"></div>
        </div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="card shadow-sm h-100">
        <div class="card-body py-3">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <div class="small fw-semibold text-dark">달성률</div>
            <strong id="rd-achievement" class="small text-dark">0%</strong>
          </div>
          <div class="progress">
            <div id="rd-achievementBar" class="progress-bar" style="width:0%"></div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- ✅ 4) 작업 시간 (구분이 확 되는 진한 헤더) -->
  <div class="card shadow">
    <div class="card-header py-2">
      <i class="bi bi-clock-history me-1"></i> 작업 시간
    </div>
    <div class="card-body py-3">
      <div class="row g-3">
        <div class="col-12 col-md-6">
          <div class="kv"><div class="key">작업 시작</div><div id="rd-startedAt" class="val fw-semibold"></div></div>
        </div>
        <div class="col-12 col-md-6">
          <div class="kv"><div class="key">작업 종료</div><div id="rd-endedAt" class="val fw-semibold"></div></div>
        </div>
      </div>
    </div>
  </div>

</div>


</div>



<!-- ✅ 모달 닫기 버튼 -->
<div class="modal-footer">
  <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
</div>


    </div>
  </div>
</div>


     <!-- ✅ 페이징 영역 -->
         <!-- 페이지네이션 -->
<!-- ✅ Bootstrap 페이징 스타일 -->
<div class="d-flex justify-content-center mt-4">
<nav>
  <ul class="pagination justify-content-center mt-4">

    <c:if test="${pageMaker.cri.page>1}">
      <li class="page-item">
        <a class="page-link" href="?page=${pageMaker.startPage - 1}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">&laquo;</a>
      </li>
    </c:if>

    <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
      <li class="page-item ${p == cri.page ? 'active' : ''}">
        <a class="page-link" href="?page=${p}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">${p}</a>
      </li>
    </c:forEach>

    <c:if test="${pageMaker.cri.page<pageMaker.endPage}">
      <li class="page-item">
        <a class="page-link" href="?page=${pageMaker.cri.page + 1}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">&raquo;</a>
      </li>
    </c:if>

  </ul>
  
</nav>

</div>

<!-- 페이징 처리 끝 -->
 		  </div>
        <!-- content-wrapper 끝 -->
	  <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
	
     </div>
     <!-- 본문.jsp main-panel ends -->
  </div>   
  <!-- container-fluid page-body-wrapper 끝 -->
</div>
<!-- container-scroller 끝-->  

<style>
  .neutral-arrow {
    color: #ccc;
  }
</style>


<script>
(function () {
  function setAchievementBarFromText() {
    const txt = document.getElementById('rd-achievement');
    const bar = document.getElementById('rd-achievementBar');
    if (!txt || !bar) return;

    const n = parseFloat((txt.textContent || '0').replace('%','')) || 0;
    const p = Math.max(0, Math.min(100, n));

    bar.style.width = p + '%';
    bar.setAttribute('aria-valuenow', p.toFixed(1));

    // 색상 초기화 후 구간별 지정
    bar.classList.remove('bg-success','bg-warning','bg-danger');
    // 부트스트랩 5는 CSS 변수 기반이라 변수도 덮어줍니다
    bar.style.removeProperty('--bs-progress-bar-bg');

    if (p >= 70) {
      bar.classList.add('bg-success');
      bar.style.setProperty('--bs-progress-bar-bg', '#198754');
    } else if (p >= 40) {
      bar.classList.add('bg-warning');
      bar.style.setProperty('--bs-progress-bar-bg', '#ffc107');
    } else {
      bar.classList.add('bg-danger');
      bar.style.setProperty('--bs-progress-bar-bg', '#dc3545');
    }
  }

  // 1) 최초 1회
  setAchievementBarFromText();

  // 2) 모달이 열릴 때마다 다시 계산 (Ajax 완료 후 텍스트가 채워진 경우 대비)
  const modal = document.getElementById('resultDetailModal');
  if (modal) {
    modal.addEventListener('shown.bs.modal', setAchievementBarFromText);
  }

  // 3) 텍스트가 Ajax로 바뀌는 순간도 감지
  const ach = document.getElementById('rd-achievement');
  if (ach) {
    const obs = new MutationObserver(setAchievementBarFromText);
    obs.observe(ach, { childList: true, characterData: true, subtree: true });
  }
})();
</script>

<script src="<c:url value='/resources/js/date-range-sync.js'/>"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

