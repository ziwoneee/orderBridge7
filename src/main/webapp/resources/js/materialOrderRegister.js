/* ---------------------------------------
 * [1] 공통 상태
 * ------------------------------------- */
let itemIndex = 1;                     // 동적 네이밍
let materialMap = {};                  // materialId -> item
let supplierItemMap = {};              // materialId -> item

/* ---------------------------------------
 * [2] 행 생성
 * ------------------------------------- */
function addItemRowFromMaterial(item) {
  const tbody = document.querySelector("#itemTable tbody");
  const idx = itemIndex++;

  const purchaseUnit = (item.unit || 'EA').toUpperCase();            // 구매단위
  const priceUnit    = (item.priceUnit || purchaseUnit).toUpperCase(); // 단가단위
  const conv         = Number(item.convToPriceUnit) || 1;              // 환산
  const minQ         = Number(item.minOrderQty) || 1;
  const mul          = Number(item.orderMultiple) || 1;

  const row = document.createElement("tr");
  row.innerHTML = `
    <td>
      <select name="orderItems[${idx}].materialId" class="form-control">
        <option value="${item.materialId}">${item.materialName}</option>
      </select>
      <input type="hidden" name="orderItems[${idx}].unit" value="${purchaseUnit}">
      <input type="hidden" name="orderItems[${idx}].priceUnit" value="${priceUnit}">
      <input type="hidden" name="orderItems[${idx}].convToPriceUnit" value="${conv}">
    </td>
    <td>
      <input type="number" name="orderItems[${idx}].orderQuantity"
             class="form-control"
             value="${Math.max(minQ, mul)}" min="${minQ}" step="1"
             data-multiple="${mul}"
             data-purchase-unit="${purchaseUnit}"
             data-price-unit="${priceUnit}"
             data-conv="${conv}"
             oninput="enforceMultiple(this)" onchange="rowRecalc(this)" required>
      <small class="text-muted d-block">
        ${purchaseUnit} 1개 ≈ ${conv.toLocaleString()} ${priceUnit}
        ${minQ>1?` | 최소 ${minQ}개`:''}${mul>1?` | ${mul}개 배수`:''}
      </small>
    </td>
    <td>
      <input type="number" name="orderItems[${idx}].unitPrice"
             class="form-control" value="${item.unitPrice||0}" min="0" step="1"
             onchange="rowRecalc(this)" required>
    </td>
    <td>
      <input type="number" name="orderItems[${idx}].visibleTotal" class="form-control" readonly value="0">
      <input type="hidden" name="orderItems[${idx}].totalPrice" value="0">
      <div class="small text-muted mt-1"><span class="js-conv">0</span></div>
    </td>
    <td>
      <input type="text" name="orderItems[${idx}].warehouseCode" class="form-control"
             value="${item.warehouseCode||''}" readonly>
    </td>
    <td>
      <button type="button" class="btn btn-sm btn-danger" onclick="removeRow(this)">삭제</button>
    </td>
  `;
  tbody.appendChild(row);

  // 바로 한 번 계산
  rowRecalc(row.querySelector("input[name$='.unitPrice']"));
}

function addItemRow() {
  // 빈 행(사용자가 직접 선택하는 행) 추가
  const tbody = document.querySelector("#itemTable tbody");
  const idx = itemIndex++;

  const row = document.createElement("tr");
  row.innerHTML = `
    <td>
      <select name="orderItems[${idx}].materialId" class="form-control">
        <option value="">선택</option>
        ${getMaterialOptions()}
      </select>
      <input type="hidden" name="orderItems[${idx}].unit" value="">
      <input type="hidden" name="orderItems[${idx}].priceUnit" value="">
      <input type="hidden" name="orderItems[${idx}].convToPriceUnit" value="1">
    </td>
    <td>
      <input type="number" name="orderItems[${idx}].orderQuantity"
             class="form-control" min="1" step="1"
             data-multiple="1"
             data-purchase-unit="EA"
             data-price-unit="EA"
             data-conv="1"
             oninput="enforceMultiple(this)" onchange="rowRecalc(this)" required>
      <small class="text-muted d-block">-</small>
    </td>
    <td>
      <input type="number" name="orderItems[${idx}].unitPrice"
             class="form-control" min="0" step="1" onchange="rowRecalc(this)" required>
    </td>
    <td>
      <input type="number" name="orderItems[${idx}].visibleTotal" class="form-control" readonly value="0">
      <input type="hidden" name="orderItems[${idx}].totalPrice" value="0">
      <div class="small text-muted mt-1"><span class="js-conv">0</span></div>
    </td>
    <td>
      <input type="text" name="orderItems[${idx}].warehouseCode" class="form-control" readonly>
    </td>
    <td>
      <button type="button" class="btn btn-sm btn-danger" onclick="removeRow(this)">삭제</button>
    </td>
  `;
  tbody.appendChild(row);
}

