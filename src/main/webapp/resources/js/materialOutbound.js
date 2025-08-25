/**
 * tbody 내부에서 같은 텍스트가 연속되는 셀을 rowspan으로 병합
 * @param {string} tbodySelector - 예: '#outboundItemsInfo'
 * @param {number[]} colIdxs - 병합할 열 인덱스 배열 (오른쪽→왼쪽 순으로 병합)
 */
function mergeRowspan(tbodySelector, colIdxs){
  var $rows = $(tbodySelector).children('tr');
  if ($rows.length === 0) return;

  // 인덱스가 바뀌지 않도록 오른쪽→왼쪽으로
  var cols = (colIdxs || []).slice().sort(function(a,b){ return b - a; });

  cols.forEach(function(colIndex){
    var prevText = null, $prevCell = null, span = 1;

    $rows.each(function(){
      var $cells = $(this).children('td');
      var $cell  = $cells.eq(colIndex);
      var text   = $.trim($cell.text());

      if ($prevCell && text === prevText){
        span += 1;
        $cell.remove();
        $prevCell.attr('rowspan', span).addClass('align-middle');
      } else {
        prevText = text;
        $prevCell = $cell;
        span = 1;
      }
    });
  });
}


/* ---------- 전역: 전체 검증 ---------- */
window.validateAll = function () {
  let ok = true;            // sum === target 모두 충족?
  let hasShortage = false;  // cap < req 인 행 존재? (물 제외)

  $('#materialLotBody tr').each(function () {
    const req = Number($(this).find('.req').data('req')) || 0;

    // 0 허용하며 null/undefined만 대체
    const capData = $(this).data('cap');
    const cap = (capData == null) ? req : Number(capData);

    const mid = String($(this).data('material') || '');
    const target = Math.min(req, cap);

    // 물 특례: target 고정
    if (mid === 'RM-0015') {
      $(this).find('.lot-qty').val(target);
      $(this).find('.sum').text(target);
    }

    const sum = Number($(this).find('.sum').text()) || 0;

    // 부족분 판단은 "가용(cap) < 필요(req)" 기준 (물 제외)
    if (mid !== 'RM-0015' && cap < req) hasShortage = true;

    // 등록 가능 여부는 "선택합 === target" 모두 충족
    if (sum !== target) ok = false;
  });

  // 버튼 상태
  if (hasShortage) {
    // 부족 있으면: 발주만 가능, 등록은 막음
    $('#btnCreateDraft').prop('disabled', false);
    $('#btnSubmit').prop('disabled', true);
  } else {
    // 부족 없으면: 발주 막고, 등록은 합계가 모두 맞을 때만
    $('#btnCreateDraft').prop('disabled', true);
    $('#btnSubmit').prop('disabled', !ok);
  }

  return !hasShortage && ok;
};


/* ---------- 부족분 발주 생성 데이터 수집 (MOQ 적용, ES5) ---------- */
window.collectShortages = function () {
  var shortagesTasks = [];
  var rows = $('#materialLotBody tr').toArray();

  rows.forEach(function (row) {
    var $row = $(row);
    var mid = String($row.data('material') || '');
    if (mid === 'RM-0015') return; // 물 제외

    var materialId = $row.data('material');
    var materialName = $row.find('.font-weight-bold').text().trim();
    var required = Number($row.find('.req').data('req') || 0);
    var capData = $row.data('cap');
    var cap = (capData == null) ? required : Number(capData);

    if (cap < required) {
      var lackQty = required - cap;
      // MOQ 적용 비동기 계산을 task로 모음
      var task = calculateOrderQuantity(materialId, lackQty).then(function (res) {
        return {
          materialId: materialId,
          materialName: materialName,
          lackQty: lackQty,     // 실제 부족량
          orderQty: res.orderQty,    // MOQ 적용 발주량
          packs: res.packs,            // 포대/박스 개수(표시용)
          packQty: res.packQty,        // 1팩 → 기본단위 환산값
          priceUnit: res.priceUnit,
          unitPrice: res.unitPrice,
          amount: res.amount,         // 금액(표시용)
          supplyUnit: res.supplyUnit   // 예: '20kg 포대'
        };
      });
      shortagesTasks.push(task);
    }
  });

  // 모든 행에 대한 MOQ 계산이 끝나면 배열로 반환
  return Promise.all(shortagesTasks);
};

//=== [ADD] FE 계산 헬퍼 ===
function applyMoqAndMultiple(lack, packQty, moq, multiple){
  var base = Math.max(lack, moq || 0);
  var step = multiple || packQty || 1;
  return Math.ceil(base / step) * step;   // 최종 orderQty(기본단위 g/ml/EA)
}

