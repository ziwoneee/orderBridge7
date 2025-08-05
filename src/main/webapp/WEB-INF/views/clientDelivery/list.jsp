<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="reservedOrderId" value="${reservedOrderId}" />

<%
    java.util.Date now = new java.util.Date();
    request.setAttribute("now", now);
%>


<% java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
   String today_1 = sdf.format(new java.util.Date()); %>
   
   <%
    java.time.LocalDate today = java.time.LocalDate.now();
    java.time.LocalDate tomorrow = today.plusDays(1);
    java.text.SimpleDateFormat sdfDate = new java.text.SimpleDateFormat("yyyy-MM-dd");
    java.text.SimpleDateFormat sdfHour = new java.text.SimpleDateFormat("HH");

    String todayStr = sdfDate.format(new java.util.Date());
    String tomorrowStr = tomorrow.toString();
    int currentHour = Integer.parseInt(sdfHour.format(new java.util.Date()));

    request.setAttribute("todayStr", todayStr);
    request.setAttribute("tomorrowStr", tomorrowStr);
    request.setAttribute("currentHour", String.valueOf(currentHour));

%>
   

<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" />

<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>      
  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>
    
    <!-- 본문 시작 -->
    <div class="main-panel">
      <div class="content-wrapper">
        <div class="row">
       
          <!-- 페이지 헤더 -->
          <div class="col-md-12 grid-margin">
            <div class="row">
              <div class="col-12 col-xl-8 mb-xl-0">
                <h3 class="font-weight-bold">출하 관리</h3>
                <h6 class="font-weight-normal text-muted">출하 대기 및 완료 내역을 관리할 수 있습니다.</h6>
              </div>
            </div>
          </div>
          
          <!-- 검색 영역 (출하 완료 탭일 때만 보임) -->
<div class="col-12 mb-4" id="searchArea" >
  <form method="get" action="/shipment/list" class="form-inline row g-2 align-items-end">

    <!-- 출하일(시작) -->
    <div class="col-md-auto">
      <label class="form-label small text-muted">출하일</label>
      <input type="date" class="form-control" id="startDate" name="startDate" value="${cri.startDate}" max="<%= today_1 %>">
    </div>

    <!-- ~ -->
    <div class="col-md-auto text-center">
      <label class="form-label small text-muted d-block">&nbsp;</label>
      <span>~</span>
    </div>

    <!-- 출하일(끝) -->
    <div class="col-md-auto">
      <label class="form-label small text-muted d-none d-md-block">&nbsp;</label>
      <input type="date" class="form-control" id="endDate" name="endDate" value="${cri.endDate}" max="<%= today_1 %>">
    </div>

    <!-- 키워드 -->
    <div class="col-md-auto">
      <label class="form-label small text-muted">검색어</label>
      <input type="text" class="form-control" id="keyword" name="keyword"
             placeholder="수주번호, 거래처명, 제품명" value="${cri.keyword}">
    </div>

    <!-- 버튼 -->
    <div class="col-md-auto">
      <label class="form-label d-none d-md-block">&nbsp;</label>
      <div class="d-flex">
        <button type="submit" class="btn btn-primary me-2"
                style="background-color: #1C355E; border-color: #1C355E;">
          <i class="ti-search"></i> 검색
        </button>
        <a href="/shipment/list?tab=completed" class="btn btn-light">
          <i class="ti-reload"></i> 초기화
        </a>
      </div>
    </div>

    <!-- 숨겨진 파라미터 -->
    <input type="hidden" name="tab" value="completed">
    <input type="hidden" name="sortOrder" value="${cri.sortOrder}">
    <input type="hidden" name="page" value="1">
    <input type="hidden" name="perPageNum" value="${cri.perPageNum}">
  </form>
