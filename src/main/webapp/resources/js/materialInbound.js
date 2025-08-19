/**
 * materialInbound.js 
 */

// 수정된 날짜 포맷 함수 (timezone 이슈 해결)
function formatDateString(v) {
  if (!v) return '-';

  // 문자열이면서 YYYY-MM-DD 형식이면 그대로 반환
  if (typeof v === 'string') {
    if (/^\d{4}-\d{2}-\d{2}$/.test(v)) return v;
    
    // ISO 형식 문자열인 경우 (예: "2024-08-11T00:00:00.000Z")
    if (/^\d{4}-\d{2}-\d{2}T/.test(v)) {
      return v.substring(0, 10); // YYYY-MM-DD 부분만 추출
    }
  }

  // Date 객체로 변환 후 UTC 기준으로 날짜 추출 (timezone 이슈 방지)
  const d = new Date(v);
  
  // Invalid Date 체크
  if (isNaN(d.getTime())) {
    console.warn('Invalid date:', v);
    return '-';
  }
  
  // UTC 기준으로 날짜 추출하여 timezone 이슈 방지
  const y = d.getUTCFullYear();
  const m = String(d.getUTCMonth() + 1).padStart(2, '0');
  const day = String(d.getUTCDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
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
      $('#inboundDetailModal #inboundId').text(data.inbound.inboundId || '-');
      $('#inboundDetailModal #orderId').text(data.inbound.orderId || '-');
      $('#inboundDetailModal #expectedArrivedDate').text(formatDateString(data.inbound.expectedArrivedDate));
      $('#inboundDetailModal #orderDate').text(formatDateString(data.inbound.orderDate));
      $('#inboundDetailModal #supplierName').text(data.inbound.supplierName || data.inbound.supplierId || '-');
      $('#inboundDetailModal #inboundDate').text(formatDateString(data.inbound.inboundDate));
      $('#inboundDetailModal #handledBy').text(data.inbound.handledBy || '-');
      $('#inboundDetailModal #modalStatus').text(data.inbound.inboundStatus || '-');

      // 항목 정보 렌더링
      const tbody = $("#inboundInfo").empty();
      const items = data.inboundItems;

      if (items && items.length > 0) {
        items.forEach(function(item){
          var inboundId = (data.inbound && data.inbound.inboundId) || '';

          // 버튼 HTML 만들기
          var actionBtn;
          if (item.inboundStatus !== '입고완료') {
            actionBtn =
              '<button class="btn btn-sm btn-outline-success btn-inbound-item"' +
              ' data-inbound-id="'    + inboundId                      + '"' +
              ' data-order-item-id="' + (item.orderItemId   || '')     + '"' +
              ' data-inbound-item-id="'+ (item.inboundItemId|| '')     + '"' +
              ' data-material-id="'   +  item.materialId               + '"' +
              ' data-material-name="' + (item.materialName || '')      + '"' +
              ' data-order-qty="'     + ((item.orderQuantity || 0))    + '"' +
              ' data-received-qty="'  + ((item.quantity      || 0))    + '"' +
              ' data-unit-price="'    + ((item.unitPrice     || 0))    + '"' +
              ' data-warehouse="'     + ((item.warehouseCode || "WH001")) + '">' +
              '입고처리</button>';
          } else {
            actionBtn = '<span class="badge badge-success">완료</span>';
          }

          // 행 HTML
          var row =
            '<tr>' +
              '<td>' + item.materialId + '</td>' +
              '<td>' + (item.materialName || '') + '</td>' +
              '<td class="text-end">' + ((item.orderQuantity || 0).toLocaleString()) + '</td>' +
              '<td class="text-end">' + ((item.quantity      || 0).toLocaleString()) + '</td>' +
              '<td class="text-center">' + actionBtn + '</td>' +
            '</tr>';

          tbody.append(row);
        });
      } else {
        tbody.append('<tr><td colspan="7" class="text-center">입고 항목이 없습니다.</td></tr>');
      }
      $('#inboundDetailModal').modal('show');
    },
    error: function(xhr, status, error) {
      console.error('상세 정보 로드 실패:', xhr.responseText);
      alert('상세 정보를 불러오는 데 실패했습니다.\n' + (xhr.responseText || error));
    }
  });
}