//orderQty: 기본단위(g/ml/EA) 기준 주문수량
//packQty : 1팩의 기본단위 수(g/ml/EA)
//priceUnit: 'KG' | 'PACK' | 'BASE' | 'BUNDLE'
function calcAmount(orderQty, packQty, unitPrice, priceUnit, bundlesPerPack){
if (priceUnit === 'KG')      return (orderQty / 1000) * unitPrice;
if (priceUnit === 'PACK')    return (orderQty / (packQty || 1)) * unitPrice; // 팩수 × 단가
if (priceUnit === 'BUNDLE')  return (orderQty / (packQty || 1)) * (bundlesPerPack || 1) * unitPrice; // (팩수×단수)×단가
return orderQty * unitPrice; // BASE
}

//=== 간단 가격 정책(단가/단위) — 빠른 임시 테이블 ===
//=== 납입단가 (FE 테이블) ===
function getPricePolicy(materialId){
  const map = {
    // 생육(kg 단가)
    'RM-0001': { priceUnit: 'KG',     unitPrice: 3300 }, // 돼지사골
    'RM-0002': { priceUnit: 'KG',     unitPrice: 3800 }, // 소사골

    // 채소류(kg 단가)
    'RM-0010': { priceUnit: 'KG',     unitPrice: 1300 }, // 양파 15kg 망
    'RM-0007': { priceUnit: 'KG',     unitPrice: 5500 }, // 통마늘 10kg 망
    'RM-0008': { priceUnit: 'KG',     unitPrice: 6000 }, // 생강 5kg 박스

    // 대파는 “단” 기준(박스=10단)
    'RM-0009': { priceUnit: 'BUNDLE', unitPrice: 2000, bundlesPerPack: 10 }, // 10단 박스

    // 조미료/향신료(팩 단가)
    'RM-0011': { priceUnit: 'PACK',   unitPrice: 1000 },  // 소금 20kg 포대 = 포대당 1,000원
    'RM-0012': { priceUnit: 'PACK',   unitPrice: 15000 }, // 후추 1kg 봉투
    'RM-0014': { priceUnit: 'PACK',   unitPrice: 10000 }, // 월계수 100g 포장
    'RM-0013': { priceUnit: 'PACK',   unitPrice: 3000 },  // 맛술 1.8L 병

    // 포장재(낱개 단가)
    'RM-0016': { priceUnit: 'BASE',   unitPrice: 150 },   // 파우치 개당
    'RM-0017': { priceUnit: 'BASE',   unitPrice: 800 },   // 박스(소) 개당
    'RM-0018': { priceUnit: 'BASE',   unitPrice: 1200 }   // 박스(대) 개당
  };
  return map[materialId] || { priceUnit: 'BASE', unitPrice: 0 };
}


/* ---------- MOQ 계산 함수 (DB 기반, ES5) ---------- */
function calculateOrderQuantity(materialId, lackQty) {
	  return $.get(ctx + '/material/order/supplier-pack-qty', { materialId: materialId })
	    .then(function (r) {
	      var packQty   = Number(r && r.packQty) || 1;
	      var moq       = Number(r && r.minOrderQty)   || 0;
	      var multiple  = Number(r && r.orderMultiple) || packQty;

	      // 서버가 주는 가격 정보(있으면 우선 사용)
	      var unitPrice = (r && r.unitPrice != null) ? Number(r.unitPrice) : null;
	      var priceUnit = (r && r.priceUnit) ? String(r.priceUnit) : null;

	      var supplyUnit = (r && r.supplyUnit) || getSupplyUnit(materialId);

	      // 부족량 → MOQ/배수 적용
	      var orderQty = applyMoqAndMultiple(lackQty, packQty, moq, multiple);
	      var packs    = Math.ceil(orderQty / packQty);

	      // 없으면 FE 정책으로 보완
	      var policy = getPricePolicy(materialId);
	      var unitPrice = policy.unitPrice;
	      var priceUnit = policy.priceUnit;
	      
	      // 금액 계산
	      var amount    = calcAmount(orderQty, packQty, unitPrice, priceUnit, policy.bundlesPerPack);

	      return {
	        orderQty: orderQty,
	        packQty: packQty,
	        packs: packs,
	        supplyUnit: supplyUnit,
	        unitPrice: unitPrice,
	        priceUnit: priceUnit,
	        amount: amount
	      };
	    }, function () {
	      // ===== 폴백(서버 실패 시) =====
	      var FALLBACK_PACK_QTY = {
	        'RM-0011':20000,'RM-0012':1000,'RM-0007':10000,'RM-0008':5000,'RM-0009':4500,
	        'RM-0010':15000,'RM-0013':1800,'RM-0014':100,'RM-0016':1000,'RM-0017':10,'RM-0018':100
	      };
	      var packQty = FALLBACK_PACK_QTY[materialId] || 1;
	      var moq = packQty, multiple = packQty;

	      // 가격 정책도 함께 보완(기존 0원→총액 0 나오는 문제 방지)
	      var policy    = getPricePolicy(materialId);
	      var unitPrice = policy.unitPrice;
	      var priceUnit = policy.priceUnit;

	      var supplyUnit = getSupplyUnit(materialId);

	      var orderQty = applyMoqAndMultiple(lackQty, packQty, moq, multiple);
	      var packs    = Math.ceil(orderQty / packQty);
	      var amount   = calcAmount(orderQty, packQty, unitPrice, priceUnit);

	      return { orderQty, packQty, packs, amount, priceUnit, unitPrice, supplyUnit };
	    });
	}