/* ---------------------------------------
 * [3] 기본 행 업그레이드(첫 행도 새 구조로)
 * ------------------------------------- */
function upgradeExistingRows() {
  document.querySelectorAll("#itemTable tbody tr").forEach(tr => {
    // 이미 업그레이드 된 행은 패스
    if (tr.querySelector("input[name$='.convToPriceUnit']")) return;

    // 숨김필드 주입
    const idx = (itemIndex++); // 이름 충돌 피하려고 새 인덱스 배정
    const materialSel = tr.querySelector("select[name$='.materialId']");
    const matCell = tr.children[0];

    // material 셀을 새 구조로 교체
    matCell.innerHTML = `
      <select name="orderItems[${idx}].materialId" class="form-control">
        <option value="">선택</option>
        ${getMaterialOptions()}
      </select>
      <input type="hidden" name="orderItems[${idx}].unit" value="">
      <input type="hidden" name="orderItems[${idx}].priceUnit" value="">
      <input type="hidden" name="orderItems[${idx}].convToPriceUnit" value="1">
    `;

    // 수량 셀 보수
    const qtyCell = tr.children[1];
    qtyCell.innerHTML = `
      <input type="number" name="orderItems[${idx}].orderQuantity"
             class="form-control" min="1" step="1"
             data-multiple="1"
             data-purchase-unit="EA"
             data-price-unit="EA"
             data-conv="1"
             oninput="enforceMultiple(this)" onchange="rowRecalc(this)" required>
      <small class="text-muted d-block">-</small>
    `;

    // 단가/총액/창고 셀 name 보정
    tr.children[2].querySelector("input").setAttribute("name", `orderItems[${idx}].unitPrice`);
    tr.children[3].innerHTML = `
      <input type="number" name="orderItems[${idx}].visibleTotal" class="form-control" readonly value="0">
      <input type="hidden" name="orderItems[${idx}].totalPrice" value="0">
      <div class="small text-muted mt-1"><span class="js-conv">0</span></div>
    `;
    tr.children[4].querySelector("input").setAttribute("name", `orderItems[${idx}].warehouseCode`);
  });
}

/* ---------------------------------------
 * [4] 거래처 변경 → 아이템 로딩
 * ------------------------------------- */
document.querySelector("#supplierSelect").addEventListener("change", function () {
  const supplierId = this.value;
  materialMap = {};
  supplierItemMap = {};

  // 옵션 초기화
  document.querySelectorAll("select[name$='.materialId']").forEach(sel => {
    sel.innerHTML = '<option value="">선택</option>';
  });
  if (!supplierId) return;

  fetch(`/supplierItem/list?supplierId=${supplierId}`)
    .then(res => { if (!res.ok) throw new Error('Network not ok'); return res.json(); })
    .then(data => {
      data.forEach(item => {
        // 서버에서 필드명 통일(없을 경우 방어)
        const normalized = {
          materialId: item.materialId,
          materialName: item.materialName,
          warehouseCode: item.warehouseCode || '',
          unit: (item.unit || 'EA').toUpperCase(),
          priceUnit: (item.priceUnit || item.unit || 'EA').toUpperCase(),
          convToPriceUnit: Number(item.convToPriceUnit || item.convToBase || item.packQty || 1),
          unitPrice: Number(item.unitPrice || 0),
          minOrderQty: Number(item.minOrderQty || 1),
          orderMultiple: Number(item.orderMultiple || 1),
        };
        materialMap[normalized.materialId] = normalized;
        supplierItemMap[normalized.materialId] = normalized;
      });

      // 기존 행 업그레이드 + 옵션 채우기
      upgradeExistingRows();
      const optionHTML = getMaterialOptions();
      document.querySelectorAll("select[name$='.materialId']").forEach(sel => {
        sel.innerHTML = '<option value="">선택</option>' + optionHTML;
      });
    })
    .catch(err => {
      console.error(err);
      alert('거래처 자재 목록을 불러오지 못했습니다.');
    });
});

/* ---------------------------------------
 * [5] 자재 선택 → 단가/환산 바인딩
 * ------------------------------------- */
