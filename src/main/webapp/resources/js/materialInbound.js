/************************************************************
 * materialInbound.js - 자재 입고 관리 관련 JavaScript 기능 모음 (수정본)
 ************************************************************/

// 날짜 포맷 함수
function formatDateString(timestamp) {
  if (!timestamp) return '-';
  const date = new Date(timestamp);
  return date.toISOString().substring(0, 10); // 'yyyy-MM-dd'
}

/* [1] 입고 상세 모달 불러오기 */
function loadInboundDetail(inboundId) {
  console.log('입고 상세 조회 요청:', inboundId);
  
  $.ajax({
    url: '/material/inbound/detail',
    method: 'GET',
    data: { inboundId: inboundId },
    success: function(data) {
      console.log('상세 정보 응답:', data);
      
      // 기본 정보 설정
      $('#inboundId').text(data.inbound.inboundId || '-');
      $('#orderId').text(data.inbound.orderId || '-');
      $('#expectedArrivedDate').text(formatDateString(data.inbound.expectedArrivedDate));
      $('#orderDate').text(formatDateString(data.inbound.orderDate));
      $('#supplierId').text(data.inbound.supplierId || '-');
      $('#inboundDate').text(formatDateString(data.inbound.inboundDate));
      $('#handledBy').text(data.inbound.handledBy || '-');
      $('#modalStatus').text(data.inbound.inboundStatus || '-');

      // 항목 정보 렌더링
      const tbody = $("#inboundInfo").empty();
      const items = data.inboundItems; // ✅ 올바른 필드명 사용

      if (items && items.length > 0) {
        items.forEach(item => {
          const safeItem = encodeURIComponent(JSON.stringify(item));

          const row = `
            <tr>
              <td>${item.materialId}</td>
              <td>${item.materialName}</td>
              <td class="text-end">${(item.orderQuantity || 0).toLocaleString()}</td>
              <td class="text-end">${(item.quantity || 0).toLocaleString()}</td>
              <td class="text-end">${(item.unitPrice || 0).toLocaleString()}</td>
              <td class="text-end">${(item.totalPrice || 0).toLocaleString()}</td>
              <td class="text-center">
                ${item.inboundStatus !== '입고완료' ? 
                  `<button class="btn btn-sm btn-outline-success btn-inbound"
                          data-item='${safeItem}'>
                    입고처리
                  </button>` : 
                  '<span class="badge badge-success">완료</span>'
                }
              </td>
            </tr>
          `;
          tbody.append(row);
        });
      } else {
        tbody.append(`
          <tr>
            <td colspan="7" class="text-center">입고 항목이 없습니다.</td>
          </tr>
        `);
      }


      $('#inboundDetailModal').modal('show');
    },
    error: function(xhr, status, error) {
      console.error('상세 정보 로드 실패:', xhr.responseText);
      alert('상세 정보를 불러오는 데 실패했습니다.\n' + (xhr.responseText || error));
    }
  });
}

/* [2] 입고 모달 열기 */
function openInboundModal(itemOrId) {
  console.log('[모달 열기] 전달된 값:', itemOrId);

  // 1. 문자열인데 JSON이 아니면 → 그냥 inboundId로 간주해서 전체 상세 불러오기
  if (typeof itemOrId === 'string' && !itemOrId.trim().startsWith('{')) {
    loadInboundDetail(itemOrId); // ✅ 모달 자동으로 뜨는 구조
    return;
  }

  // 2. 문자열인데 JSON 형태라면 → parse
  let item = itemOrId;
  if (typeof itemOrId === 'string') {
    try {
      item = JSON.parse(decodeURIComponent(itemOrId));
    } catch (e) {
      console.error('item 파싱 실패:', e);
      alert('항목 정보를 읽을 수 없습니다.');
      return;
    }
  }

  // 3. 자재 항목 기반 모달 오픈
  const materialId = item.materialId;
  if (!materialId) {
    console.error('materialId가 없습니다:', item);
    alert('자재 ID 정보가 없습니다.');
    return;
  }

  $.ajax({
    url: '/material/inbound/generate-lot',
    method: 'GET',
    data: { materialId: materialId },
    success: function(lotNo) {
      console.log('LOT No:', lotNo);

      // 모달 필드 설정
      $('#materialId').val(item.materialId);
      $('#materialName').val(item.materialName);
      $('#lotNo').val(lotNo);
      $('#expirationDate').val(item.expirationDate || '');
      $('#quantity').val(item.orderQuantity || item.orderQty || 0);
      $('#warehouseCode').val('WH001');
      $('#inboundId').val(item.inboundId);

      $('#inboundModal').modal('show');
    },
    error: function(xhr, status, error) {
      console.error('LOT 번호 생성 실패:', xhr.responseText);
      alert('LOT 번호를 생성하지 못했습니다.\n' + (xhr.responseText || error));
    }
  });
}