/* ---------- 발주 버튼 클릭 핸들러 (ES5, 단일) ---------- */
$('#btnCreateDraft').off('click.draft').on('click.draft', function (e) {
  e.preventDefault();

  var workOrderId = $('#workOrderIdHidden').val()
                  || new URLSearchParams(location.search).get('workOrderId');
  if (!workOrderId) { alert('작업지시서를 먼저 선택하세요.'); return; }

  var $btn = $(this);
  if ($btn.prop('disabled')) return;

  $btn.prop('disabled', true).text('계산 중...');

  window.collectShortages().then(function (shortages) {
    shortages = shortages || [];
    if (!shortages.length) {
      alert('부족분이 없습니다. (발주 생성 없이 목록으로 이동합니다)');
      location.href = ctx + '/material/order/list';
      return Promise.reject('NO_SHORTAGE');
    }

    var confirmed = showOrderPreviewConfirm(shortages);
    if (!confirmed) {
      $btn.prop('disabled', false).text('부족분 발주');
      return Promise.reject('CANCELLED');
    }

    $btn.text('생성 중...');

    // 서버가 아직 lackQty만 받는다면 lackQty에 MOQ적용 값을 넣어 전송
    var orderData = {
      workOrderId: workOrderId,
      items: shortages.map(function (item) {
        return {
          materialId: item.materialId,
          materialName: item.materialName,
          // 서버 미수정: lackQty로 보냄(= MOQ 적용량)
          lackQty: item.orderQty
          // 서버 수정 완료 시:
          // orderQty: item.orderQty,
          // lackQty: item.lackQty
        };
      })
    };

    return createDraftOrder(orderData);
  }).catch(function (err) {
    if (err !== 'NO_SHORTAGE' && err !== 'CANCELLED') {
      console.error('부족분 발주 처리 오류:', err);
      alert('부족분 발주 처리 중 오류가 발생했습니다.\n' + (err && err.message ? err.message : err));
    }
    $btn.prop('disabled', false).text('부족분 발주');
  });
});

//보기 좋은 수량 표기
function formatEqQty(item){
  const mid = item.materialId;
  const policy = getPricePolicy(mid);

  if (policy.priceUnit === 'KG') {
    return (item.orderQty / 1000).toLocaleString() + 'kg';
  }
  if (policy.priceUnit === 'BUNDLE') {
    const bundles = (item.packs || 0) * (policy.bundlesPerPack || 1);
    return bundles.toLocaleString() + '단';
  }
  if (mid === 'RM-0013') { // 맛술: 1.8L 병
    return (item.orderQty / 1000).toLocaleString() + 'L';
  }
  // 기본: 숫자만
  return item.orderQty.toLocaleString();
}


