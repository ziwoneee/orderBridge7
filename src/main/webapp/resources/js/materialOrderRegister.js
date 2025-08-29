/* ---------------------------------------
 * [1] 공통 상태
 * ------------------------------------- */
var itemIndex = 1;              // 동적 네이밍
var materialMap = {};           // materialId -> item (정규화된 객체)
var supplierItemMap = {};       // materialId -> item (정규화된 객체)

/* ---------------------------------------
 * [2] 행 생성
 * ------------------------------------- */
function addItemRowFromMaterial(item) {
    var tbody = document.querySelector("#itemTable tbody");
    var idx = itemIndex++;

    var purchaseUnit = (item.unit || 'EA').toUpperCase();                 // 주문 입력 단위
    var priceUnitCalc= (item.priceUnitCalc || purchaseUnit).toUpperCase();// 계산용 단위
    var displayUnit  = (item.displayUnit   || priceUnitCalc).toUpperCase();// 표시용 단위
    var convBase     = Number(item.convBase || item.convToBase || 1); // 1 PACK→과금단위
    var convDisplay  = Number(item.convDisplay || 1);                 // 1 PACK→표시단위
    
    var minQ = Number(item.minOrderQty || 1);
    var mul  = Number(item.orderMultiple || 1);
    var defaultQty = Math.max(minQ, mul);
    var _coef = (priceUnitCalc === purchaseUnit ? 1 : convBase);
    var unitPrice = Number(item.unitPrice || 0);

    var row = document.createElement("tr");
    row.innerHTML =
        '<td>'
      + '  <select name="orderItems['+idx+'].materialId" class="form-control">'
      + '    <option value="'+item.materialId+'">'+item.materialName+'</option>'
      + '  </select>'
      // 서버 저장용(표시 기준을 유지하고 싶으면 그대로 사용)
      + '  <input type="hidden" name="orderItems['+idx+'].unit" value="'+purchaseUnit+'">'
      + '  <input type="hidden" name="orderItems['+idx+'].priceUnit" value="'+priceUnitCalc+'">' // 과금단위
      + '  <input type="hidden" name="orderItems['+idx+'].convToBase" value="'+convBase+'">'     // 1PACK→과금단위
      + '</td>'
      + '<td>'
      + '  <input type="number" name="orderItems['+idx+'].orderQuantity"'
      + '         class="form-control"'
      + '         value="'+defaultQty+'" min="'+minQ+'" step="1"'
      + '         data-multiple="'+mul+'"'
      // 계산용 데이터셋
      + '         data-purchase-unit="'+purchaseUnit+'"'
      + '         data-price-unit="'+priceUnitCalc+'"'
      + '         data-conv="'+(priceUnitCalc===purchaseUnit ? 1 : convBase)+'"'
      // 표시용 데이터셋
      + '         data-display-unit="'+displayUnit+'"'
      + '         data-conv-display="'+convDisplay+'"'
      + '         oninput="enforceMultiple(this)" onchange="rowRecalc(this)" required>'
      + '  <small class="text-muted d-block">'
      +        purchaseUnit+' 1개 ≈ '+convDisplay.toLocaleString()+' '+displayUnit
      +        (minQ>1? ' | 최소 '+minQ+'개' : '')
      +        (mul>1?  ' | '+mul+'개 배수' : '')
      + '  </small>'
      + '</td>'
      + '<td>'
      + '  <input type="number" name="orderItems['+idx+'].unitPrice"'
      + '         class="form-control" value="'+unitPrice+'" min="0" step="1"'
      + '         onchange="rowRecalc(this)" required>'
      + '</td>'
      + '<td>'
      + '  <input type="number" name="orderItems['+idx+'].visibleTotal"'
      + '         class="form-control" readonly'
      + '         value="'+ Math.round(defaultQty * _coef * unitPrice) +'">'
      + '  <input type="hidden" name="orderItems['+idx+'].totalPrice"'
      + '         value="'+ Math.round(defaultQty * _coef * unitPrice) +'">'
      + '  <div class="small text-muted mt-1"><span class="js-conv">'+(defaultQty*convDisplay).toLocaleString()+' '+displayUnit+'</span></div>'
      + '</td>'
      + '<td>'
      + '  <input type="text" name="orderItems['+idx+'].warehouseCode" class="form-control" value="'+(item.warehouseCode||'')+'" readonly>'
      + '</td>'
      + '<td>'
      + '  <button type="button" class="btn btn-sm btn-danger" onclick="removeRow(this)">삭제</button>'
      + '</td>';

    tbody.appendChild(row);
    rowRecalc(row.querySelector("input[name$='.unitPrice']"));
}

function addItemRow() {
    var tbody = document.querySelector("#itemTable tbody");
    var idx = itemIndex++;

    var row = document.createElement("tr");
    row.innerHTML =
        '<td>'
      + '  <select name="orderItems['+idx+'].materialId" class="form-control" onchange="onMaterialSelect(this)">'
      + '    <option value="">선택</option>'
      +        getMaterialOptions()
      + '  </select>'
      + '  <input type="hidden" name="orderItems['+idx+'].unit" value="">'
      + '  <input type="hidden" name="orderItems['+idx+'].priceUnit" value="">'
      + '  <input type="hidden" name="orderItems['+idx+'].convToBase" value="1">'
      + '</td>'
      + '<td>'
      + '  <input type="number" name="orderItems['+idx+'].orderQuantity"'
      + '         class="form-control" min="1" step="1" value="1"'
      + '         data-multiple="1"'
      + '         data-purchase-unit="EA"'
      + '         data-price-unit="EA"'
      + '         data-conv="1"'
      + '         data-display-unit="EA"'
      + '         data-conv-display="1"'
      + '         oninput="enforceMultiple(this)" onchange="rowRecalc(this)" required>'
      + '  <small class="text-muted d-block">-</small>'
      + '</td>'
      + '<td>'
      + '  <input type="number" name="orderItems['+idx+'].unitPrice"'
      + '         class="form-control" min="0" step="1" value="0" onchange="rowRecalc(this)" required>'
      + '</td>'
      + '<td>'
      + '  <input type="number" name="orderItems['+idx+'].visibleTotal" class="form-control" readonly value="0">'
      + '  <input type="hidden" name="orderItems['+idx+'].totalPrice" value="0">'
      + '  <div class="small text-muted mt-1"><span class="js-conv">0 EA</span></div>'
      + '</td>'
      + '<td>'
      + '  <input type="text" name="orderItems['+idx+'].warehouseCode" class="form-control" readonly>'
      + '</td>'
      + '<td>'
      + '  <button type="button" class="btn btn-sm btn-danger" onclick="removeRow(this)">삭제</button>'
      + '</td>';

    tbody.appendChild(row);
}

