<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>
<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>
  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

    <div class="main-panel">
      <div class="content-wrapper">
        <!-- 페이지 타이틀 -->
        <div class="row mb-3">
          <div class="col-12">
            <h3 class="font-weight-bold">AI 납기 예측</h3>
          </div>
        </div>
        
        <!-- 상단: 빠른 선택 + 예측 실행 -->
        <div class="row">
          <div class="col-12">
            <form method="post" action="${pageContext.request.contextPath}/ai/eta" class="form-inline">
              <!-- datalist 자동완성 -->
              <input class="form-control mr-2" style="min-width:320px"
                     list="woList" name="workOrderId" id="workOrderId"
                     value="${workOrderId}" placeholder="작업지시 ID 입력 또는 선택" />
              <datalist id="woList">
                <c:forEach var="wo" items="${workOrders}">
                  <option value="${wo.orderId}">
                  	${wo.orderId} | ${empty wo.productName ? wo.productId : wo.productName} | ${wo.status}
                   </option>
                </c:forEach>
              </datalist>

              <button type="submit" class="btn btn-primary mr-2">예측 실행</button>

              <!-- 검색어(제품/작업지시) -->
              <a class="btn btn-light" href="${pageContext.request.contextPath}/ai/eta">초기화</a>
            </form>
          </div>
        </div>

    	<!-- 결과 알림 배너 -->
		<c:if test="${not empty result}">
		  <c:set var="stage" value="${result.stage}" />
		  <c:choose>
		    <c:when test="${stage=='READY'}">
		      <c:set var="stageClass" value="badge-success"/><c:set var="stageText" value="READY: 즉시 투입 가능"/>
		    </c:when>
		    <c:when test="${stage=='CHECKED_ONLY'}">
		      <c:set var="stageClass" value="badge-info"/><c:set var="stageText" value="출고처리 대기"/>
		    </c:when>
		    <c:when test="${stage=='PO_PLACED'}">
		      <c:set var="stageClass" value="badge-warning"/><c:set var="stageText" value="부족분 발주 진행중"/>
		    </c:when>
		    <c:when test="${stage=='PLANNED'}">
		      <c:set var="stageClass" value="badge-secondary"/><c:set var="stageText" value="초기 계획"/>
		    </c:when>
		    <c:when test="${stage=='IN_PROGRESS'}">
		      <c:set var="stageClass" value="badge-primary"/><c:set var="stageText" value="생산중"/>
		    </c:when>
		    <c:when test="${stage=='COMPLETED'}">
		      <c:set var="stageClass" value="badge-dark"/><c:set var="stageText" value="완료"/>
		    </c:when>
		    <c:otherwise>
		      <c:set var="stageClass" value="badge-light"/><c:set var="stageText" value="${stage}"/>
		    </c:otherwise>
		  </c:choose>
		
		  <div class="row mt-3">
		    <div class="col-12">
		      <div class="alert alert-success d-flex align-items-center justify-content-between">
		        <div>
		          <span class="badge ${stageClass} mr-2">${stageText}</span>
		
		          <strong>${result.workOrderId}</strong> 의
		          <span class="font-weight-bold">예상 납기일</span>은
		          <span class="badge badge-info" style="font-size:100%">${etaDateStr}</span>
		          입니다.
		
		          <c:if test="${not empty dueDateStr}">
		            <br/>
		            <span class="text-muted">요청 납기일:</span>
		            <span>${dueDateStr}</span>
		
		            <c:choose>
		              <c:when test="${isDelayed == true}">
		                <span class="ml-2 text-danger">→ ${etaDelay}일 지연 예상</span>
		              </c:when>
		              <c:otherwise>
		                <span class="ml-2 text-success">→ 납기 충족 가능</span>
		              </c:otherwise>
		            </c:choose>
		          </c:if>
		        </div>
		
		        <span class="badge
		          <c:choose>
		            <c:when test="${result.riskLevel=='HIGH'}">badge-danger</c:when>
		            <c:when test="${result.riskLevel=='MEDIUM'}">badge-warning</c:when>
		            <c:otherwise>badge-success</c:otherwise>
		          </c:choose>">
		          ${result.riskLevel}
		        </span>
		      </div>
		    </div>
		  </div>
		</c:if>

	
	        <!-- 오류 알림 -->
	        <c:if test="${not empty error}">
	          <div class="row mt-3"><div class="col-12">
	            <div class="alert alert-danger">${error}</div>
	          </div></div>
	        </c:if>
	
	        <!-- 예측 상세 카드 -->
	        <c:if test="${not empty result}">
	          <div class="row mt-2">
	            <div class="col-12">
	              <div class="card">
	                <div class="card-body">
	                  <h4 class="card-title mb-3">납기 예측 분석</h4>
	
	                  <div class="row">
	                    <div class="col-md-7">
	                      <h6 class="text-muted">근거</h6>
	                      <!-- ADD: READY/CHECKED_ONLY 단계 가드 -->
	                      <c:choose>
	                        <c:when test="${result.stage=='READY'}">
	                          <p class="mb-4">자재 확보 및 출고 완료 상태입니다. 즉시 투입 가능합니다.</p>
	                        </c:when>
	                        <c:when test="${result.stage=='CHECKED_ONLY'}">
	                          <p class="mb-4">자재는 창고에 확보되어 있으며 출고처리만 남았습니다.</p>
	                        </c:when>
	                        <c:otherwise>
	                          <p class="mb-4" style="white-space:pre-line">${result.reason}</p>
	                        </c:otherwise>
	                      </c:choose>
	
	                      <span class="text-muted">예상 소요:</span>
	                      <strong>${result.etaDays}일</strong>
	                    </div>
	                    <div class="col-md-5">
	                      <h6 class="text-muted">권장 액션</h6>
	                      <c:choose>
	                        <c:when test="${empty result.actions}">
	                          <p class="text-muted mb-0">권장 액션 없음</p>
	                        </c:when>
	                        <c:otherwise>
	                          <ul class="mb-0">
	                            <c:forEach var="a" items="${result.actions}">
	                              <li>${a}</li>
	                            </c:forEach>
	                          </ul>
	                        </c:otherwise>
	                      </c:choose>
	                    </div>
	                  </div>
	
	                </div>
	              </div>
	            </div>
	          </div>
	        </c:if>

        <!-- 접히는(옵션) 목록 -->
        <div class="row mt-4">
          <div class="col-12">
            <button class="btn btn-outline-secondary btn-sm mb-2" type="button"
                    data-toggle="collapse" data-target="#woTable" aria-expanded="false">
              목록 열기/닫기
            </button>

            <div class="collapse" id="woTable">
              <div class="table-responsive">
                <table class="table table-hover">
                  <thead>
                    <tr>
                      <th>작업지시번호</th>
                      <th>제품</th>
                      <th>수량</th>
                      <th>상태</th>
                      <th>납기일</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:forEach var="wo" items="${workOrders}">
                      <tr>
                        <td>${wo.orderId}</td>
                        <td>
						  <c:choose>
						    <c:when test="${not empty wo.productName}">${wo.productName}</c:when>
						    <c:otherwise>${wo.productId}</c:otherwise>
						  </c:choose>
						  <span class="text-muted small">(${wo.productId})</span>
						</td>
                        <td>${wo.orderQty}</td>
                        <td>
                          <span class="badge
                            <c:choose>
                              <c:when test="${wo.status=='READY'}">badge-success</c:when>
                              <c:otherwise>badge-warning</c:otherwise>
                            </c:choose>">${wo.status}</span>
                        </td>
                        <td><fmt:formatDate value="${wo.dueDate}" pattern="yyyy-MM-dd"/></td>
                        <td class="text-right">
                          <form method="post" action="${pageContext.request.contextPath}/ai/eta">
                            <input type="hidden" name="workOrderId" value="${wo.orderId}"/>
                            <button class="btn btn-sm btn-outline-primary">예측 실행</button>
                          </form>
                        </td>
                      </tr>
                    </c:forEach>
                    <c:if test="${empty workOrders}">
                      <tr><td colspan="6" class="text-center text-muted">표시할 작업지시가 없습니다.</td></tr>
                    </c:if>
                  </tbody>
                </table>
              </div>
            </div>

          </div>
        </div>

      </div>
      <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
    </div>
  </div>
</div>

<!-- (선택) 결과 위치로 스크롤 -->
<c:if test="${not empty result}">
  <script>
    window.addEventListener('load', function(){
      document.querySelector('.alert.alert-success')?.scrollIntoView({behavior:'smooth', block:'start'});
    });
  </script>
</c:if>