/* ---------- 발주 미리보기 확인 대화상자 ---------- */
function showOrderPreviewConfirm(shortages) {
	  var totalLack = 0;
	  var totalOrder = 0;
	  var totalAmount = 0;
	  var adjustedItems = [];
	  var packLines = [];

	  // 표기용 헬퍼
	  function formatEqualNote(item) {
	    // orderQty(기본단위, g/ml/EA)가 없으면 packs*packQty로 보정
	    var packQty = Number(item.packQty) || 1;
	    var packs   = Number(item.packs)   || Math.ceil((Number(item.orderQty)||0) / packQty) || 0;
	    var baseQty = (Number(item.orderQty) > 0) ? Number(item.orderQty) : (packs * packQty);

	    // priceUnit 기준으로 보기 좋게
	    var pu = String(item.priceUnit || '').toUpperCase();
	    if (pu === 'KG') {
	      return '= ' + (baseQty / 1000).toLocaleString() + 'kg';
	    } else if (pu === 'PACK') {
	      return '= ' + packs.toLocaleString() + '팩';
	    } else { // BASE(개/EA)
	      return '= ' + baseQty.toLocaleString() + '개';
	    }
	  }

	  shortages.forEach(function(item){
	    totalLack  += (Number(item.lackQty)  || 0);
	    totalOrder += (Number(item.orderQty) || 0);
	    totalAmount+= (Number(item.amount)   || 0);

	    if ((Number(item.orderQty)||0) > (Number(item.lackQty)||0)) {
	      adjustedItems.push('• ' + item.materialName + ': ' + item.lackQty + ' → ' + item.orderQty);
	    }

	    var packs = Number(item.packs) || Math.ceil((Number(item.orderQty)||0) / (Number(item.packQty)||1)) || 0;
	    var supply = item.supplyUnit || getSupplyUnit(item.materialId);

	    packLines.push(
	      '• ' + item.materialName + ' : ' + supply + ' × ' + packs.toLocaleString()
	      + '  (' + formatEqualNote(item) + ')'
	    );
	  });

	  var message = '부족분 발주를 생성하고,\n현재 가용분을 이번 작업지시로 예약합니다.\n\n'
	              + '총 ' + shortages.length + '개 자재의 발주를 생성합니다.\n\n'
	              + '실제 부족량: ' + totalLack.toLocaleString() + '\n'
	              + '발주 수량: '  + totalOrder.toLocaleString() + '\n';

	  if (packLines.length > 0) {
	    message += '\n발주(팩) 표기:\n' + packLines.join('\n') + '\n';
	  }
	  if (adjustedItems.length > 0) {
	    message += '\n최소 발주단위가 적용된 항목:\n' + adjustedItems.join('\n') + '\n';
	  }

	  message += '\n총 금액(추정): ' + totalAmount.toLocaleString() + '원\n\n'
	          + '[확인] 발주+예약 진행 / [취소] 중단';

	  return confirm(message);
	}


/* ---------- 실제 발주 생성 함수 (ES5) ---------- */
function createDraftOrder(orderData) {
  // 1) 발주 생성
  return $.ajax({
    url: ctx + '/material/order/draft',
    method: 'POST',
    contentType: 'application/json; charset=utf-8',
    dataType: 'json',
    data: JSON.stringify(orderData),
    xhrFields: { withCredentials: true }
  }).then(function (response) {
    // 2) 생성된 발주 ID 수집
    var ids = Array.isArray(response && response.orderIds) ? response.orderIds
            : (response && response.orderId ? [response.orderId] : []);

    // 3) 예약 처리
    return $.post(ctx + '/material/reservation/reserve-only', {
      workOrderId: orderData.workOrderId
    }).then(function (reserveResponse) {

      if (reserveResponse && reserveResponse.ok === true) {
        alert('부족분 발주가 생성되었고,\n이번 작업지시 기준으로 재고 예약을 완료했습니다.');
      } else {
        alert('발주는 생성되었지만 예약에 실패했습니다.');
      }

      // 4) 목록 이동
      if (ids && ids.length) {
        var qs = '?status=DRAFT&highlight=' + encodeURIComponent(ids.join(','));
        location.href = ctx + '/material/order/list' + qs;
      } else {
        location.href = ctx + '/material/order/list';
      }

      // 호출측 then에서 필요하면 쓰라고 응답 리턴
      return { response: response, reserveResponse: reserveResponse };
    });
  });
}

function getSupplyUnit(materialId) {
	  const units = {
	    // 생육류
	    'RM-0001': '20kg 박스',
	    'RM-0002': '20kg 박스',
	    'RM-0003': '1kg 진공포장',
	    'RM-0004': '2kg 벌크',
	    'RM-0005': '2kg 벌크',
	    'RM-0006': '1kg 진공포장',

	    // 채소류
	    'RM-0007': '10kg 망',
	    'RM-0008': '5kg 박스',
	    'RM-0009': '10단 박스',
	    'RM-0010': '15kg 망',

	    // 조미료/액상
	    'RM-0011': '20kg 포대',
	    'RM-0012': '1kg 봉투',
	    'RM-0013': '1.8L 병',
	    'RM-0014': '100g 포장',

	    // 포장재
	    'RM-0016': '1,000장 단위',
	    'RM-0017': '10개 박스',
	    'RM-0018': '100개 박스'
	  };
	  return units[materialId] || '-';
	}



