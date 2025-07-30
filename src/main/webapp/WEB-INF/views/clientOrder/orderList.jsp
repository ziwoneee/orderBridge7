<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>
<%
  java.time.LocalDate today = java.time.LocalDate.now();
  String todayStr = today.toString();
  request.setAttribute("today", todayStr);
%>

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
            <h3 class="font-weight-bold">수주 목록</h3>
          </div>

          <!-- 검색 영역 -->
          <div class="d-flex justify-content-between align-items-center mb-2">
            <form method="get" class="form-inline flex-wrap">
              <label for="startDate" class="mr-1">수주일자</label>
              <input type="date" id="startDate" name="startDate" value="${empty cri.startDate ? '' : cri.startDate}" max="${today}" class="form-control mr-2">
              <span class="mx-1">~</span>
              <input type="date" id="endDate" name="endDate" value="${empty cri.endDate ? '' : cri.endDate}" max="${today}" class="form-control mr-2">

              <input type="text" name="keyword" value="${cri.keyword}" class="form-control mr-2" placeholder="거래처/제품명 검색">

            <button type="submit" class="btn btn-primary mr-2">검색</button>
            <a href="/clientorder/list" class="btn btn-light">
                      <i class="ti-reload"></i> 초기화
                    </a>            
            </form>
          </div>

          <!-- 상태별 탭 -->
          <div class="col-12">
            <div class="d-flex justify-content-between align-items-center mb-0">
  <ul class="nav nav-underline-custom" id="statusTab" role="tablist">

    <!-- 전체 -->
    <li class="nav-item">
      <a class="nav-link ${param.status == null ? 'active' : ''}"
         href="/clientorder/list?keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
        전체
        <span class="badge badge-light ms-1">${totalCount}</span>
      </a>
    </li>

    <!-- 주문접수 -->
    <li class="nav-item">
      <a class="nav-link ${param.status == 'REQUESTED' ? 'active' : ''}"
         href="/clientorder/list?status=REQUESTED&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
        주문접수
        <span class="badge badge-light ms-1">${requestedCount}</span>
      </a>
    </li>

    <!-- 확정 -->
    <li class="nav-item">
      <a class="nav-link ${param.status == 'CONFIRMED' ? 'active' : ''}"
         href="/clientorder/list?status=CONFIRMED&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
        확정
        <span class="badge badge-light ms-1">${confirmedCount}</span>
      </a>
    </li>

    <!-- 출하 -->
    <li class="nav-item">
      <a class="nav-link ${param.status == 'SHIPPED' ? 'active' : ''}"
         href="/clientorder/list?status=SHIPPED&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
        출하
        <span class="badge badge-light ms-1">${shippedCount}</span>
      </a>
    </li>

    <!-- 취소 -->
    <li class="nav-item">
      <a class="nav-link ${param.status == 'CANCELLED' ? 'active' : ''}"
         href="/clientorder/list?status=CANCELLED&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
        취소
        <span class="badge badge-light ms-1">${cancelledCount}</span>
      </a>
    </li>

  </ul>
</div>
            
          <!-- 수주 목록 테이블 -->
          <div class="table-responsive mt-4">
            <table id="clorderTable" class="table table-bordered text-center">
              <thead>
                <tr>
                  <th><input type="checkbox" id="selectAll"></th>
                  <th>수주번호</th>
                  <th>거래처명</th>
                  <th>수주일자</th>
                  <th>납기요청일</th>
                  <th>수주상태</th>
                  <th>메모</th>
                  <th>상세</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="order" items="${orderList}">
                  <tr>
                    <td><input type="checkbox" name="orderChk" value="${order.clOrderId}"></td>
                    <td>${order.clOrderNum}</td>
                    <td>${order.clientName}</td>
                    <td><fmt:formatDate value="${order.clOrderDate}" pattern="yyyy-MM-dd"/></td>
                    <td><fmt:formatDate value="${order.clDeliveryDate}" pattern="yyyy-MM-dd"/></td>
                    <td>
                      <c:choose>
                        <c:when test="${order.clOrderStatus == 'REQUESTED'}">
                          <span class="badge badge-success">접수</span>
                        </c:when>
                        <c:when test="${order.clOrderStatus == 'CONFIRMED'}">
                          <span class="badge badge-danger">확정</span>
                        </c:when>
                        <c:when test="${order.clOrderStatus == 'SHIPPED'}">
                          <span class="badge badge-warning">출하</span>
                        </c:when>
                        <c:when test="${order.clOrderStatus == 'CANCELLED'}">
                          <span class="badge badge-secondary">취소</span>
                        </c:when>
                        <c:otherwise>
                          <span style="color: #6c757d;">알 수 없음</span>
                        </c:otherwise>
                      </c:choose>
                    </td>
                    <td>${order.clOrderMemo}</td>
                    <td>
                      <a href="${pageContext.request.contextPath}/clientorder/detail?clOrderId=${order.clOrderId}" class="btn btn-outline-secondary btn-sm">상세</a>
                    </td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>

          <!-- 등록 버튼 -->
          <div class="text-right mb-3">
            <a href="${pageContext.request.contextPath}/clientorder/register" class="btn btn-primary">+ 신규 수주 등록</a>
          </div>
</div>
  </div>
          <!-- 페이징 -->
          <div class="d-flex justify-content-center mt-4">
            <nav>
              <ul class="pagination justify-content-center mt-4">
                <c:if test="${pageMaker.cri.page > 1}">
                  <li class="page-item">
                    <a class="page-link" href="?page=${pageMaker.startPage - 1}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">&laquo;</a>
                  </li>
                </c:if>
                <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
                  <li class="page-item ${p == cri.page ? 'active' : ''}">
                    <a class="page-link" href="?page=${p}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">${p}</a>
                  </li>
                </c:forEach>
                <c:if test="${pageMaker.cri.page < pageMaker.endPage}">
                  <li class="page-item">
                    <a class="page-link" href="?page=${pageMaker.cri.page + 1}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">&raquo;</a>
                  </li>
                </c:if>
              </ul>
            </nav>
          </div>

      
      <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
      </div>
    </div>
  </div>
</div>

<!-- 전체 선택 스크립트 -->
<script>
  document.getElementById('selectAll').onclick = function() {
    var checkboxes = document.getElementsByName('orderChk');
    for (var checkbox of checkboxes) {
      checkbox.checked = this.checked;
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