</div>

          
          <!-- 출하 관리 탭 -->
          <div class="col-12">
            <!-- 탭 네비게이션 -->
            <div class="d-flex justify-content-between align-items-center mb-0">
              <ul class="nav nav-underline-custom" id="shipmentTab" role="tablist">
                <li class="nav-item">
                  <a class="nav-link ${empty param.tab || param.tab == 'pending' ? 'active' : ''}" 
                     href="/shipment/list?tab=pending">
                    출하 대기 <span class="badge badge-light ms-1">${pendingCount}</span>
                  </a>
                </li>
                <li class="nav-item">
                  <a class="nav-link ${param.tab == 'completed' ? 'active' : ''}" 
                     href="/shipment/list?tab=completed">
                    출하 완료 <span class="badge badge-light ms-1">${completedCount}</span>
                  </a>
                </li>
              </ul>
            </div>
                
            <!-- 출하 대기 탭 내용 -->
            <div class="tab-content" id="pendingContent" style="display: ${empty param.tab || param.tab == 'pending' ? 'block' : 'none'};">
              <form action="${pageContext.request.contextPath}/shipment/process" method="post">
                <div class="table-responsive">
                  <table class="table table-hover">
                    <thead style="background-color: #1C355E; color: white; border-top: none;">
                      <tr>
                        <th><input type="checkbox" id="checkAll" onclick="toggleAll(this)" class="highlight-checkbox" /> 선택</th>
                        <th>수주번호</th>
                        <th>거래처명</th>
                        <th>제품명</th>
                        <th>수주 수량</th>
                        <th>현재 재고</th>                      
                        <th>납기 요청일</th>
                        <th>출하 가능 여부</th>
                      </tr>
                    </thead>
                    <tbody>
                      <c:forEach var="group" items="${groupedList}">
  <c:set var="shippable" value="true"/>
  <c:forEach var="item" items="${group.productList}">
    <c:if test="${item.stockQty lt item.orderQty}">
      <c:set var="shippable" value="false"/>
    </c:if>
  </c:forEach>

  <!-- ✅ rowspan 계산 -->
  <c:set var="rowCount" value="${fn:length(group.productList)}"/>

  <c:forEach var="item" items="${group.productList}" varStatus="status">
    <tr>
      <!-- ✅ 체크박스 및 출하 여부: 첫 행에만 출력 -->
      <c:if test="${status.first}">
        <td class="text-center" rowspan="${rowCount}">
          <div class="d-flex justify-content-center align-items-center">
            <input type="checkbox"
                   name="clOrderIds"
                   value="${group.clOrderId}"
                   class="highlight-checkbox"
                   <c:if test="${not shippable}">disabled</c:if> />
            <c:if test="${shippable}">
              <span class="badge border border-success text-success ml-2 d-flex align-items-center" style="gap: 5px;">
                <i class="fas fa-shipping-fast"></i> 출하
              </span>
            </c:if>
          </div>
        </td>

        <!-- ✅ 수주번호: 첫 행에만 rowspan -->
        <td class="font-weight-medium" rowspan="${rowCount}">
          ${group.clOrderId}
        </td>
        
         <!-- ✅ 고객사명: 첫 행에만 rowspan -->
        <td class="font-weight-medium" rowspan="${rowCount}">
          ${group.clientName}
        </td>
      </c:if>

      <!-- ✅ 나머지 칼럼은 반복 -->     
      <td>${item.productName}</td>
      <td class="text-end"><fmt:formatNumber value="${item.orderQty}" pattern="#,###"/></td>
      <td class="text-end"><fmt:formatNumber value="${item.stockQty}" pattern="#,###"/></td>
      <td>
        <fmt:formatDate value="${item.clDeliveryDate}" pattern="yyyy-MM-dd"/>
        <c:if test="${not empty item.clDeliveryDate}">
          <c:if test="${(item.clDeliveryDate.time - now.time)/(1000*60*60*24) le 5 && (item.clDeliveryDate.time - now.time)/(1000*60*60*24) ge 0}">
            <span class="badge badge-warning ml-2">임박</span>
          </c:if>
        </c:if>
      </td>
     <td>
  <c:choose>
    <c:when test="${item.stockQty ge item.orderQty}">
      <c:choose>
        <c:when test="${fn:contains(reservedOrderIds, group.clOrderId)}">
          <!-- 예약중 상태일 때 버튼 -->
          <button type="button" class="btn btn-sm btn-outline-secondary mt-1"
                  onclick="toggleReservation('${group.clOrderId}', true)">
            <i class="fas fa-times-circle"></i> 예약중
          </button>
        </c:when>
        <c:otherwise>
          <!-- 예약 전 상태일 때 버튼 -->
          <button type="button" class="btn btn-sm btn-outline-primary mt-1"
                  onclick="toggleReservation('${group.clOrderId}', false)">
            <i class="fas fa-boxes"></i> 예약
          </button>
        </c:otherwise>
      </c:choose>
    </c:when>
    <c:otherwise>
      <span class="badge badge-danger">부족</span>
    </c:otherwise>
  </c:choose>