/* ---------------------------------------
 * [3] 기존 행 업그레이드 (첫 기본 행 포함)
 * ------------------------------------- */
function upgradeExistingRows() {
    var trs = document.querySelectorAll("#itemTable tbody tr");
    for (var i=0; i<trs.length; i++) {
        var tr = trs[i];
        if (tr.querySelector("input[name$='.convToBase']")) continue; // 이미 새 구조

        var idx = itemIndex++;
        
        // ★ 추가: 교체 전에 기존 materialId 확보
        var prevHidden = tr.querySelector("input[type='hidden'][name$='.materialId']");
        var prevSelect = tr.querySelector("select[name$='.materialId']");
        var prevMatId  = (prevHidden && prevHidden.value) || (prevSelect && prevSelect.value) || '';

        // 1) 자재 셀 교체
        var matCell = tr.children[0];
        matCell.innerHTML =
            '<select name="orderItems['+idx+'].materialId" class="form-control" onchange="onMaterialSelect(this)">' +
            '  <option value="">선택</option>' + getMaterialOptions() +
            '</select>' +
            '<input type="hidden" name="orderItems['+idx+'].unit" value="">' +
            '<input type="hidden" name="orderItems['+idx+'].priceUnit" value="">' +
            '<input type="hidden" name="orderItems['+idx+'].convToBase" value="1">';

        // ★ 추가: 이전 materialId를 data-prev로 보관
        var newSel = matCell.querySelector("select[name$='.materialId']");
        if (newSel) newSel.dataset.prev = prevMatId;

        // 2) 수량 셀 교체
        var qtyCell = tr.children[1];
        qtyCell.innerHTML =
            '<input type="number" name="orderItems['+idx+'].orderQuantity"'
          + '       class="form-control" min="1" step="1" value="1"'
          + '       data-multiple="1"'
          + '       data-purchase-unit="EA"'
          + '       data-price-unit="EA"'
          + '       data-conv="1"'
          + '       data-display-unit="EA"'
          + '       data-conv-display="1"'
          + '       oninput="enforceMultiple(this)" onchange="rowRecalc(this)" required>'
          + '<small class="text-muted d-block">-</small>';

        // 3) 단가 name 보정
        var unitInput = tr.children[2].querySelector("input");
        if (unitInput) {
            unitInput.setAttribute("name", 'orderItems['+idx+'].unitPrice');
            unitInput.setAttribute("onchange", "rowRecalc(this)");
            if (!unitInput.value) unitInput.value = "0";
        }

        // 4) 총액 셀 교체
        tr.children[3].innerHTML =
            '<input type="number" name="orderItems['+idx+'].visibleTotal" class="form-control" readonly value="0">'
          + '<input type="hidden" name="orderItems['+idx+'].totalPrice" value="0">'
          + '<div class="small text-muted mt-1"><span class="js-conv">0 EA</span></div>';

        // 5) 창고 name 보정
        var whInput = tr.children[4] && tr.children[4].querySelector("input");
        if (whInput) whInput.setAttribute("name", 'orderItems['+idx+'].warehouseCode');
    }
}

/* ---------------------------------------
 * [4] 거래처 변경 → 품목 로딩
 * ------------------------------------- */
