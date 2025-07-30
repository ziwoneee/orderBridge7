/************************************************************
 * materialInbound.js
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

/* [3] 미입고 발주 목록 불러오기 - URL 수정 */
function loadUnreceivedOrders(page = 1) {
  console.log('미입고 발주 목록 요청 시작...');
  
  // 로딩 상태 표시
  const tbody = document.querySelector('#unreceivedOrderTable tbody');
  tbody.innerHTML = `
    <tr>
      <td colspan="11" class="text-center py-4">
        <div class="spinner-border text-primary" role="status">
          <span class="sr-only">Loading...</span>
        </div>
        <p class="mt-2">미입고 발주 목록을 불러오는 중...</p>
      </td>
    </tr>
  `;

  $.ajax({
    url: "/material/inbound/unreceived-orders", // URL 수정
    method: 'GET',
    dataType: 'json',
    data: {
        page: 1,           // 기본 1페이지부터
        perPageNum: 10     // 페이지당 10건
    },
    success: function(data) {
      console.log('응답 데이터:', data);
      renderUnreceivedOrders(data.list);
      renderPagination(data.pageMaker);  // 페이징 버튼도 렌더링
    },
    error: function(xhr, status, error) {
      console.error('AJAX 오류 상세:', {
        status: xhr.status,
        statusText: xhr.statusText,
        responseText: xhr.responseText,
        error: error
      });
      
      const tbody = document.querySelector('#unreceivedOrderTable tbody');
      tbody.innerHTML = `
        <tr>
          <td colspan="11" class="text-center text-danger py-4">
            <i class="ti-alert"></i>
            <p class="mt-2">미입고 발주 목록을 불러오는 중 오류가 발생했습니다.</p>
            <small>오류: ${xhr.status} - ${xhr.statusText}</small>
          </td>
        </tr>
      `;
      
      alert(`미입고 발주 목록을 불러오는 중 오류가 발생했습니다.\n상태코드: ${xhr.status}\n오류: ${error}`);
    }
  });
}

/* [4] 미입고 발주 테이블 렌더링 - 개선된 버전 */
function renderUnreceivedOrders(orderList) {
  console.log('렌더링할 주문 목록:', orderList);
  
  const tbody = document.querySelector('#unreceivedOrderTable tbody');
  tbody.innerHTML = ''; // 기존 내용 초기화

  if (!orderList || orderList.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="11" class="text-center text-muted py-4">
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
        return '<span class="badge badge-danger badge-pill">지연</span>';
      } else if (diffDays <= 2) {
        return `<span class="badge badge-warning badge-pill">D-${diffDays}</span>`;
      }
      return '';
    };

    row.innerHTML = `
      <td class="font-weight-medium">미등록</td> 
      <td>-</td>
      <td><span class="badge badge-danger">미입고</span></td> 
      <td>${order.materialName || '발주 품목 다수'}</td>
      <td class="text-end">${order.totalQuantity || 0}</td> 
      <td class="text-end">0</td> 
      <td class="font-weight-medium">${order.orderId}</td> 
      <td>
        ${formatTimestamp(order.expectedArrivedDate)}
        ${calculateDDay(order.expectedArrivedDate)}
      </td> 
      <td>${order.createdBy || '-'}</td>
      <td>
        <button class="btn btn-sm btn-outline-info" onclick="viewOrderDetail('${order.orderId}')">
          발주상세
        </button>
      </td> 
      <td>
        <button class="btn btn-outline-success btn-sm" onclick="registerInbound('${order.orderId}')">
          입고등록
        </button>
      </td>
    `;
    tbody.appendChild(row);
  });
  
  console.log(`${orderList.length}개의 미입고 발주건을 렌더링했습니다.`);
}

/* [5] 날짜 포맷 (yyyy-MM-dd) */
function formatDate(dateStr) {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  const yyyy = date.getFullYear();
  const mm = ('0' + (date.getMonth() + 1)).slice(-2);
  const dd = ('0' + date.getDate()).slice(-2);
  return `${yyyy}-${mm}-${dd}`;
}

/* [6] 발주 상세 보기 */
function viewOrderDetail(orderId) {
  // 발주 상세 모달을 여는 기능 (구현 필요)
  alert(`발주 상세보기: ${orderId}`);
}

/* [7] 입고 등록 처리 */
function registerInbound(orderId) {
  if (!confirm(`발주번호 ${orderId}를 입고 등록하시겠습니까?`)) return;
  
  $.ajax({
    url: '/material/inbound/register',
    type: 'POST',
    data: { orderId: orderId },
    success: function(response) {
      alert('입고 등록이 완료되었습니다.');
      location.reload(); // 페이지 새로고침
    },
    error: function(xhr, status, error) {
      console.error('입고 등록 실패:', xhr.responseText);
      alert('입고 등록 중 오류가 발생했습니다.');
    }
  });
}

/* [8] 선택된 발주 처리 (기존 호환성 유지) */
function selectOrder(orderId) {
  registerInbound(orderId);
}

/* [9] 문서 로딩 시 이벤트 바인딩 */
$(document).ready(function() {
  // 미입고건 불러오기 버튼 클릭 이벤트
  $("button[onclick='loadUnreceivedOrders()']").on("click", function() {
    loadUnreceivedOrders();
  });
  
  // 기존 버튼 ID가 있다면 추가 바인딩
  $("#btn-pending-orders").on("click", function() {
    loadUnreceivedOrders();
  });
  
  console.log('materialInbound.js 로드 완료');
});


/* [10] 페이징 버튼 렌더링 */
function renderPagination(pageMaker) {
  const container = document.getElementById("unreceivedPagination");
  container.innerHTML = ""; // 초기화

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

/* [11] 미입고건을 DB에 저장 (material_inbound + item 테이블 insert) */
$('#btn-insert-unreceived').on('click', function () {
  if (!confirm('미입고 발주건을 입고관리 DB에 저장하시겠습니까?')) return;

  $.ajax({
    type: 'POST',
    url: '/material/inbound/insert-unreceived',
    success: function () {
      alert('미입고건이 성공적으로 DB에 저장되었습니다.');
      // insert 후 미입고 목록 새로고침 (선택)
      loadUnreceivedOrders();
    },
    error: function (xhr, status, error) {
      console.error('미입고건 DB 저장 실패:', xhr.responseText);
      alert('저장 중 오류가 발생했습니다.');
    }
  });
});