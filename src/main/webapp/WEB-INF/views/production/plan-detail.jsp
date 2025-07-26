<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- 모달 내용 시작 -->
<input type="hidden" id="planDetailStatus" value="${plan.status}" />

<div class="mb-3">
  <label class="form-label fw-bold">생산 계획 ID</label>
  <div>${plan.planId}</div>
</div>

<div class="mb-3">
  <label class="form-label fw-bold">제품명</label>
  <div>${plan.productName}</div>
</div>

<div class="mb-3">
  <label class="form-label fw-bold">우선순위</label>
  <div>
    <c:choose>
      <c:when test="${plan.priority eq 'HIGH'}">높음</c:when>
      <c:when test="${plan.priority eq 'NORMAL'}">보통</c:when>
      <c:when test="${plan.priority eq 'LOW'}">낮음</c:when>
      <c:when test="${plan.priority eq 'EMERGENCY'}">긴급</c:when>
      <c:otherwise>${plan.priority}</c:otherwise>
    </c:choose>
  </div>
</div>

<div class="mb-3">
  <label class="form-label fw-bold">상태</label>
  <div>
    <c:choose>
      <c:when test="${plan.status eq 'WAITING'}">미생산</c:when>
      <c:when test="${plan.status eq 'CONFIRMED'}">확정</c:when>
      <c:when test="${plan.status eq 'IN_PROGRESS'}">생산중</c:when>
      <c:when test="${plan.status eq 'DONE'}">완료</c:when>
      <c:otherwise>${plan.status}</c:otherwise>
    </c:choose>
  </div>
</div>

<div class="mb-3">
  <label class="form-label fw-bold">계획 수량</label>
  <div>${plan.plannedQty} 개</div>
</div>

<div class="mb-3">
  <label class="form-label fw-bold">납기일</label>
  <div><fmt:formatDate value="${plan.dueDate}" pattern="yyyy-MM-dd" /></div>
</div>

<!-- 확정 버튼은 자바스크립트에서 보여줌 -->
<div class="mt-3 text-end">
  <button id="confirmBtn" class="btn btn-success btn-sm" style="display: none;">생산 계획 확정</button>
</div>
<!-- 모달 내용 끝 -->