/**
 * tbody 내부에서 같은 텍스트가 연속되는 셀을 rowspan으로 병합
 * @param {string} tbodySelector - 예: '#outboundItemsInfo'
 * @param {number[]} colIdxs - 병합할 열 인덱스 배열 (오른쪽→왼쪽 순으로 병합)
 */
function mergeRowspan(tbodySelector, colIdxs){
  var $rows = $(tbodySelector).children('tr');
  if ($rows.length === 0) return;

  // 인덱스가 바뀌지 않도록 오른쪽→왼쪽으로
  var cols = (colIdxs || []).slice().sort(function(a,b){ return b - a; });

  cols.forEach(function(colIndex){
    var prevText = null, $prevCell = null, span = 1;

    $rows.each(function(){
      var $cells = $(this).children('td');
      var $cell  = $cells.eq(colIndex);
      var text   = $.trim($cell.text());

      if ($prevCell && text === prevText){
        span += 1;
        $cell.remove();
        $prevCell.attr('rowspan', span).addClass('align-middle');
      } else {
        prevText = text;
        $prevCell = $cell;
        span = 1;
      }
    });
  });
}


/* ---------- 전역: 전체 검증 ---------- */
window.validateAll = function () {
  let ok = true;            // sum === target 모두 충족?
  let hasShortage = false;  // cap < req 인 행 존재? (물 제외)

  $('#materialLotBody tr').each(function () {
    const req = Number($(this).find('.req').data('req')) || 0;

    // 0 허용하며 null/undefined만 대체
    const capData = $(this).data('cap');
    const cap = (capData == null) ? req : Number(capData);

    const mid = String($(this).data('material') || '');
    const target = Math.min(req, cap);

    // 물 특례: target 고정
    if (mid === 'RM-0015') {
      $(this).find('.lot-qty').val(target);
      $(this).find('.sum').text(target);
    }

    const sum = Number($(this).find('.sum').text()) || 0;

    // 부족분 판단은 "가용(cap) < 필요(req)" 기준 (물 제외)
    if (mid !== 'RM-0015' && cap < req) hasShortage = true;

    // 등록 가능 여부는 "선택합 === target" 모두 충족
    if (sum !== target) ok = false;
  });

  // 버튼 상태
  if (hasShortage) {
    // 부족 있으면: 발주만 가능, 등록은 막음
    $('#btnCreateDraft').prop('disabled', false);
    $('#btnSubmit').prop('disabled', true);
  } else {
    // 부족 없으면: 발주 막고, 등록은 합계가 모두 맞을 때만
    $('#btnCreateDraft').prop('disabled', true);
    $('#btnSubmit').prop('disabled', !ok);
  }

  return !hasShortage && ok;
};


/* ---------- 부족분 발주 생성 데이터 수집 ---------- */
// 부족 판단도 "cap < req" 기준으로 수정 (기존 sum<req 제거)
window.collectShortages = function () {
  const shortages = [];
  $('#materialLotBody tr').each(function () {
    const mid = String($(this).data('material') || '');
    if (mid === 'RM-0015') return; // 물 제외

    const materialId = $(this).data('material');
    const materialName = $(this).find('.font-weight-bold').text().trim();
    const required = +($(this).find('.req').data('req') || 0);

    const capData = $(this).data('cap');
    const cap = (capData == null) ? required : Number(capData);

    if (cap < required) {
      shortages.push({
        materialId,
        materialName,
        lackQty: required - cap
      });
    }
  });
  return shortages;
};


/* ---------- 공통 유틸 ---------- */
function toYmd(dateValue) {
  if (!dateValue) return '';
  const date = (dateValue instanceof Date) ? dateValue : new Date(dateValue);
  if (isNaN(date.getTime())) return '';
  const year = date.getFullYear();
  const month = ('0' + (date.getMonth() + 1)).slice(-2);
  const day = ('0' + date.getDate()).slice(-2);
  return `${year}-${month}-${day}`;
}
function fmtDate(value) { return toYmd(value); }

