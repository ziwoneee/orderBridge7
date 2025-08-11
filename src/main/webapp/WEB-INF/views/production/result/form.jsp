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

        <!-- 제목 -->
        <div class="mb-4">
          <h3 class="mb-0">생산 실적 등록</h3>
        </div>

        <!-- 디버깅 정보 (임시) -->
        <div class="alert alert-info">
          작업지시 목록 개수: ${workOrderList != null ? workOrderList.size() : 0}개
        </div>

        <!-- 등록 폼 -->
        <form method="post" action="/production/result/register" id="productionForm">
          
          <!-- 기본 정보 섹션 -->
          <div class="card-section">
            <h5 class="section-title">기본 정보</h5>
            
            <div class="row">
              <div class="col-md-6">
                <div class="form-group">
                  <label class="form-label required">작업지시 선택</label>
					<select name="orderId" id="orderId" class="form-control" required>
					  <option value="">작업지시를 선택하세요</option>
					  <c:forEach var="order" items="${workOrderList}">
					    <option value="${order.orderId}" 
					            data-product="${order.productName}" 
					            data-product-id="${order.productId}"
					            data-qty="${order.orderQty}" 
					            data-line="${order.lineName}">
					      ${order.orderId} - ${order.productName} (${order.status})
					    </option>
					  </c:forEach>
					</select>

					<!-- hidden 필드는 select 바깥에 한 번만 -->
					<input type="hidden" name="productId" id="productId">
					
					<small class="form-text text-muted">진행중/완료 상태의 작업지시만 표시됩니다.</small>
                </div>
              </div>
              
              <div class="col-md-6">
                <div class="form-group">
                  <label class="form-label required">작업자명</label>
                  <input type="text" name="workerName" class="form-control" placeholder="작업자명을 입력하세요" required>
                </div>
              </div>
            </div>

            <div class="row">
              <div class="col-md-6">
                <div class="form-group">
                  <label class="form-label">제품명</label>
                  <input type="text" id="productDisplay" class="form-control" readonly style="background-color: #f8f9fa;">
                </div>
              </div>
              
              <div class="col-md-6">
                <div class="form-group">
                  <label class="form-label">라인명</label>
                  <input type="text" id="lineDisplay" class="form-control" readonly style="background-color: #f8f9fa;">
                </div>
              </div>
            </div>
          </div>

          <!-- 수량 정보 섹션 -->
          <div class="card-section">
            <h5 class="section-title">생산 수량</h5>
            
            <div class="row">
              <div class="col-md-4">
                <div class="form-group">
                  <label class="form-label">계획수량</label>
                  <input type="number" id="orderQtyDisplay" class="form-control" readonly style="background-color: #f8f9fa;">
                </div>
              </div>
              
              <div class="col-md-4">
                <div class="form-group">
                  <label class="form-label required">생산수량</label>
                  <input type="number" name="actualQty" id="actualQty" class="form-control" placeholder="생산수량" min="0" required>
                  <small class="form-text text-danger" id="qtyWarning" style="display: none;">계획수량을 초과할 수 없습니다.</small>
                </div>
              </div>
              
              <div class="col-md-4">
                <div class="form-group">
                  <label class="form-label">불량품수량</label>
                  <input type="number" name="defectQty" id="defectQty" class="form-control" placeholder="불량품수량" min="0" value="0">
                  <small class="form-text text-danger" id="defectWarning" style="display: none;">불량품수량이 생산수량을 초과할 수 없습니다.</small>
                </div>
              </div>
            </div>
          </div>

          <!-- 시간 정보 섹션 -->
          <div class="card-section">
            <h5 class="section-title">작업 시간</h5>
            
            <div class="row">
              <div class="col-md-6">
                <div class="form-group">
                  <label class="form-label required">작업시작시간</label>
                  <input type="datetime-local" name="startedAt" class="form-control" required>
                </div>
              </div>
              
              <div class="col-md-6">
                <div class="form-group">
                  <label class="form-label">작업종료시간</label>
                  <input type="datetime-local" name="endedAt" class="form-control">
                </div>
              </div>
            </div>
          </div>

          <!-- 버튼 영역 -->
          <div class="d-flex justify-content-end mt-4">
            <button type="submit" class="btn btn-primary me-4">
              등록
            </button>
            <a href="/production/result/list" class="btn btn-secondary">
              취소
            </a>
          </div>

        </form>

      </div>
      <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
    </div>
    <!-- 본문 끝 -->
  </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function () {
  const form = document.getElementById('productionForm');
  const orderSelect = document.getElementById('orderId');
  const productDisplay = document.getElementById('productDisplay');
  const orderQtyDisplay = document.getElementById('orderQtyDisplay');
  const lineDisplay = document.getElementById('lineDisplay');
  const actualQtyInput = document.getElementById('actualQty');
  const defectQtyInput = document.getElementById('defectQty');
  const qtyWarning = document.getElementById('qtyWarning');
  const defectWarning = document.getElementById('defectWarning');
  const startedAtInput = document.querySelector('input[name="startedAt"]');
  const endedAtInput = document.querySelector('input[name="endedAt"]');
  const productIdHidden = document.getElementById('productId');

  // 로컬시간을 datetime-local 형식으로
  function getLocalDatetimeForInput(date = new Date()) {
    const d = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
    return d.toISOString().slice(0, 16);
  }

  // 초기 로딩 시 종료시간 = 현재 로컬시각
  endedAtInput.value = getLocalDatetimeForInput(new Date());

  // 작업지시 선택 시 표시값/hidden 세팅
  orderSelect.addEventListener('change', function () {
    const opt = this.options[this.selectedIndex];

    if (opt && opt.value) {
      productDisplay.value = opt.dataset.product || '';
      orderQtyDisplay.value = opt.dataset.qty || '';
      lineDisplay.value = opt.dataset.line || '';
      productIdHidden.value = opt.dataset.productId || ''; // ★ hidden 설정
    } else {
      productDisplay.value = '';
      orderQtyDisplay.value = '';
      lineDisplay.value = '';
      productIdHidden.value = '';
    }

    // 경고 초기화
    actualQtyInput.classList.remove('is-invalid');
    defectQtyInput.classList.remove('is-invalid');
    qtyWarning.style.display = 'none';
    defectWarning.style.display = 'none';
  });

  // 페이지가 열렸을 때 이미 선택된 값이 있다면 hidden도 세팅
  (function initSelected() {
    const opt = orderSelect.options[orderSelect.selectedIndex];
    if (opt && opt.value) {
      productIdHidden.value = opt.dataset.productId || '';
    }
  })();

  // 생산수량 실시간 검증
  actualQtyInput.addEventListener('input', function () {
    const actual = parseInt(this.value) || 0;
    const plan = parseInt(orderQtyDisplay.value) || 0;

    if (plan > 0 && actual > plan) {
      qtyWarning.style.display = 'block';
      this.classList.add('is-invalid');
    } else {
      qtyWarning.style.display = 'none';
      this.classList.remove('is-invalid');
    }
    validateDefectQty();
  });

  // 불량수량 실시간 검증
  defectQtyInput.addEventListener('input', validateDefectQty);
  function validateDefectQty() {
    const actual = parseInt(actualQtyInput.value) || 0;
    const defect = parseInt(defectQtyInput.value) || 0;

    if (actual > 0 && defect > actual) {
      defectWarning.style.display = 'block';
      defectQtyInput.classList.add('is-invalid');
    } else {
      defectWarning.style.display = 'none';
      defectQtyInput.classList.remove('is-invalid');
    }
  }

  // 제출 최종 검증
  form.addEventListener('submit', function (e) {
    const orderId = orderSelect.value;
    const actual = parseInt(actualQtyInput.value) || 0;
    const defect = parseInt(defectQtyInput.value) || 0;
    const plan = parseInt(orderQtyDisplay.value) || 0;

    if (!orderId) { alert('작업지시를 선택하세요.'); e.preventDefault(); return; }
    if (actual <= 0) { alert('생산수량은 0보다 커야 합니다.'); e.preventDefault(); return; }
    if (defect < 0) { alert('불량수량은 0 이상이어야 합니다.'); e.preventDefault(); return; }
    if (defect > actual) { alert('불량수량이 생산수량을 초과할 수 없습니다.'); e.preventDefault(); return; }
    if (plan > 0 && actual > plan) { alert('생산수량이 계획수량을 초과했습니다.'); e.preventDefault(); return; }

    const startedVal = startedAtInput.value;
    const endedVal = endedAtInput.value;
    if (startedVal && endedVal) {
      const startDate = new Date(startedVal);
      const endDate = new Date(endedVal);
      if (endDate < startDate) {
        alert('작업종료시간은 작업시작시간 이후여야 합니다.');
        e.preventDefault();
        return;
      }
    }

    if (!endedVal) {
      endedAtInput.value = getLocalDatetimeForInput(new Date());
    }
  });
});
</script>