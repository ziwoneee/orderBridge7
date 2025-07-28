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
			
			<!-- 페이지 헤더 -->
			<div class="col-md-12 grid-margin">
              <div class="row">
                <div class="col-12 col-xl-8 mb-4 mb-xl-0">
                  <h3 class="font-weight-bold">작업지시 관리</h3>
                </div>
              </div>
            </div>
            
            <!-- 검색 영역 -->
            <div class="col-12 mb-3">
              <form method="get" action="/workorder/list" class="forms-sample">
                <div class="row align-items-end">
                  <div class="col-md-2 form-group">
                    <label class="form-label text-muted small">작업지시일</label>
                    <input type="date" class="form-control" id="startDate" name="startDate" value="${cri.startDate}">
                  </div>
                  <div class="col-md-auto form-group text-center px-2">
                    <label class="form-label text-muted small">&nbsp;</label>
                    <div class="mt-2">~</div>
                  </div>
                  <div class="col-md-2 form-group">
                    <label class="form-label text-muted small">&nbsp;</label>
                    <input type="date" class="form-control" id="endDate" name="endDate" value="${cri.endDate}">
                  </div>
                  <div class="col-md-4 form-group">
                    <input type="text" class="form-control" id="keyword" name="keyword" 
                           placeholder="작업지시번호, 수주번호, 제품명, 거래처명 검색" value="${cri.keyword}">
                  </div>
                  <div class="col-md-3 form-group">
                    <button type="submit" class="btn btn-primary me-2" style="background-color: #1C355E; border-color: #1C355E;">
                      <i class="ti-search"></i> 검색
                    </button>
                    <a href="/workorder/list" class="btn btn-light">
                      <i class="ti-reload"></i> 초기화
                    </a>
                  </div>
                </div>
                <!-- 숨겨진 파라미터 -->
                <input type="hidden" name="sortColumn" value="${cri.sortColumn}">
                <input type="hidden" name="sortOrder" value="${cri.sortOrder}">
                <input type="hidden" name="page" value="1">
                <input type="hidden" name="perPageNum" value="${cri.perPageNum}">
              </form>
            </div>
            
            <!-- 작업지시 목록 -->
            <div class="col-12">
              <!-- 탭과 등록 버튼 -->
              <div class="d-flex justify-content-between align-items-center mb-0">
                <ul class="nav nav-underline-custom" id="lineTab" role="tablist">
                  <li class="nav-item">
                    <a class="nav-link ${empty cri.status ? 'active' : ''}" 
                       href="/workorder/list?keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
                      전체 <span class="badge badge-light ms-1">${allCount}</span>
                    </a>
                  </li>
                  <li class="nav-item">
                    <a class="nav-link ${cri.status == 'WAITING' ? 'active' : ''}" 
                       href="/workorder/list?status=WAITING&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
                      대기 <span class="badge badge-light ms-1">${waitingCount}</span>
                    </a>
                  </li>
                  <li class="nav-item">
                    <a class="nav-link ${cri.status == 'IN_PROGRESS' ? 'active' : ''}" 
                       href="/workorder/list?status=IN_PROGRESS&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
                      생산중 <span class="badge badge-light ms-1">${inProgressCount}</span>
                    </a>
                  </li>
                  <li class="nav-item">
                    <a class="nav-link ${cri.status == 'COMPLETED' ? 'active' : ''}" 
                       href="/workorder/list?status=COMPLETED&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
                      생산완료
                    </a>
                  </li>
                </ul>
                
                <!-- 작업지시 등록 버튼 - 모달 트리거용 -->
				<button type="button" class="btn btn-primary"
				        style="background-color: #1C355E; border-color: #1C355E;"
				        data-toggle="modal" data-target="#orderModal">
				  <i class="ti-plus"></i> 작업지시 등록
				</button>              
              </div>
                  
              <!-- 테이블 -->
              <div class="table-responsive">
                <table class="table table-hover">
                  <thead style="background-color: #1C355E; color: white; border-top: none;">
                    <tr>
                      <th>
                        <a href="/workorder/list?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&status=${cri.status}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=order_id&sortOrder=${cri.sortColumn == 'order_id' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
                           class="text-white text-decoration-none">
                          작업지시번호 
                          <c:if test="${cri.sortColumn == 'order_id'}">
                            <i class="ti-arrow-${cri.sortOrder == 'asc' ? 'up' : 'down'}"></i>
                          </c:if>
                        </a>
                      </th>
                      <th>
                        <a href="/workorder/list?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&status=${cri.status}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=cl_order_id&sortOrder=${cri.sortColumn == 'cl_order_id' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
                           class="text-white text-decoration-none">
                          수주번호
                          <c:if test="${cri.sortColumn == 'cl_order_id'}">
                            <i class="ti-arrow-${cri.sortOrder == 'asc' ? 'up' : 'down'}"></i>
                          </c:if>
                        </a>
                      </th>
                      <th>
                        <a href="/workorder/list?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&status=${cri.status}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=created_at&sortOrder=${cri.sortColumn == 'created_at' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
                           class="text-white text-decoration-none">
                          작업지시일자
                          <c:if test="${cri.sortColumn == 'created_at'}">
                            <i class="ti-arrow-${cri.sortOrder == 'asc' ? 'up' : 'down'}"></i>
                          </c:if>
                        </a>
                      </th>
                      <th>
                        <a href="/workorder/list?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&status=${cri.status}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=due_date&sortOrder=${cri.sortColumn == 'due_date' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
                           class="text-white text-decoration-none">
                          납기일
                          <c:if test="${cri.sortColumn == 'due_date'}">
                            <i class="ti-arrow-${cri.sortOrder == 'asc' ? 'up' : 'down'}"></i>
                          </c:if>
                        </a>
                      </th>
                      <th>라인ID</th>
                      <th>제품명</th>
                      <th>지시수량</th>
                      <th>
                        <a href="/workorder/list?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&status=${cri.status}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=priority&sortOrder=${cri.sortColumn == 'priority' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
                           class="text-white text-decoration-none">
                          우선순위
                          <c:if test="${cri.sortColumn == 'priority'}">
                            <i class="ti-arrow-${cri.sortOrder == 'asc' ? 'up' : 'down'}"></i>
                          </c:if>
                        </a>
                      </th>
                      <th>상태</th>
                      <th>상세</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:forEach var="workOrder" items="${workOrders}">
                      <tr>
                        <td class="font-weight-medium">${workOrder.orderId}</td>
                        <td>${workOrder.clOrderId}</td>
                        <td>
                          <fmt:formatDate value="${workOrder.createdAt}" pattern="yyyy-MM-dd"/>
                        </td>
                        <td>
                          <fmt:formatDate value="${workOrder.dueDate}" pattern="yyyy-MM-dd"/>
                        </td>
                        <td>${workOrder.lineId}</td>
                        <td>${workOrder.productName}</td>
                        <td class="text-end">
                          <fmt:formatNumber value="${workOrder.orderQty}" pattern="#,###"/>
                        </td>
                        <td>
                          <c:choose>
                            <c:when test="${workOrder.priority == 'EMERGENCY'}">
                              <span class="badge badge-danger">긴급</span>
                            </c:when>
                            <c:when test="${workOrder.priority == 'HIGH'}">
                              <span class="badge badge-warning">높음</span>
                            </c:when>
                            <c:when test="${workOrder.priority == 'NORMAL'}">
                              <span class="badge badge-info">보통</span>
                            </c:when>
                            <c:when test="${workOrder.priority == 'LOW'}">
                              <span class="badge badge-secondary">낮음</span>
                            </c:when>
                          </c:choose>
                        </td>
                        <td>
                          <c:choose>
                            <c:when test="${workOrder.status == 'WAITING'}">
                              <span class="badge badge-secondary">대기</span>
                            </c:when>
                            <c:when test="${workOrder.status == 'IN_PROGRESS'}">
                              <span class="badge badge-warning">생산중</span>
                            </c:when>
                            <c:when test="${workOrder.status == 'COMPLETED'}">
                              <span class="badge badge-success">생산완료</span>
                            </c:when>
                            <c:otherwise>
                              <span class="badge badge-light">${workOrder.status}</span>
                            </c:otherwise>
                          </c:choose>
                        </td>
                        <td>
                          <a href="/workorder/detail?id=${workOrder.orderId}" 
                             class="btn btn-outline-primary btn-sm" 
                             style="border-color: #1C355E; color: #1C355E;">
                            상세
                          </a>
                        </td>
                      </tr>
                    </c:forEach>
                    <!-- 데이터가 없을 때 -->
                    <c:if test="${empty workOrders}">
                      <tr>
                        <td colspan="10" class="text-center py-4">
                          <div class="text-muted">
                            <i class="ti-info-alt" style="font-size: 24px;"></i>
                            <p class="mt-2">조회된 작업지시가 없습니다.</p>
                          </div>
                        </td>
                      </tr>
                    </c:if>
                  </tbody>
                </table>
              </div>
            </div>
			
          </div>
          
          <!-- 페이징 처리 시작 -->
          <div class="d-flex justify-content-center mt-4">
            <nav>
              <ul class="pagination justify-content-center mt-4">
                
                <c:if test="${pageMaker.cri.page > 1}">
                  <li class="page-item">
                    <a class="page-link" 
                       href="/workorder/list?page=${pageMaker.startPage - 1}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&status=${cri.status}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}"
                       style="color: #1C355E;">
                      &laquo;
                    </a>
                  </li>
                </c:if>
                
                <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
                  <li class="page-item ${p == cri.page ? 'active' : ''}">
                    <a class="page-link" 
                       href="/workorder/list?page=${p}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&status=${cri.status}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}"
                       style="${p == cri.page ? 'background-color: #1C355E; border-color: #1C355E; color: white;' : 'color: #1C355E;'}">
                      ${p}
                    </a>
                  </li>
                </c:forEach>
                
                <c:if test="${pageMaker.cri.page < pageMaker.endPage}">
                  <li class="page-item">
                    <a class="page-link" 
                       href="/workorder/list?page=${pageMaker.cri.page + 1}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&status=${cri.status}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}"
                       style="color: #1C355E;">
                      &raquo;
                    </a>
                  </li>
                </c:if>
                
              </ul>
            </nav>
          </div>
          <!-- 페이징 처리 끝 -->
          
        </div>
        <!-- content-wrapper 끝 -->
        