</td>

    </tr>
  </c:forEach>
</c:forEach>

                      <!-- 데이터가 없을 때 -->
                      <c:if test="${empty groupedList}">
                        <tr>
                          <td colspan="7" class="text-center py-4">
                            <div class="text-muted">
                              <i class="ti-info-alt" style="font-size: 24px;"></i>
                              <p class="mt-2">출하 대기 중인 항목이 없습니다.</p>
                            </div>
                          </td>
                        </tr>
                      </c:if>
                    </tbody>
                  </table>
                </div>

                <c:if test="${not empty message}">
                  <div class="alert alert-success mt-2">${message}</div>
                </c:if>

                <div class="mt-3">
                  <button type="submit" class="btn btn-primary" style="background-color: #1C355E; border-color: #1C355E;">
                    <i class="fas fa-shipping-fast"></i> 출하처리
                  </button>
                </div>
              </form>
            </div>
            
            <!-- ✅ 출하대기 전용 페이지네이션 -->
<c:if test="${empty param.tab || param.tab == 'pending'}">
  <div class="d-flex justify-content-center mt-4">
    <nav>
      <ul class="pagination justify-content-center mt-4">
        <c:if test="${pendingPage.cri.page > 1}">
          <li class="page-item">
            <a class="page-link"
               href="/shipment/list?tab=pending&page=${pendingPage.startPage - 1}&perPageNum=${pendingPage.cri.perPageNum}&keyword=${cri.keyword}">
              &laquo;
            </a>
          </li>
        </c:if>

        <c:forEach var="p" begin="${pendingPage.startPage}" end="${pendingPage.endPage}">
          <li class="page-item ${p == pendingPage.cri.page ? 'active' : ''}">
            <a class="page-link"
               href="/shipment/list?tab=pending&page=${p}&perPageNum=${pendingPage.cri.perPageNum}&keyword=${cri.keyword}">
              ${p}
            </a>
          </li>
        </c:forEach>

        <c:if test="${pendingPage.cri.page < pendingPage.endPage}">
          <li class="page-item">
            <a class="page-link"
               href="/shipment/list?tab=pending&page=${pendingPage.cri.page + 1}&perPageNum=${pendingPage.cri.perPageNum}&keyword=${cri.keyword}">
              &raquo;
            </a>
          </li>
        </c:if>
      </ul>
    </nav>
  </div>
</c:if>
            

            <!-- 출하 완료 탭 내용 -->
    <!-- 출하 완료 탭 내용 -->
<div class="tab-content" id="completedContent" style="display: ${param.tab == 'completed' ? 'block' : 'none'};">

  <!-- ✅ 출하 완료 테이블 -->
 
