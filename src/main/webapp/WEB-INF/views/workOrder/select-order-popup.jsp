<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>

<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/popup-style.css">

<head>
  <title>확정 수주 선택</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.1">
  <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.6.0/css/bootstrap.min.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>

</head>
<body>
  <div class="page-title">
    <i class="fas fa-clipboard-list"></i>
    확정 수주 선택
  </div>

  <!-- 검색 영역 - 개선된 구조 -->
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
			      data-product-name="${order.productName}"
			      data-client-name="${order.clientName}"
			      data-due-date="<fmt:formatDate value='${order.dueDate}' pattern='yyyy-MM-dd' />"
			      data-required-qty="${order.requiredQty}"
			      tabindex="0"
			      onkeypress="if(event.key==='Enter') $(this).click()">
			    <td>${order.clOrderId}</td>
			    <td>${order.clientName}</td>
			    <td>${order.productName}</td>
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


<script>
  $(document).ready(function () {
    // 검색창 포커스 + Enter submit
    $('input[name="keyword"]').focus().on('keypress', function (e) {
      if (e.which === 13) {
        $(this).closest('form').submit();
      }
    });

    // 행 클릭 시 → 현재 팝업 창이 register-popup.jsp로 이동
    $('.order-row').on('click', function () {
	  const clOrderId = $(this).data('order-id');
	  const productId = $(this).data('product-id');
	  const productName = encodeURIComponent($(this).data('product-name'));
	  const clientName = encodeURIComponent($(this).data('client-name'));
	  const dueDate = encodeURIComponent($(this).data('due-date'));
	  const requiredQty = encodeURIComponent($(this).data('required-qty'));
	
	  const popupUrl = `/workorder/register-popup`
		  + `?clOrderId=${clOrderId}`
		  + `&productId=${productId}`
		  + `&productName=${productName}`
		  + `&clientName=${clientName}`
		  + `&dueDate=${dueDate}`
		  + `&requiredQty=${requiredQty}`;
	
	  location.href = popupUrl;
	});

    // ESC로 팝업 닫기
    $(document).on('keydown', function (e) {
      if (e.key === 'Escape') {
        window.close();
      }
    });
  });
</script>
  
  <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.6.0/js/bootstrap.bundle.min.js"></script>
</body>
</html>