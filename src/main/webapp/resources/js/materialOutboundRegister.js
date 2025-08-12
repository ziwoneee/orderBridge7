/* ---------- 전역: 전체 검증 ---------- */
window.validateAll = function () {
  let hasSupplyShortage = false;      // cap < req 인 행 존재?
  let hasSelectionMismatch = false;   // sum !== target 인 행 존재?

  $('#materialLotBody tr').each(function () {
    const req = Number($(this).find('.req').data('req')) || 0;

    // null/undefined만 대체(0 허용)
    const capData = $(this).data('cap');
    const cap = (capData == null) ? req : Number(capData);

    const mid = String($(this).data('material') || '');
    const target = Math.min(req, cap);

    // 물 특례: target으로 고정
    if (mid === 'RM-0015') {
      $(this).find('.lot-qty').val(target);
      $(this).find('.sum').text(target);
    }

    const sum = Number($(this).find('.sum').text()) || 0;

    // 공급 부족 여부(물은 제외)
    if (mid !== 'RM-0015' && cap < req) {
      hasSupplyShortage = true;
    }
    // 선택합 불일치 여부
    if (sum !== target) {
      hasSelectionMismatch = true;
    }
  });

  // 등록: 공급부족 없고 + 선택합도 모두 OK일 때만
  const canSubmit = !hasSupplyShortage && !hasSelectionMismatch;
  $('#btnSubmit').prop('disabled', !canSubmit);

  // 부족분 발주: 공급부족이 하나라도 있을 때만
  $('#btnCreateDraft').prop('disabled', !hasSupplyShortage);

  return canSubmit;
};


// 부족분 발주 생성
window.collectShortages = function () {
  const shortages = [];
  $('#materialLotBody tr').each(function(){
	const mid = String($(this).data('material') || '');
	if (mid === 'RM-0015') return;  // 물은 발주대상 제외
	
    const materialId = $(this).data('material');
    const materialName = $(this).find('.font-weight-bold').text().trim();
    const required = +($(this).find('.req').data('req') || 0);
    const capData = $(this).data('cap');
    const cap = (capData == null) ? required : Number(capData);
    
    if (cap < required) {
    	shortages.push({ materialId, lackQty: required - cap, materialName });
    }
  });
  return shortages;
};

/* ---------- 공통 유틸 ---------- */
function toYmd(dateValue) {
  if (!dateValue) return '';
  const date = (dateValue instanceof Date) ? dateValue : new Date(dateValue);
  if (isNaN(date.getTime())) return '';        // 파싱 실패 방지
  
  const year = date.getFullYear();
  const month = ('0' + (date.getMonth() + 1)).slice(-2);
  const day = ('0' + date.getDate()).slice(-2);
  return `${year}-${month}-${day}`;
}

function fmtDate(value) { 
  return toYmd(value); 
}