/* [2] 입고 모달 열기 - 수정된 버전 */
function openInboundModal(itemOrId) {
	  console.log('[openInboundModal] arg=', itemOrId);

	  if (typeof itemOrId === 'string' && !itemOrId.trim().startsWith('{')) {
	    loadInboundDetail(itemOrId);
	    return;
	  }

	  var item = itemOrId;
	  if (!item || !item.materialId) {
	    alert('자재 정보가 없습니다.');
	    return;
	  }

	  console.log('입고 모달 열기 - 아이템 정보:', item);

	  // 1) 기본 필드 세팅
	  $('#inboundModal #materialId').val(item.materialId);
	  $('#inboundModal #materialName').val(item.materialName || '');
	  $('#inboundModal #lotNo').val('생성중...');
	  $('#inboundModal #expirationDate').val(item.expirationDate || '');

	  var orderedQty = item.orderQuantity || 0;
	  var currentReceivedQty = item.quantity || 0;  // 기입고
	  var remainingQty = Math.max(orderedQty - currentReceivedQty, 0);
	  
	  // === 여기부터 추가 ===
	  // 수량 입력 제한: 최소 1, 최대 '남은 수량'
	  const $qty = $('#inboundModal #quantity');
	  const maxQty = remainingQty > 0 ? remainingQty : (orderedQty || 1);

	  $qty.attr({ min: 1, max: maxQty });

	  // 기본 표시값도 제한 범위에 맞춰 보정
	  const initVal = remainingQty > 0 ? remainingQty : 1;
	  $qty.val(initVal);

	  // 남은 수량 0이면 읽기전용(완납 상태 보호)
	  $qty.prop('readonly', remainingQty <= 0);
	  
	  // ★ 창고 우선 세팅 (서버에서 내려온 값 우선)
	  $('#inboundModal #warehouseCode').val(item.warehouseCode || 'WH001');
	  $('#inboundModal #inboundId').val(item.inboundId || '');
	  $('#inboundModal #orderItemId').val(item.orderItemId || '');
	  $('#inboundModal #inboundItemId').val(item.inboundItemId || '');

	  // 2) 모달 먼저 띄워서 사용자 대기 최소화
	  $('#btnSaveInbound').prop('disabled', false).removeClass('disabled');
	  $('#inboundModal').modal('show');

	  // 3) LOT 번호 비동기 생성
	  $.get('/material/inbound/generate-lot', { materialId: item.materialId })
	    .done(function(lotNo){
	      $('#inboundModal #lotNo').val(lotNo || '');
	      validateInboundInputs && validateInboundInputs();
	    })
	    .fail(function(xhr){
	      console.error('LOT 생성 실패:', xhr.responseText);
	      $('#inboundModal #lotNo').val('');
	    });

	  // 4) ★ 창고 자동 매칭 (서버 보정값이 없을 때만)
	  if (!item.warehouseCode) {
	    $.get('/material/inbound/default-warehouse', {
	      materialId: item.materialId,
	      orderItemId: item.orderItemId || ''
	    }).done(function(wh){
	      if ($('#inboundModal').is(':visible')) {
	    	  $('#inboundModal #warehouseCode').val(wh || 'WH001');
	        validateInboundInputs && validateInboundInputs();
	      }
	    });
	  }
	}

/* [3] 미입고 발주 목록 불러오기 - 모달 버전 */
function loadUnreceivedOrders(page = 1) {
  console.log('미입고 발주 목록 요청, 페이지:', page);

  // 모달 먼저 오픈
  $('#unreceivedOrdersModal').modal('show');

  $.ajax({
    url: '/material/inbound/unreceived-orders',
    method: 'GET',
    data: { page: page, perPageNum: 10 },
    success: function(response) {
      console.log('미입고 발주 응답:', response);

      if (response && response.list) {
        renderUnreceivedOrdersModal(response.list);
        if (response.pageMaker) {
          renderPaginationModal(response.pageMaker);
        }
      } else {
        alert('미입고 발주 목록을 불러오는데 실패했습니다.');
      }
    },
    error: function(xhr) {
      console.error('미입고 발주 목록 로드 실패:', xhr.responseText);
      alert('미입고 발주 목록을 불러오는데 실패했습니다.\n' + (xhr.responseText || ''));
    }
  });
}

