<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- 작업지시 상세 모달 내용 (Ajax로 로딩됨) -->
<div class="modal-header text-white" style="background-color: #1C355E;">
  <h5 class="modal-title">작업지시 상세</h5>
  <button type="button" class="close text-white" data-dismiss="modal"><span>&times;</span></button>
</div>

<div class="modal-body">
  <!-- 기본 정보 -->

   <table class="table table-bordered mb-4 text-center">
  <tbody>
    <!-- 1. 작업지시번호: 한 줄 전체 병합 -->
    <tr>
      <th style="width: 20%;">작업지시번호</th>
      <td colspan="3">${workOrder.orderId}</td>
    </tr>

    <!-- 2. 납기일 + 작업지시일자 -->
    <tr>
      <th>납기일</th>
      <td><fmt:formatDate value="${workOrder.dueDate}" pattern="yyyy-MM-dd"/></td>
      <th>작업지시일자</th>
      <td><fmt:formatDate value="${workOrder.createdAt}" pattern="yyyy-MM-dd"/></td>
    </tr>

    <!-- 3. 제품명 + 거래처 -->
    <tr>
      <th>제품명</th>
      <td>${workOrder.productName}</td>
      <th>거래처</th>
      <td>${workOrder.clientName}</td>
    </tr>

    <!-- 4. 지시 수량 + 우선순위 -->
    <tr>
      <th>지시 수량</th>
      <td><fmt:formatNumber value="${workOrder.orderQty}" pattern="#,###"/></td>
      <th>우선순위</th>
      <td>
        <c:choose>
          <c:when test="${workOrder.priority == 'EMERGENCY'}">
            <span class="badge badge-danger">긴급</span>
          </c:when>
          <c:when test="${workOrder.priority == 'HIGH'}">
            <span class="badge badge-warning text-dark">높음</span>
          </c:when>
          <c:when test="${workOrder.priority == 'NORMAL'}">
           <span class="badge badge-info">보통</span>
          </c:when>
          <c:when test="${workOrder.priority == 'LOW'}">
            <span class="badge badge-secondary">낮음</span>
          </c:when>
        </c:choose>
      </td>
    </tr>

    <!-- 5. 상태 + 라인 -->
    <tr>
      <th>상태</th>
      <td>
        <c:choose>
          <c:when test="${workOrder.status == 'WAITING'}"><span class="badge badge-secondary">대기</span></c:when>
          <c:when test="${workOrder.status == 'IN_PROGRESS'}"><span class="badge badge-warning text-dark">생산중</span></c:when>
          <c:when test="${workOrder.status == 'COMPLETED'}"><span class="badge badge-success">완료</span></c:when>
          <c:otherwise>${workOrder.status}</c:otherwise>
        </c:choose>
      </td>
      <th>라인</th>
      <td>${workOrder.lineId}</td>
    </tr>
  </tbody>
</table>

  <!-- 자재 BOM 정보 -->
  <h5 class="text-primary font-weight-bold" style="color: #1C355E !important;">
  자재 소요량 정보
</h5>
  <div class="table-responsive">
    <table id="bomTable" class="table table-bordered text-center">
      <thead>
        <tr>
          <th>자재코드</th>
          <th>자재명</th>
          <th>공정유형</th>
          <th>단위</th>
          <th>1팩당 소요량</th>
          <th>총 필요 수량</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="item" items="${bomList}">
          <tr>
            <td>${item.materialId}</td>
            <td>${item.materialName}</td>
            <td>${item.materialType}</td>
            <td>${item.unit}</td>
            <td><fmt:formatNumber value="${item.qty}" pattern="#,###"/></td>
            <td><fmt:formatNumber value="${item.totalQty}" pattern="#,###"/></td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
</div>

<div class="modal-footer justify-content-end">
  <!-- 삭제 버튼 -->
  <button type="button" class="btn btn-danger" onclick="confirmDelete('${workOrder.orderId}')">
    <i class="fas fa-trash-alt"></i> 삭제
  </button>

  <!-- 수정 버튼 -->
  <c:if test="${workOrder.status == 'WAITING'}">
  <button type="button" class="btn btn-primary" onclick="editWorkOrder('${workOrder.orderId}')">
    <i class="fas fa-edit"></i> 수정
  </button>
</c:if>
</div>

<style>
.modal-header .close span {
  color: #FFF !important;
  font-size: 3.2rem;
  font-weight: bold;
  opacity: 1 !important;
}
</style>
