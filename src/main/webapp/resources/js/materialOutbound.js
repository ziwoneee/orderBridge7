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

    // 부족분 판단: 부동소수 오차 허용
    if (mid !== 'RM-0015' && cap + EPS < req) hasShortage = true;

    // sum 텍스트가 소수/콤마 포함 가능 → 안전 파싱 후 오차 허용 비교
    const sumText = ($(this).find('.sum').text() || '').replace(/,/g,'');
    const sumVal  = parseFloat(sumText) || 0;
    if (Math.abs(sumVal - target) > EPS) ok = false;
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
//[정상 버전 - 유지]
function calcAmount(orderQty, packQty, unitPrice, priceUnit, bundlesPerPack){
  if (priceUnit === 'KG' || priceUnit === 'L') return (orderQty) * unitPrice;
  if (priceUnit === 'PACK')   return (orderQty / (packQty || 1)) * unitPrice;
  if (priceUnit === 'BUNDLE') return (orderQty / (packQty || 1)) * (bundlesPerPack || 1) * unitPrice;
  return orderQty * unitPrice; // BASE
}


//=== [BOM: 10팩 기준 레시피 - kg/L/개 버전] ===================================
const BOM10 = {
  SBG: { // 순대국밥
    'RM-0001': { name:'돼지사골', qty:16.0,  unit:'kg' },
    'RM-0010': { name:'양파',     qty:0.10,  unit:'kg' },
    'RM-0007': { name:'통마늘',   qty:0.05,  unit:'kg' },
    'RM-0008': { name:'생강',     qty:0.03,  unit:'kg' },
    'RM-0009': { name:'대파(뿌리)',qty:0.05, unit:'kg' },
    'RM-0015': { name:'물',       qty:50.0,  unit:'L'  },
    'RM-0011': { name:'소금',     qty:0.02,  unit:'kg' },
    'RM-0012': { name:'후추',     qty:0.005, unit:'kg' },
    'RM-0005': { name:'삶은 머리고기', qty:0.70, unit:'kg' },
    'RM-0004': { name:'삶은 순대',    qty:1.18, unit:'kg' },
    'RM-0016': { name:'레토르트 파우치', qty:10, unit:'개' }
  },
  HGT: { // 한우곰탕
    'RM-0002': { name:'소사골',   qty:16.0,  unit:'kg' },
    'RM-0010': { name:'양파',     qty:0.15,  unit:'kg' },
    'RM-0007': { name:'통마늘',   qty:0.08,  unit:'kg' },
    'RM-0008': { name:'생강',     qty:0.05,  unit:'kg' },
    'RM-0009': { name:'대파(뿌리)',qty:0.05, unit:'kg' },
    'RM-0015': { name:'물',       qty:54.0,  unit:'L'  },
    'RM-0011': { name:'소금',     qty:0.03,  unit:'kg' },
    'RM-0012': { name:'후추',     qty:0.005, unit:'kg' },
    'RM-0006': { name:'양지머리', qty:1.40,  unit:'kg' },
    'RM-0014': { name:'월계수',   qty:0.005, unit:'kg' },
    'RM-0013': { name:'맛술',     qty:0.10,  unit:'L'  },
    'RM-0016': { name:'레토르트 파우치', qty:10, unit:'개' }
  },
  DGG: { // 돼지국밥
    'RM-0001': { name:'돼지사골', qty:16.0,  unit:'kg' },
    'RM-0010': { name:'양파',     qty:0.15,  unit:'kg' },
    'RM-0007': { name:'통마늘',   qty:0.08,  unit:'kg' },
    'RM-0009': { name:'대파(뿌리)',qty:0.05, unit:'kg' },
    'RM-0008': { name:'생강',     qty:0.03,  unit:'kg' },
    'RM-0015': { name:'물',       qty:57.0,  unit:'L'  },
    'RM-0011': { name:'소금',     qty:0.03,  unit:'kg' },
    'RM-0012': { name:'후추',     qty:0.005, unit:'kg' },
    'RM-0003': { name:'삼겹/목살', qty:1.40,  unit:'kg' },
    'RM-0013': { name:'맛술',     qty:0.10,  unit:'L'  },
    'RM-0016': { name:'레토르트 파우치', qty:10, unit:'개' }
  }
};

