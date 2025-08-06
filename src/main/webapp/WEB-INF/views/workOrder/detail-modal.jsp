<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- JavaScript에서 사용할 데이터를 hidden input 또는 data 속성으로 전달 -->
<div id="workOrderData" 
     data-order-id="${workOrder.orderId}"
     data-priority="${workOrder.priority}"
     data-line-id="${workOrder.lineId}"
     data-remarks="${workOrder.remarks}"
     data-status="${workOrder.status}">
</div>

<div class="modal-header text-white" style="background-color: #1C355E;">
  <h5 class="modal-title">작업지시 상세</h5>
  <button type="button" class="close text-white" data-dismiss="modal"><span>&times;</span></button>
</div>

<div class="modal-body">
  <table class="table table-bordered mb-4 text-center workorder-detail-table">
    <tbody>
      <!-- 작업지시번호 강조 -->
      <tr class="workorder-id-row">
        <th style="width: 15%;">작업지시번호</th>
        <td colspan="5">${workOrder.orderId}</td>
      </tr>

      <!-- 납기일 / 작업지시일자 / 수주번호 -->
      <tr>
        <th>납기일</th>
        <td><fmt:formatDate value="${workOrder.dueDate}" pattern="yyyy-MM-dd"/></td>
        <th>작업지시일자</th>
        <td><fmt:formatDate value="${workOrder.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
        <th>수주번호</th>
        <td>${workOrder.clOrderId}</td>
      </tr>

      <!-- 제품명 / 거래처 / 우선순위 -->
      <tr>
        <th>제품명</th>
        <td>${workOrder.productName}</td>
        <th>거래처</th>
        <td>${workOrder.clientName}</td>
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

      <!-- 지시 수량 / 상태 / 라인 -->
      <tr>
        <th>지시 수량</th>
        <td><fmt:formatNumber value="${workOrder.orderQty}" pattern="#,###"/></td>
        <th>상태</th>
        <td>
          <c:choose>
            <c:when test="${workOrder.status == 'WAITING'}">
              <span class="badge badge-secondary">대기</span>
            </c:when>
            <c:when test="${workOrder.status == 'IN_PROGRESS'}">
              <span class="badge badge-warning text-dark">생산중</span>
            </c:when>
            <c:when test="${workOrder.status == 'COMPLETED'}">
              <span class="badge badge-success">완료</span>
            </c:when>
          </c:choose>
        </td>
        <th>라인</th>
        <td>
          <!-- 보기 모드 -->
          <div class="view-mode">
            <span id="lineDisplay">${workOrder.lineId}</span>
          </div>
          <!-- 편집 모드 -->
          <div class="edit-mode" style="display:none;">
            <select id="lineSelect" name="lineId" class="form-control form-control-sm">
              <option value="">라인 선택</option>
              <c:forEach var="line" items="${lineList}">
                <option value="${line.lineId}" 
                  <c:if test="${line.lineId == workOrder.lineId}">selected</c:if>>
                  ${line.lineName}
                </option>
              </c:forEach>
            </select>
          </div>
        </td>
      </tr>

      <!-- 특이사항 -->
      <tr>
        <th>특이사항</th>
        <td colspan="5">
          <!-- 보기 모드 -->
          <div class="view-mode">
            <span id="remarksDisplay">${empty workOrder.remarks ? '-' : workOrder.remarks}</span>
          </div>
          <!-- 편집 모드 -->
          <div class="edit-mode" style="display:none;">
            <textarea id="remarksTextarea" name="remarks" class="form-control" rows="3" 
                      placeholder="특이사항을 입력하세요">${workOrder.remarks}</textarea>
          </div>
        </td>
      </tr>
    </tbody>
  </table>

  <!-- 자재 BOM 정보 -->
  <h5 class="text-primary font-weight-bold" style="color: #1C355E !important;">자재 소요량 정보</h5>
  <div class="table-responsive">
    <table id="bomTable" class="table table-bordered text-center">
      <thead>
        <tr>
          <th>자재코드</th>
          <th>자재명</th>
          <th>공정유형</th>
          <th>1팩당 소요량</th>
          <th>총 필요 수량</th>
          <th>단위</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="item" items="${bomList}">
          <tr>
            <td>${item.materialId}</td>
            <td>${item.materialName}</td>
            <td>${item.materialType}</td>
            <td><fmt:formatNumber value="${item.qty}" pattern="#,###"/></td>
            <td><fmt:formatNumber value="${item.totalQty}" pattern="#,###"/></td>
            <td>${item.unit}</td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
</div>

<div class="modal-footer justify-content-end">
  <!-- 보기 모드 버튼들 -->
  <div class="view-mode-buttons">
    <c:if test="${workOrder.status == 'WAITING'}">
      <button type="button" class="btn btn-danger" onclick="confirmDelete('${workOrder.orderId}')">
        <i class="fas fa-trash-alt"></i> 삭제
      </button>
      <button type="button" class="btn btn-primary" onclick="enableEditMode()">
        <i class="fas fa-edit"></i> 수정
      </button>
    </c:if>
    <button type="button" class="btn btn-secondary" data-dismiss="modal">닫기</button>
  </div>
  
  <!-- 편집 모드 버튼들 -->
  <div class="edit-mode-buttons" style="display:none;">
    <button type="button" class="btn btn-success" onclick="saveChanges()">
      <i class="fas fa-save"></i> 저장
    </button>
    <button type="button" class="btn btn-secondary" onclick="cancelEditMode()">
      <i class="fas fa-times"></i> 취소
    </button>
  </div>
</div>

<style>
.modal-header .close span {
  color: #FFF !important;
  font-size: 3.2rem;
  font-weight: bold;
  opacity: 1 !important;
}

.workorder-detail-table th {
  background-color: #1C355E;
  color: white;
  vertical-align: middle;
}

.workorder-id-row {
  background-color: #e3eaf4;
}

/* 편집 모드 스타일 */
.edit-mode select,
.edit-mode textarea {
  width: 100%;
}

.edit-mode-buttons .btn {
  margin-left: 5px;
}
</style>