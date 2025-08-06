<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="modal-header bg-primary text-white">
  <h5 class="modal-title">작업지시 수정</h5>
  <button type="button" class="close text-white" data-dismiss="modal">
    <span>&times;</span>
  </button>
</div>

<div class="modal-body">
  <form id="editForm">
    <!-- 작업지시번호 (readonly) -->
    <input type="hidden" name="orderId" value="${order.orderId}">

    <div class="form-group">
      <label>작업지시번호</label>
      <input type="text" class="form-control" value="${order.orderId}" readonly>
    </div>

    <div class="form-group">
      <label>지시 수량</label>
      <input type="number" name="orderQty" id="edit_orderQty" class="form-control" value="${order.orderQty}" min="1" required>
    </div>

    <div class="form-group">
      <label>작업지시일자</label>
      <input type="date" name="createdAt" class="form-control" value="<fmt:formatDate value='${order.createdAt}' pattern='yyyy-MM-dd'/>">
    </div>

    <div class="form-group">
      <label>생산 라인</label>
      <input type="text" name="lineId" id="edit_lineId" class="form-control" value="${order.lineId}" required>
    </div>

    <div class="form-group">
      <label>상태</label>
      <select name="status" class="form-control">
        <option value="WAITING" ${order.status == 'WAITING' ? 'selected' : ''}>대기</option>
        <option value="IN_PROGRESS" ${order.status == 'IN_PROGRESS' ? 'selected' : ''}>생산중</option>
        <option value="DONE" ${order.status == 'DONE' ? 'selected' : ''}>생산완료</option>
      </select>
    </div>
  </form>
</div>

<div class="modal-footer">
  <button type="button" class="btn btn-secondary" data-dismiss="modal">닫기</button>
  <button type="button" class="btn btn-primary" onclick="submitEditForm()">저장</button>
</div>