//* ---------- register.jsp: 초기 로드 ---------- */
$(function(){
  if (!$('#outboundForm').length) return;

  const params = new URLSearchParams(location.search);
  const workOrderId = params.get('workOrderId');
  const inboundId   = params.get('inboundId');

  // 둘 다 없으면 대기
  if (!workOrderId && !inboundId) return;

  // 1) workOrderId 있으면: 기존대로 로드
  if (workOrderId) {
    $.get(ctx + '/material/outbound/work-order', { workOrderId }, function(dto){
      const no = dto.workOrderId || '';
      const dueStr = toYmd(dto.dueDate) || '';
        $('#workOrderIdView').val(no);
        $('#workOrderIdHidden').val(no);
      
        $('#productId').val(dto.productId || '');
        $('#productIdHidden').val(dto.productId || '');
      
        $('#lineId').val(dto.lineId || '');
        $('#lineIdHidden').val(dto.lineId || '');
      
        $('#dueDateView').val(dueStr);
        $('#dueDateHidden').val(dueStr);
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

  if (!items || items.length === 0) {
    $tbody.append('<tr><td colspan="6" class="text-center text-muted">자재 정보가 없습니다.</td></tr>');
    return;
  }

  const promises = (items || []).map(function(item) {
	  const workOrderId = $('#workOrderIdHidden').val()
	                   || new URLSearchParams(location.search).get('workOrderId');
	  const required = Number(item.requiredQty) || 0;

	  // LOT 먼저 호출 → availability는 실패해도 fallback
	  return $.get(ctx + '/material/inventory/lot-details', { materialId: item.materialId })
	  .then(function(lots) {
	    lots = lots || [];

	    // availability 실패도 '성공 payload'로 변환해서 넘김
	    return $.get(ctx + '/material/inventory/availability',
	                 { materialId: item.materialId, workOrderId: workOrderId })
	      .then(
	        function(avail) {
	          return { lots: lots, avail: (avail || {}) };
	        },
	        function() { // ← 404 등 실패
	          var onhandFromLots = 0;
	          for (var i = 0; i < lots.length; i++) {
	            onhandFromLots += Number(lots[i].quantity) || 0;
	          }
	          return {
	            lots: lots,
	            avail: {
	              onhandTotal: onhandFromLots,
	              reservedOthers: 0,   // 다른 WO 예약
	              reservedForThis: 0   // 이 WO 예약
	              // availableForThisWO 는 아예 넣지 않음 (아래 계산식에서 구함)
	            }
	          };
	        }
	      );
	  })
	  .then(function(payload) {
	    try {
	      var lots  = payload.lots;
	      var avail = payload.avail;

	      var required = Number(item.requiredQty) || 0;
	      
	      var onhandTotal = Number(avail.onhandTotal) || 0;
	      // lots 물리합계(LOT 편차 방지용)
	      var onhandFromLots = (lots || []).reduce((s, l) => s + (Number(l.quantity) || 0), 0);

	      // 예약 분리(서버가 reservedOthers/reservedForThis 안 주면 reservedTotal을 others로 간주)
	      var reservedOthers = Number((avail.reservedOthers != null ? avail.reservedOthers : avail.reservedTotal) || 0);
	      var reservedThis   = Number(avail.reservedForThis || 0);

	      // 이 WO 가용
	      let a4wo = (avail.availableForThisWO != null)
	        ? Number(avail.availableForThisWO)
	        : Math.max(0, onhandTotal - reservedOthers) + reservedThis;

	      // 물리 재고 상한(LOT 합과 전체 onhand 모두 고려)
	      a4wo = Math.min(a4wo, onhandTotal, onhandFromLots);

	      // 최종 cap = 이번 행에서 쓸 수 있는 상한
	      var cap = Math.max(0, Math.min(required, a4wo));

	      // ★ 물(RM-0015)은 직접출고: 가용/예약에 묶지 말고 cap을 필요수량으로 고정
	      var mid = item.materialId;
	      if (mid === 'RM-0015') {
	    	         a4wo = required;   // 표시용. 맘에 들면 '직접출고'로 바꿔도 됨
	    	         cap  = required;   // 핵심: target = min(required, cap) = required
	      }
	      
	      var lotRequired = (item.lotRequired === 'N' || item.lotFlag === 'N' || item.materialId === 'RM-0015') ? 'N' : 'Y';
	      var defaultWh   = item.defaultWarehouseCode || item.warehouseCode || 'WH003';

	      var $row = $('<tr>')
	        .attr('data-material', item.materialId)
	        .attr('data-lot-required', lotRequired)
	        .attr('data-default-warehouse', defaultWh)
	        .data('cap', cap)
	      	.data('a4wo', a4wo);
	      	
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
	        // FEFO
	        lots.sort(function(a, b) {
	          var da = new Date(a.expirationDate || '9999-12-31').getTime();
	          var db = new Date(b.expirationDate || '9999-12-31').getTime();
	          return da - db;
	        });
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
	      
	      const a4woText = (mid === 'RM-0015') ? '직접출고' : a4wo;
	       $row.append(
	         $('<td>').addClass('text-right align-middle a4wo').text(a4woText)
	       );
	      
	      // 합계 + 부족 셀
	      $row.append(
	    		  $('<td>').addClass('text-right align-middle')
	    		  .append($('<span>').addClass('sum').text('0'))
		  );
	      
	      // '부족'은 조달 관점: 처음엔 전량 부족으로 시작
	      $row.append($('<td>').addClass('text-right align-middle shortage').text(required));
	      
	      $('#materialLotBody').append($row);
	      updateRowSumAndValidate($row);
	      return true;
	    } catch (e) {
	      console.error('자재 행 렌더링 오류:', e);
	      return false;
	    }
	  })
	  .fail(function(xhr) { // LOT API가 진짜 실패했을 때만
	    console.error('자재 정보 로드 실패:', item.materialId, xhr);
	    var $row = $('<tr>')
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
  
  // ✅ 모든 행 렌더링이 끝난 뒤 자동배정 + 검증 실행
  Promise.all(promises).then(function () {
    autoAllocateAll(false);   // FEFO 기준 자동 채우기
    
    // ★ 물 행은 합계/입력값을 target으로 동기화
    $('#materialLotBody tr').each(function () {
    	  const mid = String($(this).data('material') || '');
    	  if (mid === 'RM-0015') {
    	    const req = Number($(this).find('.req').data('req')) || 0;

    	    // 0을 허용하면서 null/undefined만 대체
    	    const capData = $(this).data('cap');
    	    const cap = (capData == null) ? req : Number(capData);

    	    const target = Math.min(req, cap);
    	    $(this).find('.lot-qty').val(target);
    	    $(this).find('.sum').text(target);
    	    updateRowSumAndValidate($(this));
    	  }
    	});

    
    window.validateAll();     // 버튼 활성/비활성 최종 결정
  });
}

/* ---------- 행별 합계 계산 및 상태 업데이트 ---------- */
function updateRowSumAndValidate($row) {
	  const required = Number($row.find('.req').data('req')) || 0;

	  const capData = $row.data('cap');
	  const cap = (capData == null) ? required : Number(capData);

	  const target = Math.min(required, cap);
	  const lotRequired = ($row.data('lot-required') === 'N') ? 'N' : 'Y';

	  let sum = 0;
	  $row.find('.lot-qty').each(function () {
	    sum += Number(this.value) || 0;
	  });
	  $row.find('.sum').text(sum);
	  
	  // 화면용 '부족' = 필요수량 - 현재 선택합계 (음수 방지)
	  const mid = String($row.data('material') || '');
	  const shortageUi = (mid === 'RM-0015') ? 0 : Math.max(0, required - sum);
	  $row.find('.shortage').text(shortageUi);
	  
	  const noShortage = (cap >= required);
	  const selectedOk = (sum === target);
	  
	  $row.removeClass('table-success table-warning table-danger');
	  if (lotRequired === 'N') {
	    if (noShortage && sum >= target && target > 0) $row.addClass('table-success');
	    else if (sum > 0) $row.addClass('table-warning');
	    else $row.addClass('table-danger');
	    return;
	  }
	  // LOT 필요한 자재는 기존 규칙
	  if (target === 0 && required > 0) $row.addClass('table-warning');
	  else if (noShortage && selectedOk) $row.addClass('table-success');
	  
	  else if (sum > 0) $row.addClass('table-warning');
	  else $row.addClass('table-danger');
	}


/* ---------- 입력 제한 및 실시간 검증 ---------- */
$(document).on('wheel', '.lot-qty', function(e) { 
  e.preventDefault(); 
});

$(document).on('input change blur', '.lot-qty', function() {
  const $input = $(this);
  const $row = $input.closest('tr');

  // ★ max 없으면 Infinity로 처리 (이전: 0으로 깎여버림)
  const hasMax   = $input.is('[max]');
  const maxLotQty = hasMax ? Number($input.attr('max')) : Infinity;

  let inputValue = Number($input.val()) || 0;
  
  // 음수 방지
  if (inputValue < 0) inputValue = 0;
  // LOT 재고량 초과 방지
  if (inputValue > maxLotQty) inputValue = maxLotQty;

  const required = Number($row.find('.req').data('req')) || 0;

  const capData = $row.data('cap');
  const cap = (capData == null) ? required : Number(capData);

  const othersSum = $row.find('.lot-qty').not($input).toArray()
    .reduce((sum, el) => sum + (Number(el.value) || 0), 0);
  const remaining = Math.max(Math.min(required, cap) - othersSum, 0); // ✅ cap 반영
  // 필요수량 초과 방지
  if (inputValue > remaining) inputValue = remaining;

  $input.val(inputValue);
  updateRowSumAndValidate($row);
  window.validateAll();
});

/* ---------- 폼 제출 시 hidden 필드 생성 + 예약 선처리 ---------- */
$(document).off('submit.resv', '#outboundForm')
.on('submit.resv', '#outboundForm', function(e) {
  e.preventDefault();                 // 1) 기본 submit 막기
  const formEl = this;                // 네이티브 form 엘리먼트
  const $form  = $(formEl);

  // 작업지시서 번호
  const workOrderId = $('#workOrderIdHidden').val()
                   || new URLSearchParams(location.search).get('workOrderId');
  if (!workOrderId) { 
    alert('작업지시서를 먼저 선택하세요.'); 
    return; 
  }

  // (선택) 다중 클릭 방지
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
      $form.append($('<input>', { type:'hidden', name:'lotNoList',         value: $el.data('lot') || ''       }));
      $form.append($('<input>', { type:'hidden', name:'qtyList',           value: quantity              }));
      $form.append($('<input>', { type:'hidden', name:'lotWarehouseList',  value: ($el.data('warehouse') || '') }));
    }
  });

  // ✅ 예약만 선처리
  $.post(ctx + '/material/reservation/reserve-only', { workOrderId: workOrderId })
    .done(function(res){
      if (!res || res.ok !== true) {
        alert(res && res.message ? res.message : '예약 실패');
        $form.data('reserving', false);
        $('#btnSubmit').prop('disabled', false).text('등록');
        return;
      }
      // ✅ jQuery가 아닌 "네이티브 submit" 호출 → 이벤트 재진입 없음
      formEl.submit();
    })
    .fail(function(xhr){
      console.error('예약 처리 실패:', xhr);
      alert('예약 처리 중 서버 오류가 발생했습니다.');
      $form.data('reserving', false);
      $('#btnSubmit').prop('disabled', false).text('등록');
    });
});

/* ---------- 부족분 발주 초안 생성 ---------- */
$('#btnCreateDraft').off('click.draft').on('click.draft', function (e) {
  e.preventDefault();

  const workOrderId = $('#workOrderIdHidden').val()
                   || new URLSearchParams(location.search).get('workOrderId');
  if (!workOrderId) {
    alert('작업지시서를 먼저 선택하세요.');
    return;
  }
  
  //🔹 여기서 사용자에게 최종 확인
  if (!confirm('이 작업지시서의 부족분 발주 초안을 생성하시겠습니까?')) {
    return; // 사용자가 "취소" 누르면 그냥 종료
  }

  const $btn = $(this);
  if ($btn.prop('disabled')) return;
  $btn.prop('disabled', true).text('생성 중...');

  $.post(ctx + '/material/reservation/create-shortage-po', { workOrderId })
    .done(function(res) {
      // 서버 응답 포맷: { ok:true, orderIds:["PO-...","PO-..."] } 또는 { ok:true, orderId:"PO-..." }
      const ids = (res && (res.orderIds || (res.orderId ? [res.orderId] : []))) || [];

      if (res && res.ok === true && ids.length > 0) {
    	alert('부족분 발주 초안이 생성되었습니다.');
        // 발주목록(초안 필터)로 이동 + 방금 생성된 항목 하이라이트
        const qs = '?status=DRAFT&highlight=' + encodeURIComponent(ids.join(','));
        location.href = ctx + '/material/order/list' + qs;
        return;
      }

      if (res && res.ok === true) {
        // 생성할 부족분이 없을 때: 출고목록으로 보냄
        alert('부족분이 없어 발주를 생성하지 않았습니다.');
        location.href = ctx + '/material/outbound/list';
      } else {
        alert((res && res.message) ? res.message : '부족분 발주 생성에 실패했습니다.');
        $btn.prop('disabled', false).text('부족분 발주');
      }
    })
    .fail(function(xhr) {
      console.error('부족분 발주 실패:', xhr);
      const msg = (xhr.responseJSON && xhr.responseJSON.message) || xhr.responseText || '서버 오류';
      alert('부족분 발주 생성 중 오류가 발생했습니다.\n' + msg);
      $btn.prop('disabled', false).text('부족분 발주');
    });
});



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

	  // 총 가능 수량 계산
	  const totalAvailable = $lotInputs.toArray().reduce((sum, el) => {
	    const hasMax = $(el).is('[max]');
	    const maxQty = hasMax ? Number($(el).attr('max')) : target;
	    return sum + Math.max(0, maxQty);
	  }, 0);

	  // 충분하지 않으면 배정하지 않음 (옵션에 따라)
	  if (onlyIfEnough && totalAvailable < target) return false;

	  // FEFO 순으로 배정
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


/* ========== 입고건 사용상태 관리 모듈 ========== */

/**
 * 입고건 사용상태 관리 객체
 */
var InboundUsageManager = {
  
  // 사용상태 캐시
  statusCache: {},
  
  /**
   * 사용상태 업데이트
   * @param {string} inboundId 입고ID
   * @param {function} callback 완료 콜백
   */
  updateUsageStatus: function(inboundId, callback) {
    if (!inboundId) {
      console.warn('InboundUsageManager.updateUsageStatus: inboundId is required');
      return;
    }
    
    var self = this;
    
    $.post(ctx + '/material/outbound/update-inbound-status', { inboundId: inboundId })
      .done(function(result) {
        console.log('입고건 사용상태 업데이트 완료:', inboundId, result);
        
        // 캐시 무효화
        delete self.statusCache[inboundId];
        
        if (typeof callback === 'function') {
          callback(null, result);
        }
      })
      .fail(function(xhr) {
        var error = '입고건 사용상태 업데이트 실패: ' + (xhr.responseText || xhr.statusText);
        console.error(error, xhr);
        
        if (typeof callback === 'function') {
          callback(error, null);
        }
      });
  },
  
  /**
   * 여러 입고건의 사용상태 일괄 업데이트
   * @param {string[]} inboundIds 입고ID 배열
   * @param {function} callback 완료 콜백
   */
  batchUpdateUsageStatus: function(inboundIds, callback) {
    if (!inboundIds || !Array.isArray(inboundIds) || inboundIds.length === 0) {
      console.warn('InboundUsageManager.batchUpdateUsageStatus: valid inboundIds array is required');
      return;
    }
    
    var self = this;
    var results = [];
    var errors = [];
    var completed = 0;
    var total = inboundIds.length;
    
    function checkCompletion() {
      completed++;
      if (completed === total) {
        if (typeof callback === 'function') {
          callback(errors.length > 0 ? errors : null, results);
        }
      }
    }
    
    inboundIds.forEach(function(inboundId) {
      self.updateUsageStatus(inboundId, function(error, result) {
        if (error) {
          errors.push({ inboundId: inboundId, error: error });
        } else {
          results.push({ inboundId: inboundId, result: result });
        }
        checkCompletion();
      });
    });
  },
  
  /**
   * 사용상태 배지 HTML 생성
   * @param {string} status 사용상태
   * @return {string} 배지 HTML
   */
  getStatusBadgeHtml: function(status) {
    switch(status) {
      case 'AVAILABLE':
        return '<span class="badge badge-success">사용가능</span>';
      case 'PARTIALLY_USED':
        return '<span class="badge badge-warning">부분사용</span>';
      case 'FULLY_USED':
        return '<span class="badge badge-secondary">완전사용</span>';
      default:
        return '<span class="badge badge-light">미확인</span>';
    }
  },
  
  /**
   * 사용상태 텍스트 반환
   * @param {string} status 사용상태
   * @return {string} 상태 텍스트
   */
  getStatusText: function(status) {
    switch(status) {
      case 'AVAILABLE': return '사용가능';
      case 'PARTIALLY_USED': return '부분사용';
      case 'FULLY_USED': return '완전사용';
      default: return '미확인';
    }
  },
  
  /**
   * 테이블 행의 사용상태 업데이트
   * @param {jQuery} $row 테이블 행 jQuery 객체
   * @param {string} newStatus 새로운 상태
   */
  updateRowStatus: function($row, newStatus) {
    var $statusCell = $row.find('.usage-status-cell');
    if ($statusCell.length > 0) {
      $statusCell.html(this.getStatusBadgeHtml(newStatus));
    }
  },
  
  /**
   * 출고처리 완료 시 관련 입고건들의 상태 자동 업데이트
   * @param {string} outboundId 출고ID
   * @param {function} callback 완료 콜백
   */
  updateStatusAfterOutbound: function(outboundId, callback) {
    if (!outboundId) {
      console.warn('InboundUsageManager.updateStatusAfterOutbound: outboundId is required');
      return;
    }
    
    var self = this;
    
    // 출고건과 연관된 입고건들 조회 (서버 API 필요 시 구현)
    // 현재는 단일 입고건만 처리하는 것으로 가정
    console.log('출고처리 완료 후 입고건 상태 업데이트:', outboundId);
    
    if (typeof callback === 'function') {
      callback(null, { message: '상태 업데이트 완료' });
    }
  }
};

/* ========== 통합 이벤트 핸들러 ========== */

/**
 * 출고 처리 완료 시 입고건 상태 자동 업데이트
 */
$(document).on('outboundProcessed', function(e, data) {
  if (data && data.outboundId) {
    InboundUsageManager.updateStatusAfterOutbound(data.outboundId, function(error, result) {
      if (error) {
        console.error('출고 후 입고건 상태 업데이트 실패:', error);
      } else {
        console.log('출고 후 입고건 상태 업데이트 완료:', result);
      }
    });
  }
});

/**
 * 페이지별 초기화
 */
$(document).ready(function() {
  
  // list.jsp에서 사용상태 컬럼이 있으면 실시간 업데이트 기능 활성화
  if ($('.usage-status-cell').length > 0) {
    console.log('입고건 사용상태 관리 기능 활성화');
    
    // 5분마다 사용상태 자동 갱신 (선택사항)
    setInterval(function() {
      $('.usage-status-cell').each(function() {
        var $cell = $(this);
        var inboundId = $cell.closest('tr').attr('data-inbound-id');
        if (inboundId) {
          InboundUsageManager.updateUsageStatus(inboundId);
        }
      });
    }, 5 * 60 * 1000); // 5분
  }
  
  // register.jsp에서 입고기반 등록 시 상태 관리
  var urlParams = new URLSearchParams(window.location.search);
  var inboundId = urlParams.get('inboundId');
  
  if (inboundId && $('#outboundForm').length > 0) {
    console.log('입고기반 출고등록 - 사용상태 관리 활성화:', inboundId);
    
    // 폼 제출 시 입고건 상태 업데이트
    $('#outboundForm').on('submit', function() {
      setTimeout(function() {
        InboundUsageManager.updateUsageStatus(inboundId, function(error, result) {
          if (!error) {
            console.log('출고등록 후 입고건 상태 업데이트 완료');
          }
        });
      }, 1000); // 출고등록 완료 후 1초 대기
    });
  }
});

/* ========== 확장 유틸리티 함수들 ========== */

/**
 * 입고건별 사용률 계산
 * @param {Object} inboundData 입고 데이터
 * @return {number} 사용률 (0-100)
 */
function calculateInboundUsageRate(inboundData) {
  if (!inboundData || !inboundData.totalInboundQty) return 0;
  
  var usedQty = inboundData.usedQty || 0;
  var totalQty = inboundData.totalInboundQty || 0;
  
  return totalQty > 0 ? Math.round((usedQty / totalQty) * 100) : 0;
}

/**
 * 사용률에 따른 진행바 HTML 생성
 * @param {number} usageRate 사용률 (0-100)
 * @return {string} 진행바 HTML
 */
function createUsageProgressBar(usageRate) {
  var progressClass = 'bg-success';
  if (usageRate >= 80) progressClass = 'bg-warning';
  if (usageRate >= 100) progressClass = 'bg-danger';
  
  return '<div class="progress" style="height: 6px;">' +
           '<div class="progress-bar ' + progressClass + '" role="progressbar" ' +
                'style="width: ' + usageRate + '%" aria-valuenow="' + usageRate + '" ' +
                'aria-valuemin="0" aria-valuemax="100"></div>' +
         '</div>' +
         '<small class="text-muted">' + usageRate + '%</small>';
}

/**
 * 입고건 상세 사용현황 모달 표시
 * @param {string} inboundId 입고ID
 */
function showInboundUsageDetail(inboundId) {
  if (!inboundId) return;
  
  // 가용 자재 정보 로드
  $.getJSON(ctx + '/material/outbound/available-materials', { inboundId: inboundId })
    .done(function(materials) {
      var modalHtml = '<div class="modal fade" id="inboundUsageModal" tabindex="-1">' +
                        '<div class="modal-dialog modal-lg">' +
                          '<div class="modal-content">' +
                            '<div class="modal-header">' +
                              '<h5 class="modal-title">입고건 사용현황 - ' + inboundId + '</h5>' +
                              '<button type="button" class="close" data-dismiss="modal">&times;</button>' +
                            '</div>' +
                            '<div class="modal-body">' +
                              generateUsageDetailTable(materials) +
                            '</div>' +
                            '<div class="modal-footer">' +
                              '<button type="button" class="btn btn-secondary" data-dismiss="modal">닫기</button>' +
                            '</div>' +
                          '</div>' +
                        '</div>' +
                      '</div>';
      
      // 기존 모달 제거 후 새 모달 추가
      $('#inboundUsageModal').remove();
      $('body').append(modalHtml);
      $('#inboundUsageModal').modal('show');
    })
    .fail(function() {
      alert('입고건 사용현황을 불러올 수 없습니다.');
    });
}

/**
 * 사용현황 상세 테이블 생성
 * @param {Array} materials 자재 목록
 * @return {string} 테이블 HTML
 */
function generateUsageDetailTable(materials) {
  if (!materials || materials.length === 0) {
    return '<div class="text-center text-muted">사용현황 데이터가 없습니다.</div>';
  }
  
  var tableHtml = '<div class="table-responsive">' +
                    '<table class="table table-bordered table-sm">' +
                      '<thead class="thead-light">' +
                        '<tr>' +
                          '<th>자재명</th>' +
                          '<th>LOT번호</th>' +
                          '<th class="text-center">입고수량</th>' +
                          '<th class="text-center">사용수량</th>' +
                          '<th class="text-center">가용수량</th>' +
                          '<th class="text-center">사용률</th>' +
                        '</tr>' +
                      '</thead>' +
                      '<tbody>';
  
  materials.forEach(function(mat) {
    var inboundQty = mat.inboundQty || 0;
    var usedQty = mat.usedQty || 0;
    var availableQty = mat.availableQty || 0;
    var usageRate = inboundQty > 0 ? Math.round((usedQty / inboundQty) * 100) : 0;
    
    tableHtml += '<tr>' +
                   '<td>' + (mat.materialName || '') + '</td>' +
                   '<td><span class="badge badge-light">' + (mat.lotNo || '') + '</span></td>' +
                   '<td class="text-center">' + inboundQty + '</td>' +
                   '<td class="text-center">' + usedQty + '</td>' +
                   '<td class="text-center"><strong>' + availableQty + '</strong></td>' +
                   '<td class="text-center">' + createUsageProgressBar(usageRate) + '</td>' +
                 '</tr>';
  });
  
  tableHtml += '</tbody></table></div>';
  
  return tableHtml;
}

//register.jsp에 추가할 JavaScript 함수들

/* ========== 입고기반 출고등록 관련 함수들 ========== */

/**
 * 입고 ID를 기반으로 가용 자재 정보를 로드하여 자동 LOT 선택
 */
function loadMaterialsFromInbound(inboundId) {
  if (!inboundId) return;
  
  console.log('Loading materials from inbound:', inboundId);
  
  $.getJSON(ctx + '/material/outbound/available-materials', { inboundId: inboundId })
    .done(function(materials) {
      if (!materials || materials.length === 0) {
        console.warn('No available materials found for inbound:', inboundId);
        return;
      }
      
      console.log('Available materials loaded:', materials.length);
      
      // 자재별로 그룹화
      var materialGroups = {};
      materials.forEach(function(mat) {
        var materialId = mat.materialId;
        if (!materialGroups[materialId]) {
          materialGroups[materialId] = {
            materialId: materialId,
            materialName: mat.materialName,
            requiredQty: mat.requiredQty,
            lots: []
          };
        }
        
        materialGroups[materialId].lots.push({
          lotNo: mat.lotNo,
          availableQty: mat.availableQty,
          expirationDate: mat.expirationDate,
          inventoryId: mat.inventoryId
        });
      });
      
      // FEFO 정렬 및 자동 선택 적용
      applyAutoLotSelection(materialGroups);
    })
    .fail(function(xhr) {
      console.error('Failed to load materials from inbound:', xhr);
      showNotification('입고건 자재 로딩 실패', 'error');
    });
}

/**
 * 자동 LOT 선택 적용 (FEFO 기준)
 */
function applyAutoLotSelection(materialGroups) {
  Object.keys(materialGroups).forEach(function(materialId) {
    var group = materialGroups[materialId];
    var requiredQty = group.requiredQty || 0;
    var remainingQty = requiredQty;
    
    // 유통기한 순으로 정렬 (FEFO)
    group.lots.sort(function(a, b) {
      var dateA = new Date(a.expirationDate || '9999-12-31');
      var dateB = new Date(b.expirationDate || '9999-12-31');
      return dateA - dateB;
    });
    
    // 자재별 LOT 체크박스들 찾기
    var $materialRow = $('#materialLotBody tr[data-material-id="' + materialId + '"]');
    if ($materialRow.length === 0) return;
    
    // LOT별로 자동 선택 및 수량 입력
    group.lots.forEach(function(lot) {
      if (remainingQty <= 0) return;
      
      var $lotCheckbox = $materialRow.find('input[type="checkbox"][data-lot-no="' + lot.lotNo + '"]');
      var $lotQtyInput = $materialRow.find('input[type="number"][data-lot-no="' + lot.lotNo + '"]');
      
      if ($lotCheckbox.length && $lotQtyInput.length) {
        var selectQty = Math.min(remainingQty, lot.availableQty);
        
        if (selectQty > 0) {
          $lotCheckbox.prop('checked', true);
          $lotQtyInput.val(selectQty).prop('disabled', false);
          remainingQty -= selectQty;
          
          // 선택 이벤트 트리거
          $lotCheckbox.trigger('change');
        }
      }
    });
    
    // 자재별 선택합계 업데이트
    updateMaterialSelection(materialId);
  });
  
  // 전체 선택상태 검증
  validateAllSelections();
  
  showNotification('입고기반 자동 LOT 선택이 완료되었습니다.', 'success');
}

/**
 * 입고건 사용상태 실시간 업데이트
 */
function updateInboundStatusRealtime(inboundId) {
  if (!inboundId) return;
  
  $.post(ctx + '/material/outbound/update-inbound-status', { inboundId: inboundId })
    .done(function(result) {
      console.log('입고건 사용상태 업데이트:', result);
    })
    .fail(function(xhr) {
      console.warn('입고건 사용상태 업데이트 실패:', xhr.responseText);
    });
}

/**
 * 알림 메시지 표시
 */
function showNotification(message, type) {
  type = type || 'info';
  var alertClass = 'alert-' + (type === 'error' ? 'danger' : type);
  
  var $alert = $('<div class="alert ' + alertClass + ' alert-dismissible fade show" role="alert">')
    .html(message + '<button type="button" class="close" data-dismiss="alert"><span>&times;</span></button>');
  
  // 기존 알림 제거 후 새 알림 추가
  $('.content-wrapper').find('.alert').remove();
  $('.content-wrapper').prepend($alert);
  
  // 3초 후 자동 제거
  setTimeout(function() {
    $alert.alert('close');
  }, 3000);
}

/**
 * URL 파라미터에서 inboundId 추출 및 자동 처리
 */
function handleInboundParameter() {
  var urlParams = new URLSearchParams(window.location.search);
  var inboundId = urlParams.get('inboundId');
  var workOrderId = urlParams.get('workOrderId');
  
  if (inboundId && workOrderId) {
    console.log('Processing inbound-based registration:', inboundId, '→', workOrderId);
    
    // 작업지시서 정보 로드 후 입고 자재 자동 선택
    setTimeout(function() {
      loadMaterialsFromInbound(inboundId);
    }, 1000); // DOM 로딩 완료 대기
    
    // 폼에 입고ID 히든필드 추가
    if ($('#inboundIdHidden').length === 0) {
      $('#outboundForm').append('<input type="hidden" name="inboundId" id="inboundIdHidden" value="' + inboundId + '">');
    }
    
    showNotification('입고완료건 기반 출고등록 모드입니다.', 'info');
  }
}

/**
 * 입고기반 등록 완료 후 처리
 */
function handleInboundBasedSubmit() {
  var inboundId = $('#inboundIdHidden').val();
  
  if (inboundId) {
    // 입고건 사용상태 업데이트
    updateInboundStatusRealtime(inboundId);
    
    // 성공 메시지에 입고ID 포함
    console.log('출고등록 완료 - 연관 입고건:', inboundId);
  }
}

// 페이지 로드 시 자동 실행
$(document).ready(function() {
  // 기존 초기화 코드 실행 후
  setTimeout(function() {
    handleInboundParameter();
  }, 500);
  
  // 폼 제출 시 입고기반 처리
  $('#outboundForm').on('submit', function(e) {
    handleInboundBasedSubmit();
    // 기존 제출 로직은 그대로 진행
  });
});

/* ========== 추가 유틸리티 함수들 ========== */

/**
 * 자재별 LOT 선택 현황 표시
 */
function showMaterialLotStatus() {
  var status = {};
  
  $('#materialLotBody tr[data-material-id]').each(function() {
    var $row = $(this);
    var materialId = $row.attr('data-material-id');
    var materialName = $row.find('td:first').text().trim();
    var requiredQty = parseInt($row.find('.required-qty').text()) || 0;
    var selectedQty = parseInt($row.find('.selected-sum').text()) || 0;
    
    status[materialId] = {
      materialName: materialName,
      requiredQty: requiredQty,
      selectedQty: selectedQty,
      shortage: Math.max(0, requiredQty - selectedQty),
      isComplete: selectedQty >= requiredQty
    };
  });
  
  console.table(status);
  return status;
}

/**
 * 전체 LOT 선택 초기화
 */
function resetAllLotSelections() {
  $('#materialLotBody input[type="checkbox"]').prop('checked', false);
  $('#materialLotBody input[type="number"]').val('').prop('disabled', true);
  $('#materialLotBody .selected-sum').text('0');
  $('#materialLotBody .shortage').text('0');
  
  validateAllSelections();
  showNotification('모든 LOT 선택이 초기화되었습니다.', 'info');
}

/**
 * 최적 LOT 선택 제안 (FEFO + 최소 LOT 수)
 */
function suggestOptimalLotSelection(materialId) {
  var $materialRow = $('#materialLotBody tr[data-material-id="' + materialId + '"]');
  if ($materialRow.length === 0) return;
  
  var requiredQty = parseInt($materialRow.find('.required-qty').text()) || 0;
  var lots = [];
  
  // 현재 자재의 모든 LOT 정보 수집
  $materialRow.find('.lot-selection input[type="checkbox"]').each(function() {
    var $checkbox = $(this);
    var lotNo = $checkbox.attr('data-lot-no');
    var availableQty = parseInt($checkbox.closest('.lot-item').find('.available-qty').text()) || 0;
    var expirationDate = $checkbox.closest('.lot-item').find('.expiration-date').text();
    
    lots.push({
      lotNo: lotNo,
      availableQty: availableQty,
      expirationDate: expirationDate,
      element: $checkbox
    });
  });
  
  // FEFO 정렬
  lots.sort(function(a, b) {
    return new Date(a.expirationDate) - new Date(b.expirationDate);
  });
  
  // 최적 조합 계산
  var suggestion = [];
  var remainingQty = requiredQty;
  
  lots.forEach(function(lot) {
    if (remainingQty <= 0) return;
    
    var selectQty = Math.min(remainingQty, lot.availableQty);
    if (selectQty > 0) {
      suggestion.push({
        lotNo: lot.lotNo,
        selectQty: selectQty,
        element: lot.element
      });
      remainingQty -= selectQty;
    }
  });
  
  // 제안 적용 여부 확인
  if (suggestion.length > 0) {
    var message = '최적 LOT 선택 제안:\n';
    suggestion.forEach(function(s) {
      message += '- ' + s.lotNo + ': ' + s.selectQty + '개\n';
    });
    
    if (confirm(message + '\n이 제안을 적용하시겠습니까?')) {
      // 제안 적용
      suggestion.forEach(function(s) {
        s.element.prop('checked', true).trigger('change');
        s.element.closest('.lot-item').find('input[type="number"]').val(s.selectQty);
      });
      
      updateMaterialSelection(materialId);
    }
  }
}