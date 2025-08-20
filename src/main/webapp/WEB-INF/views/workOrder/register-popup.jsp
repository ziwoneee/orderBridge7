<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
  <title>작업지시 등록</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.1">

  <link rel="icon" href="${pageContext.request.contextPath}/favicon.ico" type="image/x-icon" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/vendors/css/vendor.bundle.base.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/vertical-layout-light/style.css">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@mdi/font@6.5.95/css/materialdesignicons.min.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/popup-style.css">
</head>
<body>

<div class="page-title">
  <i class="fas fa-plus-circle"></i> 작업지시 등록
</div>

<form id="workOrderForm">
  <!-- 기본 파라미터 -->
  <input type="hidden" name="dueDate" value="${dueDate}" />
  <input type="hidden" name="productId" id="productId" value="${productId}" />
  <input type="hidden" name="orderQty" id="orderQty" value="${requiredQty}" />
  <input type="hidden" name="status" value="WAITING" />

  <!-- 병합 수주 JSON (안전 파싱용) -->
  <script id="clOrderIdsJson" type="application/json">
    ${fn:escapeXml(clOrderIdsJson)}
  </script>

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
              <th>수주번호</th>
              <th>거래처</th>
              <th class="text-right">수량</th>
            </tr>
            </thead>
            <tbody id="mergedOrdersTableBody"><!-- 동적 생성 --></tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <!-- 작업지시 설정 -->
<div class="card">
  <div class="card-body">
    <div class="row">
      <!-- 생산라인: 선택 가능 -->
      <div class="col-md-6">
        <label>생산라인 <span class="text-danger">*</span></label>
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
        <label>우선순위 <span class="text-danger">*</span></label>
        <select class="form-control" name="priority" required>
          <option value="HIGH">높음</option>
          <option value="NORMAL" selected>보통</option>
          <option value="LOW">낮음</option>
        </select>
      </div>
    </div>

    <div class="form-group mt-3">
      <label>특이사항</label>
      <textarea class="form-control" name="remarks" rows="2"></textarea>
    </div>
  </div>
</div>

  <!-- BOM 자재 소요량 -->
  <div class="card mt-3">
    <div class="card-header bg-info text-white">
      <i class="fas fa-boxes"></i> 자재 소요량
    </div>
    <div class="card-body">
      <table class="table table-bordered">
        <thead>
        <tr>
          <th>자재코드</th>
          <th>자재명</th>
          <th>용도</th>
          <th class="text-center">10팩당</th>
          <th class="text-center">총 소요량</th>
          <th>단위</th>
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

  <!-- 버튼 -->
  <div class="mt-4 text-right">
    <button type="button" class="btn btn-secondary" onclick="window.close()">취소</button>
    <button type="submit" class="btn btn-primary">등록</button>
  </div>
</form>

<!-- JS: jQuery + Bootstrap Bundle (collapse 동작용) -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="${pageContext.request.contextPath}/resources/vendors/js/vendor.bundle.base.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/js/bootstrap.bundle.min.js"></script>

