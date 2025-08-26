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

/* ==== 상태 뱃지 유틸 ==== */
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

//=== [단가 정책/팩사이즈 폴백] ===
function n(v){ return (v===''||v==null) ? NaN : Number(v); }

// 1팩을 기본단위(g/ml/ea)로 환산
const MATERIAL_META = {
  'RM-0001': { packQty: 20, packUnit: 'KG', priceUnit: 'KG', unitPrice: 3300 },
  'RM-0002': { packQty: 20, packUnit: 'KG', priceUnit: 'KG', unitPrice: 3800 },
  'RM-0003': { packQty: 1, packUnit: 'KG', priceUnit: 'KG', unitPrice: 7800 },
  'RM-0004': { packQty: 2, packUnit: 'KG', priceUnit: 'KG', unitPrice: 4500 },
  'RM-0005': { packQty: 2, packUnit: 'KG', priceUnit: 'KG', unitPrice: 6500 },
  'RM-0006': { packQty: 1, packUnit: 'KG', priceUnit: 'KG', unitPrice: 9500 },
  'RM-0007': { packQty: 10, packUnit: 'KG', priceUnit: 'KG', unitPrice: 5500 },
  'RM-0008': { packQty: 5, packUnit: 'KG', priceUnit: 'KG', unitPrice: 6000 },
  'RM-0009': { packQty: 10, packUnit: 'BUNDLE', priceUnit: 'BUNDLE', unitPrice: 2000, bundlesPerPack: 10 },
  'RM-0010': { packQty: 15, packUnit: 'KG', priceUnit: 'KG', unitPrice: 1300 },
  'RM-0011': { packQty: 20, packUnit: 'KG', priceUnit: 'KG', unitPrice: 1000 },
  'RM-0012': { packQty: 1, packUnit: 'KG', priceUnit: 'KG', unitPrice: 15000 },
  'RM-0013': { packQty: 1.8, packUnit: 'L', priceUnit: 'L', unitPrice: 3000 },
  'RM-0014': { packQty: 0.1, packUnit: 'KG', priceUnit: 'KG', unitPrice: 10000 }
};

// 기본단위 → 과금단위 수량 환산
function computeBillingQty(qtyBase, packQty, priceUnit, bundlesPerPack){
  qtyBase = Number(qtyBase)||0;
  packQty = Number(packQty)||1;
  const pu = String(priceUnit||'BASE').toUpperCase();
  if (pu === 'KG')      return { qty: qtyBase/1000, unit:'KG' };
  if (pu === 'PACK')    return { qty: Math.ceil(qtyBase/packQty), unit:'PACK' };
  if (pu === 'BUNDLE')  return { qty: Math.ceil(qtyBase/packQty) * (bundlesPerPack||1), unit:'BUNDLE' };
  return { qty: qtyBase, unit:'BASE' };
}

function fmtMoney(v){ return (Number(v)||0).toLocaleString(); }
function baseUnitOf(mid){ return (mid==='RM-0013')?'ml' : (['RM-0016','RM-0017','RM-0018'].includes(mid)?'ea':'g'); }
function fmtBase(qty, mid){
  const u = baseUnitOf(mid);
  if (u==='g')  return qty>=1000 ? (qty/1000).toLocaleString()+'kg' : qty.toLocaleString()+'g';
  if (u==='ml') return qty>=1000 ? (qty/1000).toLocaleString()+'L'  : qty.toLocaleString()+'ml';
  return qty.toLocaleString()+'개';
}

// 공급사 메타 조회(팩사이즈/가격단위/단가) — 서버 응답 없으면 폴백
function loadPriceMeta(materialId, supplierId){
	
	const info = MATERIAL_META[materialId] || { packQty: 1, priceUnit: 'KG', unitPrice: 0 };
    
	return Promise.resolve({
        packQty: info.packQty,
        priceUnit: info.priceUnit,
        unitPrice: info.unitPrice,
        bundlesPerPack: info.bundlesPerPack || 1
    });
}