//[4] 거래처 변경 → 품목 로딩  (DOMContentLoaded 안에서 보장)
document.addEventListener('DOMContentLoaded', function () {
  var supplierSel = document.getElementById('supplierSelect')
                 || document.querySelector("select[name='order.supplierId']");
  if (!supplierSel) return;

  supplierSel.addEventListener("change", function () {
    var supplierId = this.value;
    materialMap = {};
    supplierItemMap = {};

    // 모든 자재 select 초기화
    var allSel = document.querySelectorAll("select[name$='.materialId']");
    for (var i=0; i<allSel.length; i++) allSel[i].innerHTML = '<option value="">선택</option>';
    if (!supplierId) return;

    fetch('/supplierItem/list?supplierId=' + encodeURIComponent(supplierId))
    .then(function(res){ if(!res.ok) throw new Error('Network error'); return res.json(); })
    .then(function(data){
      // 1) 맵 갱신
      materialMap = {}; supplierItemMap = {};
      for (var i=0; i<data.length; i++) {
        var it = data[i];
        
        var purchaseUnit = String(it.orderUnit || it.unit || 'EA').toUpperCase();
        var priceUnit    = String(it.priceUnit || it.price_unit || purchaseUnit).toUpperCase();
        var stockUnit    = String(it.stockUnit || it.stock_unit || priceUnit).toUpperCase();
        var convBaseRaw  = (it.convToBase  != null ? it.convToBase  : it.conv_to_base);
        var convStockRaw = (it.convToStock != null ? it.convToStock : it.conv_to_stock);
   
       // ✅ 과금 환산: 서버의 conv_to_base를 최우선, 없으면 conv_to_stock으로 폴백
        var convBase = Number(
         (convBaseRaw != null && convBaseRaw !== '') ? convBaseRaw
                                                       : (convStockRaw || 1)
         );
   
       // 표시 환산: 재고/표시 단위 기준. 없으면 convBase 사용
        var convDisplay = Number(convStockRaw || 0);
        
       if (!convDisplay) {
         if (priceUnit === 'L'  && (stockUnit === 'ML' || stockUnit === 'CC')) convDisplay = convBase * 1000;
         else if (priceUnit === 'KG' && (stockUnit === 'G' || stockUnit === 'GRAM')) convDisplay = convBase * 1000;
         else convDisplay = convBase;
       }
        
        var normalized = {
          materialId   : it.materialId,
          materialName : it.materialName,
          warehouseCode: it.warehouseCode || '',
          unit         : purchaseUnit,       // BOTTLE
          priceUnitCalc: priceUnit,          // EA
          displayUnit  : stockUnit,          // ML
          unitPrice    : Number(it.unitPrice || 0),
          convBase     : convBase,           // ★ 1 (EA 과금)
          convDisplay  : convDisplay,        // ★ 1800 (표시용)
          minOrderQty  : Number(it.minOrderQty || 1),
          orderMultiple: Number(it.orderMultiple || 1)
        };
        materialMap[normalized.materialId]     = normalized;
        supplierItemMap[normalized.materialId] = normalized;
      }

      upgradeExistingRows();

      // 2) select 옵션 갱신 + 기존 선택 복원 + onMaterialSelect 강제 호출
      var optionHTML = getMaterialOptions();
      var sels = document.querySelectorAll("select[name$='.materialId']");
      for (var j=0; j<sels.length; j++) {
        var prev = sels[j].value;
        if (!prev) prev = sels[j].dataset.prev || '';
        if (!prev) {
            var hid = sels[j].parentNode.querySelector('input[type="hidden"][name$=".materialId"]');
            if (hid && hid.value) prev = hid.value;
        }

        sels[j].innerHTML = '<option value="">선택</option>' + optionHTML;
        sels[j].setAttribute("onchange", "onMaterialSelect(this)");

        if (prev) {
          sels[j].value = prev;
          onMaterialSelect(sels[j]); // dataset/hidden 주입
        }
      }

      // 3) 모든 행 강제 재계산(안전망)
      setTimeout(function(){
        var rows = document.querySelectorAll('#itemTable tbody tr');
        for (var k=0; k<rows.length; k++){
          var trg = rows[k].querySelector("input[name$='.unitPrice']") ||
                    rows[k].querySelector("input[name$='.orderQuantity']");
          if (trg) rowRecalc(trg);
        }
      }, 0);
    })
    .catch(function(err){
      console.error(err);
      alert('거래처 자재 목록을 불러오지 못했습니다.');
    });
  });   // ✅ change 핸들러 닫기
  
  //✅ 초기 로드 때도 한 번 강제로 change 날려서 메타 로딩/계산 실행
  if (supplierSel.value) { try { _dispatchChange(supplierSel); } catch(e) { supplierSel.dispatchEvent(new Event('change')); } }
});     // ✅ DOMContentLoaded 닫기

/* ---------------------------------------
 * [5] 자재 선택 → 단가/단위 바인딩
 * ------------------------------------- */
function onMaterialSelect(selectElement) {
    var selectedId = selectElement.value;
    var item = supplierItemMap[selectedId] || materialMap[selectedId];
    if (!item) return;

    var row = selectElement.closest ? selectElement.closest("tr") : (function(n){while(n && n.tagName!=='TR'){n=n.parentNode;} return n;})(selectElement);
    if (!row) return;

    var unitPriceInput = row.querySelector("input[name$='.unitPrice']");
    var locationInput  = row.querySelector("input[name$='.warehouseCode']");
    var priceUnitInput = row.querySelector("input[name$='.priceUnit']");
    var unitInput      = row.querySelector("input[name$='.unit']");
    var convInput      = row.querySelector("input[name$='.convToBase']");
    var qtyInput       = row.querySelector("input[name$='.orderQuantity']");
    var hintSmall      = row.querySelector("td:nth-child(2) small.text-muted");

    // 단위/환산
    var purchaseUnit = (item.unit || 'EA').toUpperCase();
    var priceUnitCalc= (item.priceUnitCalc || purchaseUnit).toUpperCase();
    var displayUnit  = (item.displayUnit   || priceUnitCalc).toUpperCase();
    var convBase     = Number(item.convBase || item.convToBase || 1);
    var convDisplay  = Number(item.convDisplay || 1);

    // 값 주입(서버 저장용 hidden)
    if (unitInput)      unitInput.value      = purchaseUnit;
    if (priceUnitInput) priceUnitInput.value = priceUnitCalc; // ★ 과금단위 저장
    if (convInput)      convInput.value      = convBase;      // ★ 1PACK→과금단위
    if (locationInput)  locationInput.value  = item.warehouseCode || '';
    if (unitPriceInput) unitPriceInput.value = item.unitPrice || 0;

    // dataset 갱신 + 최소/배수
    if (qtyInput) {
        qtyInput.dataset.purchaseUnit = purchaseUnit;
        qtyInput.dataset.priceUnit    = priceUnitCalc;          // 계산용(KG/L/…)
        qtyInput.dataset.conv         = String(convBase); // 과금환산은 항상 convBase

        qtyInput.dataset.displayUnit  = displayUnit;                                 // 표시용
        qtyInput.dataset.convDisplay  = String(convDisplay);

        qtyInput.min              = String(item.minOrderQty || 1);
        qtyInput.dataset.multiple = String(item.orderMultiple || 1);
        
        var defaultQty = Math.max(item.minOrderQty || 1, item.orderMultiple || 1);
        qtyInput.value = defaultQty;
    }

    // 힌트
    if (hintSmall) {
        var hint = purchaseUnit + ' 1개 ≈ ' + convDisplay.toLocaleString() + ' ' + displayUnit;
        if ((item.minOrderQty || 1) > 1) hint += ' | 최소 ' + item.minOrderQty + '개';
        if ((item.orderMultiple || 1) > 1) hint += ' | ' + item.orderMultiple + '개 배수';
        hintSmall.textContent = hint;
    }

    // 계산 실행
    rowRecalc(unitPriceInput || qtyInput);
}