/* [4] 입고 처리 (단일 항목) - 수정된 버전 */
function processInboundItem() {
  const lotNo = $('#inboundModal #lotNo').val().trim();
  const expirationDate = $('#inboundModal #expirationDate').val().trim();
  const quantity = parseInt($('#inboundModal #quantity').val(), 10);
  const warehouseCode = $('#inboundModal #warehouseCode').val();
  const materialId = $('#inboundModal #materialId').val();
  const inboundId = $('#inboundModal #inboundId').val();
  const orderItemId = $('#inboundModal #orderItemId').val();
  const inboundItemId = $('#inboundModal #inboundItemId').val(); // 추가

  // 유효성 검사
  if (!lotNo) return alert('LOT 번호를 입력하거나 자동 생성해야 합니다.');
  if (!expirationDate) return alert('유통기한을 입력해주세요.');
  if (!quantity || quantity <= 0) return alert('입고 수량은 1 이상이어야 합니다.');
  if (!warehouseCode) return alert('창고를 선택해주세요.');
  if (!materialId) return alert('자재 ID가 없습니다.');
  if (!inboundId) return alert('입고 ID가 없습니다.');

  // ✅ 중요: quantity는 이번에 입고할 수량만 전송
  const inboundItemData = {
    inboundId,
    materialId,
    lotNo,
    expirationDate,
    quantity, // 이번에 입고할 수량 (누적 아님)
    warehouseCode,
    orderItemId,
    inboundItemId // 추가
  };

  console.log('입고 처리 요청 데이터:', inboundItemData);

  $.ajax({
    url: '/material/inbound/item/process',
    type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(inboundItemData),
    success: function () {
	  // 입력 모달 닫고
	  $('#inboundModal').modal('hide');

	  // 상세 모달이 열려있으면 그쪽만 최신화
	  if ($('#inboundDetailModal').is(':visible')) {
	    loadInboundDetail($('#inboundModal #inboundId').val());
	  } else {
	    location.reload();
	  }
	},
    error: function (xhr) {
      console.error('입고 처리 실패:', xhr.responseText);
      alert('입고 처리 중 오류가 발생했습니다.\n' + xhr.responseText);
    }
  });
}

