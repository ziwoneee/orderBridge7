// 공통 유틸
window.formatYMD = d => !d ? '-' : new Date(d).toISOString().slice(0,10);
window.calcRowTotal = (row) => {
  const qty = +((row.querySelector("input[name$='.orderQuantity']")||{}).value||0);
  const unit= +((row.querySelector("input[name$='.unitPrice']")||{}).value||0);
  return Math.round(qty*unit);
};


const COL_STATUS = 5;     // 상태
const COL_DETAIL = 6;     // 상세
const COL_REQUEST = 7;    // 발주요청

// 상세 모달 오픈
$(document).on('click', '.btnOrderDetail', function(){
  const id = $(this).data('id');
  $.get(ctx + '/material/order/detail', { orderId: id })
    .done(res => {
      const h = res.header || {};
      const fmt = d => !d ? '-' : new Date(d).toISOString().slice(0,10);
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
    .fail(xhr => alert('상세 조회 실패: ' + ((xhr.responseJSON && xhr.responseJSON.message) || xhr.statusText)));
});

// 발주요청
$(document).on('click', '.btnSubmitOrder', function(){
  const $btn=$(this), orderId=$btn.data('id');
  if (!confirm('이 발주 초안을 "요청" 상태로 전환할까요?')) return;

  $.post(ctx + '/material/order/submit', { orderId })
    .done(res => {
      alert(res.message || '발주요청 완료');
      const $tr = $btn.closest('tr');
      $tr.find('td').eq(COL_STATUS).html('<span class="badge badge-warning">요청</span>');
      $tr.find('td').eq(COL_REQUEST).html('-'); // 요청 버튼 칸 비우기
    })
    .fail(xhr => alert('발주요청 실패: ' + ((xhr.responseJSON && xhr.responseJSON.message) || xhr.statusText)));
});
