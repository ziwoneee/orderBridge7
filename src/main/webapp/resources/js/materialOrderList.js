/************************************
 * materialOrderList.js (완성본)
 * - 상세 모달
 * - 발주요청(초안→요청)
 * - 요청 성공 후 협력사 승인요청 메일 전송
 ************************************/

/** ===== 전역 유틸 ===== */
const pad = n => String(n).padStart(2,'0');

 window.formatYMD = (d) => {
   if (!d) return '-';
   // 백엔드가 'YYYY-MM-DD' 문자열을 주면 그대로 사용
   if (typeof d === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(d)) return d;
   const dt = new Date(d); // 로컬 타임존으로 생성
   return `${dt.getFullYear()}-${pad(dt.getMonth()+1)}-${pad(dt.getDate())}`;
 };

 // 상세에서 "날짜+시간"이 필요하면 같이 추가(선택)
 window.formatYMDHM = (d) => {
   if (!d) return '-';
   const dt = new Date(d);
   return `${dt.getFullYear()}-${pad(dt.getMonth()+1)}-${pad(dt.getDate())} ${pad(dt.getHours())}:${pad(dt.getMinutes())}`;
 };

window.calcRowTotal = (row) => {
  const qty = +((row.querySelector("input[name$='.orderQuantity']")||{}).value||0);
  const unit= +((row.querySelector("input[name$='.unitPrice']")||{}).value||0);
  return Math.round(qty*unit);
};

/** ===== 설정(필요시 변경) ===== */
// 컨트롤러 엔드포인트: 기본 권장 경로
const APPROVAL_ENDPOINT = ctx + '/material/order/request-approval';
// 만약 컨트롤러가 "/material/order/request"를 사용한다면 ↓ 이 줄로 바꾸세요
// const APPROVAL_ENDPOINT = ctx + '/material/order/request';

/** ===== (선택) CSRF 사용 시 활성화 ===== */
var token = $("meta[name='_csrf']").attr("content");
var header = $("meta[name='_csrf_header']").attr("content");

if (token && header) {
  $(document).ajaxSend(function(e, xhr){
    xhr.setRequestHeader(header, token);
  });
}


/** ===== 테이블 컬럼 인덱스 ===== */
const COL_STATUS  = 5; // 상태
const COL_DETAIL  = 6; // 상세
const COL_REQUEST = 7; // 발주요청

/** ===== 상세 모달 오픈 ===== */
$(document).on('click', '.btnOrderDetail', function(){
  const id = $(this).data('id');
  $.get(ctx + '/material/order/detail', { orderId: id })
    .done(res => {
      const h = res.header || {};
      $('#modalOrderId').text(h.orderId || '-');
      $('#modalSupplierId').text(h.supplierName || h.supplierId || '-');
      $('#modalOrderDate').text(window.formatYMD(h.orderDate));
      $('#modalExpectedDate').text(window.formatYMD(h.expectedArrivedDate));
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

/** ===== 발주요청 (초안 → 요청) + 메일 전송 ===== */
$(document).on('click', '.btnSubmitOrder', function(){
  const $btn = $(this);
  const orderId = $btn.data('id');

  if (!confirm('이 발주 초안을 "요청" 상태로 전환하고, 협력사에 승인요청 메일을 전송할까요?')) return;

  // 1) 상태 전환: 초안 → 요청
  $.post(ctx + '/material/order/submit', { orderId })
    .done(res => {
      // 서버가 {success, message, ...} 형태로 응답한다고 가정
      if (res && res.success) {
        // 2) 승인요청 메일 전송
        $.post(APPROVAL_ENDPOINT, { orderId })
          .done(mailRes => {
            // 문자열("success"/"fail") 또는 {success:true} 모두 커버
            const ok = (mailRes === 'success') || (mailRes && mailRes.success === true);
            if (ok) {
              alert((res.message || '발주요청 완료') + '\n협력사에 승인요청 메일을 전송했습니다.');
            } else {
              alert((res.message || '발주요청 완료') + '\n메일 전송은 실패했습니다. 관리자에게 문의하세요.');
            }
          })
          .fail(() => {
            alert((res.message || '발주요청 완료') + '\n메일 전송 중 오류가 발생했습니다.');
          });

        // UI 갱신
        const $tr = $btn.closest('tr');
        $tr.find('td').eq(COL_STATUS).html('<span class="badge badge-warning">요청</span>');
        $tr.find('td').eq(COL_REQUEST).html(''); // 버튼 제거
      } else {
        alert('발주요청 실패: ' + (res && res.message ? res.message : '알 수 없는 오류'));
      }
    })
    .fail(xhr => {
      alert('발주요청 실패: ' + ((xhr.responseJSON && xhr.responseJSON.message) || xhr.statusText));
    });
});