<!-- ✅ 작업지시 등록 모달 - Bootstrap 4 최종 리팩토링 -->
<div class="modal fade" id="orderModal" tabindex="-1" role="dialog" aria-labelledby="orderModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-xl modal-dialog-centered" role="document">
    <div class="modal-content">

      <!-- ✅ 헤더 -->
      <div class="modal-header">
        <h5 class="modal-title" id="orderModalLabel">확정 수주 선택</h5>
      </div>

      <!-- ✅ 바디 -->
      <div class="modal-body">

        <!-- 🔍 검색 영역 -->
        <div class="p-3 mb-3" style="background-color: #f4f6fa; border-radius: 8px;">
          <form id="orderSearchForm" class="form-inline justify-content-center">
            <div class="form-group mb-0 mr-2">
              <input type="text" class="form-control" id="searchKeyword"
                     name="keyword" placeholder="수주번호 입력" style="width: 250px;">
            </div>
            <div class="form-group mb-0">
              <button type="submit" class="btn"
                      style="background-color: #1C355E; color: white;">
                검색
              </button>
            </div>
          </form>
        </div>

        <!-- 📋 수주 목록 테이블 -->
        <div class="table-responsive">
          <table class="table table-hover text-center align-middle">
            <thead style="background-color: #1C355E; color: white;">
              <tr>
                <th></th>
                <th>수주번호</th>
                <th>거래처</th>
                <th>제품명</th>
                <th>수주일</th>
                <th>납기일</th>
                <th>수량</th>
              </tr>
            </thead>
            <tbody id="orderListBody">
              <!-- JavaScript로 동적 바인딩 -->
            </tbody>
          </table>
        </div>

        <!-- 📄 페이징 -->
        <div class="d-flex justify-content-center mt-4">
          <ul class="pagination pagination-sm" id="orderPagination">
            <!-- JavaScript로 구성됨 -->
          </ul>
        </div>
      </div>

      <!-- ✅ 푸터: 닫기 버튼만 -->
      <div class="modal-footer justify-content-end">
        <button type="button" class="btn btn-secondary" data-dismiss="modal">닫기</button>
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