// 이벤트 위임 방식도 유지
document.addEventListener("change", function (e) {
    var t = e.target || e.srcElement;
    if (!t || !t.matches || !t.matches("select[name$='.materialId']")) return;
    onMaterialSelect(t);
});

/* ---------------------------------------
 * [6] 계산/보정
 * ------------------------------------- */
function enforceMultiple(el) {
    var mul = parseInt(el.dataset.multiple || '1', 10) || 1;
    var min = parseInt(el.min || '1', 10) || 1;
    var v = parseInt(el.value || '0', 10) || 0;

    if (v < min) v = min;
    if (mul > 1) {
        var rem = (v - min) % mul;
        if (rem !== 0) v = v - rem + mul; // 올림 보정
    }
    el.value = v;
}

//★ REPLACE: 기존 rowRecalc 전체를 아래로 교체
//★ 수정된 rowRecalc 함수 - 발주 상세와 동일한 계산 로직 적용
function rowRecalc(input){
  var row = input.closest ? input.closest('tr') : (function(n){while(n && n.tagName!=='TR'){n=n.parentNode;}return n;})(input);
  if (!row) return;

  // 기본 값들
  var qtyInput    = row.querySelector("input[name$='.orderQuantity']");
  var unitPriceEl = row.querySelector("input[name$='.unitPrice']");
  var hiddenTot   = row.querySelector("input[type='hidden'][name$='.totalPrice']");
  var visibleTot  = row.querySelector("input[name$='.visibleTotal']") 
                 || (hiddenTot && hiddenTot.parentNode.querySelector("input[type='number']"));
  var convSpan    = row.querySelector(".js-conv");

  var packs = Number(qtyInput && qtyInput.value ? qtyInput.value : 0) || 0;
  var unitPrice = Number(unitPriceEl && unitPriceEl.value ? unitPriceEl.value : 0) || 0;

  // ① 과금단위 확인
  var priceUnit = (qtyInput && qtyInput.dataset && qtyInput.dataset.priceUnit)
               || (row.querySelector("input[name$='.priceUnit']") || {}).value
               || 'KG';
  priceUnit = String(priceUnit).toUpperCase();

  // ② 팩당 과금수량 확인 (1 PACK = ? KG/L/BUNDLE)
  var packQty = Number(qtyInput && qtyInput.dataset && qtyInput.dataset.conv) ||
                Number((row.querySelector("input[name$='.convToBase']") || {}).value) ||
                1;

  // ③ 번들 특수 케이스
  var bundlesPerPack = Number(qtyInput && qtyInput.dataset && qtyInput.dataset.bundlesPerPack) || 1;

  //✅ 공통 공식 (단위 무관): 과금수량 = PACK × convBase
  var billedQty = packs * packQty;
  
  // 총액 계산: 과금수량 × 단가
  var total = Math.round(billedQty * unitPrice);

  // 디버깅 로그 (개발시에만)
  console.log('계산 디버깅:', {
    packs: packs,
    packQty: packQty, 
    priceUnit: priceUnit,
    unitPrice: unitPrice,
    billedQty: billedQty,
    total: total,
    calculation: `${packs} PACK × ${packQty} ${priceUnit} × ${unitPrice}원 = ${total}원`
  });

  if (hiddenTot)  hiddenTot.value  = total;
  if (visibleTot) visibleTot.value = total;

  //표시용 라벨: PACK × convDisplay
  if (convSpan) {
  var convDisp = Number(qtyInput && qtyInput.dataset && qtyInput.dataset.convDisplay) || packQty;
  var dispUnit = (qtyInput && qtyInput.dataset && qtyInput.dataset.displayUnit) || priceUnit;
  var displayQty = packs * convDisp;
  // KG/L는 소수 표시, 그 외 정수
  if (dispUnit === 'KG' || dispUnit === 'L') {
    convSpan.textContent = Number(displayQty.toFixed(2)).toLocaleString() + (dispUnit === 'KG' ? 'kg' : 'L');
  } else if (dispUnit === 'EA') {
    convSpan.textContent = displayQty.toLocaleString() + 'EA';
  } else {
    convSpan.textContent = displayQty.toLocaleString() + ' ' + dispUnit;
  }
 }
}

/* ---------------------------------------
 * [7] 공용 유틸
 * ------------------------------------- */
function removeRow(btn) {
    var tr = btn.closest ? btn.closest("tr") : (function(n){while(n && n.tagName!=='TR'){n=n.parentNode;} return n;})(btn);
    if (tr && tr.parentNode) tr.parentNode.removeChild(tr);
}

function getMaterialOptions() {
    var arr = [];
    for (var k in supplierItemMap) {
        if (!supplierItemMap.hasOwnProperty(k)) continue;
        var it = supplierItemMap[k];
        arr.push('<option value="'+it.materialId+'">'+it.materialName+'</option>');
    }
    return arr.join('');
}

/* ---------------------------------------
 * [8] 자재명 기반 거래처 검색 기능
 * ------------------------------------- */
function setupMaterialSearch() {
    var materialInput = document.querySelector('input[name*="material"], input[placeholder*="자재"]');
    if (!materialInput) {
        // 자재명 입력 필드가 없다면 생성 (기본 정보 섹션에)
        var supplierField = document.querySelector('input[name*="supplier"]');
        if (supplierField && supplierField.parentNode) {
            var materialDiv = document.createElement('div');
            materialDiv.className = 'form-group';
            materialDiv.innerHTML = 
                '<label>자재명</label>' +
                '<div class="input-group">' +
                '<input type="text" id="materialSearchInput" class="form-control" placeholder="자재명 2글자 이상 입력하세요">' +
                '<div class="input-group-append">' +
                '<button type="button" class="btn btn-outline-secondary" onclick="searchMaterialSuppliers()">🔍</button>' +
                '</div>' +
                '</div>' +
                '<div id="supplierSearchResults" class="list-group mt-2" style="display:none;"></div>';
            
            supplierField.parentNode.parentNode.appendChild(materialDiv);
            materialInput = document.getElementById('materialSearchInput');
        }
    }
    
    if (materialInput) {
        var searchTimeout;
        
        materialInput.addEventListener('input', function() {
            var query = this.value.trim();
            
            clearTimeout(searchTimeout);
            
            if (query.length < 2) {
                hideSearchResults();
                return;
            }
            
            // 디바운스: 300ms 후 검색
            searchTimeout = setTimeout(function() {
                searchMaterialSuppliers(query);
            }, 300);
        });
        
        // 포커스 잃으면 결과 숨김 (약간의 지연으로 클릭 가능하게)
        materialInput.addEventListener('blur', function() {
            setTimeout(hideSearchResults, 200);
        });
    }
}

