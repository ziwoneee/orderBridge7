<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<form id="planForm" method="post" action="/plan/register-form">

  <!-- 수주 ID / 납기일 -->
	<input type="hidden" class="clOrderId" name="clOrderId" value="${clOrderId}" />
	<input type="hidden" class="dueDate" name="dueDate" value="${dueDate}" />

  <table class="table table-bordered text-center">
    <thead class="thead-light align-middle">
      <tr>
        <th>제품명</th>
        <th>수주 수량</th>
        <th>현재 재고</th>
        <th>예약 수량</th>
        <th>가용 재고</th>
        <th>생산 필요 수량</th>
        <th>우선순위</th>
        <th>선택</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="item" items="${detailList}">
        <tr class="align-middle">
          <td>${item.productName}</td>
          <td>${item.orderQty}개</td>
          <td>${item.stockQty}개</td>
          <td>${item.reservedQty}개</td>
          <td>${item.availableQty}개</td>
          <td class="required-qty">
            ${item.requiredQty}개
            <input type="hidden" name="plannedQty" value="${item.requiredQty}" />
          </td>
          <td>
            <select name="priorityList" class="form-control form-control-sm">
              <option value="NORMAL" selected>보통</option>
              <option value="HIGH">높음</option>
            </select>
          </td>
          <td>
            <div class="form-check d-flex justify-content-center">
              <input class="form-check-input position-static" type="checkbox" name="selectedProduct" value="${item.productId}" />
            </div>
            <input type="hidden" name="productIdList" value="${item.productId}" />
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</form>

<style>
  thead.thead-light th {
    background-color: #f8f9fa !important;
    font-weight: bold;
    vertical-align: middle;
  }

  table td, table th {
    vertical-align: middle !important;
    white-space: nowrap;
    font-size: 14px;
  }

  .required-qty {
    font-weight: bold;
    color: #000;
  }

  select.form-control.form-control-sm {
    padding: 0.25rem 0.5rem;
    height: 32px;
    font-size: 0.875rem;
    box-sizing: border-box;
  }

  input.form-check-input {
    margin: 0 auto;
    display: block;
  }
</style>
