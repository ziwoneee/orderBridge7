<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
  <title>작업지시 등록</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.1">
  <meta charset="UTF-8">

  <link rel="icon" href="${pageContext.request.contextPath}/favicon.ico" type="image/x-icon" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/vendors/css/vendor.bundle.base.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/vertical-layout-light/style.css">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@mdi/font@6.5.95/css/materialdesignicons.min.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
  
  <!-- 팀 공식 CSS 적용 -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/orderBridge.css">
  
  <style>
    body {
      padding: 20px;
      background-color: #f8f9fa;
    }
  </style>
</head>
<body>

<!-- 모달 타이틀 - orderBridge.css 스타일 자동 적용 -->
<div class="modal-header" style="background-color: #1c355e;">
  <h5 class="modal-title text-white">
    <i class="fas fa-plus-circle"></i> 작업지시 등록
  </h5>
</div>

<form id="workOrderForm">
  <!-- 기본 파라미터 -->
  <input type="hidden" name="dueDate" value="${dueDate}" />
  <input type="hidden" name="productId" id="productId" value="${productId}" />
  <input type="hidden" name="orderQty" id="orderQty" value="${requiredQty}" />
  <input type="hidden" name="status" value="WAITING" />

  <!-- 병합 수주 JSON (안전 파싱용) -->
  <script id="clOrderIdsJson" type="application/json">${clOrderIdsJson}</script>

  <!-- 기본 정보 표시 -->
  <div class="card mb-3">
    <div class="card-body">
      <div class="row">
        <div class="col-md-4">
          <small class="text-muted">제품명</small>
          <p class="font-weight-bold">${productName}</p>
        </div>
        <div class="col-md-4">
          <small class="text-muted">총 생산수량</small>
          <p class="font-weight-bold" id="displayOrderQty">${requiredQty}</p>
        </div>
        <div class="col-md-4">
          <small class="text-muted">납기일</small>
          <p class="font-weight-bold">${dueDate}</p>
        </div>
      </div>

      <!-- 병합 수주 목록 (2개 이상일 때만 표시) -->
      <div id="mergedOrdersSection" style="display:none;" class="mt-3">
        <hr>
        <a href="#mergedOrdersList" class="text-primary" data-toggle="collapse" aria-expanded="false" aria-controls="mergedOrdersList">
          <i class="fas fa-chevron-down"></i> 병합된 수주 목록 (<span id="mergedCount">0</span>건)
        </a>
        <div class="collapse mt-2" id="mergedOrdersList">
          <table class="table table-sm">
            <thead>
            <tr>
              <th class="bg-light">수주번호</th>
              <th class="bg-light">거래처</th>
              <th class="bg-light text-right">수량</th>
            </tr>
            </thead>
            <tbody id="mergedOrdersTableBody"><!-- 동적 생성 --></tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <!-- 작업지시 설정 - orderBridge.css의 card-section 스타일 적용 -->
  <div class="card-section">
    <h5 class="section-title">작업지시 설정</h5>
    
    <div class="row">
      <!-- 생산라인: 선택 가능 -->
      <div class="col-md-6">
        <label class="form-label required">생산라인</label>
        <select class="form-control" name="lineId" id="lineId" required>
          <option value="">라인을 선택하세요</option>
          <c:choose>
            <c:when test="${not empty availableLines}">
              <c:forEach var="line" items="${availableLines}">
                <c:if test="${line.status == 'ACTIVE'}">
                  <option value="${line.lineId}"
                    <c:if test="${not empty autoLine and autoLine.lineId == line.lineId}">selected</c:if>>
                    ${line.lineName}
                    <c:if test="${not empty line.availableProduct}">(${line.availableProduct})</c:if>
                  </option>
                </c:if>
              </c:forEach>
            </c:when>
            <c:when test="${not empty lineList}">
              <c:forEach var="line" items="${lineList}">
                <option value="${line.lineId}">${line.lineName}</option>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <option value="" disabled>등록된 라인이 없습니다</option>
            </c:otherwise>
          </c:choose>
        </select>
      </div>

      <!-- 우선순위 -->
      <div class="col-md-6">
        <label class="form-label required">우선순위</label>
        <select class="form-control" name="priority" required>
          <option value="HIGH">높음</option>
          <option value="NORMAL" selected>보통</option>
          <option value="LOW">낮음</option>
        </select>
      </div>
    </div>

    <div class="form-group mt-3">
      <label class="form-label">특이사항</label>
      <textarea class="form-control" name="remarks" rows="2" placeholder="특이사항을 입력하세요"></textarea>
    </div>
  </div>

  <!-- BOM 자재 소요량 -->
  <div class="card mt-3">
    <div class="card-header bg-info text-white">
      <i class="fas fa-boxes"></i> 자재 소요량
    </div>
    <div class="card-body">
      <table class="table table-bordered table-header-dark">
        <thead>
        <tr>
          <th class="bg-light">자재코드</th>
          <th class="bg-light">자재명</th>
          <th class="bg-light">용도</th>
          <th class="bg-light">10팩당</th>
          <th class="bg-light">총 소요량</th>
          <th class="bg-light">단위</th>
        </tr>
        </thead>
        <tbody id="bomTableBody">
        <tr>
          <td colspan="6" class="text-center text-muted">불러오는 중...</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>

  <!-- 버튼 - orderBridge.css 스타일 자동 적용 -->
  <div class="mt-4 text-right">
    <button type="button" class="btn btn-secondary" onclick="window.close()">취소</button>
    <button type="submit" class="btn btn-primary" id="submitBtn">등록</button>
  </div>
</form>

<!-- JS: jQuery + Bootstrap Bundle -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/js/bootstrap.bundle.min.js"></script>

<!-- 컨텍스트 경로 전역 변수만 노출 -->
<script>
  window.CONTEXT_PATH = '${pageContext.request.contextPath}';
</script>

<!-- 외부 JS (아래 2번 파일) -->
<script src="${pageContext.request.contextPath}/resources/js/register-popup.js"></script>

</body>
</html>