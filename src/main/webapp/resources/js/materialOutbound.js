/* ---------- 전역: 전체 검증 ---------- */
window.validateAll = function () {
  let ok = true;
  $('#materialLotBody tr').each(function () {
    const req = +($(this).find('.req').data('req') || 0);
    const sum = +($(this).find('.sum').text() || 0);
    if (req !== sum) { ok = false; return false; }
  });
  $('#btnSubmit').prop('disabled', !ok);
  return ok;
};

// 부족분 발주 생성
window.collectShortages = function () {
  const s = [];
  $('#materialLotBody tr').each(function(){
    const mid  = $(this).data('material');
    const name = $(this).find('.font-weight-bold').text().trim();
    const req  = +($(this).find('.req').data('req')||0);
    const sum  = +($(this).find('.sum').text()||0);
    if (sum < req) s.push({ materialId: mid, lackQty: req - sum, materialName: name });
  });
  return s;
};

/* ---------- 공통 유틸 ---------- */
function toYmd(d){
	  if (!d) return '';
	  const dt = (d instanceof Date) ? d : new Date(d);
	  if (isNaN(dt.getTime())) return '';        // 파싱 실패 방지
	  const y = dt.getFullYear();
	  const m = ('0' + (dt.getMonth() + 1)).slice(-2);
	  const dd = ('0' + dt.getDate()).slice(-2);
	  return `${y}-${m}-${dd}`;
	}
	function fmtDate(val){ return toYmd(val); }


/* ---------- list.jsp 로직 (그대로) ---------- */
// ... loadWaitingOrders, 선택 이동 등 기존 코드 유지 ...

/* ---------- register.jsp: 초기 로드 ---------- */
$(function(){
  if (!$('#outboundForm').length) return;
  const params = new URLSearchParams(location.search);
  const workOrderId = params.get('workOrderId');
  if (!workOrderId) return;

  $.get(ctx + '/material/outbound/work-order', { workOrderId }, function(dto){
    $('#workOrderNo').val(dto.workOrderNo || '');
    $('#productId').val(dto.productId || '');
    $('#lineId').val(dto.lineId || '');
    $('#dueDate').val(toYmd(dto.dueDate));
    renderMaterialRows(dto.materialList || []);
  });
});

/* ---------- 자재 행 렌더 (행 만들기만) ---------- */
function renderMaterialRows(items){
  const $body = $('#materialLotBody').empty();

  const reqs = (items || []).map(function(it){
    return $.get(ctx + '/material/inventory/lot-details', { materialId: it.materialId })
      .then(function(lots){
        lots = lots || [];
        lots.sort((a,b) => new Date(a.expirationDate) - new Date(b.expirationDate));

        const $tr = $('<tr>').attr('data-material', it.materialId);

        // 1) 자재
        $tr.append(
          $('<td>').append(
            $('<div>').addClass('font-weight-bold').text(it.materialName || it.materialId),
            $('<div>').addClass('text-muted').append($('<small>').text(it.materialId))
          )
        );

        // 2) 필요수량
        $tr.append(
          $('<td>').addClass('text-right align-middle')
                   .append($('<span>').addClass('req').attr('data-req', it.requiredQty).text(it.requiredQty))
        );

        // 3) LOT 입력
        const $lotCell = $('<td>');
        if (!lots.length) {
          $lotCell.append($('<span>').addClass('text-danger').text('사용 가능한 LOT 없음'));
        } else {
          lots.forEach(function(l){
            const wh   = l.warehouseCode || it.defaultWarehouseCode || '';
            const qty0 = (l.quantity != null ? l.quantity : 0);

            const $row = $('<div>').addClass('form-row align-items-center lot-row mb-1')
                                   .attr('data-lot', l.lotNo);

            const $info = $('<div>').addClass('col-md-5')
              .append($('<small>').addClass('text-muted').text(l.lotNo || '-'))
              .append('<br>')
              .append($('<small>').text('유통기한: ' + fmtDate(l.expirationDate) + ' / 재고: ' + qty0));

            const $qtyCol = $('<div>').addClass('col-md-4')
              .append($('<input>', {
                type:'number', min:0, max: qty0, step:1, value:0, 'aria-label':'수량 입력'
              }).addClass('form-control form-control-sm lot-qty')
                .data({ material: it.materialId, lot: l.lotNo, wh: wh }));

            const $whCol = $('<div>').addClass('col-md-3')
              .append($('<span>').addClass('badge badge-secondary').text(wh));

            $row.append($info, $qtyCol, $whCol);
            $lotCell.append($row);
          });
        }
        $tr.append($lotCell);

        // 4) 합계
        $tr.append($('<td>').addClass('text-right align-middle')
                            .append($('<span>').addClass('sum').text('0')));

        $('#materialLotBody').append($tr);
        updateRowSumAndValidate($tr);
      });
  });

  // LOT 로드 끝난 뒤 전체 검증
  $.when.apply($, reqs).then(function(){ 
	  // 페이지 진입 시: 재고가 '충분한' 자재만 자동 배정
	  autoAllocateAll(false);     // true = 충분한 경우에만 꽉 채움
	  window.validateAll(); });
} // ← 여기서 renderMaterialRows 끝!