<style>
/* 언더라인 탭 스타일 - 상단 라인 */
.nav-underline-custom {
    border-bottom: 1px solid #dee2e6;
    margin-bottom: 0;
}

.nav-underline-custom .nav-link {
    border: none;
    border-top: 3px solid transparent;  /* 하단 → 상단으로 변경 */
    color: #6c757d;
    padding: 0.75rem 1.5rem;
    font-weight: 500;
    background: none;
}

.nav-underline-custom .nav-link.active {
    color: #1C355E;
    border-top-color: #1C355E;  /* 상단 라인 */
    background: none;
    font-weight: 700;
}

.nav-underline-custom .nav-link:hover {
    color: #1C355E;
    border-top-color: rgba(28, 53, 94, 0.5);  /* 상단 라인 */
    background: none;
}

/* 전체 탭만 숫자 표시, 생산완료는 숫자 없음 */
.nav-underline-custom .nav-link .badge {
    font-size: 0.75rem;
    font-weight: 500;
}

/* 배지 스타일 */
.nav-link .badge {
    font-size: 0.75rem;
    font-weight: 500;
}

/* 테이블 호버 효과 */
.table-hover tbody tr:hover {
    background-color: rgba(28, 53, 94, 0.05);
}

/* 정렬 링크 스타일 */
.table thead th a:hover {
    color: #f8f9fa !important;
    text-decoration: underline !important;
}