/** ===== 테이블 컬럼 인덱스 ===== */
const COL_STATUS  = 5; // 상태
const COL_DETAIL  = 6; // 상세
const COL_REQUEST = 7; // 발주요청

//발주 상세 모달의 items.map 부분 수정
$(document).on('click', '.btnOrderDetail', function () {
    const id = $(this).data('id') || $(this).attr('data-order-id');
    if (!id) { alert('orderId 없음'); return; }

    $.get(ctx + '/material/order/detail', { orderId: id })
        .done(res => {
            const h = res.header || {};
            const supplierId = h.supplierId;

            $('#modalOrderId').text(h.orderId || '-');
            $('#modalSupplierId').text(h.supplierName || h.supplierId || '-');
            $('#modalOrderDate').text(window.formatYMD(h.orderDate));
            $('#modalExpectedDate').text(window.formatYMD(h.expectedArrivedDate));
            $('#modalOrderStatus').html(statusBadge(h.orderStatus));
            $('#modalHandler').text(h.handlerName || h.handledBy || '-');
            $('#modalNote').text(h.note || '');

            const $tbody = $('#orderItemsInfo').empty();
            const items = res.items || [];

            const tasks = items.map(it => {
                const packs = Number(it.orderQuantity) || 0;

                return loadPriceMeta(it.materialId, supplierId).then(meta => {
                    const packQty = Number(meta.packQty || 1);
                    const priceUnit = String(meta.priceUnit || 'KG').toUpperCase();
                    const unitPrice = Number(meta.unitPrice || 0);

                    let billedQty = 0;
                    let billedUnit = priceUnit;
                    let displayWeight = '';

                    if (priceUnit === 'KG') {
                        // KG으로 과금: PACK수 × kg/PACK
                        billedQty = packs * packQty;
                        billedUnit = 'KG';
                        displayWeight = billedQty.toLocaleString() + 'kg';
                    } else if (priceUnit === 'L') {
                        // L로 과금: PACK수 × L/PACK
                        billedQty = packs * packQty;
                        billedUnit = 'L';
                        displayWeight = billedQty.toLocaleString() + 'L';
                    } else if (priceUnit === 'BUNDLE') {
                        // 번들로 과금 (대파 등)
                        billedQty = packs * (meta.bundlesPerPack || 1);
                        billedUnit = 'BUNDLE';
                        displayWeight = billedQty.toLocaleString() + '단';
                    } else {
                        // 기타
                        billedQty = packs;
                        billedUnit = 'PACK';
                        displayWeight = (packs * packQty).toLocaleString() + getDisplayUnit(it.materialId);
                    }

                    const amount = Math.round(billedQty * unitPrice);

                    console.log(`${it.materialId} 계산:`, {
                        packs: packs,
                        packQty: packQty,
                        priceUnit: priceUnit,
                        unitPrice: unitPrice,
                        billedQty: billedQty,
                        amount: amount,
                        calculation: `${packs} PACK × ${packQty} ${priceUnit} × ${unitPrice}원 = ${amount}원`
                    });

                    $tbody.append(
                        `<tr>
                           <td>${it.materialId}</td>
                           <td>${it.materialName || ''}</td>
                           <td class="text-right">
                             ${packs.toLocaleString()} PACK
                             <br><small class="text-muted">≈ ${displayWeight}</small>
                           </td>
                           <td class="text-right">${unitPrice.toLocaleString()} / ${billedUnit}</td>
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

function getDisplayUnit(materialId) {
    if (materialId === 'RM-0013') return 'L';
    if (['RM-0016', 'RM-0017', 'RM-0018'].includes(materialId)) return '개';
    return 'kg';
}

//4. 디버깅용 함수 추가
function debugCalculation(materialId, quantity, unitPrice, convToStock) {
    console.log('=== 계산 디버깅 ===');
    console.log('자재ID:', materialId);
    console.log('주문수량(PACK):', quantity);
    console.log('단가:', unitPrice);
    console.log('환산비율:', convToStock);
    console.log('계산식:', quantity + ' × ' + convToStock + ' × ' + unitPrice);
    console.log('총액:', quantity * convToStock * unitPrice);
    console.log('================');
}


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
