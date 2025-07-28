<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>
  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>
    <div class="main-panel">
      <div class="content-wrapper">

        <!-- 제목 -->
        <div class="row mb-4">
          <div class="col-md-12">
            <h3 class="font-weight-bold">출하 관리</h3>
          </div>
        </div>

        <!-- 탭 메뉴 -->
        <div class="row mb-3">
          <div class="col-12">
            <ul class="nav nav-underline-custom">
              <li class="nav-item">
                <a class="nav-link ${tab ne 'completed' ? 'active' : ''}" href="/shipment/list?tab=pending">출하 대기</a>
              </li>
              <li class="nav-item">
                <a class="nav-link ${tab eq 'completed' ? 'active' : ''}" href="/shipment/list?tab=completed">출하 완료</a>
              </li>
            </ul>
          </div>
        </div>

        <!-- 출하 완료 검색/정렬 UI -->
        <c:if test="${tab eq 'completed'}">
          <form method="get" action="/shipment/list" class="form-inline mb-3">
            <input type="hidden" name="tab" value="completed" />
            <input type="date" name="startDate" value="${cri.startDate}" class="form-control mr-2" /> ~
            <input type="date" name="endDate" value="${cri.endDate}" class="form-control mr-2" />
            <input type="text" name="keyword" value="${cri.keyword}" placeholder="제품명, 수주번호, 거래처명 검색" class="form-control mr-2" />
            <select name="sortColumn" class="form-control mr-2">
              <option value="deliveryDate" ${cri.sortColumn eq 'deliveryDate' ? 'selected' : ''}>출하일</option>
              <option value="productName" ${cri.sortColumn eq 'productName' ? 'selected' : ''}>제품명</option>
              <option value="clientName" ${cri.sortColumn eq 'clientName' ? 'selected' : ''}>거래처명</option>
            </select>
            <select name="sortOrder" class="form-control mr-2">
              <option value="asc" ${cri.sortOrder eq 'asc' ? 'selected' : ''}>오름차순</option>
              <option value="desc" ${cri.sortOrder eq 'desc' ? 'selected' : ''}>내림차순</option>
            </select>
            <button type="submit" class="btn btn-primary">검색</button>
          </form>
        </c:if>

        <!-- 출하 대기 -->
        <c:if test="${tab ne 'completed'}">
          <div class="row">
            <div class="col-12">
              <table class="table table-bordered table-hover">
                <thead class="thead-light">
                  <tr>
                    <th>수주번호</th>
                    <th>거래처명</th>
                    <th>제품명</th>
                    <th>주문수량</th>
                    <th>재고수량</th>
                    <th>출하처리</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="item" items="${groupedList}">
                    <c:set var="canShip" value="true" />
                    <c:forEach var="prod" items="${item.productList}">
                      <c:if test="${prod.stockQty lt prod.orderQty}">
                        <c:set var="canShip" value="false" />
                      </c:if>
                    </c:forEach>

                    <c:forEach var="prod" items="${item.productList}" varStatus="status">
                      <tr>
                        <c:if test="${status.first}">
                          <td rowspan="${fn:length(item.productList)}">${item.clOrderId}</td>
                          <td rowspan="${fn:length(item.productList)}">${item.clientName}</td>
                        </c:if>
                        <td>${prod.productName}</td>
                        <td>${prod.orderQty}</td>
                        <td>
                          <span class="badge ${prod.stockQty lt prod.orderQty ? 'badge-danger' : 'badge-success'}">
                            ${prod.stockQty}
                          </span>
                        </td>
                        <c:if test="${status.first}">
                          <td rowspan="${fn:length(item.productList)}">
                            <form method="post" action="/shipment/process" onsubmit="return confirm('출하 처리하시겠습니까?');">
                              <input type="hidden" name="clOrderIds" value="${item.clOrderId}" />
                              <button type="submit" class="btn btn-sm btn-outline-success" ${!canShip ? 'disabled' : ''}>출하처리</button>
                            </form>
                          </td>
                        </c:if>
                      </tr>
                    </c:forEach>
                  </c:forEach>
                  <c:if test="${empty groupedList}">
                    <tr><td colspan="6" class="text-center">출하 대기 내역이 없습니다.</td></tr>
                  </c:if>
                </tbody>
              </table>
            </div>
          </div>
        </c:if>

        <!-- 출하 완료 -->
        <c:if test="${tab eq 'completed'}">
          <div class="row">
            <div class="col-12">
              <table class="table table-bordered table-hover">
                <thead class="thead-light">
                  <tr>
                    <th>출하ID</th>
                    <th>수주번호</th>
                    <th>출하일</th>
                    <th>제품명</th>
                    <th>LOT번호</th>
                    <th>출하수량</th>
                    <th>거래처명</th>
                    <th>송장번호</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="item" items="${completedList}">
                    <tr>
                      <td>${item.deliveryId}</td>
                      <td>${item.clOrderId}</td>
                      <td><fmt:formatDate value="${item.deliveryDate}" pattern="yyyy-MM-dd" /></td>
                      <td>${item.productName}</td>
                      <td>${item.lotNo}</td>
                      <td>${item.deliveryQty}</td>
                      <td>${item.clientName}</td>
                      <td>${item.trackingNumber}</td>
                    </tr>
                  </c:forEach>
                  <c:if test="${empty completedList}">
                    <tr><td colspan="8" class="text-center">출하 완료 내역이 없습니다.</td></tr>
                  </c:if>
                </tbody>
              </table>

              <!-- 페이징 -->
              <div class="d-flex justify-content-center">
                <ul class="pagination">
                  <c:if test="${pageMaker.startPage > 1}">
                    <li class="page-item"><a class="page-link" href="?tab=completed&page=${pageMaker.startPage - 1}">&laquo;</a></li>
                  </c:if>
                  <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
                    <li class="page-item ${p == cri.page ? 'active' : ''}">
                      <a class="page-link" href="?tab=completed&page=${p}">${p}</a>
                    </li>
                  </c:forEach>
                  <c:if test="${pageMaker.endPage < (pageMaker.totalCount / cri.perPageNum)}">
                    <li class="page-item"><a class="page-link" href="?tab=completed&page=${pageMaker.endPage + 1}">&raquo;</a></li>
                  </c:if>
                </ul>
              </div>
            </div>
          </div>
        </c:if>

      </div>
      <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
    </div>
  </div>
</div>

<style>
.nav-underline-custom {
  border-bottom: 1px solid #dee2e6;
}
.nav-underline-custom .nav-link {
  border: none;
  border-top: 3px solid transparent;
  color: #6c757d;
  padding: 0.75rem 1.5rem;
  font-weight: 500;
}
.nav-underline-custom .nav-link.active {
  color: #1C355E;
  border-top-color: #1C355E;
  font-weight: 700;
}
.nav-underline-custom .nav-link:hover {
  color: #1C355E;
  border-top-color: rgba(28, 53, 94, 0.5);
}
</style>