<script>
(function() {
  // 병합 수주 JSON 안전 파싱
  let mergedOrders = [];
  try {
    const el = document.getElementById('clOrderIdsJson');
    mergedOrders = el && el.textContent ? JSON.parse(el.textContent) : [];
  } catch (e) {
    console.warn('clOrderIdsJson parse error', e);
    mergedOrders = [];
  }
  console.log('병합된 수주:', mergedOrders);

  // 병합 수주 섹션 표시/렌더링
  if (Array.isArray(mergedOrders) && mergedOrders.length > 1) {
    document.getElementById('mergedOrdersSection').style.display = 'block';
    document.getElementById('mergedCount').innerText = mergedOrders.length;

    const tbody = document.getElementById('mergedOrdersTableBody');
    tbody.innerHTML = '';
    mergedOrders.forEach(function(item) {
      // item: { clOrderId, clientName, orderQty }  또는 문자열 배열
      const id = item.clOrderId || item;
      const client = item.clientName || '-';
      const qty = (item.orderQty != null) ? item.orderQty : '-';
      const tr = document.createElement('tr');
      tr.innerHTML =
        '<td>' + id + '</td>' +
        '<td>' + client + '</td>' +
        '<td class="text-right">' + qty + '</td>';
      tbody.appendChild(tr);
    });
  }

  // BOM 로딩
  const productId = document.getElementById('productId').value;
  const orderQty = parseInt(document.getElementById('orderQty').value || '0', 10);

  function loadBom(productId, orderQty) {
    $.ajax({
      url: '${pageContext.request.contextPath}/workorder/getBomByProduct',
      type: 'GET',
      data: { productId: productId, orderQty: orderQty },
      success: function(bomList) {
        const tbody = $('#bomTableBody');
        tbody.empty();

        if (!bomList || !bomList.length) {
          tbody.html('<tr><td colspan="6" class="text-center text-muted">BOM 정보 없음</td></tr>');
          window.materialList = [];
          return;
        }

        const packs = orderQty / 10; // 10팩 기준
        const hiddenMaterials = [];

        bomList.forEach(function(item) {
          const per10 = Number(item.qty) || 0;
          const total = per10 * packs; // 필요 시 Math.ceil(total) 로 변경 가능

          hiddenMaterials.push({ materialId: item.materialId, requiredQty: total });

          const row =
            '<tr>' +
              '<td>' + item.materialId + '</td>' +
              '<td>' + (item.materialName || '') + '</td>' +
              '<td>' + (item.materialType || '') + '</td>' +
              '<td class="text-center">' + per10 + '</td>' +
              '<td class="text-center font-weight-bold">' + total.toFixed(1) + '</td>' +
              '<td>' + (item.unit || '') + '</td>' +
            '</tr>';
          tbody.append(row);
        });

        window.materialList = hiddenMaterials;
      },
      error: function() {
        $('#bomTableBody').html('<tr><td colspan="6" class="text-center text-danger">로딩 실패</td></tr>');
        window.materialList = [];
      }
    });
  }

  if (productId && orderQty > 0) {
    loadBom(productId, orderQty);
  }

  // 병합 수주 접기/펼치기 아이콘 토글
  $('#mergedOrdersList').on('show.bs.collapse', function () {
    $('a[href="#mergedOrdersList"] i').removeClass('fa-chevron-down').addClass('fa-chevron-up');
  }).on('hide.bs.collapse', function () {
    $('a[href="#mergedOrdersList"] i').removeClass('fa-chevron-up').addClass('fa-chevron-down');
  });

  // CSRF 토큰(SPRING SECURITY 사용 시)
  var csrfHeader = $('meta[name="_csrf_header"]').attr('content');
  var csrfToken  = $('meta[name="_csrf"]').attr('content');

  // 제출
  let isSubmitting = false;
  $('#workOrderForm').on('submit', function(e) {
    e.preventDefault();
    if (isSubmitting) return;

    const lineIdVal = $('#lineId').val();
    if (!lineIdVal) {
      alert('생산라인을 선택하세요.');
      return;
    }

    isSubmitting = true;

    const payload = {
      productId: productId,
      orderQty: orderQty,
      dueDate: $('input[name="dueDate"]').val(),
      lineId: lineIdVal, // 드롭다운 값 사용
      priority: $('select[name="priority"]').val(),
      remarks: $('textarea[name="remarks"]').val(),
      status: "WAITING",
      materialList: window.materialList || [],
      mergedOrders: mergedOrders
    };

    const $btn = $('button[type="submit"]').prop('disabled', true).text('등록 중...');

    $.ajax({
      url: '${pageContext.request.contextPath}/workorder/register',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(payload),
      beforeSend: function(xhr) {
        if (csrfHeader && csrfToken) xhr.setRequestHeader(csrfHeader, csrfToken);
      },
      success: function(res) {
        if (res && res.success) {
          // ✅ 부모 새로고침 트리거 (3중 안전망)
          try {
            if (window.opener && !window.opener.closed) {
              // 1) postMessage
              window.opener.postMessage({ type: 'WORK_ORDER_CREATED' }, window.location.origin);
            }
          } catch(e) {}

          try {
            // 2) storage 이벤트 (팝업 닫혀도 부모가 감지)
            localStorage.setItem('WORK_ORDER_REFRESH', String(Date.now()));
          } catch(e) {}

          try {
            // 3) 직접 리로드 시도
            if (window.opener && !window.opener.closed) {
              window.opener.location.reload();
            }
          } catch(e) {}

          alert('등록 성공!');
          window.close();
        } else {
          alert('등록 실패: ' + (res && res.message ? res.message : '알 수 없는 오류'));
          isSubmitting = false;
          $btn.prop('disabled', false).text('등록');
        }
      },
      error: function(err) {
        console.error(err);
        alert('서버 오류가 발생했습니다.');
        isSubmitting = false;
        $btn.prop('disabled', false).text('등록');
      }
    });
  });
})();
</script>

</body>
</html>