<div class="table-responsive">
  <table class="table table-hover">
    <thead style="background-color: #1C355E; color: white;">
      <tr>
         <th>
      <a href="/shipment/list?tab=completed&page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=cl_order_id&sortOrder=${cri.sortColumn eq 'cl_order_id' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}" 
         class="text-white text-decoration-none">
        수주번호
        <c:choose>
          <c:when test="${cri.sortColumn eq 'cl_order_id'}">
            <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
          </c:when>
          <c:otherwise>
            <span class="neutral-arrow">⇅</span>
          </c:otherwise>
        </c:choose>
      </a>
    </th>
        <th>
      <a href="/shipment/list?tab=completed&page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=client_name&sortOrder=${cri.sortColumn eq 'client_name' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}" 
         class="text-white text-decoration-none">
        거래처명
        <c:choose>
          <c:when test="${cri.sortColumn eq 'client_name'}">
            <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
          </c:when>
          <c:otherwise>
            <span class="neutral-arrow">⇅</span>
          </c:otherwise>
        </c:choose>
      </a>
    </th>

       <th>
      <a href="/shipment/list?tab=completed&page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=delivery_date&sortOrder=${cri.sortColumn eq 'delivery_date' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}" 
         class="text-white text-decoration-none">
        출하일자
        <c:choose>
          <c:when test="${cri.sortColumn eq 'delivery_date'}">
            <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
          </c:when>
          <c:otherwise>
            <span class="neutral-arrow">⇅</span>
          </c:otherwise>
        </c:choose>
      </a>
    </th>

        <th>상세보기</th>
        <th>관 리</th>
      </tr>
    </thead>
   <tbody>
  <c:forEach var="group" items="${groupedCompletedList}" varStatus="status">
    <tr>
      <td>${group.clOrderId}</td>
      <td>${group.clientName}</td>
      <td><fmt:formatDate value="${group.deliveryDate}" pattern="yyyy-MM-dd"/></td>

      <td>
        <button type="button" class="btn btn-sm btn-outline-primary"
                data-bs-toggle="modal"
                data-bs-target="#modal-${status.index}">
          상세보기
        </button>
      </td>

 <td>
  <form method="post" action="/shipment/cancel" style="display:inline;"
        onsubmit="return confirm('출하를 취소하시겠습니까?');">
    <input type="hidden" name="deliveryId" value="${group.productList[0].deliveryId}" />
    <button type="submit" class="btn btn-sm btn-outline-danger">출하 취소</button>
  </form>
</td>




    </tr>
  </c:forEach>
</tbody>
  </table>
</div>

<!-- ✅ 모달은 테이블 밖에서 출력해야 함 -->
<c:forEach var="group" items="${groupedCompletedList}" varStatus="status">
  <div class="modal fade" id="modal-${status.index}" tabindex="-1" aria-labelledby="modalLabel-${status.index}" aria-hidden="true">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="modalLabel-${status.index}">
            출하 상세 내역 - ${group.clOrderId}
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="닫기"></button>
        </div>
        <div class="modal-body">
          <table class="table table-bordered table-sm">
            <thead class="table-secondary">
              <tr>
                <th>출하 ID</th>
                <th>제품명</th>
                <th>LOT번호</th>
                <th>출하 수량</th>
                <th>송장번호</th>
                <th>상태</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="item" items="${group.productList}">
                <tr>
                  <td>${item.deliveryId}</td>
                  <td>${item.productName}</td>
                  <td>${item.lotNo}</td>
                  <td class="text-end"><fmt:formatNumber value="${item.deliveryQty}" pattern="#,###"/></td>
                  <td>${item.trackingNumber}</td>
                  <td><span class="badge bg-success">${item.deliveryStatus}</span></td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</c:forEach>


      <c:if test="${empty groupedCompletedList}">
        <tr>
          <td colspan="4" class="text-center py-4">
            <div class="text-muted">
              <i class="ti-info-alt" style="font-size: 24px;"></i>
              <p class="mt-2">출하 완료된 항목이 없습니다.</p>
            </div>
          </td>
        </tr>
      </c:if>
    </tbody>
  </table>
</div>


<!-- ✅ 페이징 유지 -->
<!-- ✅ 출하완료 탭에서만 페이징 출력 -->
<c:if test="${param.tab == 'completed'}">
  <div class="d-flex justify-content-center mt-4">
    <nav>
      <ul class="pagination justify-content-center mt-4">
        <c:if test="${pageMaker.cri.page > 1}">
          <li class="page-item">
            <a class="page-link"
               href="/shipment/list?tab=completed&page=${pageMaker.startPage - 1}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">
              &laquo;
            </a>
          </li>
        </c:if>

        <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
          <li class="page-item ${p == cri.page ? 'active' : ''}">
            <a class="page-link"
               href="/shipment/list?tab=completed&page=${p}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">
              ${p}
            </a>
          </li>
        </c:forEach>

        <c:if test="${pageMaker.cri.page < pageMaker.endPage}">
          <li class="page-item">
            <a class="page-link"
               href="/shipment/list?tab=completed&page=${pageMaker.cri.page + 1}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">
              &raquo;
            </a>
          </li>
        </c:if>
      </ul>
    </nav>
  </div>