function searchMaterialSuppliers(query) {
    if (!query) {
        var input = document.getElementById('materialSearchInput');
        query = input ? input.value.trim() : '';
    }
    
    if (query.length < 2) {
        hideSearchResults();
        return;
    }
    
    // 로딩 표시
    showSearchResults('<div class="list-group-item">검색 중...</div>');
    
    // 서버에서 자재명으로 거래처 검색
    fetch('/api/search-suppliers-by-material?query=' + encodeURIComponent(query))
        .then(function(res) {
            if (!res.ok) throw new Error('검색 실패');
            return res.json();
        })
        .then(function(results) {
            if (!results || results.length === 0) {
                showSearchResults('<div class="list-group-item text-muted">검색 결과가 없습니다.</div>');
                return;
            }
            
            var html = '';
            for (var i = 0; i < results.length; i++) {
                var item = results[i];
                html += 
                    '<a href="#" class="list-group-item list-group-item-action supplier-result-item" ' +
                    'data-supplier-id="' + item.supplierId + '" ' +
                    'data-material-id="' + item.materialId + '" ' +
                    'data-material-name="' + item.materialName + '" ' +
                    'onclick="selectSupplierFromSearch(this)">' +
                    '<div><strong>' + item.supplierName + '</strong></div>' +
                    '<small class="text-muted">' + item.materialName + ' | ' + item.unitPrice.toLocaleString() + '원</small>' +
                    '</a>';
            }
            showSearchResults(html);
        })
        .catch(function(err) {
            console.error('자재 검색 오류:', err);
            showSearchResults('<div class="list-group-item text-danger">검색 중 오류가 발생했습니다.</div>');
        });
}

function selectSupplierFromSearch(element) {
    var supplierId = element.dataset.supplierId;
    var materialId = element.dataset.materialId;
    var materialName = element.dataset.materialName;
    
    // 1. 거래처 선택
    var supplierSelect = document.getElementById('supplierSelect');
    if (supplierSelect) {
        // 옵션이 없으면 추가
        var option = supplierSelect.querySelector('option[value="' + supplierId + '"]');
        if (!option) {
            var supplierName = element.querySelector('strong').textContent;
            option = document.createElement('option');
            option.value = supplierId;
            option.textContent = supplierName;
            supplierSelect.appendChild(option);
        }
        
        supplierSelect.value = supplierId;
        
        // 거래처 변경 이벤트 트리거 (자재 목록 로딩)
        _dispatchChange(supplierSelect);
        
        // 자재 목록 로딩 후 선택하기 위해 잠시 대기
        setTimeout(function() {
            selectMaterialInFirstRow(materialId, materialName);
        }, 500);
    }
    
    // 검색 결과 숨김
    hideSearchResults();
    
    // 검색 입력 필드 클리어
    var searchInput = document.getElementById('materialSearchInput');
    if (searchInput) {
        searchInput.value = materialName;
    }
}

function selectMaterialInFirstRow(materialId, materialName) {
    // 첫 번째 행의 자재 선택
    var firstSelect = document.querySelector('select[name$=".materialId"]');
    if (firstSelect) {
        var option = firstSelect.querySelector('option[value="' + materialId + '"]');
        if (option) {
            firstSelect.value = materialId;
            
            // 자재 선택 이벤트 트리거
            _dispatchChange(firstSelect);
        } else {
            console.warn('자재 옵션을 찾을 수 없습니다:', materialId);
        }
    }
}

function showSearchResults(html) {
    var resultsDiv = document.getElementById('supplierSearchResults');
    if (resultsDiv) {
        resultsDiv.innerHTML = html;
        resultsDiv.style.display = 'block';
    }
}

function hideSearchResults() {
    var resultsDiv = document.getElementById('supplierSearchResults');
    if (resultsDiv) {
        resultsDiv.style.display = 'none';
    }
}

/* ---------------------------------------
 * [9] 초기화
 * ------------------------------------- */
document.addEventListener("DOMContentLoaded", function () {
    // 발주일 자동세팅(필요 시)
    var od = document.getElementById("orderDate");
    if (od && !od.value) {
        od.value = new Date().toISOString().split("T")[0];
    }

    // 페이지에 기본 tr가 있다면 새 구조로 업그레이드
    upgradeExistingRows();
    
    // 자재명 검색 기능 설정
    setTimeout(setupMaterialSearch, 100);

    console.log('발주 관리 시스템 초기화 완료');
});

/* ============================================
 * [호환] 모달(돋보기) 검색 복원 + ID 충돌 방지
 * ============================================ */

// (A) 모달이 있으면 인라인 검색 UI(setupMaterialSearch) 생성 금지 (ID 충돌 예방)
(function () {
  if (typeof window.setupMaterialSearch === 'function') {
    var _origSetup = window.setupMaterialSearch;
    window.setupMaterialSearch = function () {
      // 페이지에 Bootstrap 모달이 존재하면 인라인 검색 UI는 만들지 않음
      if (document.getElementById('supplierSearchModal')) return;
      _origSetup();
    };
  }
})();

