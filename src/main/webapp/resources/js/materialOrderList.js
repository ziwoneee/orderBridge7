/************************************
 * materialOrderList.js
 * - 발주 리스트 / 상세 모달 / 발주요청
 * - 상세 계산은 DB 메타(/supplierItem/list) 기반
 ************************************/

// 상단(파일 어딘가 공용 위치)에 하나만 선언
function coalesce() {
  for (var i = 0; i < arguments.length; i++) {
    var v = arguments[i];
    if (v !== undefined && v !== null) return v;
  }
  return undefined;
}

/** ========= 공통 유틸 ========= */
const pad = n => String(n).padStart(2, '0');

window.formatYMD = (d) => {
  if (!d) return '-';
  if (typeof d === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(d)) return d;
  const dt = new Date(d);
  return `${dt.getFullYear()}-${pad(dt.getMonth()+1)}-${pad(dt.getDate())}`;
};

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

const STATUS_BADGE = {
  '초안': 'secondary',
  '요청': 'warning',
  '승인': 'primary',
  '입고완료': 'success',
  '반려': 'danger',
  '취소': 'dark'
};
function statusBadge(status){
  const s = status || '-';
  const cls = STATUS_BADGE[s] || 'secondary';
  return `<span class="badge badge-${cls}">${s}</span>`;
}

/** ========= 승인요청 엔드포인트 ========= */
const APPROVAL_ENDPOINT = ctx + '/material/order/request-approval'; // 필요시 변경

/** ========= CSRF(선택) ========= */
var token  = $("meta[name='_csrf']").attr("content");
var header = $("meta[name='_csrf_header']").attr("content");
if (token && header) {
  $(document).ajaxSend(function(e, xhr){
    xhr.setRequestHeader(header, token);
  });
}

/** ========= 리스트 테이블 컬럼 인덱스 ========= */
const COL_STATUS  = 5; // 상태
const COL_DETAIL  = 6; // 상세
const COL_REQUEST = 7; // 발주요청

/** ========= 상세 계산: 서버 메타 사용 ========= */
/** 캐시: 같은 공급사면 1회만 로드 */
let __supplierMetaCache = null;

/**
 * /supplierItem/list?supplierId=... 호출
 * 응답 배열을 materialId -> meta 맵으로 가공
 *  - unitPrice      : 숫자
 *  - priceUnit      : 'KG' | 'L' | 'BUNDLE' | 'EA' | 'PACK' ...
 *  - packQty        : 1 PACK -> priceUnit 수량 (conv_to_base)
 *  - bundlesPerPack : 번들일 때 1 PACK의 단 수 (conv_to_base)
 *  - convToStock    : 1 PACK -> 표시/재고단위 수량 (conv_to_stock)
 *  - stockUnit      : 표시/재고 단위 (stock_unit)
 */
function fetchSupplierMetaMap(supplierId){
  if (__supplierMetaCache && __supplierMetaCache.id === supplierId) {
    return Promise.resolve(__supplierMetaCache.map);
  }
  var url = ctx + '/supplierItem/list?supplierId=' + encodeURIComponent(supplierId);
  return fetch(url)
    .then(function(res){
      if (!res.ok) throw new Error('Failed to load supplier items');
      return res.json();
    })
    .then(function(arr){
      var map = {};
      for (var i = 0; i < arr.length; i++) {
        var it = arr[i];

        var unitPrice = Number(coalesce(it.unitPrice,   it.unit_price,   0));
        var priceUnit = String(coalesce(it.priceUnit,   it.price_unit,   'EA')).toUpperCase();
        
        // 1 PACK → 과금단위 수량(convBase): conv_to_base 없으면 conv_to_stock으로 폴백
        var convBase  = Number(coalesce(it.convToBase,  it.conv_to_base,  it.convToStock, it.conv_to_stock, 1));
        // 1 PACK → 표시/재고단위 수량(convStock): 기본은 conv_to_stock, 없으면 convBase
        var convStock = Number(coalesce(it.convToStock, it.conv_to_stock, convBase));

        
        var stockUnit = String(coalesce(it.stockUnit,   it.stock_unit,   priceUnit)).toUpperCase();

        // 번들일 때만 팩=번들수량으로 사용 (삼항 금지 버전)
        var bundlesPerPack = 1;
        if (priceUnit === 'BUNDLE') {
          bundlesPerPack = convBase;
        }

        map[it.materialId] = {
          unitPrice:     unitPrice,
          priceUnit:     priceUnit,
          packQty:       convBase,
          bundlesPerPack: bundlesPerPack,
          convToStock:   convStock,
          stockUnit:     stockUnit
        };
      }

      __supplierMetaCache = { id: supplierId, map: map };
      return map;
    });
}

/** materialId에 해당하는 메타 한 건 반환 */
function loadPriceMeta(materialId, supplierId){
  return fetchSupplierMetaMap(supplierId).then(map => {
    return map[materialId] || {
      unitPrice: 0, priceUnit: 'EA', packQty: 1, bundlesPerPack: 1, convToStock: 1, stockUnit: 'EA'
    };
  });
}