/* ---------- 전역: 행 합계/상태 ---------- */
function updateRowSumAndValidate($row){
  const req = +($row.find('.req').data('req') || 0);
  let sum = 0;
  $row.find('.lot-qty').each(function(){ sum += +(this.value || 0); });
  $row.find('.sum').text(sum);

  $row.removeClass('table-success table-warning table-danger');
  if (sum === req) $row.addClass('table-success');
  else if (sum > 0 && sum !== req) $row.addClass('table-warning');
  else $row.addClass('table-danger');
}

/* ---------- 전역: 입력 제한 + 합계/검증 ---------- */
$(document).on('wheel', '.lot-qty', function(e){ e.preventDefault(); });
$(document).on('input change blur', '.lot-qty', function(){
  const $input = $(this);
  const $row   = $input.closest('tr');

  const maxLot = Number($input.attr('max')) || 0;
  let v        = Number($input.val()) || 0;
  if (v < 0) v = 0;
  if (v > maxLot) v = maxLot;

  const req = Number($row.find('.req').data('req')) || 0;
  const others = $row.find('.lot-qty').not($input).toArray()
    .reduce((s, el) => s + (Number(el.value)||0), 0);
  const remain = Math.max(req - others, 0);
  if (v > remain) v = remain;

  $input.val(v);
  updateRowSumAndValidate($row);
  window.validateAll();
});

/* ---------- 전역: 제출시 hidden 생성 ---------- */
$(document).off('submit', '#outboundForm').on('submit', '#outboundForm', function(e){
  const $f = $(this);
  $f.find('input[name=materialIdList],input[name=reqQtyList],input[name=lotMaterialIdList],input[name=lotNoList],input[name=qtyList],input[name=lotWarehouseList]').remove();

  $('#materialLotBody tr').each(function(){
    const mid = $(this).data('material');
    const req = +($(this).find('.req').data('req') || 0);
    $f.append($('<input>', { type:'hidden', name:'materialIdList', value: mid }));
    $f.append($('<input>', { type:'hidden', name:'reqQtyList',     value: req }));
  });

  $('.lot-qty').each(function(){
    const v = +(this.value || 0);
    if (v > 0) {
      const $el = $(this);
      $f.append($('<input>', { type:'hidden', name:'lotMaterialIdList', value: $el.data('material') }));
      $f.append($('<input>', { type:'hidden', name:'lotNoList',         value: $el.data('lot') }));
      $f.append($('<input>', { type:'hidden', name:'qtyList',           value: v }));
      $f.append($('<input>', { type:'hidden', name:'lotWarehouseList',  value: ($el.data('wh') || '') }));
    }
  });

  return true;
});


//==================== 부족분 발주 초안 생성 ====================
//부족분 발주 버튼 → 초안 생성 → 모달 오픈
$('#btnCreateDraft').off('click.draft').on('click.draft', function (e) {
	  e.preventDefault();

	  const workOrderId = $('#workOrderNo').val();
	  if (!workOrderId) { alert('작업지시서를 먼저 선택하세요.'); return; }

	  const shortages = collectShortages();
	  if (!shortages.length) { alert('부족분이 없습니다.'); return; }

	  const payload = {
	    workOrderId,
	    items: shortages.map(x => ({ materialId: x.materialId, lackQty: x.lackQty }))
	  };

	  $.ajax({
	    url: ctx + '/material/order/draft',
	    method: 'POST',
	    contentType: 'application/json',
	    data: JSON.stringify(payload),
	    // beforeSend: (xhr)=>xhr.setRequestHeader('X-CSRF-TOKEN', token) // CSRF 쓰면
	  })
	  .done(res => {
	    // ➊ 서비스가 '초안 미리보기'로 주는 경우
	    if (res.suppliers) {
	      renderDraftModal(res);                        // 모달 본문 채우기
	      $('#draftModal').appendTo('body').modal('show');
	      return;
	    }
	    // ➋ 서비스가 바로 임시 주문을 만들고 orderId를 주는 경우
	    if (res.orderId) {
	      location.href = ctx + '/material/order/register?orderId=' + encodeURIComponent(res.orderId);
	      return;
	    }
	    alert('생성할 초안이 없습니다.');
	  })
	  .fail(() => alert('발주 초안 생성 실패'));
	});


//행 단위 자동 배정 (FEFO 순: 너가 이미 expiration_date로 정렬함)
function autoAllocateForRow($tr, onlyIfEnough) {
  const req = +($tr.find('.req').data('req') || 0);
  const $lots = $tr.find('.lot-qty');

  // 총 가능 수량
  const totalAvail = $lots.toArray().reduce((s, el) => {
    const m = Number($(el).attr('max')) || 0;
    return s + m;
  }, 0);

  if (onlyIfEnough && totalAvail < req) {
    // 충분하지 않으면 손대지 않음
    return false;
  }

  let sum = 0;
  $lots.each(function () {
    const $inp = $(this);
    const max = Number($inp.attr('max')) || 0;
    if (sum >= req) { $inp.val(0); return; }
    const need = req - sum;
    const v = Math.min(max, need);
    $inp.val(v);
    sum += v;
  });

  updateRowSumAndValidate($tr);
  return sum >= req;
}

// 전체 자동 배정
function autoAllocateAll(onlyIfEnough) {
  $('#materialLotBody tr').each(function () {
    autoAllocateForRow($(this), onlyIfEnough);
  });
  window.validateAll();
}
