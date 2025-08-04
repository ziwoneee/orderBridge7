/************************************************************
 * materialInbound.js - 자재 입고 관리 관련 JavaScript 기능 모음 (수정본)
 ************************************************************/

/* [1] 입고 상세 모달 불러오기 */
function loadInboundDetail(inboundId) {
  $.ajax({
    url: '/material/inbound/detail',
    method: 'GET',
    data: { inboundId: inboundId },
    success: function(data) {
      $('#inboundId').text(data.inboundId);
      $('#orderId').text(data.orderId);
      $('#expectedArrivedDate').text(data.expectedArrivedDate);
      $('#orderDate').text(data.orderDate);
      $('#supplierId').text(data.supplierId);
      $('#inboundDate').text(data.inboundDate);
      $('#handledBy').text(data.handledBy);
      $('#modalStatus').text(data.status);

      const tbody = $("#inboundInfo").empty();

      data.items.forEach(item => {
        console.log('item 내용:', item); // 디버깅용

        const safeItem = encodeURIComponent(JSON.stringify(item));

        const row = `
          <tr>
            <td>${item.materialId}</td>
            <td>${item.materialName}</td>
            <td>${item.orderQty}</td>
            <td>${item.inboundQty}</td>
            <td>${item.unitPrice}</td>
            <td>${item.totalPrice}</td>
            <td>
              <button class="btn btn-sm btn-outline-success btn-inbound"
                      data-item='${safeItem}'>
                입고처리
              </button>
            </td>
          </tr>
        `;
        tbody.append(row);
      });

      $('#inboundDetailModal').modal('show');
    },
    error: function(xhr) {
      console.error('상세 정보 로드 실패:', xhr.responseText);
      alert('상세 정보를 불러오는 데 실패했습니다.');
    }
  });
}

/* [2] 입고 모달 열기 */
function openInboundModal(item) {
  console.log('[모달 열기] item:', item);

  $.ajax({
    url: '/material/inbound/generate-lot',
    method: 'GET',
    data: { materialId: item.materialId },
    success: function(lotNo) {
      console.log('LOT No:', lotNo);

      $('#materialId').val(item.materialId);
      $('#materialName').val(item.materialName);
      $('#lotNo').val(lotNo);
      $('#expirationDate').val(item.expirationDate || '');
      $('#quantity').val(item.orderQuantity || item.orderQty || 0);
      $('#warehouseCode').val('WH001');
      $('#inboundId').val(item.inboundId);

      $('#inboundModal').modal('show');
    },
    error: function(xhr) {
      console.error('LOT 번호 생성 실패:', xhr.responseText);
      alert('LOT 번호를 생성하지 못했습니다.');
    }
  });
}

/* [3] 입고 처리 (단일 항목) - 수정된 버전 */
function processInboundItem() {
  const lotNo = $('#lotNo').val().trim();
  const expirationDate = $('#expirationDate').val().trim();
  const quantity = parseInt($('#quantity').val(), 10);
  const warehouseCode = $('#warehouseCode').val();
  const materialId = $('#materialId').val();
  const inboundId = $('#inboundId').val(); // .text()가 아닌 .val() 사용

  // 유효성 검사
  if (!lotNo) return alert('LOT 번호를 입력하거나 자동 생성해야 합니다.');
  if (!expirationDate) return alert('유통기한을 입력해주세요.');
  if (!quantity || quantity <= 0) return alert('입고 수량은 1 이상이어야 합니다.');
  if (!warehouseCode) return alert('창고를 선택해주세요.');
  if (!materialId) return alert('자재 ID가 없습니다.');
  if (!inboundId) return alert('입고 ID가 없습니다.');

  const inboundItemData = {
    inboundId,
    materialId,
    lotNo,
    expirationDate,
    quantity,
    warehouseCode
  };

  $.ajax({
    url: '/material/inbound/item/process',
    type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(inboundItemData),
    success: function () {
      alert('입고 처리가 완료되었습니다.');
      $('#inboundModal').modal('hide');
      $('#inboundDetailModal').modal('hide');
      location.reload();
    },
    error: function (xhr) {
      console.error('입고 처리 실패:', xhr.responseText);
      alert('입고 처리 중 오류가 발생했습니다.\n' + xhr.responseText);
    }
  });
}

