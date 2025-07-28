<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- 🔧 모달 전용 여백 스타일 최소화 -->
<style>
  .modal-body h5 {
    margin-top: 0.5rem;
    margin-bottom: 0.8rem;
  }
  .modal-body {
    padding-top: 0.5rem !important;
  }
  .modal-body table th {
    vertical-align: middle !important;
  }
</style>

<!-- ✅ 기본 정보 -->
<h5 class="text-primary font-weight-bold">기본 정보</h5>
<div class="table-responsive">
  <table class="table table-bordered text-center align-middle">
    <colgroup>
      <col style="width: 20%;">
      <col style="width: 30%;">
      <col style="width: 20%;">
      <col style="width: 30%;">
    </colgroup>
    <tbody>
      <tr>
        <th class="bg-primary text-white">생산 계획 ID</th>
        <td>${plan.planId}</td>
        <th class="bg-primary text-white">수주번호</th>
        <td>${plan.clOrderId}</td>
      </tr>
      <tr>
        <th class="bg-primary text-white">제품명</th>
        <td>${plan.productName}</td>
        <th class="bg-primary text-white">제품 코드</th>
        <td>${plan.productId}</td>
      </tr>
    </tbody>
  </table>
</div>

<!-- ✅ 수량 정보 -->
<h5 class="text-primary font-weight-bold mt-3">수량 정보</h5>
<div class="table-responsive">
  <table class="table table-bordered text-center align-middle">
    <colgroup>
      <col style="width: 25%;">
      <col style="width: 25%;">
      <col style="width: 25%;">
      <col style="width: 25%;">
    </colgroup>
    <tbody>
      <tr class="bg-primary text-white">
        <th>수주 수량</th>
        <th>현재 재고</th>
        <th>예약 수량</th>
        <th>가용 재고</th>
      </tr>
      <tr>
        <td>${plan.orderQty} 개</td>
        <td>${plan.stockQty} 개</td>
        <td>${plan.reservedQty} 개</td>
        <td>${plan.availableQty} 개</td>
      </tr>
      <tr class="bg-light">
        <th colspan="3" class="text-end pr-3">생산 계획 수량</th>
        <td><strong>${plan.plannedQty} 개</strong></td>
      </tr>
    </tbody>
  </table>
</div>

<!-- ✅ 상태 및 일정 -->
<h5 class="text-primary font-weight-bold mt-3">상태 및 일정</h5>
<div class="table-responsive">
  <table class="table table-bordered text-center align-middle">
    <colgroup>
      <col style="width: 20%;">
      <col style="width: 30%;">
      <col style="width: 20%;">
      <col style="width: 30%;">
    </colgroup>
    <tbody>
      <tr>
        <th class="bg-primary text-white">우선순위</th>
        <td>
          <c:choose>
            <c:when test="${plan.priority eq 'EMERGENCY'}">
              <span class="badge badge-danger px-2 py-1">긴급</span>
            </c:when>
            <c:when test="${plan.priority eq 'HIGH'}">
              <span class="badge badge-warning px-2 py-1">높음</span>
            </c:when>
            <c:when test="${plan.priority eq 'NORMAL'}">
              <span class="badge badge-primary px-2 py-1">보통</span>
            </c:when>
            <c:otherwise>
              <span class="badge badge-secondary px-2 py-1">낮음</span>
            </c:otherwise>
          </c:choose>
        </td>
        <th class="bg-primary text-white">진행 상태</th>
        <td>
          <c:choose>
            <c:when test="${plan.status eq 'WAITING'}">
              <span class="badge badge-secondary px-2 py-1">미확정</span>
            </c:when>
            <c:when test="${plan.status eq 'CONFIRMED'}">
              <span class="badge badge-warning px-2 py-1">확정</span>
            </c:when>
            <c:when test="${plan.status eq 'IN_PROGRESS'}">
              <span class="badge badge-success px-2 py-1">생산중</span>
            </c:when>
           	 <c:when test="${plan.status eq 'DONE'}">
              <span class="badge badge-light px-2 py-1">완료</span>
               </c:when>
               <c:otherwise>
						      <span class="badge bg-light text-dark">${plan.status}</span>
						    </c:otherwise>
          </c:choose>
        </td>
      </tr>
      <tr>
        <th class="bg-primary text-white">납기일</th>
        <td>
          <fmt:formatDate value="${plan.dueDate}" pattern="yyyy-MM-dd" />
        </td>
        <th class="bg-primary text-white">등록일</th>
        <td>
          <fmt:formatDate value="${plan.createdAt}" pattern="yyyy-MM-dd HH:mm" />
        </td>
      </tr>
    </tbody>
  </table>
</div>
