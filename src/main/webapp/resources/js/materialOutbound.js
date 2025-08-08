/* materialOutbound.js (list.jsp & register.jsp 공용) */
/* 전제: 각 JSP 상단에
<script>var ctx='${pageContext.request.contextPath}';</script>
*/

// =========================================================
// 공통 유틸
// =========================================================
function toYmd(d) {
  if (!d) return '';
  if (typeof d === 'string') return d.slice(0, 10);
  if (typeof d === 'number') return new Date(d).toISOString().slice(0, 10);
  if (typeof d === 'object' && d.time) return new Date(d.time).toISOString().slice(0, 10);
  try { return new Date(d).toISOString().slice(0, 10); } catch(e){ return ''; }
}

function fmtDate(val) { return toYmd(val); }

// =========================================================
// list.jsp 영역
// =========================================================

// 대기 작업지시 모달 열릴 때 목록 로드 (한 번만 바인딩)
$(document).off('shown.bs.modal', '#orderModal').on('shown.bs.modal', '#orderModal', function () {
  loadWaitingOrders();
});

// 작업지시 불러오기 버튼
$(document).off('click', '#btnLoadOrder').on('click', '#btnLoadOrder', function(){
  $('#orderModal').modal('show');
});

// [AJAX] WAITING 지시서 목록 로드
function loadWaitingOrders() {
  $.getJSON(ctx + '/material/outbound/order-list', function(list) {
    var $tbody = $('#orderBody').empty();
    if (!list || list.length === 0) {
      $tbody.append('<tr><td colspan="6" class="text-center text-muted">대기 작업지시가 없습니다.</td></tr>');
      return;
    }

    list.forEach(function(wo){
      var orderId = wo.orderId || '';
      var product = (wo.productName && wo.productName.length) ? wo.productName : (wo.productId || '');
      var lineId  = wo.lineId || '';
      var qty     = (wo.orderQty != null ? wo.orderQty : '');
      var ymd     = toYmd(wo.dueDate);

      var $tr = $('<tr>').attr('data-order-no', orderId)
        .append($('<td>').text(orderId))
        .append($('<td>').text(product))
        .append($('<td>').text(lineId))
        .append($('<td>').addClass('text-right').text(qty))
        .append($('<td>').text(ymd))
        .append(
          $('<td>').addClass('text-center').css('width','90px')
            .append(
              $('<button>', {
                type: 'button',
                'data-order-no': orderId
              }).addClass('btn btn-primary btn-xs btn-select-wo').text('선택')
            )
        );
      $tbody.append($tr);
    });
  });
}

// 선택 → 출고 등록 화면 이동
$(document).off('click', '.btn-select-wo').on('click', '.btn-select-wo', function(){
  var orderNo = $(this).data('order-no');
  location.href = ctx + '/material/outbound/register?workOrderId=' + encodeURIComponent(orderNo);
});

// 출고 처리(목록 화면용)
function processOutbound(outboundId) {
  if(!confirm('출고처리 하시겠습니까?')) return;
  $.post(ctx + '/material/outbound/process', { outboundId: outboundId })
    .done(function(){ alert('출고처리 완료'); location.reload(); })
    .fail(function(){ alert('출고처리 중 오류'); });
}

// =========================================================
/* register.jsp 영역 */
// =========================================================

// 쿼리에서 workOrderId 읽고 상세 호출
$(function(){
  if (!$('#outboundForm').length) return; // register.jsp가 아닐 때는 패스

  var params = new URLSearchParams(location.search);
  var workOrderId = params.get('workOrderId');
  if (!workOrderId) return;

  // [1] 작업지시 헤더 + 자재 목록 로드
  $.get(ctx + '/material/outbound/work-order', { workOrderId: workOrderId }, function(dto){
    $('#workOrderNo').val(dto.workOrderNo || '');
    $('#productId').val(dto.productId || '');
    $('#lineId').val(dto.lineId || '');
    $('#dueDate').val(toYmd(dto.dueDate));
    renderMaterialRows(dto.materialList || []);
  });
});