/* [4] 미입고 발주 테이블 렌더링 */
function renderUnreceivedOrders(orderList) {
  console.log('렌더링할 주문 목록:', orderList);
  
  const tbody = document.querySelector('#unreceivedOrderTable tbody');
  tbody.innerHTML = '';

  if (!orderList || orderList.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" class="text-center text-muted py-4">
          <i class="ti-info-alt" style="font-size: 24px;"></i>
          <p class="mt-2">미입고 발주건이 없습니다.</p>
        </td>
      </tr>
    `;
    return;
  }

  orderList.forEach(order => {
    const row = document.createElement('tr');
    
    // 날짜 변환 함수
    const formatTimestamp = (timestamp) => {
      if (!timestamp) return '-';
      const date = new Date(timestamp);
      return date.toLocaleDateString('ko-KR');
    };
    
    // D-Day 계산
    const calculateDDay = (expectedDate) => {
      if (!expectedDate) return '';
      
      const today = new Date();
      const expected = new Date(expectedDate);
      const diffTime = expected - today;
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      
      if (diffDays < 0) {
        return '<span class="badge badge-danger badge-pill ml-1">지연</span>';
      } else if (diffDays <= 2) {
        return `<span class="badge badge-warning badge-pill ml-1">D-${diffDays}</span>`;
      }
      return '';
    };

    row.innerHTML = `
      <td>
        <input type="checkbox" name="selectedOrders" value="${order.orderId}" class="order-checkbox">
      </td>
      <td class="font-weight-medium">${order.orderId}</td>
      <td>${order.materialNames || order.materialName || '-'}</td>
      <td class="text-end">${(order.totalOrderQuantity || order.totalQuantity || 0).toLocaleString()}</td>
      <td>
        ${formatTimestamp(order.expectedArrivedDate)}
        ${calculateDDay(order.expectedArrivedDate)}
      </td>
      <td>${order.handledBy || order.createdBy || '-'}</td>
      <td>
        <button class="btn btn-sm btn-outline-info" onclick="viewOrderDetail('${order.orderId}')">
          상세보기
        </button>
      </td>
    `;
    tbody.appendChild(row);
  });
  
  console.log(`${orderList.length}개의 미입고 발주건을 렌더링했습니다.`);
}

/* [5] 페이징 버튼 렌더링 */
function renderPagination(pageMaker) {
  const container = document.getElementById("unreceivedPagination");
  container.innerHTML = "";

  const ul = document.createElement("ul");
  ul.className = "pagination";

  // 이전 페이지 버튼
  if (pageMaker.prev) {
    const li = document.createElement("li");
    li.className = "page-item";
    li.innerHTML = `<a class="page-link" href="#" onclick="loadUnreceivedOrders(${pageMaker.startPage - 1})">&laquo;</a>`;
    ul.appendChild(li);
  }

  // 페이지 번호 버튼들
  for (let i = pageMaker.startPage; i <= pageMaker.endPage; i++) {
    const li = document.createElement("li");
    li.className = `page-item ${pageMaker.cri.page === i ? 'active' : ''}`;
    li.innerHTML = `<a class="page-link" href="#" onclick="loadUnreceivedOrders(${i})">${i}</a>`;
    ul.appendChild(li);
  }

  // 다음 페이지 버튼
  if (pageMaker.next) {
    const li = document.createElement("li");
    li.className = "page-item";
    li.innerHTML = `<a class="page-link" href="#" onclick="loadUnreceivedOrders(${pageMaker.endPage + 1})">&raquo;</a>`;
    ul.appendChild(li);
  }

  container.appendChild(ul);
}

/* [6] 발주 상세 보기 */
function viewOrderDetail(orderId) {
  alert(`발주 상세보기 기능 구현 필요: ${orderId}`);
}

/* [7] 전체 선택/해제 */
function toggleAllCheckboxes(checkAllBox) {
  const checkboxes = document.querySelectorAll('input[name="selectedOrders"]');
  checkboxes.forEach(checkbox => {
    checkbox.checked = checkAllBox.checked;
  });
}

/* [8] 선택된 발주 입고등록 */
function registerSelectedOrders() {
  const selectedOrders = [];
  const addedIds = {};
  
  document.querySelectorAll('input[name="selectedOrders"]:checked').forEach(checkbox => {
    const orderId = checkbox.value;
    if (orderId && !addedIds[orderId]) {
      selectedOrders.push(orderId);
      addedIds[orderId] = true;
    }
  });

  if (selectedOrders.length === 0) {
    alert('입고등록할 발주를 선택해주세요.');
    return;
  }

  if (!confirm(`선택된 ${selectedOrders.length}건의 발주를 입고등록하시겠습니까?`)) {
    return;
  }

  const button = $('#btn-insert-unreceived');
  const originalText = button.html();
  button.html('<i class="ti-reload"></i> 처리중...').prop('disabled', true);

  $.ajax({
    type: 'POST',
    url: '/material/inbound/insert-unreceived',
    data: { orderIds: selectedOrders },
    traditional: true,
    success: function(response) {
      alert('선택된 발주건이 성공적으로 입고등록되었습니다.');
      location.reload();
    },
    error: function(xhr, status, error) {
      console.error('입고등록 실패:', xhr.responseText);
      alert('입고등록 중 오류가 발생했습니다.\n' + xhr.responseText);
    },
    complete: function() {
      button.html(originalText).prop('disabled', false);
    }
  });
}

/* [9] 문서 로딩 시 이벤트 바인딩 */
$(document).ready(function () {
  // 미입고 발주 입고등록 버튼
  $('#btn-insert-unreceived').on('click', function () {
    registerSelectedOrders();
  });

  // 개별 자재 입고처리 버튼 (테이블 내의 버튼)
  $(document).on('click', '.btn-inbound', function () {
    const encoded = $(this).attr('data-item');
    const decoded = decodeURIComponent(encoded);
    const item = JSON.parse(decoded);
    openInboundModal(item);
  });

  // 입고처리 모달의 저장 버튼
  $('#btnSaveInbound').on('click', function () {
    processInboundItem();
  });

  console.log('materialInbound.js 로드 완료');
});

// ============================================================================
// 추가: 테이블 상세 보기를 위한 함수 (입고처리 버튼과 구분)
// ============================================================================

/**
 * 메인 테이블의 "입고처리" 버튼 클릭 시 호출되는 함수
 * 입고 ID를 받아서 상세 모달을 엽니다.
 * @param {string} inboundId - 입고 ID
 */
function showInboundDetail(inboundId) {
  console.log('입고 상세보기 호출:', inboundId);
  loadInboundDetail(inboundId);
}