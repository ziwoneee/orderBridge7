<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>

<head>
  <title>확정 수주 선택</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.1">
  
<!-- 다른 CSS들 먼저 로드 -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/vendors/css/vendor.bundle.base.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/vertical-layout-light/style.css">

<!-- 아이콘 CDN -->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@mdi/font@6.5.95/css/materialdesignicons.min.css">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">

<!-- 우리 CSS를 가장 마지막에 로드 (최고 우선순위) -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/popup-style.css">

<!-- jQuery -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>

<body>
  <!-- 제목 -->
  <div class="page-title">
    <i class="fas fa-clipboard-list"></i>
    확정 수주 선택
  </div>

  <!-- 검색 영역 -->
  <div class="search-container">
    <form method="get" action="/workorder/select-order">
      <div class="search-form-wrapper">
        <input type="text" name="keyword" class="form-control search-input"
               placeholder="수주번호를 입력하세요" value="${cri.keyword}">
        <button type="submit" class="btn search-btn">
          <i class="fas fa-search"></i> 검색
        </button>
      </div>
    </form>
  </div>

  <!-- 수주 목록 테이블 -->
  <div class="table-container">
    <div class="table-responsive">
      <table class="table">
        <thead>
          <tr>
            <th>수주번호</th>
            <th>거래처</th>
            <th>제품명</th>
            <th>수주일</th>
            <th>납기일</th>
            <th>수주수량</th>
            <th>생산 필요 수량</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="order" items="${orderList}">
            <tr class="order-row"
                data-order-id="${order.clOrderId}"
                data-product-id="${order.productId}"
                data-product-name="${empty order.productName ? '제품명없음' : order.productName}"
                data-client-name="${empty order.clientName ? '거래처없음' : order.clientName}"
                data-due-date="<fmt:formatDate value='${order.dueDate}' pattern='yyyy-MM-dd' />"
                data-required-qty="${order.requiredQty}"
                tabindex="0"
                style="cursor: pointer;"
                onkeypress="if(event.key==='Enter') $(this).click()">
              <td>${empty order.clOrderId ? '-' : order.clOrderId}</td>
              <td>${empty order.clientName ? '-' : order.clientName}</td>
              <td>${empty order.productName ? '-' : order.productName}</td>
              <td><fmt:formatDate value="${order.clOrderDate}" pattern="yyyy-MM-dd"/></td>
              <td>
                <c:choose>
                  <c:when test="${not empty order.dueDate}">
                    <fmt:formatDate value="${order.dueDate}" pattern="yyyy-MM-dd"/>
                  </c:when>
                  <c:otherwise>-</c:otherwise>
                </c:choose>
              </td>
              <td><fmt:formatNumber value="${order.orderQty}" pattern="#,##0"/></td>
              <td class="production-qty">
                <fmt:formatNumber value="${order.requiredQty}" pattern="#,##0"/>
              </td>
            </tr>
          </c:forEach>

          <!-- 데이터 없을 때 메시지 -->
          <c:if test="${empty orderList}">
            <tr>
              <td colspan="7" class="empty-message text-center">
                <i class="fas fa-inbox fa-2x mb-3 d-block"></i>
                확정된 수주가 없습니다.
              </td>
            </tr>
          </c:if>
        </tbody>
      </table>
    </div>
  </div>

  <!-- 페이징 영역 -->
  <div class="pagination-container">
    <nav aria-label="페이지 네비게이션">
      <ul class="pagination justify-content-center">
        <!-- 이전 버튼 -->
        <c:if test="${cri.page > 1}">
          <li class="page-item">
            <a class="page-link" href="?page=${cri.page - 1}&keyword=${cri.keyword}">
              <i class="fas fa-chevron-left"></i>
            </a>
          </li>
        </c:if>

        <!-- 페이지 번호 -->
        <c:set var="startPage" value="${cri.page - 2 > 0 ? cri.page - 2 : 1}" />
        <c:set var="endPage" value="${startPage + 4 > totalPages ? totalPages : startPage + 4}" />

        <c:forEach begin="${startPage}" end="${endPage}" var="pageNum">
          <li class="page-item ${cri.page == pageNum ? 'active' : ''}">
            <a class="page-link" href="?page=${pageNum}&keyword=${cri.keyword}">${pageNum}</a>
          </li>
        </c:forEach>

        <!-- 다음 버튼 -->
        <c:if test="${cri.page < totalPages}">
          <li class="page-item">
            <a class="page-link" href="?page=${cri.page + 1}&keyword=${cri.keyword}">
              <i class="fas fa-chevron-right"></i>
            </a>
          </li>
        </c:if>
      </ul>
    </nav>

    <!-- 총 개수 -->
    <div class="text-center mt-3 text-muted">
      <small>총 ${totalCount}개 (${cri.page}/${totalPages} 페이지)</small>
    </div>
  </div>

  <!-- JavaScript 동작 -->
  <script>
  $(document).ready(function () {
    console.log("페이지 로드됨: orderList size =", ${orderList.size()});

    // 디버깅용: 각 행 정보 콘솔 출력
    $('.order-row').each(function(index) {
      const rowData = {
        'order-id': $(this).attr('data-order-id'),
        'product-id': $(this).attr('data-product-id'),
        'product-name': $(this).attr('data-product-name'),
        'client-name': $(this).attr('data-client-name')
      };
      console.log(`Row ${index}:`, rowData);
    });

    // 클릭 시 등록 화면으로 이동
    $('.order-row').on('click', function () {
      const clOrderId = $(this).attr('data-order-id');
      const productId = $(this).attr('data-product-id');

      if (!clOrderId || !productId) {
        alert(`⚠️ 필수 데이터 누락!\nclOrderId: ${clOrderId}\nproductId: ${productId}`);
        return;
      }

      const popupUrl = '/workorder/register-popup?clOrderId=' + clOrderId + '&productId=' + productId;
      location.href = popupUrl;
    });
  });
  </script>

  <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.6.0/js/bootstrap.bundle.min.js"></script>
</body>
</html>