/** 표시용 수량 포맷 */
function fmtQtyWithUnit(qty, unit){
  unit = String(unit||'').toUpperCase();
  const v = Number(qty)||0;

  if (unit === 'G') {
    return v >= 1000 ? (v/1000).toLocaleString() + 'kg' : v.toLocaleString() + 'g';
  }
  if (unit === 'ML') {
    return v >= 1000 ? (v/1000).toLocaleString() + 'L' : v.toLocaleString() + 'ml';
  }
  if (unit === 'KG') return v.toLocaleString() + 'kg';
  if (unit === 'L')  return v.toLocaleString() + 'L';
  if (unit === 'BUNDLE') return v.toLocaleString() + '단';
  if (unit === 'EA') return v.toLocaleString() + '개';
  if (unit === 'PACK') return v.toLocaleString() + 'PACK';
  return unit ? (v.toLocaleString() + ' ' + unit) : v.toLocaleString();
}

/** ========= 상세 모달 ========= */
$(document).on('click', '.btnOrderDetail', function () {
  const id = $(this).data('id') || $(this).attr('data-order-id');
  if (!id) { alert('orderId 없음'); return; }

  $.get(ctx + '/material/order/detail', { orderId: id })
    .done(res => {
      const h = res.header || {};
      const supplierId = h.supplierId;

      // 헤더
      $('#modalOrderId').text(h.orderId || '-');
      $('#modalSupplierId').text(h.supplierName || h.supplierId || '-');
      $('#modalOrderDate').text(window.formatYMD(h.orderDate));
      $('#modalExpectedDate').text(window.formatYMD(h.expectedArrivedDate));
      $('#modalOrderStatus').html(statusBadge(h.orderStatus));
      $('#modalHandler').text(h.handlerName || h.handledBy || '-');
      $('#modalNote').text(h.note || '');

      const $tbody = $('#orderItemsInfo').empty();
      const items = res.items || [];

      // 각 라인 계산
      const tasks = items.map(it => {
        const packs = Number(it.orderQuantity) || 0;

        return loadPriceMeta(it.materialId, supplierId).then(meta => {
          const unitPrice = Number(meta.unitPrice || 0);
          const priceUnit = String(meta.priceUnit || 'EA').toUpperCase();

          // 표시용: 1 PACK -> stock 단위 수량(convToStock)
          const dispQty  = packs * Number(meta.convToStock || meta.packQty || 1);
          const dispText = fmtQtyWithUnit(dispQty, meta.stockUnit || priceUnit);

          // 과금 수량 - 등록 화면과 동일한 로직 적용
          const convBase = Number(meta.packQty || 1);
          const billedQty = packs * convBase;  // 모든 단위에 대해 동일한 계산
          const amount = Math.round(billedQty * unitPrice);

          // 디버깅 로그도 수정
          console.log(`${it.materialId} 계산:`, {
            packs,
            priceUnit,
            unitPrice,
            convBase,  // packQty 대신 convBase 사용
            convToStock: meta.convToStock,
            stockUnit: meta.stockUnit,
            billedQty,
            amount,
            calculation: `${packs} PACK × ${convBase} ${priceUnit} × ${unitPrice}원 = ${amount}원`
          });
          
          $tbody.append(
            `<tr>
               <td>${it.materialId}</td>
               <td>${it.materialName || ''}</td>
               <td class="text-right">
                 ${packs.toLocaleString()} PACK
                 <br><small class="text-muted">≈ ${dispText}</small>
               </td>
               <td class="text-right">${unitPrice.toLocaleString()} / ${priceUnit}</td>
               <td class="text-right">${amount.toLocaleString()}</td>
               <td>${it.warehouseCode || '-'}</td>
             </tr>`
          );
        });
      });

      Promise.all(tasks).then(() => {
        $('#orderDetailModal').modal('show');
      });
    })
    .fail(xhr => {
      alert('상세 조회 실패: ' + ((xhr.responseJSON && xhr.responseJSON.message) || xhr.statusText));
    });
});

/** ========= 발주요청 (초안 → 요청) + 메일 전송 ========= */
$(document).on('click', '.btnSubmitOrder', function(){
  const $btn = $(this);
  const orderId = $btn.data('id');

  if (!confirm('이 발주 초안을 "요청" 상태로 전환하고, 협력사에 승인요청 메일을 전송할까요?')) return;

  // 1) 상태 전환: 초안 → 요청
  $.post(ctx + '/material/order/submit', { orderId })
    .done(res => {
      if (res && res.success) {
        // 2) 승인요청 메일 전송
        $.post(APPROVAL_ENDPOINT, { orderId })
          .done(mailRes => {
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
        $tr.find('td').eq(COL_STATUS).html(statusBadge('요청'));
        $tr.find('td').eq(COL_REQUEST).html(''); // 버튼 제거
      } else {
        alert('발주요청 실패: ' + (res && res.message ? res.message : '알 수 없는 오류'));
      }
    })
    .fail(xhr => {
      alert('발주요청 실패: ' + ((xhr.responseJSON && xhr.responseJSON.message) || xhr.statusText));
    });
});

/** ========= 디버깅 헬퍼(원하면 호출) ========= */
function debugCalculation(materialId, quantity, unitPrice, convToStock) {
  console.log('=== 계산 디버깅 ===');
  console.log('자재ID:', materialId);
  console.log('주문수량(PACK):', quantity);
  console.log('단가:', unitPrice);
  console.log('환산비율(표시):', convToStock);
  console.log('계산식(표시):', quantity + ' × ' + convToStock + ' = ' + (quantity * convToStock));
  console.log('================');
}