// 제품ID/이름 -> BOM 키 매핑(임시)
function mapProductKey(productId, productName){
  const name = (productName||'').replace(/\s+/g,'');
  if (/순대국밥/.test(name)) return 'SBG';
  if (/한우곰탕/.test(name)) return 'HGT';
  if (/돼지국밥/.test(name)) return 'DGG';
  return null;
}

// BOM → 필요수량(기본단위) 생성
function buildRequiredFromBOM(workList){
	  const total = {};
	  workList.forEach(({productKey, qtyEA}) => {
	    const bom = BOM10[productKey];
	    const factor = (qtyEA||0)/10;
	    if (!bom || factor<=0) return;
	    Object.entries(bom).forEach(([mid,row])=>{
	      const add = (row.qty||0)*factor;
	      if(!total[mid]) total[mid] = { qty:0, unit: row.unit };
	      total[mid].qty += add;
	    });
	  });
	  return Object.entries(total).map(([mid, v]) => ({
	    materialId: mid,
	    requiredQty: Number((v.qty).toFixed(3)), // 소수 유지
	    unit: v.unit
	  }));
	}


// 표기 유틸
function baseUnitOf(mid){
  if (['RM-0013'].includes(mid)) return 'L';                 // 맛술
  if (['RM-0016','RM-0017','RM-0018'].includes(mid)) return '개'; // 포장재
  return 'kg';
}
function fmtQty(qty, unit){
  if (unit === 'kg' || unit === 'L') return (Number(qty)||0).toLocaleString(undefined,{maximumFractionDigits:2}) + unit;
  if (unit === '개') return (Number(qty)||0).toLocaleString() + '개';
  return String(qty);
}
function stepByUnit(u){
  return (u === 'kg' || u === 'L') ? 0.01 : 1;   // kg,L 은 소수 허용, '개'는 정수
}

//===== 숫자 라운드/표기 유틸 =====
const EPS = 1e-6;
function round(n, dp){                     // 안전 반올림
  const m = Math.pow(10, dp||0);
  return Math.round((Number(n)||0 + Number.EPSILON) * m) / m;
}
function fmtNum(n, dp){                    // 화면 표기용
  return round(n, dp).toLocaleString(undefined, {
    minimumFractionDigits: dp, maximumFractionDigits: dp
  });
}
function dpByUnit(u){                      // 단위별 소수 자리
  u = String(u||'').toLowerCase();
  return (u === 'kg' || u === 'l') ? 2 : 0;
}


//=== [단가 정책] ===============================================
function getPricePolicy(materialId){
  const map = {
    // 생육(kg 단가)
    'RM-0001': { unitPrice: 3300, priceUnit: 'KG' },
    'RM-0002': { unitPrice: 3800, priceUnit: 'KG' },
    'RM-0003': { unitPrice: 7800, priceUnit: 'KG' },
    'RM-0004': { unitPrice: 4500, priceUnit: 'KG' },
    'RM-0005': { unitPrice: 6500, priceUnit: 'KG' },
    'RM-0006': { unitPrice: 9500, priceUnit: 'KG' },

    // 채소/잡화
    'RM-0007': { unitPrice: 5500, priceUnit: 'KG' },                 // 통마늘
    'RM-0008': { unitPrice: 6000, priceUnit: 'KG' },                 // 생강
    'RM-0009': { unitPrice: 2000, priceUnit: 'BUNDLE', bundlesPerPack: 10 }, // 대파: 10단/박스, 2천원/단
    'RM-0010': { unitPrice: 1300, priceUnit: 'KG' },                 // 양파

    // 조미/액상
    'RM-0011': { unitPrice: 1000,  priceUnit: 'KG' },   // 소금 1,000원/kg (20kg 포대 → 20,000원)
    'RM-0012': { unitPrice: 15000, priceUnit: 'KG' },   // 후추 15,000원/kg
    'RM-0013': { unitPrice: 3000,  priceUnit: 'PACK' }, // 맛술 3,000원/병(1.8L)
    'RM-0014': { unitPrice: 10000, priceUnit: 'KG' },   // 월계수 10,000원/kg (100g 포장)

    // 포장재 — 개당
    'RM-0016': { unitPrice: 150,   priceUnit: 'BASE' },
    'RM-0017': { unitPrice: 800,   priceUnit: 'BASE' },
    'RM-0018': { unitPrice: 1200,  priceUnit: 'BASE' }
  };
  return map[materialId] || { unitPrice: 0, priceUnit: 'BASE' };
}


