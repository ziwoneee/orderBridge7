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
            <form method="get" class="form-inline flex-wrap" id="searchForm">
              <!-- 현재 상태값 유지를 위한 hidden input -->
              <input type="hidden" name="status" value="${param.status}">
              
              <label for="startDate" class="mr-1">수주일자</label>
              <input type="date" id="startDate" name="startDate" value="${param.startDate}" max="${today}" class="form-control mr-2">
              <span class="mx-1">~</span>
              <input type="date" id="endDate" name="endDate" value="${param.endDate}" max="${today}" class="form-control mr-2">

              <input type="text" name="keyword" value="${param.keyword}" class="form-control mr-2" placeholder="거래처/제품명 검색">

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
                  <a class="nav-link ${param.status == null || param.status == '' ? 'active' : ''}"
                     href="javascript:void(0)" onclick="changeStatus('')">
                    전체
                    <span class="badge badge-light ms-1">${totalCount}</span>
                  </a>
                </li>

                <!-- 주문접수 -->
                <li class="nav-item">
                  <a class="nav-link ${param.status == 'REQUESTED' ? 'active' : ''}"
                     href="javascript:void(0)" onclick="changeStatus('REQUESTED')">
                    주문접수
                    <span class="badge badge-light ms-1">${requestedCount}</span>
                  </a>
                </li>

                <!-- 확정 -->
                <li class="nav-item">
                  <a class="nav-link ${param.status == 'CONFIRMED' ? 'active' : ''}"
                     href="javascript:void(0)" onclick="changeStatus('CONFIRMED')">
                    확정
                    <span class="badge badge-light ms-1">${confirmedCount}</span>
                  </a>
                </li>

                <!-- 출하 -->
                <li class="nav-item">
                  <a class="nav-link ${param.status == 'SHIPPED' ? 'active' : ''}"
                     href="javascript:void(0)" onclick="changeStatus('SHIPPED')">
                    출하
                    <span class="badge badge-light ms-1">${shippedCount}</span>
                  </a>
                </li>

                <!-- 취소 -->
                <li class="nav-item">
                  <a class="nav-link ${param.status == 'CANCELLED' ? 'active' : ''}"
                     href="javascript:void(0)" onclick="changeStatus('CANCELLED')">
                    취소
                    <span class="badge badge-light ms-1">${cancelledCount}</span>
                  </a>
                </li>

              </ul>
            </div>
            
            <!-- 수주 목록 테이블 -->
            <div class="table-responsive mt-4">
              <table id="clorderTable" class="table table-hover">
              <thead>
  <tr>
    <th>
      <a href="?page=1&sortColumn=cl_order_num&sortOrder=${cri.sortColumn eq 'cl_order_num' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}" class="text-light">
        수주번호
        <c:choose>
          <c:when test="${cri.sortColumn eq 'cl_order_num'}">
            <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
          </c:when>
          <c:otherwise>
            <span class="neutral-arrow">⇅</span>
          </c:otherwise>
        </c:choose>
      </a>
    </th>

    <th>
      <a href="?page=1&sortColumn=client_name&sortOrder=${cri.sortColumn eq 'client_name' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}" class="text-light">
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
      <a href="?page=1&sortColumn=cl_order_date&sortOrder=${cri.sortColumn eq 'cl_order_date' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}" class="text-light">
        수주일자
        <c:choose>
          <c:when test="${cri.sortColumn eq 'cl_order_date'}">
            <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
          </c:when>
          <c:otherwise>
            <span class="neutral-arrow">⇅</span>
          </c:otherwise>
        </c:choose>
      </a>
    </th>

    <th>
      <a href="?page=1&sortColumn=cl_delivery_date&sortOrder=${cri.sortColumn eq 'cl_delivery_date' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}" class="text-light">
        납기요청일
        <c:choose>
          <c:when test="${cri.sortColumn eq 'cl_delivery_date'}">
            <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
          </c:when>
          <c:otherwise>
            <span class="neutral-arrow">⇅</span>
          </c:otherwise>
        </c:choose>
      </a>
    </th>

    <th>
      <a href="?page=1&sortColumn=cl_order_status&sortOrder=${cri.sortColumn eq 'cl_order_status' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}" class="text-light">
        수주상태
        <c:choose>
          <c:when test="${cri.sortColumn eq 'cl_order_status'}">
            <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
          </c:when>
          <c:otherwise>
            <span class="neutral-arrow">⇅</span>
          </c:otherwise>
        </c:choose>
      </a>
    </th>

    <th>메모</th>
    <th>상세내역</th>
  </tr>