document.addEventListener("change", function (e) {
  if (!e.target.matches("select[name$='.materialId']")) return;

  // 중복 방지
  const selectedId = e.target.value;
  const chosen = Array.from(document.querySelectorAll("select[name$='.materialId']"))
    .filter(s => s !== e.target)
    .map(s => s.value);
  if (chosen.includes(selectedId)) {
    alert("이미 선택된 자재입니다.");
    e.target.value = "";
    return;
  }

  const item = supplierItemMap[selectedId] || materialMap[selectedId];
  if (!item) return;

  const row = e.target.closest("tr");
  const unitPriceInput = row.querySelector("input[name$='.unitPrice']");
  const locationInput  = row.querySelector("input[name$='.warehouseCode']");
  const unitHidden     = row.querySelector("input[name$='.unit']");
  const priceUnitHidden= row.querySelector("input[name$='.priceUnit']");
  const convHidden     = row.querySelector("input[name$='.convToPriceUnit']");
  const qtyInput       = row.querySelector("input[name$='.orderQuantity']");
  const hintSmall      = row.querySelector("td:nth-child(2) small.text-muted");

  // 값 주입
  unitPriceInput.value = (item.unitPrice !== undefined && item.unitPrice !== null)
  ? item.unitPrice
  : '';
  locationInput.value    = item.warehouseCode || '';
  unitHidden.value       = item.unit;
  priceUnitHidden.value  = item.priceUnit;
  convHidden.value       = item.convToPriceUnit;

  // 수량 메타
  qtyInput.min = String(item.minOrderQty || 1);
  qtyInput.dataset.multiple      = String(item.orderMultiple || 1);
  qtyInput.dataset.purchaseUnit  = item.unit;
  qtyInput.dataset.priceUnit     = item.priceUnit;
  qtyInput.dataset.conv          = String(item.convToPriceUnit);

  if (!qtyInput.value) qtyInput.value = Math.max(item.minOrderQty||1, item.orderMultiple||1);

  if (hintSmall) {
    const convStr = (item.convToPriceUnit || 1).toLocaleString();
    let hint = `${item.unit} 1개 ≈ ${convStr} ${item.priceUnit}`;
    if ((item.minOrderQty||1) > 1) hint += ` | 최소 ${item.minOrderQty}개`;
    if ((item.orderMultiple||1) > 1) hint += ` | ${item.orderMultiple}개 배수`;
    hintSmall.textContent = hint;
  }

  rowRecalc(unitPriceInput);
});

/* ---------------------------------------
 * [6] 계산/보정
 * ------------------------------------- */
function enforceMultiple(el) {
  const mul = parseInt(el.dataset.multiple || '1', 10) || 1;
  const min = parseInt(el.min || '1', 10) || 1;
  let v = parseInt(el.value || '0', 10) || 0;

  if (v < min) v = min;
  if (mul > 1) {
    const rem = (v - min) % mul;
    if (rem !== 0) v = v - rem + mul; // 올림
  }
  el.value = v;
}

function rowRecalc(input) {
  const row = input.closest('tr'); if (!row) return;

  const qtyInput   = row.querySelector("input[name$='.orderQuantity']");
  const priceInput = row.querySelector("input[name$='.unitPrice']");
  const hiddenTot  = row.querySelector("input[type='hidden'][name$='.totalPrice']");
  const visTot     = row.querySelector("input[name$='.visibleTotal']");
  const convSpan   = row.querySelector(".js-conv");

  let q = parseInt(qtyInput.value || '0', 10);
  const min = parseInt(qtyInput.min || '1', 10) || 1;
  if (!Number.isInteger(q) || q < min) { q = min; qtyInput.value = q; }

  const p = Number(priceInput.value) || 0;

  const purchaseUnit = (qtyInput.dataset.purchaseUnit || 'EA').toUpperCase();
  const priceUnit    = (qtyInput.dataset.priceUnit || purchaseUnit).toUpperCase();
  const conv         = Number(qtyInput.dataset.conv || '1') || 1;

  // 가격단위 기준 수량
  const qtyForPricing = (priceUnit !== purchaseUnit) ? (q * conv) : q;

  const total = Math.round(qtyForPricing * p);
  if (hiddenTot) hiddenTot.value = total;
  if (visTot)    visTot.value    = total;

  if (convSpan) {
    const converted = qtyForPricing;
    convSpan.textContent = `${converted.toLocaleString()} ${priceUnit}`;
  }
}

/* ---------------------------------------
 * [7] 공용 유틸
 * ------------------------------------- */
function removeRow(btn) { btn.closest("tr").remove(); }

function getMaterialOptions() {
  return Object.values(supplierItemMap).map(it =>
    `<option value="${it.materialId}">${it.materialName}</option>`
  ).join('');
}

/* ---------------------------------------
 * [8] 초기화
 * ------------------------------------- */
document.addEventListener("DOMContentLoaded", function () {
  // 발주일 자동세팅(필요 시)
  const od = document.getElementById("orderDate");
  if (od) od.value = new Date().toISOString().split("T")[0];

  // 페이지에 기본 tr가 있다면 새 구조로 업그레이드
  upgradeExistingRows();
});
