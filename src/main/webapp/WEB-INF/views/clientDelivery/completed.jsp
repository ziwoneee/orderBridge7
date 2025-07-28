<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<% java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
   String today = sdf.format(new java.util.Date()); %>

<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>
  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

    <div class="main-panel">
      <div class="content-wrapper">

        <!-- 제목 -->
        <div class="row mb-4">
          <div class="col-12">
            <h3 class="font-weight-bold">출하 완료 목록</h3>
            <h6 class="font-weight-normal text-muted">완료된 출하 내역을 확인할 수 있습니다.</h6>
          </div>
        </div>

        <!-- ✅ 검색 및 정렬 -->
        <div class="d-flex justify-content-between mb-3">
          <form method="get" class="form-inline mb-4">
            <select name="sortColumn" class="form-control mr-2">
              <option value="delivery_id" ${cri.sortColumn eq 'delivery_id' ? 'selected' : ''}>출하ID</option>
              <option value="cl_order_id" ${cri.sortColumn eq 'cl_order_id' ? 'selected' : ''}>수주번호</option>
              <option value="client_name" ${cri.sortColumn eq 'client_name' ? 'selected' : ''}>거래처명</option>
              <option value="product_name" ${cri.sortColumn eq 'product_name' ? 'selected' : ''}>제품명</option>
              <option value="delivery_date" ${cri.sortColumn eq 'delivery_date' ? 'selected' : ''}>출하일자</option>
            </select>

            <input type="text" name="keyword" value="${cri.keyword}" class="form-control mr-2" placeholder="수주번호, 거래처명, 제품명 검색" />
            <input type="date" name="startDate" value="${cri.startDate}" class="form-control mr-2" max="<%= today %>"/>
            <input type="date" name="endDate" value="${cri.endDate}" class="form-control mr-2" max="<%= today %>"/>

            <button type="submit" class="btn btn-primary">조회</button>
          </form>
        </div>

        <!-- ✅ 테이블 -->
        <div class="table-responsive">
          <table class="table table-bordered table-hover text-center">
            <thead class="thead-light">
              <tr>
                <th>출하ID</th>
                <th>수주번호</th>
                <th>거래처명</th>
                <th>
                  <a href="?page=1&sortColumn=product_name&sortOrder=${cri.sortColumn eq 'product_name' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}">
                    제품명
                    <c:if test="${cri.sortColumn eq 'product_name'}">
                      ${cri.sortOrder eq 'asc' ? '▲' : '▼'}
                    </c:if>
                  </a>
                </th>
                <th>LOT번호</th>
                <th>출하 수량</th>
                <th>
                  <a href="?page=1&sortColumn=delivery_date&sortOrder=${cri.sortColumn eq 'delivery_date' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}">
                    출하일자
                    <c:if test="${cri.sortColumn eq 'delivery_date'}">
                      ${cri.sortOrder eq 'asc' ? '▲' : '▼'}
                    </c:if>
                  </a>
                </th>
                <th>송장번호</th>
                <th>상태</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="item" items="${completedList}">
                <tr>
                  <td>${item.deliveryId}</td>
                  <td>${item.clOrderId}</td>
                  <td>${item.clientName}</td>
                  <td>${item.productName}</td>
                  <td>${item.lotNo}</td>
                  <td>${item.deliveryQty}</td>
                  <td><fmt:formatDate value="${item.deliveryDate}" pattern="yyyy-MM-dd"/></td>
                  <td>${item.trackingNumber}</td>
                  <td><span class="badge badge-success">${item.deliveryStatus}</span></td>
                </tr>
              </c:forEach>
              <c:if test="${empty completedList}">
                <tr>
                  <td colspan="9" class="text-center text-muted">출하 완료된 항목이 없습니다.</td>
                </tr>
              </c:if>
            </tbody>
          </table>
        </div>

        <!-- ✅ 페이징 -->
        <div class="d-flex justify-content-center mt-4">
          <nav>
            <ul class="pagination justify-content-center">
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

        <!-- 뒤로가기 버튼 -->
        <div class="mt-4">
          <a href="${pageContext.request.contextPath}/shipment/pending" class="btn btn-outline-secondary">
            <i class="fas fa-arrow-left"></i> 출하 대기 목록으로
          </a>
        </div>

      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
