/* ---------- 전역: 전체 검증 ---------- */
window.validateAll = function () {
  let ok = true;

  $('#materialLotBody tr').each(function () {
    const req = Number($(this).find('.req').data('req')) || 0;

    // 0을 허용하면서 null/undefined만 대체
    const capData = $(this).data('cap');
    const cap = (capData == null) ? req : Number(capData);

    const target = Math.min(req, cap);
    let sum = Number($(this).find('.sum').text()) || 0;
    const mid = String($(this).data('material') || '');

    // 물(RM-0015) 특례
    if (mid === 'RM-0015') {
      const v = target;
      $(this).find('.lot-qty').val(v);
      $(this).find('.sum').text(v);
      sum = v;
    }

    if (target !== sum) { ok = false; return false; }
  });

  $('#btnSubmit').prop('disabled', !ok);
  return ok;
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
    const sum = +($(this).find('.sum').text() || 0);
    
    if (sum < required) {
      shortages.push({ 
        materialId: materialId, 
        lackQty: required - sum, 
        materialName: materialName 
      });
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
	  
	  $row.removeClass('table-success table-warning table-danger');
	  if (lotRequired === 'N') {
	    if (sum >= target && target > 0) $row.addClass('table-success');
	    else if (sum > 0) $row.addClass('table-warning');
	    else $row.addClass('table-danger');
	    return;
	  }
	  // LOT 필요한 자재는 기존 규칙
	  if (target === 0 && required > 0) $row.addClass('table-warning'); // 가용 0
	  else if (sum === target) $row.addClass('table-success');
	  
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

  const $btn = $(this);
  if ($btn.prop('disabled')) return; // 이미 처리 중이면 무시
  
  $btn.prop('disabled', true).text('생성 중...');

  $.post(ctx + '/material/reservation/create-shortage-po', { workOrderId: workOrderId })
    .done(function(res) {
      console.log('부족분 발주 응답:', res);
      if (res && res.ok === true) {
        if (res.orderId) {
          alert('부족분 발주 초안이 생성되었습니다: ' + res.orderId);
          // 필요시 발주 목록 새창/새로고침
          // window.open(ctx + '/material/order/list', '_blank');
        } else {
          alert('부족분이 없어 발주를 생성하지 않았습니다.');
        }
      } else {
        alert((res && res.message) ? res.message : '부족분 발주 생성에 실패했습니다.');
      }
    })
    .fail(function(xhr) {
      console.error('부족분 발주 실패:', xhr);
      const errorMsg = xhr.responseJSON ? xhr.responseJSON.message : xhr.responseText;
      alert('부족분 발주 생성 중 오류가 발생했습니다.\n' + (errorMsg || '서버 오류'));
    })
    .always(function() {
      $btn.prop('disabled', false).text('부족분 발주');
    });
});

/* ---------- 출고 처리 ---------- */
window.processOutbound = function(outboundId, btnEl){
  if (!confirm('이 출고건을 처리하시겠습니까?')) return;

  $.ajax({
    type: 'POST',
    url: ctx + '/material/outbound/process',
    data: { outboundId: outboundId },
    success: function(res){
      // 성공 시: 상태 뱃지 변경 + 버튼 제거
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

/* ---------- 출고 상세 조회 ---------- */
window.loadOutboundDetail = function(outboundId) {
  $.get(ctx + '/material/outbound/detail', { outboundId: outboundId })
    .done(function(data) {
      let html = '<div class="row">';
      html += '<div class="col-12"><h6>출고 정보</h6>';
      html += '<table class="table table-sm">';
      html += '<tr><th>출고번호</th><td>' + (data.outboundId || '') + '</td>';
      html += '<th>작업지시</th><td>' + (data.workOrderId || '') + '</td></tr>';
      html += '<tr><th>출고일자</th><td>' + (data.outboundDate || '-') + '</td>';
      html += '<th>담당자</th><td>' + (data.handledBy || '') + '</td></tr>';
      html += '</table></div>';
      
      if (data.items && data.items.length > 0) {
        html += '<div class="col-12 mt-3"><h6>출고 항목</h6>';
        html += '<table class="table table-sm table-bordered">';
        html += '<thead><tr><th>자재</th><th>LOT</th><th>출고수량</th><th>창고</th></tr></thead><tbody>';
        
        data.items.forEach(function(item) {
          html += '<tr>';
          html += '<td>' + (item.materialName || '') + '<br><small class="text-muted">' + (item.materialId || '') + '</small></td>';
          html += '<td>' + (item.lotNo || '') + '</td>';
          html += '<td class="text-right">' + (item.quantity || 0) + '</td>';
          html += '<td>' + (item.warehouseCode || '') + '</td>';
          html += '</tr>';
        });
        
        html += '</tbody></table></div>';
      }
      html += '</div>';
      
      $('#outboundDetailBody').html(html);
      $('#outboundDetailModal').modal('show');
    })
    .fail(function(xhr) {
      console.error('상세 정보 로드 실패:', xhr);
      alert('상세 정보를 불러올 수 없습니다.');
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
