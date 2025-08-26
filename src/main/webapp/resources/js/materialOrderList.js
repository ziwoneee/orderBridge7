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
const PACK_SIZE = {
  'RM-0001':20000, 'RM-0002':20000, 'RM-0003':1000,
  'RM-0004':2000,  'RM-0005':2000,  'RM-0006':1000,
  'RM-0007':10000, 'RM-0008':5000,  'RM-0009':4500,
  'RM-0010':15000, 'RM-0011':20000, 'RM-0012':1000,
  'RM-0013':1800,  'RM-0014':100,   'RM-0016':1000,
  'RM-0017':10,    'RM-0018':100
};

// 단가 정책(서버가 안 주면 폴백)
const PRICE_POLICY = {
  'RM-0001': { unitPrice:3300,  priceUnit:'KG' },
  'RM-0002': { unitPrice:3800,  priceUnit:'KG' },
  'RM-0003': { unitPrice:7800,  priceUnit:'KG' },
  'RM-0004': { unitPrice:4500,  priceUnit:'KG' },
  'RM-0005': { unitPrice:6500,  priceUnit:'KG' },
  'RM-0006': { unitPrice:9500,  priceUnit:'KG' },

  'RM-0007': { unitPrice:5500,  priceUnit:'KG' },
  'RM-0008': { unitPrice:6000,  priceUnit:'KG' },
  'RM-0009': { unitPrice:2000,  priceUnit:'BUNDLE', bundlesPerPack:10 }, // 10단/박스
  'RM-0010': { unitPrice:1300,  priceUnit:'KG' },

  'RM-0011': { unitPrice:1000,  priceUnit:'KG' },     // 1,000원/kg (20kg 포대)
  'RM-0012': { unitPrice:15000, priceUnit:'KG' },
  'RM-0013': { unitPrice:3000,  priceUnit:'PACK' },   // 1.8L 병
  'RM-0014': { unitPrice:10000, priceUnit:'KG' },

  'RM-0016': { unitPrice:150,   priceUnit:'BASE' },
  'RM-0017': { unitPrice:800,   priceUnit:'BASE' },
  'RM-0018': { unitPrice:1200,  priceUnit:'BASE' }
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
  return $.get(ctx + '/material/order/supplier-pack-qty', { materialId, supplierId }).then(function(r){
    const policy = PRICE_POLICY[materialId] || { unitPrice:0, priceUnit:'BASE' };
    const packQty   = n(r && (r.packQty || r.convToBase || r.conv_to_base));
    const priceUnit = (r && r.priceUnit) || policy.priceUnit;
    const unitPrice = (r && r.unitPrice!=null) ? Number(r.unitPrice) : policy.unitPrice;
    return {
      packQty: (isFinite(packQty)&&packQty>0) ? packQty : (PACK_SIZE[materialId]||1),
      priceUnit: String(priceUnit).toUpperCase(),
      unitPrice: unitPrice,
      bundlesPerPack: policy.bundlesPerPack||1
    };
  }, function(){
    const policy = PRICE_POLICY[materialId] || { unitPrice:0, priceUnit:'BASE' };
    return {
      packQty: PACK_SIZE[materialId]||1,
      priceUnit: policy.priceUnit,
      unitPrice: policy.unitPrice,
      bundlesPerPack: policy.bundlesPerPack||1
    };
  });
}



/** ===== 테이블 컬럼 인덱스 ===== */
const COL_STATUS  = 5; // 상태
const COL_DETAIL  = 6; // 상세
const COL_REQUEST = 7; // 발주요청

/** ===== 상세 모달 오픈 (FIX) ===== */
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

      // ✅ items를 돌면서 Promise 배열 생성
      const tasks = items.map(it => {
        const packs = Number(it.orderQuantity) || 0;

        return loadPriceMeta(it.materialId, supplierId).then(meta => {
          const packQty   = Number(meta.packQty || 1);
          const priceUnit = String(meta.priceUnit || 'BASE').toUpperCase();
          const unitPrice = Number(meta.unitPrice || 0);

          let billedQty = 0;
          let billedUnit = priceUnit;

          if (priceUnit === 'KG') {
            billedQty = packs * (packQty / 1000);              // 박스→kg
          } else if (priceUnit === 'PACK') {
            billedQty = packs;                                  // 박스로 과금
          } else if (priceUnit === 'BUNDLE') {
            const bpp = Number(meta.bundlesPerPack || 1);
            billedQty = packs * bpp;
          } else {
            billedQty  = packs * packQty;                       // BASE(g/ml/ea)
            billedUnit = (baseUnitOf(it.materialId) || 'EA').toUpperCase();
          }

          const amount   = Math.round(billedQty * unitPrice);
          const baseQty  = packs * packQty;                     // g/ml/ea 총량
          const baseText = fmtBase(baseQty, it.materialId);     // "40kg" 등

          $tbody.append(
            `<tr>
               <td>${it.materialId}</td>
               <td>${it.materialName || ''}</td>
               <td class="text-right">
                 ${packs.toLocaleString()} PACK
                 <br><small class="text-muted">≈ ${baseText}</small>
               </td>
               <td class="text-right">${unitPrice.toLocaleString()} / ${billedUnit}</td>
               <td class="text-right">${amount.toLocaleString()}</td>
               <td>${it.warehouseCode || '-'}</td>
             </tr>`
          );
        });
      });

      // ✅ 모든 행 렌더 후 모달 오픈
      Promise.all(tasks).then(() => {
        $('#orderDetailModal').modal('show');
      });
    })
    .fail(xhr => {
      alert('상세 조회 실패: ' + ((xhr.responseJSON && xhr.responseJSON.message) || xhr.statusText));
    });
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