/* [5-모달] 미입고 발주 테이블 렌더링 */
function renderUnreceivedOrdersModal(orderList) {
  const tbody = document.querySelector('#unreceivedOrderTableModal tbody');
  if (!tbody) return;
  tbody.innerHTML = '';

  if (!orderList || orderList.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" class="text-center text-muted py-4">
          <i class="ti-info-alt" style="font-size:24px;"></i>
          <p class="mt-2">미입고 발주건이 없습니다.</p>
        </td>
      </tr>`;
    return;
  }

  // 수정된 날짜 포맷 함수 사용
  const fmt = ts => ts ? formatDateString(ts) : '-';
  const dday = ts => {
    if (!ts) return '';
    const targetDate = new Date(ts);
    const today = new Date();
    
    // 시간 부분을 제거하고 날짜만 비교
    targetDate.setHours(0, 0, 0, 0);
    today.setHours(0, 0, 0, 0);
    
    const diff = Math.ceil((targetDate - today) / 86400000);
    if (diff < 0) return '<span class="badge badge-danger badge-pill ml-1">지연</span>';
    if (diff <= 2) return `<span class="badge badge-warning badge-pill ml-1">D-${diff}</span>`;
    return '';
  };

  orderList.forEach(order => {
    const tr = document.createElement('tr');
    tr.setAttribute('data-order-id', order.orderId || '');
    tr.innerHTML = `
      <td><input type="checkbox" name="selectedOrdersModal" class="order-checkbox"></td>
      <td class="font-weight-medium">${order.orderId}</td>
      <td>${order.materialNames || order.materialName || '-'}</td>
      <td class="text-end">${(order.totalOrderQuantity || order.totalQuantity || 0).toLocaleString()}</td>
      <td>${fmt(order.expectedArrivedDate)} ${dday(order.expectedArrivedDate)}</td>
      <td>${order.handledBy || order.createdBy || '-'}</td>
      <td><button class="btn btn-sm btn-outline-info" onclick="viewOrderDetail('${order.orderId}')">상세</button></td>
    `;
    tbody.appendChild(tr);
  });

  // 전체 체크
  $('#checkAllModal').off('change').on('change', function () {
    $('#unreceivedOrderTableModal .order-checkbox').prop('checked', this.checked);
  });
}

/* [6-모달] 페이징 렌더링 */
function renderPaginationModal(pm) {
  const container = document.getElementById('unreceivedPaginationModal');
  if (!container) return;
  container.innerHTML = '';

  const ul = document.createElement('ul');
  ul.className = 'pagination';

  if (pm.prev) {
    ul.innerHTML += `<li class="page-item"><a class="page-link" href="#" data-page="${pm.startPage-1}">&laquo;</a></li>`;
  }
  for (let i = pm.startPage; i <= pm.endPage; i++) {
    ul.innerHTML += `<li class="page-item ${pm.cri.page===i?'active':''}">
      <a class="page-link" href="#" data-page="${i}">${i}</a></li>`;
  }
  if (pm.next) {
    ul.innerHTML += `<li class="page-item"><a class="page-link" href="#" data-page="${pm.endPage+1}">&raquo;</a></li>`;
  }

  container.appendChild(ul);

  $(container).find('a.page-link').on('click', function(e){
    e.preventDefault();
    const p = parseInt($(this).data('page'),10);
    if (!isNaN(p)) loadUnreceivedOrders(p);
  });
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

/* [7] 발주 상세 보기 - 날짜 포맷 함수 적용 */
function viewOrderDetail(orderId) {
  const fmt = d => !d ? '-' : formatDateString(d);

  $.get('/material/order/detail', { orderId })
    .done(res => {
      const h = res.header || {};
      $('#modalOrderId').text(h.orderId || '-');
      $('#modalSupplierId').text(h.supplierName || h.supplierId || '-');
      $('#modalOrderDate').text(fmt(h.orderDate));
      $('#modalExpectedDate').text(fmt(h.expectedArrivedDate));
      $('#modalOrderStatus').text(h.orderStatus || '-');
      $('#modalCreatedBy').text(h.createdBy || '-');
      $('#modalNote').text(h.note || '');

      const $tbody = $('#orderItemsInfo').empty();
      (res.items || []).forEach(it => {
        $tbody.append(
          `<tr>
            <td>${it.materialId}</td>
            <td>${it.materialName || ''}</td>
            <td class="text-right">${it.orderQuantity}</td>
            <td class="text-right">${it.unitPrice}</td>
            <td class="text-right">${it.totalPrice}</td>
            <td>${it.warehouseCode || '-'}</td>
          </tr>`
        );
      });

      $('#orderDetailModal').modal('show');
    })
    .fail(xhr => {
      alert('상세 조회 실패: ' + ((xhr.responseJSON && xhr.responseJSON.message) || xhr.statusText));
    });
}

/* [8] 전체 선택/해제 */
function toggleAllCheckboxes(checkAllBox) {
  const checkboxes = document.querySelectorAll('input[name="selectedOrders"]');
  checkboxes.forEach(checkbox => {
    checkbox.checked = checkAllBox.checked;
  });
}

/* [9-모달] 선택된 발주 입고등록 */
function registerSelectedOrdersModal() {
  const ids = [];
  const added = {};
  $('#unreceivedOrderTableModal input[name="selectedOrdersModal"]:checked').each(function(){
    const orderId = $(this).closest('tr').data('order-id');
    if (orderId && !added[orderId]) { ids.push(orderId); added[orderId] = true; }
  });

  if (ids.length === 0) return alert('입고등록할 발주를 선택하세요.');
  if (!confirm(`선택된 ${ids.length}건의 발주를 입고등록하시겠습니까?`)) return;

  const $btn = $('#btn-insert-unreceived-modal');
  const org = $btn.html();
  $btn.html('<i class="ti-reload"></i> 처리중...').prop('disabled', true);

  $.ajax({
    type: 'POST',
    url: '/material/inbound/insert-unreceived',
    data: { orderIds: ids },
    traditional: true,
    success: function(){
      alert('입고건이 생성되었습니다.');
      $('#unreceivedOrdersModal').modal('hide');
      location.reload();
    },
    error: function(xhr){ alert('입고등록 중 오류가 발생했습니다.\n'+(xhr.responseText||'')); },
    complete: function(){ $btn.html(org).prop('disabled', false); }
  });
}

/* [10] 문서 로딩 시 이벤트 바인딩 */
$(document).ready(function () {
  console.log('materialInbound.js 로드 시작');

  // 미입고 발주 입고등록 버튼
  $('#btn-insert-unreceived').on('click', function () {
	    // 목록 페이지에서 "미입고 발주 불러오기" 모달 열기
	    loadUnreceivedOrders(1);
  });

  // 모달 내 미입고 발주 입고등록 버튼
  $('#btn-insert-unreceived-modal').on('click', function () {
    registerSelectedOrdersModal();
  });

  // 메인 테이블의 입고처리 버튼 (기존 클래스)
  $(document).off('click.btnInbound')
    .on('click.btnInbound', '.btn-inbound', function(e){
      e.preventDefault(); 
      e.stopPropagation();
      console.log('[click] .btn-inbound (메인 테이블)');

      // data-*에서 값 꺼내서 객체 구성
      var $b = $(this);
      var item = {
        inboundId:     $b.data('inboundId'),
        orderItemId:   $b.data('orderItemId'),
        inboundItemId: $b.data('inboundItemId') || null,
        materialId:    $b.data('materialId'),
        materialName:  $b.data('materialName'),
        orderQuantity: Number($b.data('orderQty')    || 0),
        quantity:      Number($b.data('receivedQty') || 0), // 기입고 수량
        unitPrice:     Number($b.data('unitPrice')   || 0),
        warehouseCode: $b.data('warehouse') || 'WH001'
      };

      if (!item.inboundId)  return alert('입고 ID가 없습니다.');
      if (!item.materialId) return alert('자재 ID가 없습니다.');

      openInboundModal(item);
    });

  // 상세 모달 내의 입고처리 버튼 (새 클래스)
  $(document).off('click.btnInboundItem')
    .on('click.btnInboundItem', '.btn-inbound-item', function(e){
      e.preventDefault(); 
      e.stopPropagation();
      console.log('[click] .btn-inbound-item (상세 모달)');

      // data-*에서 값 꺼내서 객체 구성
      var $b = $(this);
      var item = {
        inboundId:     $b.data('inboundId'),
        orderItemId:   $b.data('orderItemId'),
        inboundItemId: $b.data('inboundItemId') || null,
        materialId:    $b.data('materialId'),
        materialName:  $b.data('materialName'),
        orderQuantity: Number($b.data('orderQty')    || 0),
        quantity:      Number($b.data('receivedQty') || 0), // 기입고 수량
        unitPrice:     Number($b.data('unitPrice')   || 0),
        warehouseCode: $b.data('warehouse') || 'WH001'
      };

      if (!item.inboundId)  return alert('입고 ID가 없습니다.');
      if (!item.materialId) return alert('자재 ID가 없습니다.');

      console.log('상세 모달에서 입고처리 클릭, 아이템:', item);
      openInboundModal(item);
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

//여러 모달 겹칠 때 z-index 자동 조정 (Bootstrap 4)
$(document).on('show.bs.modal', '.modal', function () {
  const z = 1040 + (10 * $('.modal:visible').length);
  $(this).css('z-index', z);
  setTimeout(() => {
    $('.modal-backdrop').not('.modal-stack')
      .css('z-index', z - 1)
      .addClass('modal-stack');
  }, 0);
});

//✅ [A] 모달 저장 버튼 유효성 토글 (유통기한 >= 오늘)
function validateInboundInputs() {
  const lotNo        = $.trim($('#inboundModal #lotNo').val());
  const expiration   = $.trim($('#inboundModal #expirationDate').val());
  
  const $qty = $('#inboundModal #quantity');
  const quantity = parseInt($qty.val(), 10);
  const maxAttr  = parseInt($qty.attr('max') || '0', 10);
  const minAttr  = parseInt($qty.attr('min') || '1', 10);
  
  const warehouse    = $.trim($('#inboundModal #warehouseCode').val());
  const materialId   = $.trim($('#inboundModal #materialId').val());
  const inboundId    = $.trim($('#inboundModal #inboundId').val());

  // 오늘 yyyy-mm-dd (로컬)
  const now = new Date(); 
  now.setHours(0,0,0,0);
  const yyyy = now.getFullYear();
  const mm   = String(now.getMonth()+1).padStart(2,'0');
  const dd   = String(now.getDate()).padStart(2,'0');
  const todayStr = `${yyyy}-${mm}-${dd}`;

  // 유통기한은 오늘 이상이어야 함
  const expOk = !!expiration && expiration >= todayStr;
  
  //수량: 숫자이며 min 이상, (max가 있으면) max 이하여야 함
  const qtyOk = Number.isFinite(quantity)
             && quantity >= minAttr
             && (!maxAttr || quantity <= maxAttr);

  const ok = !!(lotNo && expOk && warehouse && materialId && inboundId && qtyOk);
  $('#btnSaveInbound').prop('disabled', !ok);
  return ok;
}

//✅ [B] 모달 열릴 때 필드 세팅 후 즉시 검증 & 버튼 활성화 시도
$('#inboundModal').on('shown.bs.modal', function () {
  console.log('입고 모달이 표시되었습니다.');

  // 오늘 yyyy-mm-dd (로컬)
  const today = new Date();
  today.setHours(0,0,0,0);
  const yyyy = today.getFullYear();
  const mm = String(today.getMonth()+1).padStart(2,'0');
  const dd = String(today.getDate()).padStart(2,'0');
  const todayStr = `${yyyy}-${mm}-${dd}`;

  // 유통기한 최소값 = 오늘
  const $exp = $('#inboundModal #expirationDate');
  $exp.attr('min', todayStr);

  // 과거값으로 열리면 즉시 보정
  if ($exp.val() && $exp.val() < todayStr) {
    $exp.val(todayStr);
  }

  // 혹시 HTML에 disabled나 .disabled가 박혀있으면 제거
  $('#btnSaveInbound').prop('disabled', false).removeClass('disabled');

  validateInboundInputs();
});

//=== [수량 입력 단계 방어] 음수/문자/휠 입력 차단 ===

//숫자만 허용 & min/max 즉시 반영
$(document).on('input', '#inboundModal #quantity', function () {
let v = $(this).val().replace(/[^\d]/g, ''); // 숫자만 남김
if (v === '') v = '1';
const min = parseInt($(this).attr('min') || '1', 10);
const max = parseInt($(this).attr('max') || '0', 10);
let n = parseInt(v, 10);

if (!Number.isFinite(n) || n < min) n = min;
if (max && n > max) n = max;

$(this).val(n);

// 입력 바뀌면 버튼 토글
if (typeof validateInboundInputs === 'function') validateInboundInputs();
});

//'-', '+', 'e', 'E', '.' 입력 자체 차단
$(document).on('keydown', '#inboundModal #quantity', function (e) {
const blocked = ['e', 'E', '-', '+', '.'];
if (blocked.includes(e.key)) e.preventDefault();
});

//포커스 상태에서 마우스 휠로 값 변조 방지
$(document).on('wheel', '#inboundModal #quantity', function (e) {
if ($(this).is(':focus')) e.preventDefault();
});


//✅ [D] 저장 버튼 클릭 핸들러 (유통기한 2중 방어 포함)
$(document).on('click', '#btnSaveInbound', function () {
  // 유통기한 최소값 재확인
  const $exp = $('#inboundModal #expirationDate');
  const min  = $exp.attr('min');
  const val  = $exp.val();
  if (min && val && val < min) {
    alert('유통기한은 오늘 이후로 입력해야 합니다.');
    return;
  }
  
  // === 수량 min/max 최종 방어 ===
  const $q = $('#inboundModal #quantity');
  const qVal = parseInt($q.val() || '0', 10);
  const qMin = parseInt($q.attr('min') || '1', 10);
  const qMax = parseInt($q.attr('max') || '0', 10);

  if (!Number.isFinite(qVal) || qVal < qMin) {
    alert(`입고 수량은 최소 ${qMin.toLocaleString()} 이상이어야 합니다.`);
    $q.val(qMin);
    return;
  }
  if (qMax && qVal > qMax) {
    alert(`입고 수량은 남은 수량(${qMax.toLocaleString()})을 초과할 수 없습니다.`);
    $q.val(qMax);
    return;
  }

  if (!validateInboundInputs()) {
    alert('필수 항목을 모두 입력해주세요.');
    return;
  }

  processInboundItem();
});

//입력이 바뀔 때마다 저장 버튼 활성/비활성 재평가
$(document).on('input change', '#inboundModal input, #inboundModal select', validateInboundInputs);