// (B) 공용: 공급처 select 엘리먼트 찾기
function _findSupplierSelect() {
  return document.getElementById('supplierSelect')
      || document.querySelector("select[name='order.supplierId']");
}

// (C) 공용: 이벤트 디스패치 (ES5 호환)
function _dispatchChange(el) {
  try {
    var ev;
    if (typeof Event === 'function') {
      ev = new Event('change', { bubbles: true });
    } else if (document.createEvent) {
      ev = document.createEvent('Event');
      ev.initEvent('change', true, true);
    }
    if (ev) el.dispatchEvent(ev);
    else if (el.fireEvent) el.fireEvent('onchange');
  } catch (e) {}
}

// (D) 문자열 이스케이프
function _escHtml(s) {
  s = s == null ? '' : String(s);
  return s.replace(/&/g,'&amp;')
          .replace(/</g,'&lt;')
          .replace(/>/g,'&gt;')
          .replace(/"/g,'&quot;')
          .replace(/'/g,'&#39;');
}
function _escJs(s){ s = s==null?'':String(s); return s.replace(/\\/g,'\\\\').replace(/'/g,"\\'"); }

// (E) 모달 열기
function _openSupplierSearchModal() {
  if (window.jQuery && jQuery.fn && typeof jQuery.fn.modal === 'function') {
    jQuery('#supplierSearchModal').modal('show');
    jQuery('#materialSearchInput').val('');
    jQuery('#supplierSearchResult').empty();
  } else {
    alert('부트스트랩 모달 플러그인이 없어 모달을 열 수 없습니다.');
  }
}

// (F) 모달용 검색 → 표 렌더
function _wireModalSearch() {
  if (!(window.jQuery && jQuery.fn)) return;

  // 돋보기 버튼
  if (document.getElementById('btnSearchSupplier')) {
    jQuery('#btnSearchSupplier').off('click.__legacy').on('click.__legacy', function (e) {
      e.preventDefault();
      _openSupplierSearchModal();
    });
  }

  // 자재명 입력 → AJAX 검색
  if (document.getElementById('materialSearchInput')) {
    jQuery('#materialSearchInput').off('input.__legacy').on('input.__legacy', function () {
      var keyword = jQuery(this).val().trim();
      if (keyword.length < 2) return;

      jQuery.ajax({
        url   : '/material/order/search-suppliers', // ✅ 예전에 쓰던 엔드포인트
        method: 'GET',
        data  : { keyword: keyword },
        success: function (data) {
          var $tbody = jQuery('#supplierSearchResult').empty();
          if (!data || !data.length) {
            $tbody.append('<tr><td colspan="5" class="text-muted">검색 결과가 없습니다.</td></tr>');
            return;
          }
          for (var i = 0; i < data.length; i++) {
            var row = data[i];
            var tr = ''
              + '<tr>'
              + '  <td>' + _escHtml(row.materialName) + '</td>'
              + '  <td>' + _escHtml(row.supplierName) + '</td>'
              + '  <td>' + (Number(row.unitPrice || 0)) + '</td>'
              + '  <td>' + (row.warehouseCode ? _escHtml(row.warehouseCode) : '-') + '</td>'
              + '  <td>'
              + '    <button type="button" class="btn btn-sm btn-primary"'
              + '      onclick="selectSupplier(\'' + _escJs(row.supplierId) + '\', \'' + _escJs(row.supplierName) + '\')">선택</button>'
              + '  </td>'
              + '</tr>';
            $tbody.append(tr);
          }
        },
        error: function () {
          jQuery('#supplierSearchResult').html('<tr><td colspan="5" class="text-danger">검색 중 오류가 발생했습니다.</td></tr>');
        }
      });
    });
  }
}

// (G) [모달에서 선택] → 공급처 select 반영 + 자재 옵션 로딩 + 첫 행 자동 선택(가능 시)
function selectSupplier(supplierId, supplierName) {
  var sel = _findSupplierSelect();
  if (!sel) { alert('공급처 선택 컴포넌트를 찾을 수 없습니다.'); return; }

  // 옵션 없으면 추가
  var exists = false;
  for (var i = 0; i < sel.options.length; i++) {
    if (sel.options[i].value === supplierId) { exists = true; break; }
  }
  if (!exists) {
    var opt = document.createElement('option');
    opt.value = supplierId;
    opt.text  = supplierName || supplierId;
    sel.appendChild(opt);
  }
  sel.value = supplierId;
  _dispatchChange(sel);  // ✅ 기존 ES5 코드의 change 핸들러가 /supplierItem/list 호출하여 materialMap/supplierItemMap 세팅함

  // 모달 닫기
  if (window.jQuery && jQuery.fn && typeof jQuery.fn.modal === 'function') {
    jQuery('#supplierSearchModal').modal('hide');
  }

  // 키워드에 맞는 자재 자동 선택(있으면)
  var kwInput = document.getElementById('materialSearchInput');
  var kw = kwInput ? kwInput.value : '';
  if (kw) {
    // supplier change → fetch가 끝날 시간을 약간 대기
    setTimeout(function () {
      try {
        var chosenId = null;
        for (var k in supplierItemMap) {
          if (!supplierItemMap.hasOwnProperty(k)) continue;
          var nm = (supplierItemMap[k] && supplierItemMap[k].materialName) || '';
          if (nm.indexOf(kw) !== -1) { chosenId = k; break; }
        }
        if (chosenId) {
          var firstSel = document.querySelector('select[name$=".materialId"]');
          if (firstSel) {
            firstSel.value = chosenId;
            _dispatchChange(firstSel); // onMaterialSelect → 단가/창고/수량 힌트 세팅
          }
        }
      } catch (e) {}
    }, 600);
  }
}

// (H) 초기 바인딩
document.addEventListener('DOMContentLoaded', function () {
  _wireModalSearch();
  // jQuery가 없다면 최소한 버튼은 막아두기
  var btn = document.getElementById('btnSearchSupplier');
  if (btn && !(window.jQuery && jQuery.fn && typeof jQuery.fn.modal === 'function')) {
    btn.addEventListener('click', function (e) {
      e.preventDefault ? e.preventDefault() : (e.returnValue = false);
      alert('모달 플러그인이 로드되지 않아 검색 모달을 열 수 없습니다.');
    });
  }
});

//폼 제출 직전, 모든 행을 [0..n-1]로 재인덱싱
function reindexOrderItemRows() {
  var rows = document.querySelectorAll('#itemTable tbody tr');
  var idx = 0;
  for (var r = 0; r < rows.length; r++) {
    var row = rows[r];

    // 자재 미선택 행/수량 0은 스킵(제출 제외)
    var sel = row.querySelector('select[name$=".materialId"]');
    var qty = row.querySelector('input[name$=".orderQuantity"]');
    if (!sel || !sel.value || !qty || Number(qty.value) <= 0) {
    	 // ❗빈 행은 제출되지 않도록 name 제거
    	var junk = row.querySelectorAll('input,select,textarea');
    	  for (var j=0; j<junk.length; j++) {
    	    if (junk[j].name) {
    	      junk[j].setAttribute('data-skip-name', junk[j].name);
    	      junk[j].removeAttribute('name');
    	    }
    	    if (junk[j].required) junk[j].setAttribute('data-was-required','1');
    	    junk[j].required = false;
    	    junk[j].disabled = true; // 제출 검증/페이로드에서 완전 제외
    	  }
      continue;
    }

    // 이 행에 있는 name="orderItems[숫자].xxx" 전부를 [idx]로 교체
    var named = row.querySelectorAll('[name]');
    for (var i = 0; i < named.length; i++) {
      var el = named[i];
      el.name = el.name.replace(/orderItems\[\d+\]/, 'orderItems[' + idx + ']');
    }
    idx++;
  }
}

// 기존 submit 리스너에 가장 먼저 호출
document.addEventListener('DOMContentLoaded', function () {
  var form = document.querySelector('form');
  if (!form) return;

  form.addEventListener('submit', function (e) {
    // 0) 제출 전 강제 재인덱싱
    reindexOrderItemRows();

    // 1) 디버깅: 몇 개가 나가는지 즉시 확인 (개발 중에만)
    // console.log(new FormData(form)); // 필요 시 확인용

    // 2) 기존 유효성 검사/로직 그대로…
    // (너가 이미 갖고 있는 validate 로직 이어서 실행)
  }, true);
});

//제출 버튼 클릭을 가장 먼저 가로채서 재인덱싱
document.addEventListener('click', function(e){
  var t = e.target;
  if (!t || !(t.matches('button[type="submit"]') || t.matches('input[type="submit"]'))) return;
  try { reindexOrderItemRows(); } catch(_) {}
}, true); // capture 단계

//form.submit()이 직접 호출돼도 재인덱싱이 먼저 실행되도록 래핑
(function(){
  var orig = HTMLFormElement.prototype.submit;
  HTMLFormElement.prototype.submit = function(){
    try { reindexOrderItemRows(); } catch(_) {}
    return orig.call(this);
  };
})();

//jQuery가 serialize/serializeArray로 페이로드 만들 때도 제출 직전 재인덱싱
(function(){
  if (!window.jQuery || !jQuery.fn) return;
  var _ser  = jQuery.fn.serialize;
  var _sera = jQuery.fn.serializeArray;
  jQuery.fn.serialize = function(){
    try { reindexOrderItemRows(); } catch(_) {}
    return _ser.call(this);
  };
  jQuery.fn.serializeArray = function(){
    try { reindexOrderItemRows(); } catch(_) {}
    return _sera.call(this);
  };
})();

document.addEventListener('DOMContentLoaded', function () {
	  var form = document.getElementById('orderForm') || document.querySelector('form');
	  var btn  = document.getElementById('btnRegister') 
	          || document.querySelector('button[data-submit], button#register, button.btn-primary[type="button"]');

	  if (form && btn) {
	    btn.addEventListener('click', function (e) {
	      // 버튼이 type="submit" 이든 "button"이든 무조건 내가 주도
	      e.preventDefault ? e.preventDefault() : (e.returnValue = false);
	      try { reindexOrderItemRows(); } catch(_) {}
	      form.submit(); // 우리가 래핑해둔 submit이 한번 더 재인덱싱을 보장
	    }, true); // 캡처 단계에서 가장 먼저 실행
	  }
	});

//⚡ FormData(form) 호출될 때도 먼저 재인덱싱이 실행되도록 래핑
(function () {
  var OrigFD = window.FormData;
  window.FormData = function (arg) {
    try {
      if (arg && arg.tagName === 'FORM') { reindexOrderItemRows(); }
    } catch (e) {}
    return new OrigFD(arg);
  };
  window.FormData.prototype = OrigFD.prototype; // 프로토 유지
})();


/* ---------------------------------------
 * [자재 중복 검사 기능] 
 * ------------------------------------- */

// 자재 선택 시 중복 검사
function checkDuplicateMaterial(selectElement) {
    var selectedId = selectElement.value;
    if (!selectedId) return true; // 빈 선택은 허용

    var currentRow = selectElement.closest('tr');
    var allSelects = document.querySelectorAll('select[name$=".materialId"]');
    var duplicateCount = 0;
    var duplicateRows = [];

    for (var i = 0; i < allSelects.length; i++) {
        var sel = allSelects[i];
        var row = sel.closest('tr');
        
        // 현재 행이 아니고, 같은 자재가 선택되어 있으면
        if (row !== currentRow && sel.value === selectedId) {
            duplicateCount++;
            duplicateRows.push(row);
        }
    }

    if (duplicateCount > 0) {
        // 중복된 자재명 가져오기
        var materialName = selectElement.options[selectElement.selectedIndex].text;
        
        if (confirm(`"${materialName}" 자재가 이미 ${duplicateCount}개 행에서 선택되어 있습니다.\n\n계속 추가하시겠습니까?\n\n※ 동일 자재를 여러 행에 나누어 주문하면 관리가 복잡해질 수 있습니다.`)) {
            return true; // 사용자가 계속하겠다고 함
        } else {
            // 선택 취소
            selectElement.value = '';
            
            // 기존 값들 초기화
            var row = selectElement.closest('tr');
            resetRowToDefault(row);
            
            return false;
        }
    }

    return true; // 중복 없음
}

// 행을 기본값으로 리셋
function resetRowToDefault(row) {
    if (!row) return;

    var qtyInput = row.querySelector("input[name$='.orderQuantity']");
    var unitPriceInput = row.querySelector("input[name$='.unitPrice']");
    var totalInputs = row.querySelectorAll("input[name$='.totalPrice'], input[name$='.visibleTotal']");
    var warehouseInput = row.querySelector("input[name$='.warehouseCode']");
    var hintSmall = row.querySelector("td:nth-child(2) small.text-muted");
    var convSpan = row.querySelector(".js-conv");
    
    // hidden 필드들 초기화
    var hiddenInputs = row.querySelectorAll("input[name$='.unit'], input[name$='.priceUnit'], input[name$='.convToBase']");
    hiddenInputs.forEach(function(input) {
        if (input.name.includes('.unit')) input.value = 'EA';
        else if (input.name.includes('.priceUnit')) input.value = 'EA';
        else if (input.name.includes('.convToBase')) input.value = '1';
    });

    // 수량 관련 초기화
    if (qtyInput) {
        qtyInput.value = '1';
        qtyInput.min = '1';
        qtyInput.dataset.multiple = '1';
        qtyInput.dataset.purchaseUnit = 'EA';
        qtyInput.dataset.priceUnit = 'EA';
        qtyInput.dataset.conv = '1';
        qtyInput.dataset.displayUnit = 'EA';
        qtyInput.dataset.convDisplay = '1';
    }

    // 단가 초기화
    if (unitPriceInput) {
        unitPriceInput.value = '0';
    }

    // 총액 초기화
    totalInputs.forEach(function(input) {
        input.value = '0';
    });

    // 창고 초기화
    if (warehouseInput) {
        warehouseInput.value = '';
    }

    // 힌트 초기화
    if (hintSmall) {
        hintSmall.textContent = '-';
    }

    // 환산량 표시 초기화
    if (convSpan) {
        convSpan.textContent = '0 EA';
    }
}

// 기존 onMaterialSelect 함수를 래핑
var _originalOnMaterialSelect = window.onMaterialSelect;
window.onMaterialSelect = function(selectElement) {
    // 먼저 중복 검사
    if (!checkDuplicateMaterial(selectElement)) {
        return; // 중복이고 사용자가 취소했으면 여기서 중단
    }
    
    // 중복이 없거나 사용자가 계속하겠다고 하면 기존 로직 실행
    if (_originalOnMaterialSelect) {
        _originalOnMaterialSelect(selectElement);
    }
};

// 이벤트 위임도 업데이트
document.addEventListener("change", function (e) {
    var t = e.target || e.srcElement;
    if (!t || !t.matches || !t.matches("select[name$='.materialId']")) return;
    
    // 위의 래핑된 onMaterialSelect가 호출됨
    onMaterialSelect(t);
});

/* ---------------------------------------
 * [통합 요약 기능 - 동일 자재 자동 합계]
 * ------------------------------------- */
function showMaterialSummary() {
    var rows = document.querySelectorAll('#itemTable tbody tr');
    var summary = {}; // materialId -> {name, totalPacks, totalAmount, rows: []}
    
    for (var i = 0; i < rows.length; i++) {
        var row = rows[i];
        var sel = row.querySelector('select[name$=".materialId"]');
        var qtyInput = row.querySelector('input[name$=".orderQuantity"]');
        var totalInput = row.querySelector('input[name$=".totalPrice"], input[name$=".visibleTotal"]');
        
        if (!sel || !sel.value || !qtyInput || Number(qtyInput.value) <= 0) continue;
        
        var materialId = sel.value;
        var materialName = sel.options[sel.selectedIndex].text;
        var packs = Number(qtyInput.value) || 0;
        var amount = Number(totalInput ? totalInput.value : 0) || 0;
        
        if (!summary[materialId]) {
            summary[materialId] = {
                name: materialName,
                totalPacks: 0,
                totalAmount: 0,
                rows: []
            };
        }
        
        summary[materialId].totalPacks += packs;
        summary[materialId].totalAmount += amount;
        summary[materialId].rows.push(i + 1);
    }
    
    // 중복된 자재만 표시
    var duplicates = Object.keys(summary).filter(id => summary[id].rows.length > 1);
    
    if (duplicates.length === 0) {
        alert('중복된 자재가 없습니다.');
        return;
    }
    
    var message = '=== 중복 자재 요약 ===\n\n';
    duplicates.forEach(function(materialId) {
        var item = summary[materialId];
        message += `${item.name}\n`;
        message += `   • 총 수량: ${item.totalPacks.toLocaleString()} PACK\n`;
        message += `   • 총 금액: ${item.totalAmount.toLocaleString()}원\n`;
        message += `   • 행 위치: ${item.rows.join(', ')}번째\n\n`;
    });
    
    message += '\n동일 자재는 하나의 행으로 통합하는 것을 권장합니다.';
    
    alert(message);
}

// 요약 버튼을 발주 항목 테이블 상단에 추가
document.addEventListener('DOMContentLoaded', function() {
    var tableWrapper = document.querySelector('#itemTable');
    if (tableWrapper && tableWrapper.parentNode) {
        var summaryBtn = document.createElement('button');
        summaryBtn.type = 'button';
        summaryBtn.className = 'btn btn-sm btn-info float-right mb-2';
        summaryBtn.innerHTML = '자재 요약';
        summaryBtn.onclick = showMaterialSummary;
        
        tableWrapper.parentNode.insertBefore(summaryBtn, tableWrapper);
    }
});