function asDate(v){
	  if (!v) return null;
	  if (typeof v === 'string') return new Date(v.replace(' ', 'T'));
	  if (v && typeof v === 'object' && 'time' in v) return new Date(v.time);
	  try { return new Date(v); } catch(e){ return null; }
	}
	function fmtTS(v){ // yyyy-MM-dd HH:mm:ss (로컬)
	  const d = asDate(v); if(!d || isNaN(d)) return '-';
	  const p=n=>String(n).padStart(2,'0');
	  return `${d.getFullYear()}-${p(d.getMonth()+1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
	}
	function fmtYmd(v){ // yyyy-MM-dd (로컬)
	  const d = asDate(v); if(!d || isNaN(d)) return '-';
	  const p=n=>String(n).padStart(2,'0');
	  return `${d.getFullYear()}-${p(d.getMonth()+1)}-${p(d.getDate())}`;
	}


/* === [NEW] inboundIds 읽기 & 사용상태 갱신 유틸 === */
function getInboundIdsParam() {
  const p = new URLSearchParams(location.search);
  const raw = p.get('inboundIds') || p.get('inboundId') || '';
  return raw.split(',').map(s => s.trim()).filter(Boolean);
}

//페이지 이동 직전에도 안전하게 영기 위해 sendBeacon 우선 사용
function updateInboundStatuses(ids) {
  if (!ids || !ids.length) return;

  // 배치 엔드포인트가 있으면 주석 해제해서 사용
  // const url = ctx + '/material/outbound/update-inbound-status-batch';
  // const data = JSON.stringify({ inboundIds: ids });
  // if (navigator.sendBeacon) {
  //   const blob = new Blob([data], { type: 'application/json' });
  //   navigator.sendBeacon(url, blob);
  //   return;
  // }

  // 단건 엔드포인트로 fallback
  ids.forEach(id => {
    const url = ctx + '/material/outbound/update-inbound-status';
    const form = new FormData();
    form.append('inboundId', id);

    if (navigator.sendBeacon) {
      navigator.sendBeacon(url, form);
    } else {
      // 비콘이 없으면 비동기로라도 발사 (페이지 전환 중 드랍될 수 있음)
      $.post(url, { inboundId: id }).catch(()=>{});
    }
  });
  
  
}

/* ---------- register.jsp: 초기 로드 ---------- */
$(function(){
  if (!$('#outboundForm').length) return;

  const params = new URLSearchParams(location.search);
  const workOrderId = params.get('workOrderId');
  const inboundId   = params.get('inboundId');
  const inboundIds  = params.get('inboundIds');
  
  //☆ 선택한 입고건들 hidden으로 넘기기(모달에서 온 값 유지)
  if (inboundIds && !$('#outboundForm input[name="inboundIds"]').length) {
    $('#outboundForm').append(
      $('<input>', { type:'hidden', name:'inboundIds', value: inboundIds })
    );
  }

  // (선택) 단일 inboundId도 서버에서 받게 하려면 유지
  if (inboundId && !$('#outboundForm input[name="inboundId"]').length) {
    $('#outboundForm').append(
      $('<input>', { type:'hidden', name:'inboundId', value: inboundId })
    );
  }

  // 둘 다 없으면 대기
  if (!workOrderId && !inboundId) return;

  // 1) workOrderId 있으면: 기존대로 로드
  if (workOrderId) {
    $.get(ctx + '/material/outbound/work-order', { workOrderId }, function(dto){
      const no  = dto.workOrderId || '';
      const due = toYmd(dto.dueDate) || '';
      $('#workOrderIdView').val(no);
      $('#dueDateView').val(due);
      $('#workOrderIdHidden').val(no);
      $('#dueDateHidden').val(due);
      $('#productId').val(dto.productId || '');
      $('#lineId').val(dto.lineId || '');
      renderMaterialRows(dto.materialList || []);
    }).fail(function(xhr) {
      console.error('작업지시서 정보 로드 실패:', xhr);
      alert('작업지시서 정보를 불러올 수 없습니다.');
    });
    return;
  }

  // 2) inboundId만 있으면: 서버에 매핑 질의 → 성공시 리다이렉트
  if (inboundId) {
    $.get(ctx + '/material/outbound/resolve-workorder', { inboundId })
      .done(function(res){
        if (res && res.workOrderId) {
          window.location.replace(
            ctx + '/material/outbound/register?workOrderId='
            + encodeURIComponent(res.workOrderId)
            + '&inboundId=' + encodeURIComponent(inboundId)
          );
        } else {
          alert('이 입고건에 연결된 작업지시를 찾지 못했습니다. 상단 "작업지시 불러오기"로 선택해주세요.');
        }
      })
      .fail(function(){
        alert('작업지시 매핑을 찾지 못했습니다. 상단 "작업지시 불러오기"로 선택해주세요.');
      });
  }
});

/* ---------- 자재 행 렌더링 ---------- */
function renderMaterialRows(items) {
  const $tbody = $('#materialLotBody').empty();
  
  // 자재코드(숫자 섞여도 자연정렬) 기준 정렬
  const sorted = (items || []).slice().sort((a,b) =>
    String(a.materialId || '').localeCompare(String(b.materialId || ''), 'en', { numeric: true })
  );

  if (!sorted.length) {
	  $tbody.append('<tr><td colspan="7" class="text-center text-muted">자재 정보가 없습니다.</td></tr>');
	  return;
  }

  const promises = sorted.map(function(item, idx) {
    const workOrderId = $('#workOrderIdHidden').val()
                     || new URLSearchParams(location.search).get('workOrderId');
    const required = Number(item.requiredQty) || 0;

    return $.get(ctx + '/material/inventory/lot-details', { materialId: item.materialId })
	.then(function(resp) {
		
	// ✅ 다양한 래핑 대응
	    var lots = [];
	    if (Array.isArray(resp)) lots = resp;
	    else if (resp && Array.isArray(resp.lotList)) lots = resp.lotList;
	    else if (resp && Array.isArray(resp.list))    lots = resp.list;
	    else if (resp && Array.isArray(resp.rows))    lots = resp.rows;
	    else if (resp && Array.isArray(resp.data))    lots = resp.data;
	    else if (resp && Array.isArray(resp.lots))    lots = resp.lots;   // ← 추가 후보
	    else {
	      console.warn('lots is not an array for material:', item.materialId, resp);
	      lots = [];
    
	    }                                         
	    
	        return $.get(ctx + '/material/inventory/availability',
	                     { materialId: item.materialId, workOrderId: workOrderId })
	          .then(
	            function(avail) { return { lots: lots, avail: (avail || {}) }; },
	            function() {
	              var onhandFromLots = lots.reduce(function(s,l){ return s + (Number(l.quantity)||0); }, 0);
	              return { lots: lots,
	                       avail: { onhandTotal:onhandFromLots, reservedOthers:0, reservedForThis:0 } };
	            }
	          );
      })
      .then(function(payload) {
      try {
        var lots  = payload.lots || [];
        var avail = payload.avail || {};
        var required = Number(item.requiredQty) || 0;

        // ★ 다시 한번 배열 확인
        if (!Array.isArray(lots)) {
          console.warn('lots is still not an array after processing:', lots);
          lots = [];
        }

        var onhandTotal = Number(avail.onhandTotal) || 0;
        var onhandFromLots = 0;
        
        // lots가 배열인지 확인 후 처리
        if (Array.isArray(lots)) {
          onhandFromLots = lots.reduce((s, l) => s + (Number(l.quantity) || 0), 0);
        } else {
          console.warn('lots is not an array:', lots);
          onhandFromLots = 0;
        }
        
        var reservedThis = Number(
        		  (avail.reservedForThis != null) ? avail.reservedForThis
        		  : (avail.woReserved != null)    ? avail.woReserved
        		  : 0
        		);

        		var reservedOthers = (function(){
        		  if (avail.reservedOthers != null) return Number(avail.reservedOthers) || 0;
        		  // reservedTotal이 전체이면 = total - this
        		  if (avail.reservedTotal != null) {
        		    var tot = Number(avail.reservedTotal) || 0;
        		    return Math.max(0, tot - reservedThis);
        		  }
        		  return 0;
        		})();

        let a4wo = (avail.availableForThisWO != null)
          ? Number(avail.availableForThisWO)
          : Math.max(0, onhandTotal - reservedOthers) + reservedThis;

        a4wo = Math.min(a4wo, onhandTotal, onhandFromLots);
        var cap = Math.max(0, Math.min(required, a4wo));

        var mid = item.materialId;
        if (mid === 'RM-0015') { a4wo = required; cap = required; }

        var lotRequired = (item.lotRequired === 'N' || item.lotFlag === 'N' || mid === 'RM-0015') ? 'N' : 'Y';
        var defaultWh   = item.defaultWarehouseCode || item.warehouseCode || 'WH003';

        var $row = $('<tr>')
          .attr('data-order', idx)
          .attr('data-material', item.materialId)
          .attr('data-lot-required', lotRequired)
          .attr('data-default-warehouse', defaultWh)
          .data('cap', cap)
          .data('a4wo', a4wo);

        // 회색 보조문구에 보여줄 전체 예약(others + this)
                var totalReserved = reservedOthers + reservedThis;
                var info = '가용 ' + a4wo + '  /  현재고 ' + onhandTotal + '  /  예약 ' + totalReserved;
        
        $row.append(
          $('<td>').append(
            $('<div>').addClass('font-weight-bold').text(item.materialName || item.materialId),
            $('<div>').addClass('text-muted').append($('<small>').text(item.materialId + '  |  ' + info))
          )
        );

        $row.append(
          $('<td>').addClass('text-right align-middle')
                   .append($('<span>').addClass('req').attr('data-req', required).text(required))
        );

        var $lotCell = $('<td>');
        if (!lots || lots.length === 0) {
          if (lotRequired === 'N') {
            var init = Math.min(required, cap);
            var $lotRow = $('<div>').addClass('form-row align-items-center lot-row mb-1');
            var $infoCol = $('<div>').addClass('col-md-5')
              .append($('<small>').addClass('text-muted').text('LOT 불필요'))
              .append('<br>')
              .append($('<small>').text('직접 출고'));
            var $qtyCol = $('<div>').addClass('col-md-4').append(
              $('<input>', { type:'number', min:0, step:1, value:init, max:init })
                .addClass('form-control form-control-sm lot-qty')
                .data({ material: item.materialId, lot: '', warehouse: defaultWh })
            );
            var $whCol = $('<div>').addClass('col-md-3')
              .append($('<span>').addClass('badge badge-secondary').text(defaultWh));
            $lotRow.append($infoCol, $qtyCol, $whCol);
            $lotCell.append($lotRow);
          } else {
            $lotCell.append($('<span>').addClass('text-danger').text('사용 가능한 LOT 없음'));
          }
        } else {
          // ★ 정렬 전 배열 확인
          if (Array.isArray(lots)) {
            lots.sort(function(a, b) {
              var da = new Date(a.expirationDate || '9999-12-31').getTime();
              var db = new Date(b.expirationDate || '9999-12-31').getTime();
              return da - db;
            });
          }
          
          for (var j = 0; j < lots.length; j++) {
            var lot = lots[j];
            var warehouseCode = lot.warehouseCode || defaultWh;
            var availableQty  = Number(lot.quantity) || 0;

            var $lotRow2 = $('<div>').addClass('form-row align-items-center lot-row mb-1')
              .attr('data-lot', lot.lotNo);
            var $infoCol2 = $('<div>').addClass('col-md-5')
              .append($('<small>').addClass('text-muted').text(lot.lotNo || '-'))
              .append('<br>')
              .append($('<small>').text('유통기한: ' + fmtDate(lot.expirationDate) + ' / 재고: ' + availableQty));
            var $qtyCol2 = $('<div>').addClass('col-md-4').append(
              $('<input>', { type:'number', min:0, max:availableQty, step:1, value:0 })
                .addClass('form-control form-control-sm lot-qty')
                .data({ material: item.materialId, lot: lot.lotNo, warehouse: warehouseCode })
            );
            var $whCol2 = $('<div>').addClass('col-md-3')
              .append($('<span>').addClass('badge badge-secondary').text(warehouseCode));
            $lotRow2.append($infoCol2, $qtyCol2, $whCol2);
            $lotCell.append($lotRow2);
          }
        }

        $row.append($lotCell);

        // "예약수량" = 이번 작업지시 예약만 (필요수량 초과는 표시 제한)
        const reservedThisDisplay = Math.min(required, reservedThis);
        const reservedText = (mid === 'RM-0015') ? '직접출고' : reservedThisDisplay;
        $row.append($('<td>').addClass('text-right align-middle reserved-this').text(reservedText));
        
        // ✅ 예상예약(프리뷰) 셀: 처음엔 0으로 시작
        const previewInit = (mid === 'RM-0015') ? '-' : 0;
        $row.append($('<td>').addClass('text-right align-middle preview-reserve').text(previewInit));	
        
        $row.append($('<td>').addClass('text-right align-middle').append($('<span>').addClass('sum').text('0')));
        $row.append($('<td>').addClass('text-right align-middle shortage').text(required));

        $('#materialLotBody').append($row);
        updateRowSumAndValidate($row);
        return true;
      } catch (e) {
        console.error('자재 행 렌더링 오류:', e);
        return false;
      }
    })
    .fail(function(xhr) { // LOT API 자체 실패
      console.error('자재 정보 로드 실패:', item.materialId, xhr);
      var $row = $('<tr>')
        .attr('data-order', idx)
        .attr('data-material', item.materialId)
        .attr('data-lot-required', 'Y')
        .attr('data-default-warehouse', 'WH003')
        .data('cap', 0);

      $row.append(
        $('<td>').append(
          $('<div>').addClass('font-weight-bold').text(item.materialName || item.materialId),
          $('<div>').addClass('text-muted').append($('<small>').text(item.materialId + '  |  정보 로드 실패'))
        ),
        $('<td>').addClass('text-right align-middle')
                 .append($('<span>').addClass('req').attr('data-req', required).text(required)),
        $('<td>').append($('<span>').addClass('text-danger').text('정보를 불러올 수 없습니다')),
        $('<td>').addClass('text-right align-middle a4wo').text(0),
        $('<td>').addClass('text-right align-middle').append($('<span>').addClass('sum').text('0')),
        $('<td>').addClass('text-right align-middle shortage').text(required)
      );

      $('#materialLotBody').append($row);
      return false;
    });
  });

  // 모든 행 렌더링 이후 자동배정 + 검증
  Promise.all(promises).then(function () {
	// ★ 로딩이 끝난 뒤, data-order 기준으로 DOM 재정렬
	const rows = $tbody.children('tr').get().sort(function(a,b){
	  return (parseInt(a.dataset.order)||0) - (parseInt(b.dataset.order)||0);
	});
	$tbody.append(rows);
	  
    autoAllocateAll(false);
    // 물 특례 동기화
    $('#materialLotBody tr').each(function () {
      const mid = String($(this).data('material') || '');
      if (mid === 'RM-0015') {
        const req = Number($(this).find('.req').data('req')) || 0;
        const capData = $(this).data('cap');
        const cap = (capData == null) ? req : Number(capData);
        const target = Math.min(req, cap);
        $(this).find('.lot-qty').val(target);
        $(this).find('.sum').text(target);
        updateRowSumAndValidate($(this));
      }
    });
    window.validateAll();
  });
}

/* ---------- 행별 합계 계산 및 상태 업데이트 ---------- */
function updateRowSumAndValidate($row) {
  const required = Number($row.find('.req').data('req')) || 0;
  const capData  = $row.data('cap');
  const cap      = (capData == null) ? required : Number(capData);
  const target   = Math.min(required, cap);

  // 합계 계산
  let sum = 0;
  $row.find('.lot-qty').each(function(){ sum += Number(this.value) || 0; });
  $row.find('.sum').text(sum);

  // 예상예약/부족 표시(기존 코드 유지)
  const mid = String($row.data('material') || '');
  const previewReserve = (mid === 'RM-0015') ? '-' : Math.min(sum, required);
  $row.find('.preview-reserve').text(previewReserve);
  const shortageUi = (mid === 'RM-0015') ? 0 : Math.max(0, required - sum);
  $row.find('.shortage').text(shortageUi);

  // 색상 규칙: 충족도 기준
  $row.removeClass('table-success table-warning table-danger table-secondary');
  if (required <= 0) {
    $row.addClass('table-secondary');            // 필요 없음
  } else if (sum >= required) {
    $row.addClass('table-success');              // 완전 충족(부족 0)
  } else if (sum > 0) {
    $row.addClass('table-warning');              // 일부만 충족(부족 있음)
  } else {
    $row.addClass('table-danger');               // 전혀 배정 안 됨
  }
  
  
}


/* ---------- 입력 제한 및 실시간 검증 ---------- */
$(document).on('wheel', '.lot-qty', function(e) { e.preventDefault(); });

$(document).on('input change blur', '.lot-qty', function() {
  const $input = $(this);
  const $row = $input.closest('tr');

  const hasMax   = $input.is('[max]');
  const maxLotQty = hasMax ? Number($input.attr('max')) : Infinity;

  let inputValue = Number($input.val()) || 0;
  if (inputValue < 0) inputValue = 0;
  if (inputValue > maxLotQty) inputValue = maxLotQty;

  const required = Number($row.find('.req').data('req')) || 0;
  const capData = $row.data('cap');
  const cap = (capData == null) ? required : Number(capData);

  const othersSum = $row.find('.lot-qty').not($input).toArray()
    .reduce((sum, el) => sum + (Number(el.value) || 0), 0);
  const remaining = Math.max(Math.min(required, cap) - othersSum, 0);
  if (inputValue > remaining) inputValue = remaining;

  $input.val(inputValue);
  updateRowSumAndValidate($row);
  window.validateAll();
});

/* ---------- 폼 제출 시 hidden 필드 생성 + 예약 선처리 ---------- */
$(document).off('submit.resv', '#outboundForm')
.on('submit.resv', '#outboundForm', function(e) {
  e.preventDefault();
  const formEl = this;
  const $form  = $(formEl);

  const workOrderId = $('#workOrderIdHidden').val()
                   || new URLSearchParams(location.search).get('workOrderId');
  if (!workOrderId) { alert('작업지시서를 먼저 선택하세요.'); return; }

  if ($form.data('reserving') === true) return;
  $form.data('reserving', true);
  $('#btnSubmit').prop('disabled', true).text('등록 중...');

  // 기존 hidden 초기화
  $form.find('input[name=materialIdList],input[name=reqQtyList],input[name=lotMaterialIdList],input[name=lotNoList],input[name=qtyList],input[name=lotWarehouseList]').remove();

  // 자재별 정보 hidden 생성
  $('#materialLotBody tr').each(function() {
    const materialId = $(this).data('material');
    const required   = +($(this).find('.req').data('req') || 0);
    if (materialId) {
      $form.append($('<input>', { type:'hidden', name:'materialIdList', value: materialId }));
      $form.append($('<input>', { type:'hidden', name:'reqQtyList',    value: required   }));
    }
  });

  // LOT별 hidden 생성
  $('.lot-qty').each(function() {
    const quantity = +(this.value || 0);
    if (quantity > 0) {
      const $el = $(this);
      $form.append($('<input>', { type:'hidden', name:'lotMaterialIdList', value: $el.data('material') }));
      $form.append($('<input>', { type:'hidden', name:'lotNoList',         value: $el.data('lot') || '' }));
      $form.append($('<input>', { type:'hidden', name:'qtyList',           value: quantity }));
      $form.append($('<input>', { type:'hidden', name:'lotWarehouseList',  value: ($el.data('warehouse') || '') }));
    }
  });

  // 예약만 선처리
  $.post(ctx + '/material/reservation/reserve-only', { workOrderId: workOrderId })
    .done(function(res){
      if (!res || res.ok !== true) {
        alert(res && res.message ? res.message : '예약 실패');
        $form.data('reserving', false);
        $('#btnSubmit').prop('disabled', false).text('등록');
        return;
      }
      
      // === [NEW] 선택된 모든 입고건 사용상태 갱신 ===
      const ids = getInboundIdsParam();
      if (ids.length) updateInboundStatuses(ids);
      
      formEl.submit(); // 네이티브 submit
    })
    .fail(function(xhr){
      console.error('예약 처리 실패:', xhr);
      alert('예약 처리 중 서버 오류가 발생했습니다.');
      $form.data('reserving', false);
      $('#btnSubmit').prop('disabled', false).text('등록');
    });
});

/* ---------- 부족분 발주 초안 생성 (+ 동시 예약) with Alerts ---------- */
$('#btnCreateDraft').off('click.draft').on('click.draft', function (e) {
  e.preventDefault();

  const workOrderId = $('#workOrderIdHidden').val()
                   || new URLSearchParams(location.search).get('workOrderId');
  if (!workOrderId) { alert('작업지시서를 먼저 선택하세요.'); return; }

  // ✅ 클릭 확인 알림 (사용자 확인 필수)
  if (!confirm('부족분 발주를 생성하고,\n현재 가용분을 이번 작업지시로 예약하시겠습니까?\n\n[확인] 발주+예약 진행 / [취소] 중단')) {
    return;
  }

  const $btn = $(this);
  if ($btn.prop('disabled')) return;

  const restoreBtn = () => $btn.prop('disabled', false).text('부족분 발주');
  const goToOrderList = (ids) => {
    const goto = () => {
      if (ids && ids.length) {
        const qs = '?status=DRAFT&highlight=' + encodeURIComponent(ids.join(','));
        location.href = ctx + '/material/order/list' + qs;
      } else {
        alert('부족분이 없어 발주를 생성하지 않았습니다.');
        location.href = ctx + '/material/outbound/list';
      }
    };
    // alert가 잘 보이도록 아주 짧게 지연
    setTimeout(goto, 300);
  };

  $btn.prop('disabled', true).text('생성 중...');

  // 1) 부족분 발주 생성
  $.post(ctx + '/material/reservation/create-shortage-po', { workOrderId })
    .done(function(res) {
      const ids = (res && (res.orderIds || (res.orderId ? [res.orderId] : []))) || [];

      if (!(res && res.ok === true)) {
        alert((res && res.message) ? res.message : '부족분 발주 생성에 실패했습니다.');
        return restoreBtn();
      }

      // 2) 발주 성공 → 예약 수행
      const doReserve = () => $.post(ctx + '/material/reservation/reserve-only', { workOrderId });

      doReserve()
        .done(function(r1){
          if (r1 && r1.ok === true) {
            alert('부족분 발주가 생성되었고,\n이번 작업지시 기준으로 재고 예약을 완료했습니다.');
            goToOrderList(ids);
          } else {
            // 재시도 1회
            doReserve()
              .done(function(r2){
                if (r2 && r2.ok === true) {
                  alert('부족분 발주 생성 완료.\n예약은 재시도에서 성공했습니다.');
                  goToOrderList(ids);
                } else {
                  if (confirm('발주는 생성됐지만 예약에 실패했습니다.\n발주 목록으로 이동할까요?')) {
                    goToOrderList(ids);
                  } else {
                    restoreBtn();
                  }
                }
              })
              .fail(function(){
                if (confirm('발주는 생성됐지만 예약 호출에 실패했습니다.\n발주 목록으로 이동할까요?')) {
                  goToOrderList(ids);
                } else {
                  restoreBtn();
                }
              });
          }
        })
        .fail(function(){
          // 첫 호출 실패 → 재시도
          doReserve()
            .done(function(r2){
              if (r2 && r2.ok === true) {
                alert('부족분 발주 생성 완료.\n예약은 재시도에서 성공했습니다.');
                goToOrderList(ids);
              } else {
                if (confirm('발주는 생성됐지만 예약에 실패했습니다.\n발주 목록으로 이동할까요?')) {
                  goToOrderList(ids);
                } else {
                  restoreBtn();
                }
              }
            })
            .fail(function(){
              if (confirm('발주는 생성됐지만 예약 호출에 실패했습니다.\n발주 목록으로 이동할까요?')) {
                goToOrderList(ids);
              } else {
                restoreBtn();
              }
            });
        });
    })
    .fail(function(xhr) {
      console.error('부족분 발주 실패:', xhr);
      const msg = (xhr.responseJSON && xhr.responseJSON.message) || xhr.responseText || '서버 오류';
      alert('부족분 발주 생성 중 오류가 발생했습니다.\n' + msg);
      restoreBtn();
    });
});



/* ---------- 출고 처리 ---------- */
window.processOutbound = function(outboundId, btnEl) {
	  if (!confirm('이 출고건을 처리하시겠습니까?')) return;

	  $.ajax({
	    type: 'POST',
	    url: ctx + '/material/outbound/process',
	    data: { outboundId: outboundId },
	    success: function(res){
	      if (res === 'OK') {
	        alert('출고처리 완료되었습니다.'); // ✅ 여기서 알림
	      }
	      var $tr = $(btnEl).closest('tr');
	      var $statusTd = $tr.find('td').eq(2);
	      $statusTd.html('<span class="badge badge-success">출고완료</span>');
	      $(btnEl).closest('td').empty();
	    },
	    error: function(xhr){
	      console.error('출고처리 오류:', xhr);
	      alert('출고처리 중 오류가 발생했습니다.\n' + (xhr.responseText || ''));
	    }
	  });
	};


//상태 뱃지 & 날짜 유틸
function badgeStatus(st){
  st = String(st || '').toUpperCase();
  if (st === 'COMPLETED' || st === 'ISSUED') return '<span class="badge badge-success">출고완료</span>';
  if (st === 'CANCELLED' || st === 'CANCELED') return '<span class="badge badge-secondary">취소</span>';
  return '<span class="badge badge-danger">미출고</span>'; // DRAFT, PARTIAL 등
}

// ☆ 새 모달 전용 구현
window.loadOutboundDetail = function(outboundId){
	  // 1) 초기화 & 모달 오픈
	  $('#modalOutboundId,#modalWorkOrderId,#modalIssuedAt,#modalHandledBy,#modalStatus,#modalNote').text('');
	  $('#outboundItemsInfo').html('<tr><td colspan="7" class="text-muted">불러오는 중...</td></tr>');
	  $('#outboundDetailModal').modal('show');

	  // 2) 데이터 조회
	  $.getJSON(ctx + '/material/outbound/detail', { outboundId })
	    .done(function(res){
	      // 헤더/아이템 방어적 매핑
	      var h = (res && (res.header || res.outbound || res)) || {};
	      var items = (res && (res.items || res.detailItems)) || [];
	      
	      items.sort(function(a,b){
	          return (a.materialId||'').localeCompare(b.materialId||'')
	              || (a.materialName||'').localeCompare(b.materialName||'')
	              || (a.lotNo||'').localeCompare(b.lotNo||'');
	        });

	      var issuedAt = h.outboundDate || h.issuedAt || h.createdDate;
	      var handledBy = h.handledBy || h.userName || h.createdBy;
	      var status = h.status || h.statusCode || h.status_display;
	      var note = h.note || h.remark || '';

	      // 3) 헤더 채우기
	      $('#modalOutboundId').text(h.outboundId || '-');
	      $('#modalWorkOrderId').text(h.workOrderId || '-');
	      $('#modalIssuedAt').text(fmtYmd(issuedAt));
	      $('#modalHandledBy').text(handledBy || '-');
	      $('#modalStatus').html(badgeStatus(status));
	      $('#modalNote').text(note || '-');

	      // 4) 상세 항목
	      if (!items.length){
	        $('#outboundItemsInfo').html('<tr><td colspan="7" class="text-muted">항목이 없습니다.</td></tr>');
	        return;
	      }

	      var rows = items.map(function(it){
	    	var exp = fmtYmd(it.expirationDate);
	        var wh = it.warehouseCode || it.warehouse_code || it.storageLocation || '-';

	        return '<tr>'
	             + '<td>' + (it.materialId||'') + '</td>'
	             + '<td>' + (it.materialName||'') + '</td>'
	             + '<td>' + (it.lotNo||'') + '</td>'
	             + '<td class="text-right">' + ((it.quantity != null) ? it.quantity : 0) + '</td>'
	             + '<td>' + wh + '</td>'
	             + '<td>' + exp + '</td>'
	             + '<td>' + (it.remark||'') + '</td>'
	             + '</tr>';
	      }).join('');

	      $('#outboundItemsInfo').html(rows);
	      
	      mergeRowspan('#outboundItemsInfo', [0, 1]);
	    })
	    .fail(function(xhr){
	        $('#outboundItemsInfo').html('<tr><td colspan="7" class="text-danger">상세를 불러오지 못했습니다.</td></tr>');
	        console.error('outbound/detail error', xhr);
	      });
	};



/* ---------- 자동 배정 기능 ---------- */
// 행 단위 자동 배정 (FEFO 순으로 배정)
function autoAllocateForRow($row, onlyIfEnough) {
  const required = Number($row.find('.req').data('req')) || 0;
  const capData = $row.data('cap');
  const cap = (capData == null) ? required : Number(capData);
  const target = Math.min(required, cap);
  const $lotInputs = $row.find('.lot-qty');
  if (!$lotInputs.length || target <= 0) {
    $row.find('.lot-qty').val(0);
    updateRowSumAndValidate($row);
    return false;
  }

  const totalAvailable = $lotInputs.toArray().reduce((sum, el) => {
    const hasMax = $(el).is('[max]');
    const maxQty = hasMax ? Number($(el).attr('max')) : target;
    return sum + Math.max(0, maxQty);
  }, 0);

  if (onlyIfEnough && totalAvailable < target) return false;

  let allocatedSum = 0;
  $lotInputs.each(function () {
    const hasMax = $(this).is('[max]');
    const maxQty = hasMax ? Number($(this).attr('max')) : target;
    if (allocatedSum >= target) { $(this).val(0); return; }
    const needed = target - allocatedSum;
    const allocateQty = Math.min(Math.max(0, maxQty), needed);
    $(this).val(allocateQty);
    allocatedSum += allocateQty;
  });

  updateRowSumAndValidate($row);
  return allocatedSum >= target;
}

// 전체 자재 자동 배정
function autoAllocateAll(onlyIfEnough) {
  $('#materialLotBody tr').each(function () {
    autoAllocateForRow($(this), onlyIfEnough);
  });
  window.validateAll();
}

const STATUS_LABEL = { DRAFT:'미출고', PARTIAL:'부분출고', ISSUED:'출고완료', CANCELED:'취소' };