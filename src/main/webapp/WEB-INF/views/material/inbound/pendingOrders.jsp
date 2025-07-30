<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
   

<table class="table table-bordered">
  <thead>
    <tr>
      <th>발주번호</th>
      <th>공급처</th>
      <th>발주일자</th>
      <th>예상입고일</th>
      <th>발주수량</th>
      <th>입고등록</th>
    </tr>
  </thead>
  <tbody>
    <c:forEach var="order" items="${pendingOrders}">
      <tr>
        <td>${order.orderId}</td>
        <td>${order.supplierName}</td>
        <td><fmt:formatDate value="${order.orderDate}" pattern="yyyy-MM-dd"/></td>
        <td><fmt:formatDate value="${order.expectedArrivedDate}" pattern="yyyy-MM-dd"/></td>
        <td>${order.totalQuantity}</td>
        <td>
          <button class="btn btn-sm btn-success" onclick="registerInbound('${order.orderId}')">입고처리</button>
        </td>
      </tr>
    </c:forEach>
  </tbody>
</table>    