// 자재 행 렌더 + LOT(FEFO) 로드
function renderMaterialRows(items){
  var $body = $('#materialLotBody').empty();

  var reqs = (items || []).map(function(it){
    return $.get(ctx + '/material/inventory/lots', { materialId: it.materialId })
      .then(function(lots){
        lots = lots || [];
        // FEFO: 유통기한 오름차순
        lots.sort(function(a,b){ return new Date(a.expiration_date) - new Date(b.expiration_date); });

        // tr 생성
        var $tr = $('<tr>').attr('data-material', it.materialId);

        // 1) 자재 표시 셀
        $tr.append(
          $('<td>').append(
            $('<div>').addClass('font-weight-bold').text(it.materialName || it.materialId),
            $('<div>').addClass('text-muted').append($('<small>').text(it.materialId))
          )
        );

        // 2) 필요수량 셀
        $tr.append(
          $('<td>').addClass('text-right align-middle')
            .append($('<span>').addClass('req').attr('data-req', it.requiredQty).text(it.requiredQty))
        );

     // 3) LOT 입력 셀
        var $lotCell = $('<td>');
        if (!lots || lots.length === 0) {
          $lotCell.append($('<span>').addClass('text-danger').text('사용 가능한 LOT 없음'));
        } else {
          lots.forEach(function(l){
            var wh   = l.warehouse_code || it.defaultWarehouseCode || '';
            var qty0 = (l.quantity != null ? l.quantity : 0);

            var $row = $('<div>')
              .addClass('form-row align-items-center lot-row mb-1')
              .attr('data-lot', l.lot_no);

            // LOT 정보
            var $info = $('<div>').addClass('col-md-5');
            $info.append($('<small>').addClass('text-muted').text('LOT: ' + l.lot_no));
            $info.append('<br>');
            $info.append($('<small>').text('유통기한: ' + fmtDate(l.expiration_date) + ' / 재고: ' + qty0));
            $row.append($info);

            // 수량 입력
            var $qtyCol = $('<div>').addClass('col-md-4');
            $qtyCol.append(
              $('<input>')
                .attr({ type:'number', min:0, max: qty0, value:0 })
                .addClass('form-control form-control-sm lot-qty')
                .data({ material: it.materialId, lot: l.lot_no, wh: wh })
            );
            $row.append($qtyCol);

            // 창고 표기
            var $whCol = $('<div>').addClass('col-md-3');
            $whCol.append($('<span>').addClass('badge badge-secondary').text(wh));
            $row.append($whCol);

            $lotCell.append($row);
          });
        }
        $tr.append($lotCell);

        // 4) 합계 셀
        $tr.append(
          $('<td>').addClass('text-right align-middle').append($('<span>').addClass('sum').text('0'))
        );

        // 입력 변화시 합계/검증 갱신
        $tr.on('input', '.lot-qty', function(){
          updateRowSumAndValidate($tr);
          validateAll();
        });

        $body.append($tr);
        updateRowSumAndValidate($tr);


// 행 합계/상태 표시
function updateRowSumAndValidate($row){
  var req = parseInt($row.find('.req').data('req'), 10) || 0;
  var sum = 0;
  $row.find('.lot-qty').each(function(){
    var v = parseInt(this.value, 10) || 0;
    sum += v;
  });

  $row.find('.sum').text(sum);

  $row.removeClass('table-success table-warning table-danger');
  if (sum === req) $row.addClass('table-success');
  else if (sum > 0 && sum !== req) $row.addClass('table-warning');
  else if (sum === 0) $row.addClass('table-danger');
}

// 전체 검증: 모든 행 req==sum 일 때만 제출 가능
function validateAll(){
  var ok = true;
  $('#materialLotBody tr').each(function(){
    var req = parseInt($(this).find('.req').data('req'), 10) || 0;
    var sum = parseInt($(this).find('.sum').text(), 10) || 0;
    if (req !== sum) { ok = false; return false; }
  });
  $('#btnSubmit').prop('disabled', !ok);
}

// 제출 시 서버 VO(List)로 hidden 생성 (+ 창고 포함)
$(document).off('submit', '#outboundForm').on('submit', '#outboundForm', function(){
  var $f = $(this);
  // 기존 hidden 제거
  $f.find('input[name=materialIdList],input[name=reqQtyList],input[name=lotMaterialIdList],input[name=lotNoList],input[name=qtyList],input[name=lotWarehouseList]').remove();

  // 자재 행 -> materialIdList, reqQtyList
  $('#materialLotBody tr').each(function(){
    var mid = $(this).data('material');
    var req = parseInt($(this).find('.req').data('req'), 10) || 0;
    $f.append($('<input>', {type:'hidden', name:'materialIdList', value: mid}));
    $f.append($('<input>', {type:'hidden', name:'reqQtyList',     value: req}));
  });

  // LOT 입력 -> lotMaterialIdList, lotNoList, qtyList, lotWarehouseList
  $('.lot-qty').each(function(){
    var v = parseInt(this.value, 10) || 0;
    if (v > 0) {
      var $el = $(this);
      $f.append($('<input>', {type:'hidden', name:'lotMaterialIdList', value: $el.data('material')}));
      $f.append($('<input>', {type:'hidden', name:'lotNoList',         value: $el.data('lot')}));
      $f.append($('<input>', {type:'hidden', name:'qtyList',           value: v}));
      $f.append($('<input>', {type:'hidden', name:'lotWarehouseList',  value: ($el.data('wh') || '')}));
    }
  });
});
