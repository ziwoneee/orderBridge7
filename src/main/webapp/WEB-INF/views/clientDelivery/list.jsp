<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" />
<style>
  .highlight-checkbox {
    width: 18px;
    height: 18px;
    accent-color: #28a745; /* 초록 강조 */
    cursor: pointer;
  }

  .highlight-checkbox:hover {
    box-shadow: 0 0 5px #28a745;
    transform: scale(1.2);
    transition: all 0.2s ease;
  }
</style>

<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>
  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

    <div class="main-panel">
      <div class="content-wrapper">
        <div class="row mb-4">
          <div class="col-12">
            <h3 class="font-weight-bold">출하 대기 목록</h3>
            <h6 class="font-weight-normal text-muted">수주 확정 건 중 출하 가능한 항목을 선택하여 출하처리할 수 있습니다.</h6>
          </div>
        </div>

        <form action="${pageContext.request.contextPath}/shipment/process" method="post">
          <table class="table table-bordered">
            <thead>
              <tr>
                <th><input type="checkbox" id="checkAll" onclick="toggleAll(this)" /> 선택</th>
                <th>수주번호</th>
                <th>거래처명</th>
                <th>제품명</th>
                <th>수주 수량</th>
                <th>현재 재고</th>
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

                <c:set var="firstRow" value="true"/>
                <c:forEach var="item" items="${group.productList}">
                  <tr>
                    <td class="text-center">
  <c:choose>
  <c:when test="${firstRow}">
    <div class="d-flex justify-content-center align-items-center">
      <input type="checkbox"
             name="clOrderIds"
             value="${group.clOrderId}"
             class="highlight-checkbox"
             <c:if test="${not shippable}">disabled</c:if> />

      <c:if test="${shippable}">
  <span class="badge border border-primary text-primary ml-2 d-flex align-items-center" style="gap: 5px;">
    <i class="fas fa-shipping-fast"></i> 출 하
  </span>
</c:if>

    </div>
    <c:set var="firstRow" value="false"/>
  </c:when>
  <c:otherwise>
    &nbsp;
  </c:otherwise>
</c:choose>
</td>

                    <td>${group.clOrderId}</td>
                    <td>${group.clientName}</td>
                    <td>${item.productName}</td>
                    <td>${item.orderQty}</td>
                    <td>${item.stockQty}</td>
                    <td>
                      <c:choose>
                        <c:when test="${item.stockQty ge item.orderQty}">
                          <span class="badge badge-success">가 능</span>
                        </c:when>
                        <c:otherwise>
                          <span class="badge badge-danger">부 족</span>
                        </c:otherwise>
                      </c:choose>
                    </td>
                  </tr>
                </c:forEach>
              </c:forEach>
            </tbody>
          </table>

          <c:if test="${not empty message}">
            <div class="alert alert-success mt-2">${message}</div>
          </c:if>

          <button type="submit" class="btn btn-primary">출하처리</button>
       <!-- ✅ 출하 완료 목록 보기 버튼 (링크 연결) -->
<a href="${pageContext.request.contextPath}/shipment/completed" class="btn btn-outline-secondary ml-2">
  <i class="fas fa-check-circle"></i> 출하 완료 목록 보기
</a>
       
       
        </form>
      </div>
    </div>
  </div>
</div>

<script>
  function toggleAll(source) {
    const checkboxes = document.querySelectorAll("input[name='clOrderIds']");
    checkboxes.forEach(cb => {
      if (!cb.disabled) cb.checked = source.checked;
    });
  }
</script>

<%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
