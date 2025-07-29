<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>
  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

    <!-- 본문 시작 -->
    <div class="main-panel">
      <div class="content-wrapper">
        <h3 class="font-weight-bold mb-4">자재 발주 등록</h3>

        <form action="/material/order/register" method="post">

          <!-- 기본 정보 -->
          <div class="card-section">
            <h5 class="section-title">기본 정보</h5>
            <div class="row">
              <div class="col-md-3 mb-3">
                <label>발주일</label>
                <input type="date" name="orderDate" class="form-control" value="<%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()) %>" required>
              </div>
              <div class="col-md-3 mb-3">
                <label>납기요청일</label>
                <input type="date" name="expectedArrivedDate" class="form-control" required>
              </div>
              <div class="col-md-3 mb-3">
                <label>거래처</label>
                <select name="supplierId" class="form-control" required>
                  <option value="">선택</option>
                  <c:forEach var="supplier" items="${supplierList}">
                    <option value="${supplier.supplierId}">${supplier.supplierName}</option>
                  </c:forEach>
                </select>
              </div>
              <div class="col-md-3 mb-3">
                <label>담당자</label>
                <input type="text" name="createdBy" class="form-control" placeholder="예: 홍길동" required>
              </div>
              <div class="col-md-12">
                <label>비고</label>
                <textarea name="note" class="form-control" rows="2"></textarea>
              </div>
            </div>
          </div>

          <!-- 항목 정보 -->
          <div class="card-section">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <h5 class="section-title">발주 항목</h5>
              <button type="button" class="btn btn-sm btn-outline-primary" onclick="addItemRow()">+ 항목 추가</button>
            </div>

            <table class="table table-bordered text-center" id="itemTable">
              <thead class="thead-light">
                <tr>
                  <th>자재명</th>
                  <th>수량</th>
                  <th>단가</th>
                  <th>총금액</th>
                  <th>입고창고</th>
                  <th>삭제</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>
                    <select name="items[0].materialId" class="form-control">
                      <option value="">선택</option>
                      <c:forEach var="mat" items="${materialList}">
                        <option value="${mat.materialId}">${mat.materialName}</option>
                      </c:forEach>
                    </select>
                  </td>
                  <td><input type="number" name="items[0].quantity" class="form-control" onchange="calculateTotal(this)" required></td>
                  <td><input type="number" name="items[0].unitPrice" class="form-control" onchange="calculateTotal(this)" required></td>
                  <td><input type="number" name="items[0].totalPrice" class="form-control" readonly></td>
                  <td><input type="text" name="items[0].storageLocation" class="form-control"></td>
                  <td><button type="button" class="btn btn-sm btn-danger" onclick="removeRow(this)">삭제</button></td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 버튼 -->
          <div class="text-right">
            <button type="submit" class="btn custom-navy mr-2">등록</button>
            <a href="/material/order/list" class="btn btn-secondary">목록</a>
          </div>

        </form>
        
       </div>
       <!-- content-wrapper 끝 -->
     <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
     </div>
     <!-- 본문.jsp main-panel ends -->
  </div>   
  <!-- container-fluid page-body-wrapper 끝 -->
</div>
<!-- container-scroller 끝-->

<script src="${pageContext.request.contextPath}/resources/js/materialOrder.js"></script>