/* ---------- MOQ 계산 함수 (DB 기반, ES5) ---------- */
//숫자 파서
function n(v){ return v==null || v==='' ? NaN : Number(v); }

// 1팩 → 기본단위(g/ml/EA) 폴백 테이블
// 1팩 → 기본단위(kg/L/개)
const PACK_SIZE = {
  'RM-0001': 20,   // 20kg 박스
  'RM-0002': 20,   // 20kg 박스
  'RM-0003': 1,    // 1kg 진공
  'RM-0004': 2,    // 2kg 벌크
  'RM-0005': 2,    // 2kg 벌크
  'RM-0006': 1,    // 1kg 진공
  'RM-0007': 10,   // 10kg 망
  'RM-0008': 5,    // 5kg 박스
  'RM-0009': 5,    // 10단 박스(총량 5kg로 가정)
  'RM-0010': 15,   // 15kg 망
  'RM-0011': 20,   // 20kg 포대
  'RM-0012': 1,    // 1kg 봉투
  'RM-0013': 1.8,  // 1.8L 병
  'RM-0014': 0.1,  // 100g 포장 → 0.1kg
  'RM-0016': 1000, // 1,000장 단위
  'RM-0017': 10,   // 10개 박스
  'RM-0018': 100   // 100개 박스
};


// 부족량 → MOQ/배수 반올림
function applyMoqAndMultiple(lack, packQty, moqBase, multipleBase){
  const base = Math.max(lack, moqBase || 0);
  const step = multipleBase || packQty || 1;
  return Math.ceil(base / step) * step;
}

window.calculateOrderQuantity = function(materialId, lackQty){
  return $.get(ctx + '/material/order/supplier-pack-qty', { materialId }).then(function(r){

	  // 부족량 → MOQ/배수 반올림 이전, 서버 응답 처리 부분
	  let packQty = n(r && r.packQty);
	  const conv  = n(r && (r.convToBase || r.conv_to_base));

	  // packQty가 없거나 1이면(비정상) conv 또는 PACK_SIZE로 보정
	  if (!isFinite(packQty) || packQty <= 0 || packQty === 1) {
	    packQty = (isFinite(conv) && conv > 1) ? conv : (PACK_SIZE[materialId] || 1);
	  }

	
    let moq      = n(r && r.minOrderQty);
     let multiple = n(r && r.orderMultiple);
     // 값이 없으면 1팩 기준, 값이 '1' 같은 팩 단위면 기본단위로 변환
     if (!isFinite(moq)      || moq      <= 0) moq      = packQty;
     else if (moq < packQty)      moq      *= packQty;
     if (!isFinite(multiple) || multiple <= 0) multiple = packQty;
     else if (multiple < packQty) multiple *= packQty;

    const supplyUnit = (r && r.supplyUnit) || getSupplyUnit(materialId);

    // --- MOQ/배수 적용 ---
    const orderQty = applyMoqAndMultiple(lackQty, packQty, moq, multiple);
    const packs    = Math.ceil(orderQty / packQty);

    // --- 단가(서버 우선, 없으면 정책) ---
    const policy         = getPricePolicy(materialId);
    const unitPrice      = (r && r.unitPrice != null) ? Number(r.unitPrice) : policy.unitPrice;
    const priceUnit      = (r && r.priceUnit) ? String(r.priceUnit).toUpperCase() : policy.priceUnit;
    const bundlesPerPack = policy.bundlesPerPack;

    const amount = calcAmount(orderQty, packQty, unitPrice, priceUnit, bundlesPerPack);

    return { orderQty, packQty, packs, supplyUnit, unitPrice, priceUnit, amount };
  }, function(){
    // 서버 실패 시에도 동일 로직
    const packQty = PACK_SIZE[materialId] || 1;
    const moq     = packQty, multiple = packQty;

    const policy  = getPricePolicy(materialId);
    const orderQty= applyMoqAndMultiple(lackQty, packQty, moq, multiple);
    const packs   = Math.ceil(orderQty / packQty);
    const amount  = calcAmount(orderQty, packQty, policy.unitPrice, policy.priceUnit, policy.bundlesPerPack);

    return { orderQty, packQty, packs, supplyUnit:getSupplyUnit(materialId),
             unitPrice:policy.unitPrice, priceUnit:policy.priceUnit, amount };
  });
};




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
      throw 'NO_SHORTAGE';
    }

    // ✅ 여기부터 바뀐 부분 (비동기 확인)
    return showOrderPreviewConfirm(shortages).then(function (confirmed) {
      if (!confirmed) {
        $btn.prop('disabled', false).text('부족분 발주');
        throw 'CANCELLED';
      }

      $btn.text('생성 중...');

      // orderData 만들기 부분만 교체
      var orderData = {
        workOrderId: workOrderId,
        items: shortages.map(function (item) {
          // item.packs는 calculateOrderQuantity에서 이미 계산됨(ceil(orderQty/packQty))
          return {
            materialId: item.materialId,
            materialName: item.materialName,
            // ✅ “기본단위(kg/L/EA)의 수량”을 보낸다
            //   - FE가 MOQ/배수까지 반영했다면 orderQty 사용
            //   - “진짜 부족량”만 보내고 서버가 반올림/배수처리 하게 하려면 lackQty 사용
            lackQty: item.orderQty   // 또는 lackQty: item.lackQty
          };
        })
      };


      return createDraftOrder(orderData);
    });
  }).catch(function (err) {
    if (err !== 'NO_SHORTAGE' && err !== 'CANCELLED') {
      console.error('부족분 발주 처리 오류:', err);
      alert('부족분 발주 처리 중 오류가 발생했습니다.\n' + (err && err.message ? err.message : err));
    }
    $btn.prop('disabled', false).text('부족분 발주');
  });
});



