<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">

  <%@ include file="/WEB-INF/views/main/top.jsp" %>      

  <div class="container-fluid page-body-wrapper">

    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

      <!-- 본문 시작 -->
      <div class="main-panel">
        <div class="content-wrapper">
          <div class="row">
          
          
    
      <div class="col-12 grid-margin">
        <div class="card">
          <div class="card-body">
            <h4 class="card-title">생산 계획 목록</h4>
            
            <div class="mb-2">
			 <button class="btn btn-warning btn-sm text-dark fw-bold" id="confirmSelectedBtn" onclick="confirmSelectedPlans()">선택 확정</button>
			</div>

            <div class="table-responsive">
              <table class="table table-bordered text-center">
              	
              	<thead class="text-white text-center align-middle">
              	<tr style="background-color: #4B49AC;">
  				<th>선택</th>
  				<th>생산계획 ID</th>
   				<th>제품명</th>

    <!-- 우선순위 정렬 -->
    <th>
      <a href="?page=1&sortColumn=priority&sortOrder=${cri.sortColumn eq 'priority' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}"
         class="text-white d-flex justify-content-center align-items-center text-decoration-none gap-1">
        우선순위
        <span style="font-size: 12px;">
          <span style="color: ${cri.sortColumn eq 'priority' and cri.sortOrder eq 'asc' ? 'white' : '#777'};">▲</span>
          <span style="color: ${cri.sortColumn eq 'priority' and cri.sortOrder eq 'desc' ? 'white' : '#777'};">▼</span>
        </span>
      </a>
    </th>

    <!-- 상태: 정렬 없음 -->
    <th>상태</th>

    <th>생산 예정 수량</th>

    <!-- 납기일 정렬 -->
    <th>
      <a href="?page=1&sortColumn=due_date&sortOrder=${cri.sortColumn eq 'due_date' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}"
         class="text-white d-flex justify-content-center align-items-center text-decoration-none gap-1">
        납기일
        <span style="font-size: 12px;">
          <span style="color: ${cri.sortColumn eq 'due_date' and cri.sortOrder eq 'asc' ? 'white' : '#777'};">▲</span>
          <span style="color: ${cri.sortColumn eq 'due_date' and cri.sortOrder eq 'desc' ? 'white' : '#777'};">▼</span>
        </span>
      </a>
    </th>

    <!-- 등록일 정렬 -->
    <th>
      <a href="?page=1&sortColumn=created_at&sortOrder=${cri.sortColumn eq 'created_at' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}"
         class="text-white d-flex justify-content-center align-items-center text-decoration-none gap-1">
        등록일
        <span style="font-size: 12px;">
          <span style="color: ${cri.sortColumn eq 'created_at' and cri.sortOrder eq 'asc' ? 'white' : '#777'};">▲</span>
          <span style="color: ${cri.sortColumn eq 'created_at' and cri.sortOrder eq 'desc' ? 'white' : '#777'};">▼</span>
        </span>
      </a>
    </th>

    <th>상세</th>
  </tr>
</thead>
    			
                <tbody>
                
                  <c:forEach var="plan" items="${planList}">
                    <tr>
                    <td>
					  <c:if test="${plan.status eq 'WAITING'}">
					    <input type="checkbox" class="plan-checkbox" value="${plan.planId}" />
					  </c:if>
					</td>
                      <td>${plan.planId}</td>
                      <td>${plan.productName}</td>
                      <td>
						  <c:choose>
						    <c:when test="${plan.priority eq 'EMERGENCY'}">
						      <span class="badge bg-danger">긴급</span>
						    </c:when>
						    <c:when test="${plan.priority eq 'HIGH'}">
						      <span class="badge bg-warning text-dark">높음</span>
						    </c:when>
						    <c:when test="${plan.priority eq 'NORMAL'}">
						      <span class="badge bg-primary">보통</span>
						    </c:when>
						    <c:when test="${plan.priority eq 'LOW'}">
						      <span class="badge bg-secondary">낮음</span>
						    </c:when>
						    <c:otherwise>
						      <span class="badge bg-light text-dark">${plan.priority}</span>
						    </c:otherwise>
						  </c:choose>
						</td>
                      <td>
						  <c:choose>
						    <c:when test="${plan.status eq 'WAITING'}">
						      <span class="badge bg-secondary">미확정</span>
						    </c:when>
						    <c:when test="${plan.status eq 'CONFIRMED'}">
						      <span class="badge bg-primary">확정</span>
						    </c:when>
						    <c:when test="${plan.status eq 'IN_PROGRESS'}">
						      <span class="badge bg-warning text-dark">생산중</span>
						    </c:when>
						    <c:when test="${plan.status eq 'DONE'}">
						      <span class="badge bg-success">완료</span>
						    </c:when>
						    <c:otherwise>
						      <span class="badge bg-light text-dark">${plan.status}</span>
						    </c:otherwise>
						  </c:choose>
						</td>
                      <td>${plan.plannedQty}</td>
                      <td><fmt:formatDate value="${plan.dueDate}" pattern="yyyy-MM-dd" /></td>
                       <td><fmt:formatDate value="${plan.createdAt}" pattern="yyyy-MM-dd" /></td>
                        <td>
						    <button type="button"
						            class="btn btn-outline-primary btn-sm"
						            onclick="openDetailModal('${plan.planId}')">
						      상세
						    </button>
						  </td>
                    </tr>
                    
                  </c:forEach>

                  <c:if test="${empty planList}">
                    <tr>
                      <td colspan="6" class="text-muted text-center">등록된 생산 계획이 없습니다.</td>
                    </tr>
                  </c:if>
                </tbody>
              </table>
            </div>

            <!-- Bootstrap 페이징 스타일 -->
			<div class="d-flex justify-content-center mt-4">
			<nav>
			  <ul class="pagination justify-content-center mt-4">
			
			    <c:if test="${pageMaker.cri.page>1}">
			      <li class="page-item">
			        <a class="page-link" href="?page=${pageMaker.startPage - 1}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">&laquo;</a>
			      </li>
			    </c:if>
			
			    <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
			      <li class="page-item ${p == cri.page ? 'active' : ''}">
			        <a class="page-link" href="?page=${p}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">${p}</a>
			      </li>
			    </c:forEach>
			
			    <c:if test="${pageMaker.cri.page<pageMaker.endPage}">
			      <li class="page-item">
			        <a class="page-link" href="?page=${pageMaker.cri.page + 1}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">&raquo;</a>
			      </li>
			    </c:if>
			
			  </ul>
			  
			</nav>
			
			</div>
