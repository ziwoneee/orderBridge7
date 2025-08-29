<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- 라인 기본 정보 -->
<div class="row mb-4">
  <div class="col-md-6">
    <table class="table table-bordered">
      <tbody>
        <tr>
          <th class="bg-light" style="width:30%;">라인ID</th>
          <td>${line.lineId}</td>
        </tr>
        <tr>
          <th class="bg-light">라인명</th>
          <td>${line.lineName}</td>
        </tr>
        <tr>
          <th class="bg-light">현재 상태</th>
          <td>
            <c:choose>
              <c:when test="${line.status eq 'ACTIVE'}"><span class="badge badge-success">활성</span></c:when>
              <c:otherwise><span class="badge badge-secondary">비활성</span></c:otherwise>
            </c:choose>
          </td>
        </tr>
        <tr>
          <th class="bg-light">현재 작업</th>
          <td>
            <c:choose>
              <c:when test="${not empty currentWork}">
                <span class="text-warning font-weight-bold">${currentWork.orderId} 생산중</span>
              </c:when>
              <c:when test="${line.status eq 'ACTIVE'}"><span class="text-muted">대기중</span></c:when>
              <c:otherwise><span class="text-muted">-</span></c:otherwise>
            </c:choose>
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  <div class="col-md-6">
    <div class="card">
      <div class="card-header text-white" style="background-color:#1C355E;">
        <h6 class="mb-0">상태 관리</h6>
      </div>
      <div class="card-body text-center">
        <p class="mb-3">
          현재 상태:
          <strong>
            <c:choose>
              <c:when test="${line.status eq 'ACTIVE'}"><span class="text-success">활성</span></c:when>
              <c:otherwise><span class="text-secondary">비활성</span></c:otherwise>
            </c:choose>
          </strong>
        </p>

        <c:set var="hasRunning" value="${not empty currentWork}" />
        
	<!-- 라인이 ACTIVE일 때 -->
	<!-- 진행중 작업 있으면: 비활성화 버튼 잠그고 툴팁은 span에 -->
	<!-- 진행중 작업 없으면: 정상 클릭 가능 -->
	<!-- 라인이 INACTIVE일 때 -->
		<c:choose>
  
  <c:when test="${line.status eq 'ACTIVE'}">
    <c:choose>
      
      <c:when test="${hasRunning}">
        <span class="d-inline-block" tabindex="0"
              data-toggle="tooltip"
              title="진행중 작업이 있어 비활성화할 수 없습니다.">
          <button type="button"
                  class="btn btn-warning js-toggle-line"
                  data-line-id="${line.lineId}"
                  data-next="INACTIVE"
                  disabled
                  style="pointer-events: none;">비활성화</button>
        </span>
      </c:when>
      
      <c:otherwise>
        <button type="button"
                class="btn btn-warning js-toggle-line"
                data-line-id="${line.lineId}"
                data-next="INACTIVE">비활성화</button>
      </c:otherwise>
    </c:choose>
  </c:when>

  <c:otherwise>
    <button type="button"
            class="btn btn-success js-toggle-line"
            data-line-id="${line.lineId}"
            data-next="ACTIVE">활성화</button>
  </c:otherwise>
</c:choose>
      </div>
    </div>
  </div>
</div>

<!-- 작업 현황 -->
<div class="row">
  <div class="col-12">
    <h5 class="mb-3">작업 현황</h5>

    <!-- 진행 중인 작업 (남색 헤더) -->
    <div class="card mb-3">
      <div class="card-header text-white" style="background-color:#1C355E;">
        <h6 class="mb-0">진행 중인 작업</h6>
      </div>
      <div class="card-body">
        <c:choose>
          <c:when test="${not empty currentWork}">
            <div class="alert alert-secondary mb-0">
              <strong>${currentWork.orderId}</strong> - ${currentWork.productName}<br/>
              <small>
                지시수량: ${currentWork.orderQty}EA
                <c:if test="${not empty currentWork.startedAt}">
                  &nbsp;|&nbsp; 시작: <fmt:formatDate value="${currentWork.startedAt}" pattern="MM-dd HH:mm"/>
                </c:if>
              </small>
            </div>
          </c:when>
          <c:otherwise>
            <p class="text-muted mb-0">진행 중인 작업이 없습니다.</p>
          </c:otherwise>
        </c:choose>
      </div>
    </div>

    <!-- 대기 중인 작업 (남색 헤더) -->
    <div class="card mb-3">
      <div class="card-header text-white" style="background-color:#1C355E;">
        <h6 class="mb-0">대기 중인 작업 (${waitingWorkCount}건)</h6>
      </div>
      <div class="card-body">
        <c:choose>
          <c:when test="${not empty waitingWorks}">
            <div class="table-responsive">
              <table class="table table-sm table-navy">
                <thead>
                  <tr>
                          <th>작업지시번호</th>
					      <th>제품명</th>
					      <th>수량</th>
					      <th>우선순위</th>
					      <th>납기일</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="work" items="${waitingWorks}">
                    <tr>
                      <td>${work.orderId}</td>
                      <td>${work.productName}</td>
                      <td>${work.orderQty}EA</td>
                      <td>
						  <c:choose>
						    <c:when test="${work.priority eq 'EMERGENCY'}">
						      <span class="badge badge-danger">긴급</span>
						    </c:when>
						    <c:when test="${work.priority eq 'HIGH'}">
						      <span class="badge badge-warning">높음</span>
						    </c:when>
						    <c:when test="${work.priority eq 'NORMAL'}">
						      <span class="badge badge-success">보통</span>  
						    </c:when>
						    <c:otherwise>
						      <span class="badge badge-secondary">낮음</span>
						    </c:otherwise>
						  </c:choose>
						</td>
                      <td>
						  <c:choose>
						    <c:when test="${not empty work.dueDate}">
						      <fmt:formatDate value="${work.dueDate}" pattern="yyyy-MM-dd"/>
						    </c:when>
						    <c:otherwise>-</c:otherwise>
						  </c:choose>
						</td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
          </c:when>
          <c:otherwise>
            <p class="text-muted mb-0">대기 중인 작업이 없습니다.</p>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
    <!-- “오늘 완료된 작업” 섹션은 요구에 따라 제거 -->
  </div>
</div>

<div class="modal-footer">
  <button type="button" class="btn btn-secondary" data-dismiss="modal">닫기</button>
</div>

