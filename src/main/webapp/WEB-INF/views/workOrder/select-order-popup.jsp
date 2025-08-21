<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>
<!DOCTYPE html>
<html>
<style>
/* 제목 스타일 */
 .page-title {
  font-size: 20px;
  font-weight: 700;
  color: #1C355E;
  margin-bottom: 30px;
  border-bottom: 2px solid #1C355E;
  padding-bottom: 10px;
  letter-spacing: -0.5px;
}



</style>
<head>
  <title>작업지시 등록 - 수주 선택</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.1">
 
</head>
<body>
<div class="container-fluid p-4">
  <div class="page-title">
   작업지시 등록 - 수주 선택
  </div>

  <!-- 검색 + 등록 버튼 한 줄로 정렬 -->
<div class="d-flex justify-content-between align-items-center mb-4">
  <form method="get" action="/workorder/select-order" class="form-inline mb-0">
    <div class="form-group mr-2">
      <input type="text" name="keyword" class="form-control" style="width: 420px;"
        placeholder="수주번호, 제품명, 거래처명 검색"
        value="${cri.keyword}" autofocus>
    </div>
    <button type="submit" class="btn btn-primary mr-2">
      <i class="ti-search"></i> 검색
    </button>
    <a href="/workorder/select-order" class="btn btn-light">
      <i class="ti-reload"></i> 초기화
    </a>
  </form>

  <button id="mergeSelectBtn" class="btn btn-primary" disabled>
    작업지시 등록
  </button>
</div>

  <!-- 수주 테이블 -->
  <div class="table-container">
    <div class="table-responsive">
      <table class="table table-bordered table-hover text-center">
        <thead>
          <tr>
            <th>선택</th>
            <th>수주번호</th>
            <th>거래처</th>
            <th>제품명</th>
            <th>수주일</th>
            <th>납기일</th>
            <th class="text-right">수주 수량</th>
            <th class="text-right">가용 수량</th>
            <th class="text-right">생산필요 수량</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="order" items="${orderList}">
            <tr class="order-row">
              <c:set var="formattedDueDate">
                <fmt:formatDate value="${order.dueDate}" pattern="yyyy-MM-dd" />
              </c:set>
              <td>
                <input type="checkbox" class="order-checkbox"
                  data-cl-order-id="${order.clOrderId}"
                  data-product-id="${order.productId}"
                  data-product-name="${order.productName}"
                  data-client-name="${order.clientName}"
                  data-order-qty="${order.orderQty}"
                  data-required-qty="${order.requiredQty}"
                  data-due-date="${formattedDueDate}">
              </td>
              <td class="font-weight-bold">${order.clOrderId}</td>
              <td>${order.clientName}</td>
              <td>
                <c:choose>
                  <c:when test="${order.productName eq '돼지국밥'}">
                    <span class="product-pig">${order.productName}</span>
                  </c:when>
                  <c:when test="${order.productName eq '한우곰탕'}">
                    <span class="product-beef">${order.productName}</span>
                  </c:when>
                  <c:when test="${order.productName eq '순대국밥'}">
                    <span class="product-sundae">${order.productName}</span>
                  </c:when>
                  <c:otherwise>
                    ${order.productName}
                  </c:otherwise>
                </c:choose>
              </td>
              <td><fmt:formatDate value="${order.clOrderDate}" pattern="yyyy-MM-dd" /></td>
              <td>
                <c:choose>
                  <c:when test="${not empty order.dueDate}">
                    <fmt:formatDate value="${order.dueDate}" pattern="yyyy-MM-dd" />
                  </c:when>
                  <c:otherwise><span class="text-muted">-</span></c:otherwise>
                </c:choose>
              </td>
              <td class="text-right"><fmt:formatNumber value="${order.orderQty}" pattern="#,##0" /></td>
              <td class="text-right">
                <c:choose>
                  <c:when test="${order.availableQty > 0}">
                    <span class="text-success"><fmt:formatNumber value="${order.availableQty}" pattern="#,##0" /></span>
                  </c:when>
                  <c:otherwise><span class="text-muted">0</span></c:otherwise>
                </c:choose>
              </td>
              <td class="text-right">
                <c:choose>
                  <c:when test="${order.requiredQty > 0}">
                    <span class="text-danger font-weight-bold"><fmt:formatNumber value="${order.requiredQty}" pattern="#,##0" /></span>
                  </c:when>
                  <c:otherwise><span class="text-muted">0</span></c:otherwise>
                </c:choose>
              </td>
            </tr>
          </c:forEach>

          <c:if test="${empty orderList}">
            <tr>
              <td colspan="9" class="text-center py-5 text-muted">
                <i class="fas fa-inbox fa-2x mb-2"></i><br>확정된 수주가 없습니다.
              </td>
            </tr>
          </c:if>
        </tbody>
      </table>
    </div>
  </div>

  <!-- 페이징 처리 시작 -->
  <c:if test="${pageMaker != null}">
    <div class="d-flex justify-content-center mt-4">
      <nav>
        <ul class="pagination justify-content-center mt-4">
          <c:if test="${pageMaker.cri.page > 1}">
            <li class="page-item">
              <a class="page-link" href="?page=${pageMaker.startPage - 1}&perPageNum=${pageMaker.cri.perPageNum}&keyword=${pageMaker.cri.keyword}">&laquo;</a>
            </li>
          </c:if>
          <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
            <li class="page-item ${p == pageMaker.cri.page ? 'active' : ''}">
              <a class="page-link" href="?page=${p}&perPageNum=${pageMaker.cri.perPageNum}&keyword=${pageMaker.cri.keyword}">${p}</a>
            </li>
          </c:forEach>
          <c:if test="${pageMaker.cri.page < pageMaker.endPage}">
            <li class="page-item">
              <a class="page-link" href="?page=${pageMaker.cri.page + 1}&perPageNum=${pageMaker.cri.perPageNum}&keyword=${pageMaker.cri.keyword}">&raquo;</a>
            </li>
          </c:if>
        </ul>
      </nav>
    </div>
  </c:if>
  <!-- 페이징 처리 끝 -->
</div>


<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="${pageContext.request.contextPath}/resources/js/select-order.js"></script>

</body>
</html>


