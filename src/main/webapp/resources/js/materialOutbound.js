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

/* ---------- 공통 유틸 ---------- */
function toYmd(d){ /* 그대로 */ }
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
        lots.sort((a,b) => new Date(a.expiration_date) - new Date(b.expiration_date));

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
            const wh   = l.warehouse_code || it.defaultWarehouseCode || '';
            const qty0 = (l.quantity != null ? l.quantity : 0);

            const $row = $('<div>').addClass('form-row align-items-center lot-row mb-1')
                                   .attr('data-lot', l.lot_no);

            const $info = $('<div>').addClass('col-md-5')
              .append($('<small>').addClass('text-muted').text('LOT: ' + l.lot_no))
              .append('<br>')
              .append($('<small>').text('유통기한: ' + fmtDate(l.expiration_date) + ' / 재고: ' + qty0));

            const $qtyCol = $('<div>').addClass('col-md-4')
              .append($('<input>', {
                type:'number', min:0, max: qty0, step:1, value:0, 'aria-label':'수량 입력'
              }).addClass('form-control form-control-sm lot-qty')
                .data({ material: it.materialId, lot: l.lot_no, wh: wh }));

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
  $.when.apply($, reqs).then(function(){ window.validateAll(); });
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
