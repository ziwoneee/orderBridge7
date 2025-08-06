<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>
  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

    <div class="main-panel">
      <div class="content-wrapper">
        <h3 class="font-weight-bold mb-4">자재 출고 등록</h3>

        <!-- 작업지시서 선택 -->
        <div class="mb-4">
          <button type="button" class="btn btn-outline-primary" onclick="openOrderModal()">작업지시서 불러오기</button>
        </div>

        <!-- 선택된 작업지시서 정보 -->
        <div class="card mb-4">
          <div class="card-body">
            <h5 class="card-title">기본 정보</h5>
            <div class="row">
              <div class="col-md-4">
                <label>작업지시번호</label>
                <input type="text" id="selectedOrderId" class="form-control" readonly>
              </div>
              <div class="col-md-4">
                <label>제품코드</label>
                <input type="text" id="selectedProductId" class="form-control" readonly>
              </div>
              <div class="col-md-4">
                <label>생산라인</label>
                <input type="text" id="selectedLineId" class="form-control" readonly>
              </div>
              <div class="col-md-4 mt-3">
                <label>지시 수량</label>
                <input type="text" id="selectedQty" class="form-control" readonly>
              </div>
              <div class="col-md-8 mt-3">
                <label>비고</label>
                <input type="text" id="selectedRemarks" class="form-control" readonly>
              </div>
            </div>
          </div>
        </div>

        <!-- 출고 항목 (추후 작업지시서 기반 자재 불러오기 구현 예정) -->
        <div class="card mb-4">
          <div class="card-body">
            <h5 class="card-title">출고 자재 항목</h5>
            <div class="table-responsive">
              <table class="table table-bordered text-center">
                <thead style="background-color: #1C355E; color: white;">
                  <tr>
                    <th>자재코드</th>
                    <th>자재명</th>
                    <th>필요수량</th>
                    <th>재고수량</th>
                    <th>출고수량</th>
                  </tr>
                </thead>
                <tbody id="outboundItemTableBody">
                  <!-- JS로 자재 항목 삽입 예정 -->
                  <tr>
                    <td colspan="5" class="text-muted">※ 작업지시서 선택 시 자재 항목이 자동 로딩됩니다.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- 등록 버튼 -->
        <div class="text-end">
          <button type="submit" class="btn btn-success">출고등록</button>
          <a href="/material/outbound/list" class="btn btn-secondary">목록</a>
        </div>
      </div>

      <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
    </div>
  </div>
</div>

<!-- 작업지시서 선택 모달 -->
<%@ include file="/WEB-INF/views/material/out/orderModal.jsp" %>

<!-- JS -->
<script src="${pageContext.request.contextPath}/resources/js/materialOutbound.js"></script>