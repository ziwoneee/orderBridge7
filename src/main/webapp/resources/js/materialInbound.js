/************************************************************
 * materialInbound.js - 수정된 버전
 * - 자재 입고 관리 관련 JavaScript 기능 모음
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
        const row = `
          <tr>
            <td>${item.materialId}</td>
            <td>${item.materialName}</td>
            <td>${item.orderQty}</td>
            <td>${item.inboundQty}</td>
            <td>${item.unitPrice}</td>
            <td>${item.totalPrice}</td>
          </tr>
        `;
        tbody.append(row);
      });

      $('#inboundDetailModal').modal('show');
    },
    error: function(xhr, status, error) {
      console.error('상세 정보 로드 실패:', xhr.responseText);
      alert('상세 정보를 불러오는 데 실패했습니다.');
    }
  });
}

/* [2] 입고 처리 */
function processInbound(inboundId, button) {
  if (!confirm('해당 입고건을 처리하시겠습니까?')) return;

  $.ajax({
    url: '/material/inbound/process',
    type: 'POST',
    data: { inboundId: inboundId },
    success: function() {
      alert('입고 처리 완료!');
      location.reload();
    },
    error: function(xhr, status, error) {
      console.error('입고 처리 실패:', xhr.responseText);
      alert('입고 처리 중 오류가 발생했습니다.');
    }
  });
}

/* [3] 미입고 발주 목록 불러오기 */
function loadUnreceivedOrders(page = 1) {
  console.log(`미입고 발주 목록 요청 - 페이지: ${page}`);
  
  // 미입고 발주 섹션 표시
  $('#unreceivedOrdersSection').show();
  
  // 로딩 상태 표시
  const tbody = document.querySelector('#unreceivedOrderTable tbody');
  tbody.innerHTML = `
    <tr>
      <td colspan="7" class="text-center py-4">
        <div class="spinner-border text-primary" role="status">
          <span class="sr-only">Loading...</span>
        </div>
        <p class="mt-2">미입고 발주 목록을 불러오는 중...</p>
      </td>
    </tr>
  `;

  $.ajax({
    url: "/material/inbound/unreceived-orders",
    method: 'GET',
    dataType: 'json',
    data: {
        page: page,           // page로 수정 (기존: page)
        perPageNum: 10       // perPageNum로 수정 (기존: perPageNum)
    },
    success: function(data) {
      console.log('응답 데이터:', data);
      renderUnreceivedOrders(data.list);
      renderPagination(data.pageMaker);
    },
    error: function(xhr, status, error) {
      console.error('AJAX 오류:', xhr.responseText);
      
      const tbody = document.querySelector('#unreceivedOrderTable tbody');
      tbody.innerHTML = `
        <tr>
          <td colspan="7" class="text-center text-danger py-4">
            <i class="ti-alert"></i>
            <p class="mt-2">미입고 발주 목록을 불러오는 중 오류가 발생했습니다.</p>
            <small>오류: ${xhr.responseText}</small>
          </td>
        </tr>
      `;
      alert(`오류: ${xhr.responseText}`);
    }
  });
}

/* [4] 미입고 발주 테이블 렌더링 - 수정된 버전 */
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

/* [8] 선택된 발주 입고등록 - 수정된 버전 */
function registerSelectedOrders() {
  const selectedOrders = [];
  const addedIds = {}; // 중복 체크를 위한 객체 (HashMap 대신)
  
  document.querySelectorAll('input[name="selectedOrders"]:checked').forEach(checkbox => {
    const orderId = checkbox.value;
    // 중복 체크 (이미 추가된 ID가 아닌 경우만 추가)
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

  // 버튼 비활성화 및 로딩 표시
  const button = $('#btn-insert-unreceived');
  const originalText = button.html();
  button.html('<i class="ti-reload"></i> 처리중...').prop('disabled', true);

  $.ajax({
    type: 'POST',
    url: '/material/inbound/insert-unreceived',
    data: { orderIds: selectedOrders },
    traditional: true, // 배열 전송을 위한 설정
    success: function(response) {
      alert('선택된 발주건이 성공적으로 입고등록되었습니다.');
      location.reload();
    },
    error: function(xhr, status, error) {
      console.error('입고등록 실패:', xhr.responseText);
      alert('입고등록 중 오류가 발생했습니다.\n' + xhr.responseText);
    },
    complete: function() {
      // 버튼 상태 복원
      button.html(originalText).prop('disabled', false);
    }
  });
}

/* [9] 문서 로딩 시 이벤트 바인딩 */
$(document).ready(function() {
  // 미입고건 DB 저장 버튼 - 기존 방식 (전체 미입고건 등록)
  $('#btn-insert-unreceived').on('click', function() {
    registerSelectedOrders();
  });
  
  console.log('materialInbound.js 로드 완료');
});