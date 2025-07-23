<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<style>
  tr.order-detail:hover td {
    background-color: transparent !important;
  }
  tr.order-row:hover td {
    background-color: #e6e6f5 !important;
    cursor: pointer;
  }
</style>

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
            <h4 class="card-title">생산 계획 등록</h4>
            <h5 class="mb-3"> 확정된 수주 목록</h5>

            <div class="table-responsive">
              <table class="table table-bordered">
                <thead style="background-color: #1f325b; color: white;">
                  <tr>
                    <th>수주번호</th>
                    <th>거래처명</th>
                    <th>수주일</th>
                    <th>납기일</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="order" items="${confirmedOrders}">
                    <tr class="order-row" 
						data-order-id="${order.clOrderId}" 
						data-due-date="<fmt:formatDate value='${order.clDeliveryDate}' pattern='yyyy-MM-dd' />">
                    
                      <td>${order.clOrderId}</td>
                      <td>${order.clientName}</td>
                      <td><fmt:formatDate value="${order.clOrderDate}" pattern="yyyy-MM-dd" /></td>
                      <td><fmt:formatDate value="${order.clDeliveryDate}" pattern="yyyy-MM-dd" /></td>
                    </tr>
                    <tr class="order-detail" id="detail-${order.clOrderId}" style="display:none;">
                      <td colspan="4" id="detail-container-${order.clOrderId}">
                        <div class="text-muted">상세 정보를 불러오는 중....</div>
                      </td>
                    </tr>
                  </c:forEach>

                  <c:if test="${empty confirmedOrders}">
                    <tr>
                      <td colspan="4" class="text-center text-muted py-3">확정된 수주가 없습니다.</td>
                    </tr>
                  </c:if>
                </tbody>
              </table>
            </div>

            <div class="text-right mt-3">
              <button type="button" class="btn btn-primary" onclick="submitProductionPlan()">등록</button>
            </div>

			<!--  페이징 처리  -->
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
	  <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
     </div>
     <!-- 본문.jsp main-panel ends -->
  </div>   
  <!-- container-fluid page-body-wrapper 끝 -->
</div>
<!-- container-scroller 끝-->   


<script>
  //  currentOrderInfo 전역 변수 선언
  let currentOrderInfo = { clOrderId: null, dueDate: null };

  // 수주 행 클릭 시, 상세 제품 목록을 Ajax로 불러와 아래에 삽입한다
  document.querySelectorAll('.order-row').forEach(row => {
    row.addEventListener('click', function () {
      const orderId = this.dataset.orderId;
      const dueDate = this.dataset.dueDate;

      //  선택된 수주 정보를 currentOrderInfo에 저장
      currentOrderInfo.clOrderId = orderId;
      currentOrderInfo.dueDate = dueDate;

      const detailRow = document.getElementById('detail-' + orderId);
      const container = document.getElementById('detail-container-' + orderId);
      const isVisible = detailRow.style.display === 'table-row';

      document.querySelectorAll('.order-detail').forEach(r => r.style.display = 'none');

      if (!isVisible) {
        detailRow.style.display = 'table-row';
        fetch('/plan/select?clOrderId=' + orderId)
          .then(res => res.text())
          .then(html => container.innerHTML = html)
          .catch(() => container.innerHTML = '<div class="text-danger">불러오기 실패</div>');
      }
    });
  });

  // 등록 버튼 클릭 시 생산 계획 등록
  function submitProductionPlan() {
    console.log("등록 버튼 클릭됨");

    const checkedRows = document.querySelectorAll('input[name="selectedProduct"]:checked');
    if (checkedRows.length === 0) {
      alert('제품을 선택해주세요.');
      return;
    }

    if (!currentOrderInfo || !currentOrderInfo.clOrderId || !currentOrderInfo.dueDate) {
      alert('수주 정보가 없습니다. 수주를 먼저 선택해주세요.');
      return;
    }

    const { clOrderId, dueDate } = currentOrderInfo;
    const rows = [];
    let duplicateFound = false;
    const checkPromises = [];

    checkedRows.forEach(input => {
      const row = input.closest('tr');
      const productId = row.querySelector('input[name="productIdList"]').value;
      const plannedQty = row.querySelector('input[name="plannedQty"]').value;
      const priority = row.querySelector('select[name="priorityList"]').value || 'NORMAL';

      const dto = { clOrderId, productId };

      const check = fetch('/plan/check-duplicate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dto)
      })
        .then(res => res.json())
        .then(isDuplicate => {
          if (isDuplicate) {
            duplicateFound = true;
            alert(`❗ 이미 등록된 제품입니다: [${productId}]`);
          } else {
        	  rows.push({ clOrderId, dueDate, productId, plannedQty, priority });
          }
        });

      checkPromises.push(check);
    });

    Promise.all(checkPromises).then(() => {
      if (duplicateFound) return;

      fetch('/plan/register-form', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(rows)
      })
        .then(res => res.text())
        .then(result => {
          if (result === 'success') {
            alert(' 생산 계획이 등록되었습니다.');
            location.href = '/plan/list';
          } else {
            alert('등록에 실패했습니다.');
          }
        })
        .catch(err => {
          alert('오류가 발생했습니다.');
          console.error(err);
        });
    });
  }
</script>
