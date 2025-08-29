<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>

  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

    <!-- 본문 시작 -->
    <div class="main-panel">
      <div class="content-wrapper">

        <!-- 제목/뒤로가기 -->
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h3 class="mb-0">생산 실적 등록</h3>
          <a href="${cpath}/production/result/list" class="btn btn-outline-secondary">
            <i class="ti-arrow-left"></i> 목록으로
          </a>
        </div>

        <!-- 서버 메시지(선택) -->
        <c:if test="${not empty successMessage}">
          <div class="alert alert-success">${successMessage}</div>
        </c:if>
        <c:if test="${not empty errorMessage}">
          <div class="alert alert-danger">${errorMessage}</div>
        </c:if>

        <!-- 등록 폼 -->
        <form method="post" action="${cpath}/production/result/register" id="productionForm" autocomplete="off">

          <!-- 기본 정보 -->
          <div class="card-section">
            <h5 class="section-title">기본 정보</h5>

            <div class="row">
              <!-- 작업지시 선택 (컨트롤러에서 IN_PROGRESS만 내려옴) -->
              <div class="col-md-6">
                <div class="form-group">
                  <label class="form-label required">작업지시 선택</label>
                  <select name="orderId" id="orderId" class="form-control" required>
                    <option value="">작업지시를 선택하세요</option>

                    <c:forEach var="order" items="${workOrderList}">
                      <!-- producedQty 기본값 보정 (null -> 0) -->
                      <c:set var="producedQtyVal" value="${order.producedQty}" />
                      <c:if test="${empty producedQtyVal}">
                        <c:set var="producedQtyVal" value="0" />
                      </c:if>

                      <option value="${order.orderId}"
                              data-product="${order.productName}"
                              data-product-id="${order.productId}"
                              data-qty="${order.orderQty}"
                              data-line="${order.lineName}"
                              data-produced-qty="${producedQtyVal}">
                        ${order.orderId} - ${order.productName} (생산중)
                      </option>
                    </c:forEach>
                  </select>

                  <!-- 서버 전송용 -->
                  <input type="hidden" name="productId" id="productId">
                  <small class="form-text text-muted">* 생산중(IN_PROGRESS) 작업지시만 선택 가능</small>
                </div>
              </div>

              <!-- 작업자 -->
              <div class="col-md-6">
                <div class="form-group">
                  <label class="form-label required">작업자명</label>
                  <input type="text" name="workerName" class="form-control" placeholder="작업자명을 입력하세요" required>
                </div>
              </div>
            </div>

            <!-- 표시용 제품/라인/LOT(미리보기만) -->
            <div class="row">
              <div class="col-md-4">
                <div class="form-group">
                  <label class="form-label">제품명</label>
                  <input type="text" id="productDisplay" class="form-control" readonly style="background-color:#f8f9fa;">
                </div>
              </div>

              <div class="col-md-4">
                <div class="form-group">
                  <label class="form-label">라인명</label>
                  <input type="text" id="lineDisplay" class="form-control" readonly style="background-color:#f8f9fa;">
                </div>
              </div>

              <!-- LOT 번호는 서버 자동 생성: 화면에 미리보기만 표시, name 없음 -->
              <div class="col-md-4">
                <div class="form-group">
                  <label class="form-label">LOT 번호 (자동)</label>
                  <input type="text" id="lotNoDisplay" class="form-control" readonly style="background-color:#f8f9fa;">
                  <small class="form-text text-muted">저장 시 서버에서 자동 부여됩니다.</small>
                </div>
              </div>
            </div>
          </div>

          <!-- 수량 정보 -->
          <div class="card-section">
            <h5 class="section-title">생산 수량</h5>

            <div class="row">
              <div class="col-md-4">
                <div class="form-group">
                  <label class="form-label">계획수량</label>
                  <input type="text" id="totalOrderQty" class="form-control" readonly
                         style="background-color:#f8f9fa;text-align:right;font-weight:500;">
                </div>
              </div>

              <div class="col-md-4">
                <div class="form-group">
                  <label class="form-label">누적실적</label>
                  <input type="text" id="producedQtyDisplay" class="form-control" readonly
                         style="background-color:#f8f9fa;text-align:right;font-weight:500;">
                </div>
              </div>

              <div class="col-md-4">
                <div class="form-group">
                  <label class="form-label" style="color:#007bff;">잔여수량</label>
                  <input type="text" id="remainingQty" class="form-control" readonly
                         style="background-color:#e3f2fd;text-align:right;font-weight:bold;color:#1976d2;">
                </div>
              </div>
            </div>

            <div class="row">
              <div class="col-md-3">
                <div class="form-group">
                  <label class="form-label required">생산수량</label>
                  <input type="number" name="actualQty" id="actualQty" class="form-control"
                         placeholder="생산수량" min="1" required style="text-align:right;font-weight:500;">
                  <small class="form-text" id="qtyMessage" style="display:none;"></small>
                </div>
              </div>

              <div class="col-md-3">
                <div class="form-group">
                  <label class="form-label">불량수량</label>
                  <input type="number" name="defectQty" id="defectQty" class="form-control"
                         placeholder="불량수량" min="0" value="0" style="text-align:right;">
                  <small class="form-text text-danger" id="defectWarning" style="display:none;">
                    불량수량이 생산수량을 초과할 수 없습니다.
                  </small>
                </div>
              </div>
            </div>
          </div>

          <!-- 시간 정보 -->
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
                  <small class="form-text text-muted">* 미입력 시 저장 시점으로 자동 보정됩니다.</small>
                </div>
              </div>
            </div>
          </div>

          <!-- 버튼 -->
          <div class="d-flex justify-content-end mt-4">
            <button type="submit" class="btn btn-primary me-3">등록</button>
            <a href="${cpath}/production/result/list" class="btn btn-secondary">취소</a>
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
	  const lineDisplay = document.getElementById('lineDisplay');
	  const totalOrderQty = document.getElementById('totalOrderQty');
	  const producedQtyDisplay = document.getElementById('producedQtyDisplay');
	  const remainingQty = document.getElementById('remainingQty');
	  const productIdHidden = document.getElementById('productId');
	  const actualQtyInput = document.getElementById('actualQty');
	  const defectQtyInput = document.getElementById('defectQty');
	  const defectWarning = document.getElementById('defectWarning');
	  const qtyMessage = document.getElementById('qtyMessage');
	  const startedAtInput = document.querySelector('input[name="startedAt"]');
	  const endedAtInput = document.querySelector('input[name="endedAt"]');
	  const lotNoDisplay = document.getElementById('lotNoDisplay');

	  // product_id -> LOT 접두어 매핑 (예: FG-001=DG, FG-002=SD, FG-003=HW)
	  const prefixByProductId = {
	    'FG-001': 'DG', // 돼지국밥
	    'FG-002': 'SD', // 순대국밥
	    'FG-003': 'HW'  // 한우곰탕
	    // 필요 시 추가
	  };

	  // 서버에서 전달받은 날짜 사용 (EL 표현식으로 받음)
	  const todayStr = '${todayStr}'; // 컨트롤러에서 전달한 yyyyMMdd 형식 날짜

	  // 종료시간 기본값: 현재시간
	  (function setDefaultEnd(){
	    const now = new Date();
	    const local = new Date(now.getTime() - now.getTimezoneOffset()*60000).toISOString().slice(0,16);
	    endedAtInput.value = local;
	  })();

	  // 작업지시 선택 시 표시/숫자/LOT 미리보기 세팅
	  orderSelect.addEventListener('change', function () {
	    const opt = this.options[this.selectedIndex];

	    if (!opt || !opt.value) {
	      productDisplay.value = '';
	      lineDisplay.value = '';
	      productIdHidden.value = '';
	      totalOrderQty.value = '';
	      producedQtyDisplay.value = '';
	      remainingQty.value = '';
	      lotNoDisplay.value = '';
	      qtyMessage.style.display = 'none';
	      defectWarning.style.display = 'none';
	      return;
	    }

	    const totalQty = parseInt(opt.dataset.qty) || 0;
	    const producedQty = parseInt(opt.dataset.producedQty) || 0;
	    const remaining = Math.max(0, totalQty - producedQty);
	    const productId = opt.dataset.productId || '';

	    productDisplay.value = opt.dataset.product || '';
	    lineDisplay.value = opt.dataset.line || '';
	    productIdHidden.value = productId;

	    totalOrderQty.value = totalQty ? (totalQty.toLocaleString() + '개') : '';
	    producedQtyDisplay.value = producedQty.toLocaleString() + '개';
	    remainingQty.value = remaining.toLocaleString() + '개';

	    // LOT 미리보기 (전송 안함, 서버가 실제 생성) - 서버에서 받은 날짜 사용
	    const prefix = prefixByProductId[productId] || 'XX';
	    lotNoDisplay.value = `LOT-${prefix}-${todayStr}-***`;

	    qtyMessage.style.display = 'none';
	    defectWarning.style.display = 'none';
	    defectQtyInput.classList.remove('is-invalid');
	  });

  // 불량수량 검증
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
  actualQtyInput.addEventListener('input', validateDefectQty);
  defectQtyInput.addEventListener('input', validateDefectQty);

  // 생산수량: 잔여수량 초과 방지
  actualQtyInput.addEventListener('blur', function () {
    const v = parseInt(this.value) || 0;
    const remainingText = (remainingQty.value || '').replace(/[^0-9]/g, '');
    const remaining = parseInt(remainingText) || 0;

    if (v > remaining && remaining > 0) {
      this.value = remaining;
      qtyMessage.style.display = 'block';
      qtyMessage.className = 'form-text text-danger';
      qtyMessage.textContent = `입력값이 잔여수량을 초과하여 ${remaining.toLocaleString()}개로 보정했습니다.`;
    } else {
      qtyMessage.style.display = 'none';
    }
    validateDefectQty();
  });

  // 제출 전 검증
  form.addEventListener('submit', function (e) {
    const orderId = orderSelect.value;
    const actual = parseInt(actualQtyInput.value) || 0;
    const defect = parseInt(defectQtyInput.value) || 0;

    if (!orderId) { alert('작업지시를 선택하세요.'); e.preventDefault(); return; }
    if (actual <= 0) { alert('생산수량은 1 이상이어야 합니다.'); e.preventDefault(); return; }
    if (defect < 0) { alert('불량수량은 0 이상이어야 합니다.'); e.preventDefault(); return; }
    if (defect > actual) { alert('불량수량이 생산수량을 초과할 수 없습니다.'); e.preventDefault(); return; }

    // 잔여수량 초과 방지(최종)
    const remainingText = (remainingQty.value || '').replace(/[^0-9]/g, '');
    const remaining = parseInt(remainingText) || 0;
    if (remaining > 0 && actual > remaining) {
      alert('생산수량이 잔여수량을 초과할 수 없습니다.');
      e.preventDefault();
      return;
    }

    const sv = startedAtInput.value;
    const ev = endedAtInput.value;
    if (sv && ev) {
      const s = new Date(sv), ed = new Date(ev);
      if (ed < s) {
        alert('작업종료시간은 작업시작시간 이후여야 합니다.');
        e.preventDefault();
        return;
      }
    }

    // LOT은 서버가 자동 생성하므로 전송하지 않음
  });
});
</script>