//함수
function formatEqQty(item){
  const mid = item.materialId;
  const policy = getPricePolicy(mid);
  const pu = (item.priceUnit || policy.priceUnit || '').toUpperCase();
  if (pu === 'KG')     return (item.orderQty).toLocaleString(undefined,{maximumFractionDigits:2}) + 'kg';
  if (pu === 'L')      return (item.orderQty).toLocaleString(undefined,{maximumFractionDigits:2}) + 'L';
  if (pu === 'BUNDLE') {
    const bundles = (item.packs || 0) * (policy.bundlesPerPack || 1);
    return bundles.toLocaleString() + '단';
  }
  // 맛술이 서버에서 L로 오면 위에서 처리되고, PACK이면 아래처럼 개수 표기
  return item.orderQty.toLocaleString();
}


/* ---------- 발주 미리보기 확인 대화상자 ---------- */
function showOrderPreviewConfirm(shortages){
	  var totalLack=0, totalOrder=0, totalAmount=0, adjustedItems=[], packLines=[];

	  shortages.forEach(function(item){
	    var pk = Number(item.packQty)||1;
	    var ord= Number(item.orderQty)||0;
	    var lack=Number(item.lackQty)||0;
	    var packs=(ord>0&&pk>0)?Math.ceil(ord/pk):0;

	    totalLack  += lack;
	    totalOrder += packs;         // ✅ 팩 개수 합계로
	    totalAmount+= Number(item.amount)||0;

	    const clean = v => (Math.round(Number(v || 0)*100)/100).toString().replace(/\.?0+$/,'');
	    if (ord>lack) adjustedItems.push('• ' + item.materialName + ': ' + clean(lack) + ' → ' + clean(ord));

	    var supply = item.supplyUnit || getSupplyUnit(item.materialId);
	    // ord는 항상 packs*pk라 그대로 써도 OK (아래 eqText는 기본단위 총량 표기)
	    var eqText = formatEqQty({ materialId:item.materialId, orderQty:ord, packs:packs, priceUnit:item.priceUnit });
	    packLines.push('• ' + item.materialName + ' : ' + supply + ' × ' + packs.toLocaleString()
	                   + '  (= ' + eqText + ')');
	  });

	  // 모달에 꽂기
	  $('#opv-summary').text(
		// ✅ “발주 수량(팩)”으로 라벨 바꾸면 더 명확
		'총 ' + shortages.length + '개 자재 / 실제 부족량: ' + totalLack.toLocaleString()
		+ ' / 발주 수량(팩): ' + totalOrder.toLocaleString()
	  );
	  $('#opv-packs').html(
	    packLines.length ? ('<div class="mb-1 font-weight-bold">발주(팩) 표기</div><pre class="mb-0">'
	      + packLines.join('\n') + '</pre>') : ''
	  );
	  $('#opv-adjusted').html(
	    adjustedItems.length ? ('<div class="mb-1 font-weight-bold">최소 발주단위가 적용된 항목</div><pre class="mb-0">'
	      + adjustedItems.join('\n') + '</pre>') : ''
	  );
	  $('#opv-total').text('총 금액(추정): ' + totalAmount.toLocaleString() + '원');

	  // Promise<boolean> 반환
	  return new Promise(function(resolve){
	    $('#opv-ok').off('click').on('click', function(){ $('#orderPreviewModal').modal('hide'); resolve(true); });
	    $('#orderPreviewModal').off('hidden.bs.modal').on('hidden.bs.modal', function(){ resolve(false); });
	    $('#orderPreviewModal').modal('show');
	  });
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
      
      var itemsFromServer = (dto.materialList || []);
      if (!itemsFromServer.length) {
        var key = mapProductKey(dto.productId, dto.productName);
        if (key) itemsFromServer = buildRequiredFromBOM([{ productKey: key, qtyEA: 10 }]);
      }
      
      renderMaterialRows(itemsFromServer);
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
    var rowUnit = item.unit || baseUnitOf(item.materialId);   // ★ 추가

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
            
            var unit = rowUnit;
            var idp  = inputDpByUnit(unit);
            var step = idp ? (1/Math.pow(10,idp)) : 1;
            $('<input>', { type:'number', min:0, step:step, value: fmtInput(init, idp), max:init })

            
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
            
            var unit = rowUnit;
            var idp  = inputDpByUnit(unit);
            var step = idp ? (1/Math.pow(10,idp)) : 1;
            $('<input>', { type:'number', min:0, max:availableQty, step:step, value: fmtInput(0, idp) })


            
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
       
        // ← 계산용 데이터 저장 (updateRowSumAndValidate에서 읽음)
        $row.data('reservedThis', reservedThisDisplay);
        
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
	  const capData = $row.data('cap');
	  const cap = Number(capData == null ? required : capData);  // null/undefined면 required
	  const target   = Math.min(required, cap);

	  const mid  = String($row.data('material') || '');
	  const unit = $row.data('unit') || baseUnitOf(mid);
	  const dp   = dpByUnit(unit);
	  
	  
	  // === [NEW] 입력칸 표시용 반올림 유틸 ===
	  function inputDpByUnit(u){ u=String(u||'').toLowerCase(); return (u==='kg'||u==='l') ? 3 : 0; } // 필요시 4로
	  function roundTo(n, dp){ var m=Math.pow(10,dp||0); return Math.round((Number(n)||0 + Number.EPSILON)*m)/m; }
	  function fmtInput(n, dp){ var s = roundTo(n,dp).toFixed(dp); return s.replace(/\.?0+$/,''); }


	  // 선택 합계
	  let sum = 0;
	  $row.find('.lot-qty').each(function(){ sum += Number(this.value) || 0; });
	  const sumR = round(sum, dp);
	  $row.find('.sum').text(fmtNum(sumR, dp));

	  // 기존 예약 반영해 "최종" 예상 예약 계산
	  const prevReserved = (mid === 'RM-0015') ? 0 : (Number($row.data('reservedThis')) || 0);
	  const finalAfter   = (mid === 'RM-0015') ? required : Math.min(prevReserved + sumR, required);
	  $row.find('.preview-reserve').text(mid === 'RM-0015' ? '-' : fmtNum(finalAfter, dp));

	  // 부족은 '최종 기준'
	  const shortage = (mid === 'RM-0015') ? 0 : Math.max(0, required - finalAfter);
	  $row.find('.shortage').text(fmtNum(round(shortage, dp), dp));

	  // 행 색상(부족 0이면 성공)
	  $row.removeClass('table-success table-warning table-danger table-secondary');
	  const isShortageByStock = (mid !== 'RM-0015') && (cap + EPS < required);
	  if (required <= 0)          $row.addClass('table-secondary');
	  else if (isShortageByStock) $row.addClass('table-warning');           // 재고/가용이 부족
	  else if (shortage <= EPS)   $row.addClass('table-success');           // 최종 부족 0
	  else if (sumR > 0)          $row.addClass('table-warning');           // 일부 입력
	  else                        $row.addClass('table-danger');            // 미입력
	}


/* ---------- 입력 제한 및 실시간 검증 ---------- */
$(document).on('wheel', '.lot-qty', function(e) { e.preventDefault(); });

$(document).on('input change blur', '.lot-qty', function(e) {
  const $input = $(this);
  const $row   = $input.closest('tr');

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