</thead>
              
                <tbody>
                  <c:choose>
                    <c:when test="${empty orderList}">
                      <tr>
                        <td colspan="7" class="text-center">검색 결과가 없습니다.</td>
                      </tr>
                    </c:when>
                    <c:otherwise>
                      <c:forEach var="order" items="${orderList}">
                        <tr>                   
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
                            <a href="${pageContext.request.contextPath}/clientorder/detail?clOrderId=${order.clOrderId}" class="btn btn-sm btn-outline-info">상세</a>
                          </td>
                        </tr>
                      </c:forEach>
                    </c:otherwise>
                  </c:choose>
                </tbody>
              </table>
            </div>

            <!-- 등록 버튼 -->
            <div class="text-right mb-3">
              <a href="${pageContext.request.contextPath}/clientorder/register" class="btn btn-success mb-2">신규 수주 등록</a>
            </div>
          </div>
        </div>
        
        <!-- 페이징 -->
        <div class="d-flex justify-content-center mt-4">
          <nav>
            <ul class="pagination justify-content-center mt-4">
              <!-- 이전 페이지 그룹 -->
              <c:if test="${pageMaker.prev}">
                <li class="page-item">
                  <a class="page-link" href="javascript:void(0)" onclick="goToPage(${pageMaker.startPage - 1})">&laquo;</a>
                </li>
              </c:if>
              
              <!-- 페이지 번호 -->
              <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
                <li class="page-item ${p == pageMaker.cri.page ? 'active' : ''}">
                  <a class="page-link" href="javascript:void(0)" onclick="goToPage(${p})">${p}</a>
                </li>
              </c:forEach>
              
              <!-- 다음 페이지 그룹 -->
              <c:if test="${pageMaker.next}">
                <li class="page-item">
                  <a class="page-link" href="javascript:void(0)" onclick="goToPage(${pageMaker.endPage + 1})">&raquo;</a>
                </li>
              </c:if>
            </ul>
          </nav>
        </div>
      </div>
      <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
    </div>
  </div>
</div>

<!-- JavaScript 기능 -->
<script>
// 폼 제출 함수 - 모든 파라미터를 유지하며 페이지 이동
function submitForm() {
  document.getElementById('searchForm').submit();
}

// 상태 변경 함수
function changeStatus(status) {
  // 현재 검색 조건 유지
  var form = document.getElementById('searchForm');
  var statusInput = form.querySelector('input[name="status"]');
  statusInput.value = status;
  
  // 페이지를 1로 초기화
  var pageInput = form.querySelector('input[name="page"]');
  if (!pageInput) {
    pageInput = document.createElement('input');
    pageInput.type = 'hidden';
    pageInput.name = 'page';
    form.appendChild(pageInput);
  }
  pageInput.value = '1';
  
  submitForm();
}

// 정렬 함수
function sortTable(column) {
  var form = document.getElementById('searchForm');
  
  // 정렬 컬럼 설정
  var sortColumnInput = form.querySelector('input[name="sortColumn"]');
  if (!sortColumnInput) {
    sortColumnInput = document.createElement('input');
    sortColumnInput.type = 'hidden';
    sortColumnInput.name = 'sortColumn';
    form.appendChild(sortColumnInput);
  }
  
  // 정렬 순서 설정
  var sortOrderInput = form.querySelector('input[name="sortOrder"]');
  if (!sortOrderInput) {
    sortOrderInput = document.createElement('input');
    sortOrderInput.type = 'hidden';
    sortOrderInput.name = 'sortOrder';
    form.appendChild(sortOrderInput);
  }
  
  // 현재 컬럼과 같으면 정렬 순서 토글, 다르면 ASC로 시작
  if (sortColumnInput.value === column) {
    sortOrderInput.value = (sortOrderInput.value === 'ASC') ? 'DESC' : 'ASC';
  } else {
    sortColumnInput.value = column;
    sortOrderInput.value = 'ASC';
  }
  
  // 페이지를 1로 초기화
  var pageInput = form.querySelector('input[name="page"]');
  if (!pageInput) {
    pageInput = document.createElement('input');
    pageInput.type = 'hidden';
    pageInput.name = 'page';
    form.appendChild(pageInput);
  }
  pageInput.value = '1';
  
  submitForm();
}

// 페이지 이동 함수
function goToPage(page) {
  var form = document.getElementById('searchForm');
  
  var pageInput = form.querySelector('input[name="page"]');
  if (!pageInput) {
    pageInput = document.createElement('input');
    pageInput.type = 'hidden';
    pageInput.name = 'page';
    form.appendChild(pageInput);
  }
  pageInput.value = page;
  
  // 정렬 정보 유지
  var sortColumnInput = form.querySelector('input[name="sortColumn"]');
  if (!sortColumnInput) {
    sortColumnInput = document.createElement('input');
    sortColumnInput.type = 'hidden';
    sortColumnInput.name = 'sortColumn';
    sortColumnInput.value = '${param.sortColumn}';
    form.appendChild(sortColumnInput);
  }
  
  var sortOrderInput = form.querySelector('input[name="sortOrder"]');
  if (!sortOrderInput) {
    sortOrderInput = document.createElement('input');
    sortOrderInput.type = 'hidden';
    sortOrderInput.name = 'sortOrder';
    sortOrderInput.value = '${param.sortOrder}';
    form.appendChild(sortOrderInput);
  }
  
  submitForm();
}

// 엔터키로 검색
document.addEventListener('DOMContentLoaded', function() {
  document.querySelector('input[name="keyword"]').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
      e.preventDefault();
      submitForm();
    }
  });
  
  // 날짜 입력 시 자동 검색 (선택사항)
  document.getElementById('startDate').addEventListener('change', function() {
    // submitForm(); // 자동 검색을 원하면 주석 해제
  });
  
  document.getElementById('endDate').addEventListener('change', function() {
    // submitForm(); // 자동 검색을 원하면 주석 해제
  });
});
</script>