/* [3] 미입고 발주 목록 불러오기 */
function loadUnreceivedOrders(page = 1) {
  console.log('미입고 발주 목록 요청, 페이지:', page);
  
  $.ajax({
    url: '/material/inbound/unreceived-orders',
    method: 'GET',
    data: { 
      page: page,
      perPageNum: 10
    },
    success: function(response) {
      console.log('미입고 발주 응답:', response);
      
      if (response && response.list) {
        renderUnreceivedOrders(response.list);
        if (response.pageMaker) {
          renderPagination(response.pageMaker);
        }
        $('#unreceivedOrdersSection').show();
      } else {
        console.error('응답 데이터 형식이 올바르지 않습니다:', response);
        alert('미입고 발주 목록을 불러오는데 실패했습니다.');
      }
    },
    error: function(xhr, status, error) {
      console.error('미입고 발주 목록 로드 실패:', xhr.responseText);
      alert('미입고 발주 목록을 불러오는데 실패했습니다.\n' + (xhr.responseText || error));
    }
  });
}

/* [4] 입고 처리 (단일 항목) */
function processInboundItem() {
  const lotNo = $('#lotNo').val().trim();
  const expirationDate = $('#expirationDate').val().trim();
  const quantity = parseInt($('#quantity').val(), 10);
  const warehouseCode = $('#warehouseCode').val();
  const materialId = $('#materialId').val();
  const inboundId = $('#inboundId').val();

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

/* [5] 미입고 발주 테이블 렌더링 */
function renderUnreceivedOrders(orderList) {
  console.log('렌더링할 주문 목록:', orderList);
  
  const tbody = document.querySelector('#unreceivedOrderTable tbody');
  if (!tbody) {
    console.error('테이블 tbody를 찾을 수 없습니다.');
    return;
  }
  
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


/* [6] 페이징 버튼 렌더링 */
function renderPagination(pageMaker) {
  const container = document.getElementById("unreceivedPagination");
  if (!container) return;
  
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

/* [7] 발주 상세 보기 */
function viewOrderDetail(orderId) {
  alert(`발주 상세보기 기능 구현 필요: ${orderId}`);
}

/* [8] 전체 선택/해제 */
function toggleAllCheckboxes(checkAllBox) {
  const checkboxes = document.querySelectorAll('input[name="selectedOrders"]');
  checkboxes.forEach(checkbox => {
    checkbox.checked = checkAllBox.checked;
  });
}

/* [9] 선택된 발주 입고등록 */
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

/* [10] 문서 로딩 시 이벤트 바인딩 */
$(document).ready(function () {
  console.log('materialInbound.js 로드 시작');

  // 미입고 발주 입고등록 버튼
  $('#btn-insert-unreceived').on('click', function () {
    registerSelectedOrders();
  });

  // 개별 자재 입고처리 버튼 (테이블 내의 버튼)
  $(document).on('click', '.btn-inbound', function () {
    const encoded = $(this).attr('data-item');
    if (encoded) {
      const decoded = decodeURIComponent(encoded);
      const item = JSON.parse(decoded);
      openInboundModal(item);
    }
  });

  // 입고처리 모달의 저장 버튼
  $('#btnSaveInbound').on('click', function () {
    processInboundItem();
  });

  console.log('materialInbound.js 로드 완료');
});

/**
 * 메인 테이블의 "입고처리" 버튼 클릭 시 호출되는 함수
 */
function showInboundDetail(inboundId) {
  console.log('입고 상세보기 호출:', inboundId);
  loadInboundDetail(inboundId);
}