<!-- 페이징 처리 끝 -->


          </div>
        </div>
      </div>
    </div>
    
    
    
  		</div>
        <!-- content-wrapper 끝 -->
        
<!-- 생산 계획 상세 모달 -->
<div class="modal fade" id="detailModal" tabindex="-1" role="dialog" aria-labelledby="detailModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">

      <!-- 모달 헤더 -->
      <div class="modal-header py-2 px-3 border-bottom position-relative">
        <div class="w-100 text-center">
          <h5 class="modal-title d-inline-flex align-items-center m-0" id="detailModalLabel" style="font-weight: bold; font-size: 1.2rem;">
            <i class="mdi mdi-clipboard-text-outline text-primary mr-2" style="font-size: 1.3rem;"></i>
            생산 계획 상세
          </h5>
        </div>

        <!-- 닫기 버튼 -->
        <button type="button" class="btn btn-sm p-1 position-absolute" style="top: 8px; right: 16px;" data-dismiss="modal" aria-label="닫기">
          <i class="mdi mdi-window-close text-dark mdi-24px"></i>
        </button>
      </div>

      <!-- 모달 바디 (Ajax 내용 들어오는 부분) -->
      <div class="modal-body px-4 pt-3 pb-2" id="detailModalContent">
        <%-- 여기에 plan-detail.jsp 내용이 Ajax로 삽입됨 --%>
      </div>

    </div>
  </div>
</div>


	  <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
     </div>
     <!-- 본문.jsp main-panel ends -->
  </div>   
  <!-- container-fluid page-body-wrapper 끝 -->
</div>
<!-- container-scroller 끝-->  


<script>
function openDetailModal(planId) {
	  fetch('/plan/detail?planId=' + planId)
	    .then(res => res.text())
	    .then(html => {
	      document.getElementById('detailModalContent').innerHTML = html;

	      // (선택) 상세 내용 안에 <input type="hidden" id="planDetailStatus" value="WAITING"> 가 있어야 아래 코드 실행 가능
	      const statusInput = document.getElementById('planDetailStatus');
	      const confirmBtn = document.getElementById('confirmBtn');

	      if (statusInput && confirmBtn) {
	        const status = statusInput.value;
	        if (status === 'WAITING') {
	          confirmBtn.style.display = 'inline-block';
	          confirmBtn.onclick = function () {
	            confirmPlan(planId);
	          };
	        } else {
	          confirmBtn.style.display = 'none';
	        }
	      }

	      // 모달 열기 (모달 ID는 detailModal)
	      const modal = new bootstrap.Modal(document.getElementById('detailModal'));
	      modal.show();
	    })
	    .catch(err => {
	      console.error('상세 모달 로딩 실패:', err);
	      alert('상세 정보를 불러오지 못했습니다.');
	    });
	}

function confirmSelectedPlans() {
	  const checked = document.querySelectorAll('.plan-checkbox:checked');
	  if (checked.length === 0) {
	    alert('확정할 생산계획을 선택해주세요.');
	    return;
	  }

	  const ids = Array.from(checked).map(cb => cb.value);
	  const count = ids.length;

	  //  숫자 위치 분리 + trim()으로 공백 문제 제거
	  const message = '총 ' + count.toString().trim() + '건의 생산계획을 확정 처리하시겠습니까?';

	  if (!confirm(message)) return;

	  fetch('/plan/confirm-bulk', {
	    method: 'POST',
	    headers: { 'Content-Type': 'application/json' },
	    body: JSON.stringify(ids)
	  })
	    .then(res => res.text())
	    .then(result => {
	      alert('선택된 생산 계획이 확정되었습니다.');
	      location.reload();
	    })
	    .catch(err => {
	      console.error(' 오류:', err);
	      alert('확정 처리 중 오류가 발생했습니다.');
	    });
	}
	
</script>