/* ---------- 공통 유틸 ---------- */
function toYmd(dateValue) {
	  if (!dateValue) return '';
	  var date = (dateValue instanceof Date) ? dateValue : new Date(dateValue);
	  if (isNaN(date.getTime())) return '';
	  var y = date.getFullYear();
	  var m = ('0' + (date.getMonth() + 1)).slice(-2);
	  var d = ('0' + date.getDate()).slice(-2);
	  return y + '-' + m + '-' + d;
	}
	function fmtDate(v){ return toYmd(v); }

	function asDate(v){
	  if (!v) return null;
	  if (typeof v === 'string') return new Date(v.replace(' ', 'T'));
	  if (v && typeof v === 'object' && 'time' in v) return new Date(v.time);
	  try { return new Date(v); } catch(e){ return null; }
	}
	function fmtTS(v){
	  var d = asDate(v); if(!d || isNaN(d)) return '-';
	  var p=function(n){return String(n).padStart(2,'0');};
	  return d.getFullYear() + '-' + p(d.getMonth()+1) + '-' + p(d.getDate()) + ' '
	       + p(d.getHours()) + ':' + p(d.getMinutes()) + ':' + p(d.getSeconds());
	}
	function fmtYmd(v){
	  var d = asDate(v); if(!d || isNaN(d)) return '-';
	  var p=function(n){return String(n).padStart(2,'0');};
	  return d.getFullYear() + '-' + p(d.getMonth()+1) + '-' + p(d.getDate());
	}

/* === [NEW] inboundIds 읽기 & 사용상태 갱신 유틸 === */
function getInboundIdsParam() {
  const p = new URLSearchParams(location.search);
  const raw = p.get('inboundIds') || p.get('inboundId') || '';
  return raw.split(',').map(s => s.trim()).filter(Boolean);
}

//페이지 이동 직전에도 안전하게 영기 위해 sendBeacon 우선 사용
function updateInboundStatuses(ids) {
  if (!ids || !ids.length) return;

  // 배치 엔드포인트가 있으면 주석 해제해서 사용
  // const url = ctx + '/material/outbound/update-inbound-status-batch';
  // const data = JSON.stringify({ inboundIds: ids });
  // if (navigator.sendBeacon) {
  //   const blob = new Blob([data], { type: 'application/json' });
  //   navigator.sendBeacon(url, blob);
  //   return;
  // }

  // 단건 엔드포인트로 fallback
  ids.forEach(id => {
    const url = ctx + '/material/outbound/update-inbound-status';
    const form = new FormData();
    form.append('inboundId', id);

    if (navigator.sendBeacon) {
      navigator.sendBeacon(url, form);
    } else {
      // 비콘이 없으면 비동기로라도 발사 (페이지 전환 중 드랍될 수 있음)
      $.post(url, { inboundId: id }).catch(()=>{});
    }
  });
  
  
}