/* 페이지네이션 호버 효과 */
.page-link:hover {
    background-color: rgba(28, 53, 94, 0.1);
    border-color: #1C355E;
    color: #1C355E;
}

/* 검색 버튼 호버 효과 */
.btn-primary:hover {
    background-color: #152a4a !important;
    border-color: #152a4a !important;
}
</style>

<script>
document.addEventListener("DOMContentLoaded", function () {
  const modal = document.getElementById("orderModal");
  const tbody = document.getElementById("orderListBody");
  const pagination = document.getElementById("orderPagination");

  // ✅ 수주 목록 로딩 함수 (Ajax)
  function loadConfirmedOrders(keyword = '', page = 1) {
    fetch(`/workorder/confirmed-orders?keyword=${keyword}&page=${page}`)
      .then(res => res.json())
      .then(data => {
        const orderList = data.orders;
        const pageMaker = data.pageMaker;
        tbody.innerHTML = "";
        pagination.innerHTML = "";

   		 // ✅ 수주가 없을 경우
        if (!orderList || orderList.length == 0) {
          const row = document.createElement("tr");
          row.innerHTML = `
            <td colspan="7" class="text-center py-4">
              <div class="text-muted">
                <i class="ti-info-alt" style="font-size: 22px;"></i><br>
                확정된 수주가 없습니다.
              </div>
            </td>
          `;
          tbody.appendChild(row);
          return;
        }

        // ✅ 수주 행 출력
        orderList.forEach(order => {
          const row = document.createElement("tr");
          row.classList.add("order-row");
          row.setAttribute("data-cl-order-id", order.clOrderId);
          row.setAttribute("data-client-name", order.clientName);
          row.setAttribute("data-product-name", order.productName);
          row.setAttribute("data-cl-order-date", order.clOrderDate);
          row.setAttribute("data-cl-delivery-date", order.clDeliveryDate);
          row.setAttribute("data-required-qty", order.requiredQty);

          row.innerHTML = `
            <td><i class="ti-angle-right text-secondary"></i></td>
            <td>${order.clOrderId}</td>
            <td>${order.clientName}</td>
            <td>${order.productName}</td>
            <td>${order.clOrderDate}</td>
            <td>${order.clDeliveryDate}</td>
            <td>${order.requiredQty.toLocaleString()}</td>
          `;
          tbody.appendChild(row);
        });

        // ✅ 페이징 구성
        for (let p = pageMaker.startPage; p <= pageMaker.endPage; p++) {
          const li = document.createElement("li");
          li.className = `page-item ${p == pageMaker.cri.page ? 'active' : ''}`;
          li.innerHTML = `<a class="page-link" href="#" style="${p == pageMaker.cri.page ? 'background-color:#1C355E; color:white;' : 'color:#1C355E;'}">${p}</a>`;
          li.addEventListener("click", function (e) {
            e.preventDefault();
            loadConfirmedOrders('', p);
          });
          pagination.appendChild(li);
        }
      })
      .catch(err => {
        console.error("수주 목록 불러오기 실패", err);
        tbody.innerHTML = `<tr><td colspan="7" class="text-danger text-center py-3">데이터 로딩 실패</td></tr>`;
      });
  }

  // ✅ 모달 열릴 때 수주 불러오기
  $('#orderModal').on('show.bs.modal', function () {
    loadConfirmedOrders();
  });

  // ✅ 검색 처리
  document.getElementById("orderSearchForm").addEventListener("submit", function (e) {
    e.preventDefault();
    const keyword = document.getElementById("searchKeyword").value.trim();
    loadConfirmedOrders(keyword, 1);
  });

  // ✅ 행 클릭 시 → 등록 모달로 전환
  tbody.addEventListener("click", function (e) {
    const row = e.target.closest("tr");
    if (!row || row.classList.contains("text-muted")) return; // 빈 메시지 행 제외

    const clOrderId = row.dataset.clOrderId;
    const productName = row.dataset.productName;
    const clientName = row.dataset.clientName;
    const dueDate = row.dataset.clDeliveryDate;
    const qty = row.dataset.requiredQty;

    // 🔁 등록 모달에 값 전달
    document.getElementById("detail_clOrderId").textContent = clOrderId;
    document.getElementById("detail_productName").textContent = productName;
    document.getElementById("detail_clientName").textContent = clientName;
    document.getElementById("detail_dueDate").textContent = dueDate;
    document.getElementById("detail_requiredQty").textContent = qty;

    // 모달 전환
    $('#orderModal').modal('hide');
    $('#workDetailModal').modal('show');
  });
});
</script>