</c:if>


</div>
    </div>
          </div>
       
      
      <!-- content-wrapper 끝 -->
      <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
  </div>
        
      </div>
    <!-- 본문.jsp main-panel ends -->
  </div>   
  <!-- container-fluid page-body-wrapper 끝 -->
</div>
<!-- container-scroller 끝-->

<script>
  function toggleAll(source) {
    const checkboxes = document.querySelectorAll("input[name='clOrderIds']");
    checkboxes.forEach(cb => {
      if (!cb.disabled) cb.checked = source.checked;
    });
  }

  // 탭 변경 시 검색 영역 표시/숨김
  document.addEventListener('DOMContentLoaded', function() {
    const currentTab = new URLSearchParams(window.location.search).get('tab') || 'pending';
    const searchArea = document.getElementById('searchArea');
    
    if (currentTab === 'completed') {
      searchArea.style.display = 'block';
    } else {
      searchArea.style.display = 'none';
    }
  });
</script>
<script>
  function reserveStock(clOrderId) {
    if (confirm("이 수주건의 재고를 예약하시겠습니까?")) {
      location.href = "/shipment/reserve?clOrderId=" + clOrderId;
    }
  }
  
  
</script>
<script>
  function toggleReservation(clOrderId, isReserved) {
    const action = isReserved ? "예약을 해지" : "재고를 예약";
    const url = isReserved 
      ? "/shipment/unreserve?clOrderId=" + clOrderId 
      : "/shipment/reserve?clOrderId=" + clOrderId;

    if (confirm("이 수주건의 " + action + "하시겠습니까?")) {
      location.href = url;
    }
  }
</script>


<style>
/* 언더라인 탭 스타일 - 상단 라인 */
.nav-underline-custom {
    border-bottom: 1px solid #dee2e6;
    margin-bottom: 0;
}

.nav-underline-custom .nav-link {
    border: none;
    border-top: 3px solid transparent;
    color: #6c757d;
    padding: 0.75rem 1.5rem;
    font-weight: 500;
    background: none;
}

.nav-underline-custom .nav-link.active {
    color: #1C355E;
    border-top-color: #1C355E;
    background: none;
    font-weight: 700;
}

.nav-underline-custom .nav-link:hover {
    color: #1C355E;
    border-top-color: rgba(28, 53, 94, 0.5);
    background: none;
}

/* 배지 스타일 */
.nav-link .badge {
    font-size: 0.75rem;
    font-weight: 500;
}

/* 체크박스 스타일 */
.highlight-checkbox {
    width: 18px;
    height: 18px;
    accent-color: #28a745;
    cursor: pointer;
}

.highlight-checkbox:hover {
    box-shadow: 0 0 5px #28a745;
    transform: scale(1.1);
    transition: all 0.2s ease;
}

/* 테이블 호버 효과 */
.table-hover tbody tr:hover {
    background-color: rgba(28, 53, 94, 0.05);
}

/* 정렬 링크 스타일 */
.table thead th a:hover {
    color: #f8f9fa !important;
    text-decoration: underline !important;
}

/* 페이지네이션 호버 효과 */
.page-link:hover {
    background-color: rgba(28, 53, 94, 0.1);
    border-color: #1C355E;
    color: #1C355E;
}

/* 버튼 호버 효과 */
.btn-primary:hover {
    background-color: #152a4a !important;
    border-color: #152a4a !important;
}

/* 탭 콘텐츠 부드러운 전환 */
.tab-content {
    margin-top: 20px;
}
 .neutral-arrow {
    color: #ccc;
    font-size: 12px;
    margin-left: 4px;
  }
</style>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
