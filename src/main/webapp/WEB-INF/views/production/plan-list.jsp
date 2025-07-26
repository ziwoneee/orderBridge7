<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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

            <div class="table-responsive">
              <table class="table table-bordered text-center">
                <thead class="thead-light">
                  <tr>
                    <th>생산계획 ID</th>
                    <th>제품명</th>
                    <th>라인</th>
                    <th>우선순위</th>
                    <th>상태</th>
                    <th>예정 수량</th>
                    <th>납기일</th>
                    <th>등록일</th>
                    <th>상세</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="plan" items="${planList}">
                    <tr>
                      <td>${plan.planId}</td>
                      <td>${plan.productName}</td>
                      <td>${plan.lineId}</td>
                      <td>${plan.priority}</td>
                      <td>
						  <c:choose>
						    <c:when test="${plan.status eq 'WAITING'}">
						      <span class="badge bg-secondary">미생산</span>
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

            <!-- 페이징 처리 -->
            <div class="mt-3 d-flex justify-content-center">
              <ul class="pagination">
                <c:if test="${cri.page > 1}">
                  <li class="page-item">
                    <a class="page-link" href="?page=${cri.page - 1}">&laquo;</a>
                  </li>
                </c:if>
                <c:forEach var="i" begin="1" end="${cri.totalPageCount == 0 ? 1 : cri.totalPageCount}">
                  <li class="page-item ${cri.page == i ? 'active' : ''}">
                    <a class="page-link" href="?page=${i}">${i}</a>
                  </li>
                </c:forEach>
                <c:if test="${cri.page < cri.totalPageCount}">
                  <li class="page-item">
                    <a class="page-link" href="?page=${cri.page + 1}">&raquo;</a>
                  </li>
                </c:if>
              </ul>
            </div>

          </div>
        </div>
      </div>
    </div>
    
    
    
  		</div>
        <!-- content-wrapper 끝 -->
        
        <!--  상세 모달  -->
    <div class="modal fade" id="detailModal" tabindex="-1" role="dialog" aria-labelledby="detailModalLabel" aria-hidden="true">
      <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="detailModalLabel">생산 계획 상세</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="닫기"></button>
          </div>
          <div class="modal-body" id="detailModalContent">
            <!-- Ajax로 로딩된 상세 내용 들어올 곳 -->
          </div>
        </div>
      </div>
    </div>
    <!--  상세 모달 끝  -->
        
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
        document.getElementById('planDetailContent').innerHTML = html;

        // 상태값에 따라 버튼 제어 (컨텐츠 내부에서 hidden input에 status 담는다고 가정)
        const status = document.getElementById('planDetailStatus').value;
        const confirmBtn = document.getElementById('confirmBtn');

        if (status === 'WAITING') {
          confirmBtn.style.display = 'inline-block';
          confirmBtn.onclick = function () {
            confirmPlan(planId);
          };
        } else {
          confirmBtn.style.display = 'none';
        }

        // 모달 열기
        const modal = new bootstrap.Modal(document.getElementById('planDetailModal'));
        modal.show();
      });
  }

  function confirmPlan(planId) {
    if (!confirm('해당 생산 계획을 확정하시겠습니까?')) return;

    fetch('/plan/confirm/' + planId, { method: 'POST' })
      .then(res => res.text())
      .then(result => {
        alert('확정 완료');
        location.reload(); // 목록 새로고침
      });
  }
</script>