/* ---------- register.jsp: 초기 로드 ---------- */
$(function(){
  if (!$('#outboundForm').length) return;

  const params = new URLSearchParams(location.search);
  const workOrderId = params.get('workOrderId');
  const inboundId   = params.get('inboundId');
  const inboundIds  = params.get('inboundIds');
  
  //☆ 선택한 입고건들 hidden으로 넘기기(모달에서 온 값 유지)
  if (inboundIds && !$('#outboundForm input[name="inboundIds"]').length) {
    $('#outboundForm').append(
      $('<input>', { type:'hidden', name:'inboundIds', value: inboundIds })
    );
  }

  // (선택) 단일 inboundId도 서버에서 받게 하려면 유지
  if (inboundId && !$('#outboundForm input[name="inboundId"]').length) {
    $('#outboundForm').append(
      $('<input>', { type:'hidden', name:'inboundId', value: inboundId })
    );
  }

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
  
  // 자재코드(숫자 섞여도 자연정렬) 기준 정렬
  const sorted = (items || []).slice().sort((a,b) =>
    String(a.materialId || '').localeCompare(String(b.materialId || ''), 'en', { numeric: true })
  );

  if (!sorted.length) {
	  $tbody.append('<tr><td colspan="7" class="text-center text-muted">자재 정보가 없습니다.</td></tr>');
	  return;
  }

  const promises = sorted.map(function(item, idx) {
    const workOrderId = $('#workOrderIdHidden').val()
                     || new URLSearchParams(location.search).get('workOrderId');
    const required = Number(item.requiredQty) || 0;

    return $.get(ctx + '/material/inventory/lot-details', { materialId: item.materialId })
	.then(function(resp) {
		
	// ✅ 다양한 래핑 대응
	    var lots = [];
	    if (Array.isArray(resp)) lots = resp;
	    else if (resp && Array.isArray(resp.lotList)) lots = resp.lotList;
	    else if (resp && Array.isArray(resp.list))    lots = resp.list;
	    else if (resp && Array.isArray(resp.rows))    lots = resp.rows;
	    else if (resp && Array.isArray(resp.data))    lots = resp.data;
	    else if (resp && Array.isArray(resp.lots))    lots = resp.lots;   // ← 추가 후보
	    else {
	      console.warn('lots is not an array for material:', item.materialId, resp);
	      lots = [];
    
	    }                                         
	    
	        return $.get(ctx + '/material/inventory/availability',
	                     { materialId: item.materialId, workOrderId: workOrderId })
	          .then(
	            function(avail) { return { lots: lots, avail: (avail || {}) }; },
	            function() {
	              var onhandFromLots = lots.reduce(function(s,l){ return s + (Number(l.quantity)||0); }, 0);
	              return { lots: lots,
	                       avail: { onhandTotal:onhandFromLots, reservedOthers:0, reservedForThis:0 } };
	            }
	          );
      })
      .then(function(payload) {
      try {
        var lots  = payload.lots || [];
        var avail = payload.avail || {};
        var required = Number(item.requiredQty) || 0;

        // ★ 다시 한번 배열 확인
        if (!Array.isArray(lots)) {
          console.warn('lots is still not an array after processing:', lots);
          lots = [];
        }

        var onhandTotal = Number(avail.onhandTotal) || 0;
        var onhandFromLots = 0;
        
        // lots가 배열인지 확인 후 처리
        if (Array.isArray(lots)) {
          onhandFromLots = lots.reduce((s, l) => s + (Number(l.quantity) || 0), 0);
        } else {
          console.warn('lots is not an array:', lots);
          onhandFromLots = 0;
        }
        
        var reservedThis = Number(
        		  (avail.reservedForThis != null) ? avail.reservedForThis
        		  : (avail.woReserved != null)    ? avail.woReserved
        		  : 0
        		);

        		var reservedOthers = (function(){
        		  if (avail.reservedOthers != null) return Number(avail.reservedOthers) || 0;
        		  // reservedTotal이 전체이면 = total - this
        		  if (avail.reservedTotal != null) {
        		    var tot = Number(avail.reservedTotal) || 0;
        		    return Math.max(0, tot - reservedThis);
        		  }
        		  return 0;
        		})();

        let a4wo = (avail.availableForThisWO != null)
          ? Number(avail.availableForThisWO)
          : Math.max(0, onhandTotal - reservedOthers) + reservedThis;

        a4wo = Math.min(a4wo, onhandTotal, onhandFromLots);
        var cap = Math.max(0, Math.min(required, a4wo));

        var mid = item.materialId;
        if (mid === 'RM-0015') { a4wo = required; cap = required; }

        var lotRequired = (item.lotRequired === 'N' || item.lotFlag === 'N' || mid === 'RM-0015') ? 'N' : 'Y';
        var defaultWh   = item.defaultWarehouseCode || item.warehouseCode || 'WH003';

        var $row = $('<tr>')
          .attr('data-order', idx)
          .attr('data-material', item.materialId)
          .attr('data-lot-required', lotRequired)
          .attr('data-default-warehouse', defaultWh)
          .data('cap', cap)
          .data('a4wo', a4wo);

        // 회색 보조문구에 보여줄 전체 예약(others + this)
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
          // ★ 정렬 전 배열 확인
          if (Array.isArray(lots)) {
            lots.sort(function(a, b) {
              var da = new Date(a.expirationDate || '9999-12-31').getTime();
              var db = new Date(b.expirationDate || '9999-12-31').getTime();
              return da - db;
            });
          }
          
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

        // "예약수량" = 이번 작업지시 예약만 (필요수량 초과는 표시 제한)
        const reservedThisDisplay = Math.min(required, reservedThis);
        const reservedText = (mid === 'RM-0015') ? '직접출고' : reservedThisDisplay;
        $row.append($('<td>').addClass('text-right align-middle reserved-this').text(reservedText));
        
        // ✅ 예상예약(프리뷰) 셀: 처음엔 0으로 시작
        const previewInit = (mid === 'RM-0015') ? '-' : 0;
        $row.append($('<td>').addClass('text-right align-middle preview-reserve').text(previewInit));	
        
        $row.append($('<td>').addClass('text-right align-middle').append($('<span>').addClass('sum').text('0')));
        $row.append($('<td>').addClass('text-right align-middle shortage').text(required));

        $('#materialLotBody').append($row);
        updateRowSumAndValidate($row);
        return true;
      } catch (e) {
        console.error('자재 행 렌더링 오류:', e);
        return false;
      }
    })
    .fail(function(xhr) { // LOT API 자체 실패
      console.error('자재 정보 로드 실패:', item.materialId, xhr);
      var $row = $('<tr>')
        .attr('data-order', idx)
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

  // 모든 행 렌더링 이후 자동배정 + 검증
  Promise.all(promises).then(function () {
	// ★ 로딩이 끝난 뒤, data-order 기준으로 DOM 재정렬
	const rows = $tbody.children('tr').get().sort(function(a,b){
	  return (parseInt(a.dataset.order)||0) - (parseInt(b.dataset.order)||0);
	});
	$tbody.append(rows);
	  
    autoAllocateAll(false);
    // 물 특례 동기화
    $('#materialLotBody tr').each(function () {
      const mid = String($(this).data('material') || '');
      if (mid === 'RM-0015') {
        const req = Number($(this).find('.req').data('req')) || 0;
        const capData = $(this).data('cap');
        const cap = (capData == null) ? req : Number(capData);
        const target = Math.min(req, cap);
        $(this).find('.lot-qty').val(target);
        $(this).find('.sum').text(target);
        updateRowSumAndValidate($(this));
      }
    });
    window.validateAll();
  });
}

/* ---------- 행별 합계 계산 및 상태 업데이트 ---------- */
function updateRowSumAndValidate($row) {
  const required = Number($row.find('.req').data('req')) || 0;
  const capData  = $row.data('cap');
  const cap      = (capData == null) ? required : Number(capData);
  const target   = Math.min(required, cap);

  // 합계 계산
  let sum = 0;
  $row.find('.lot-qty').each(function(){ sum += Number(this.value) || 0; });
  $row.find('.sum').text(sum);

  // 예상예약/부족 표시(기존 코드 유지)
  const mid = String($row.data('material') || '');
  const previewReserve = (mid === 'RM-0015') ? '-' : Math.min(sum, required);
  $row.find('.preview-reserve').text(previewReserve);
  const shortageUi = (mid === 'RM-0015') ? 0 : Math.max(0, required - sum);
  $row.find('.shortage').text(shortageUi);

  // 색상 규칙: 충족도 기준
  $row.removeClass('table-success table-warning table-danger table-secondary');
  if (required <= 0) {
    $row.addClass('table-secondary');            // 필요 없음
  } else if (sum >= required) {
    $row.addClass('table-success');              // 완전 충족(부족 0)
  } else if (sum > 0) {
    $row.addClass('table-warning');              // 일부만 충족(부족 있음)
  } else {
    $row.addClass('table-danger');               // 전혀 배정 안 됨
  }
  
  
}


/* ---------- 입력 제한 및 실시간 검증 ---------- */
$(document).on('wheel', '.lot-qty', function(e) { e.preventDefault(); });

$(document).on('input change blur', '.lot-qty', function() {
  const $input = $(this);
  const $row = $input.closest('tr');

  const hasMax   = $input.is('[max]');
  const maxLotQty = hasMax ? Number($input.attr('max')) : Infinity;

  let inputValue = Number($input.val()) || 0;
  if (inputValue < 0) inputValue = 0;
  if (inputValue > maxLotQty) inputValue = maxLotQty;

  const required = Number($row.find('.req').data('req')) || 0;
  const capData = $row.data('cap');
  const cap = (capData == null) ? required : Number(capData);

  const othersSum = $row.find('.lot-qty').not($input).toArray()
    .reduce((sum, el) => sum + (Number(el.value) || 0), 0);
  const remaining = Math.max(Math.min(required, cap) - othersSum, 0);
  if (inputValue > remaining) inputValue = remaining;

  $input.val(inputValue);
  updateRowSumAndValidate($row);
  window.validateAll();
});

/* ---------- 폼 제출 시 hidden 필드 생성 + 예약 선처리 ---------- */
$(document).off('submit.resv', '#outboundForm')
.on('submit.resv', '#outboundForm', function(e) {
  e.preventDefault();
  const formEl = this;
  const $form  = $(formEl);

  const workOrderId = $('#workOrderIdHidden').val()
                   || new URLSearchParams(location.search).get('workOrderId');
  if (!workOrderId) { alert('작업지시서를 먼저 선택하세요.'); return; }

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
      $form.append($('<input>', { type:'hidden', name:'lotNoList',         value: $el.data('lot') || '' }));
      $form.append($('<input>', { type:'hidden', name:'qtyList',           value: quantity }));
      $form.append($('<input>', { type:'hidden', name:'lotWarehouseList',  value: ($el.data('warehouse') || '') }));
    }
  });

  // 예약만 선처리
  $.post(ctx + '/material/reservation/reserve-only', { workOrderId: workOrderId })
    .done(function(res){
      if (!res || res.ok !== true) {
        alert(res && res.message ? res.message : '예약 실패');
        $form.data('reserving', false);
        $('#btnSubmit').prop('disabled', false).text('등록');
        return;
      }
      
      // === [NEW] 선택된 모든 입고건 사용상태 갱신 ===
      const ids = getInboundIdsParam();
      if (ids.length) updateInboundStatuses(ids);
      
      formEl.submit(); // 네이티브 submit
    })
    .fail(function(xhr){
      console.error('예약 처리 실패:', xhr);
      alert('예약 처리 중 서버 오류가 발생했습니다.');
      $form.data('reserving', false);
      $('#btnSubmit').prop('disabled', false).text('등록');
    });
});



/* ---------- 출고 처리 ---------- */
window.processOutbound = function(outboundId, btnEl) {
	  if (!confirm('이 출고건을 처리하시겠습니까?')) return;

	  $.ajax({
	    type: 'POST',
	    url: ctx + '/material/outbound/process',
	    data: { outboundId: outboundId },
	    success: function(res){
	      if (res === 'OK') {
	        alert('출고처리 완료되었습니다.'); // ✅ 여기서 알림
	      }
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


//상태 뱃지 & 날짜 유틸
function badgeStatus(st){
  st = String(st || '').toUpperCase();
  if (st === 'COMPLETED' || st === 'ISSUED') return '<span class="badge badge-success">출고완료</span>';
  if (st === 'CANCELLED' || st === 'CANCELED') return '<span class="badge badge-secondary">취소</span>';
  return '<span class="badge badge-danger">미출고</span>'; // DRAFT, PARTIAL 등
}

// ☆ 새 모달 전용 구현
window.loadOutboundDetail = function(outboundId){
	  // 1) 초기화 & 모달 오픈
	  $('#modalOutboundId,#modalWorkOrderId,#modalIssuedAt,#modalHandledBy,#modalStatus,#modalNote').text('');
	  $('#outboundItemsInfo').html('<tr><td colspan="7" class="text-muted">불러오는 중...</td></tr>');
	  $('#outboundDetailModal').modal('show');

	  // 2) 데이터 조회
	  $.getJSON(ctx + '/material/outbound/detail', { outboundId })
	    .done(function(res){
	      // 헤더/아이템 방어적 매핑
	      var h = (res && (res.header || res.outbound || res)) || {};
	      var items = (res && (res.items || res.detailItems)) || [];
	      
	      items.sort(function(a,b){
	          return (a.materialId||'').localeCompare(b.materialId||'')
	              || (a.materialName||'').localeCompare(b.materialName||'')
	              || (a.lotNo||'').localeCompare(b.lotNo||'');
	        });

	      var issuedAt = h.outboundDate || h.issuedAt || h.createdDate;
	      var handledBy = h.handledBy || h.userName || h.createdBy;
	      var status = h.status || h.statusCode || h.status_display;
	      var note = h.note || h.remark || '';

	      // 3) 헤더 채우기
	      $('#modalOutboundId').text(h.outboundId || '-');
	      $('#modalWorkOrderId').text(h.workOrderId || '-');
	      $('#modalIssuedAt').text(fmtYmd(issuedAt));
	      $('#modalHandledBy').text(handledBy || '-');
	      $('#modalStatus').html(badgeStatus(status));
	      $('#modalNote').text(note || '-');

	      // 4) 상세 항목
	      if (!items.length){
	        $('#outboundItemsInfo').html('<tr><td colspan="7" class="text-muted">항목이 없습니다.</td></tr>');
	        return;
	      }

	      var rows = items.map(function(it){
	    	var exp = fmtYmd(it.expirationDate);
	        var wh = it.warehouseCode || it.warehouse_code || it.storageLocation || '-';

	        return '<tr>'
	             + '<td>' + (it.materialId||'') + '</td>'
	             + '<td>' + (it.materialName||'') + '</td>'
	             + '<td>' + (it.lotNo||'') + '</td>'
	             + '<td class="text-right">' + ((it.quantity != null) ? it.quantity : 0) + '</td>'
	             + '<td>' + wh + '</td>'
	             + '<td>' + exp + '</td>'
	             + '<td>' + (it.remark||'') + '</td>'
	             + '</tr>';
	      }).join('');

	      $('#outboundItemsInfo').html(rows);
	      
	      mergeRowspan('#outboundItemsInfo', [0, 1]);
	    })
	    .fail(function(xhr){
	        $('#outboundItemsInfo').html('<tr><td colspan="7" class="text-danger">상세를 불러오지 못했습니다.</td></tr>');
	        console.error('outbound/detail error', xhr);
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

  const totalAvailable = $lotInputs.toArray().reduce((sum, el) => {
    const hasMax = $(el).is('[max]');
    const maxQty = hasMax ? Number($(el).attr('max')) : target;
    return sum + Math.max(0, maxQty);
  }, 0);

  if (onlyIfEnough && totalAvailable < target) return false;

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