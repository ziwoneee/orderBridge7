/* ---------------------------------------
 * [1] 항목 행 추가 관련 변수 및 함수
 * ------------------------------------- */

// 행 인덱스 (동적 네이밍용)
let itemIndex = 1;

// 거래처 선택 시 불러온 자재정보 저장용 (materialId → 자재 정보 객체)
let materialMap = {};

/**
 * 발주 항목 행 추가 함수
 */
function addItemRow() {
  const tbody = document.querySelector("#itemTable tbody");

  const row = document.createElement("tr");
  row.innerHTML = `
    <td>
      <select name="orderItems[${itemIndex}].materialId" class="form-control">
        <option value="">선택</option>
        ${getMaterialOptions()}
      </select>
    </td>
    <td>
      <input type="number" name="orderItems[${itemIndex}].orderQuantity" class="form-control" min="1" onchange="calculateTotal(this)" required>
    </td>
    <td>
      <input type="number" name="orderItems[${itemIndex}].unitPrice" class="form-control" min="0" step="0.01" onchange="calculateTotal(this)" required>
    </td>
    <td>
      <input type="number" class="form-control" readonly value="0">
      <input type="hidden" name="orderItems[${itemIndex}].totalPrice" value="0">
    </td>
    <td>
      <input type="text" name="orderItems[${itemIndex}].warehouseCode" class="form-control" readonly>
    </td>
    <td>
      <button type="button" class="btn btn-sm btn-danger" onclick="removeRow(this)">삭제</button>
    </td>
  `;

  tbody.appendChild(row);
  itemIndex++;
}

/**
 * 항목 행 삭제
 */
function removeRow(btn) {
  btn.closest("tr").remove();
}

/**
 * 자재 option HTML 생성 함수
 */
function getMaterialOptions() {
  return Object.values(materialMap)
    .map(mat => `<option value="${mat.materialId}">${mat.materialName}</option>`)
    .join("");
}


/* ---------------------------------------
 * [2] 거래처 → 자재 목록 동적 로딩
 * ------------------------------------- */

/**
 * 거래처 선택 시, 해당 거래처의 자재 목록(materialMap)에 저장
 * → 모든 자재 select 옵션 업데이트
 */
document.querySelector("select[name='order.supplierId']").addEventListener("change", function () {
  const supplierId = this.value;
  materialMap = {};

  // 모든 자재 select 초기화
  document.querySelectorAll("select[name$='.materialId']").forEach(select => {
    select.innerHTML = '<option value="">선택</option>';
  });

  if (!supplierId) return;

  fetch(`/supplierItem/list?supplierId=${supplierId}`)
    .then(res => {
      if (!res.ok) throw new Error('Network response was not ok');
      return res.json();
    })
    .then(data => {
      data.forEach(item => {
        materialMap[item.materialId] = item;
      });

      const optionHTML = getMaterialOptions();

      // 현재 등록된 모든 행에 option 갱신
      document.querySelectorAll("select[name$='.materialId']").forEach(select => {
        select.innerHTML += optionHTML;
      });
    })
    .catch(error => {
      console.error('거래처 자재 목록 조회 실패:', error);
      alert('거래처 자재 목록을 불러오는데 실패했습니다.');
    });
});


/* ---------------------------------------
 * [3] 자재 선택 → 단가/입고창고 자동 입력
 * ------------------------------------- */
document.addEventListener("change", function (e) {
  if (e.target.matches("select[name$='.materialId']")) {
    const selectedId = e.target.value;
    const item = materialMap[selectedId];
    if (!item) return;

    const row = e.target.closest("tr");
    const unitPriceInput = row.querySelector("input[name$='.unitPrice']");
    const locationInput = row.querySelector("input[name$='.warehouseCode']");

    if (unitPriceInput) unitPriceInput.value = item.unitPrice || '';
    if (locationInput) locationInput.value = item.warehouseCode || '';

    calculateTotal(unitPriceInput); // 단가 변경에 따른 총금액 갱신
  }
});


/* ---------------------------------------
 * [4] 수량 or 단가 변경 시 총금액 자동 계산
 * ------------------------------------- */
function calculateTotal(input) {
  const row = input.closest("tr");
  const qtyInput = row.querySelector("input[name$='.orderQuantity']");
  const priceInput = row.querySelector("input[name$='.unitPrice']");
  
  const qty = parseFloat(qtyInput.value) || 0;
  const price = parseFloat(priceInput.value) || 0;

  const visibleTotal = row.querySelector("input[type='number']:not([name])");
  const hiddenTotal = row.querySelector("input[type='hidden'][name$='.totalPrice']");

  const result = qty * price;

  if (visibleTotal) visibleTotal.value = result.toFixed(2);
  if (hiddenTotal) hiddenTotal.value = result.toFixed(2);
}


/* ---------------------------------------
 * [5] 폼 제출 전 유효성 검사 + 디버깅
 * ------------------------------------- */
document.addEventListener('DOMContentLoaded', function () {
  const form = document.querySelector('form');
  if (form) {
    form.addEventListener('submit', function (e) {
      console.log('=== 폼 제출 데이터 검증 ===');

      // 기본 정보 확인
      const supplierId = document.querySelector('select[name="order.supplierId"]').value;
      const expectedDate = document.querySelector('input[name="order.expectedArrivedDate"]').value;
      const createdBy = document.querySelector('input[name="order.createdBy"]').value;

      if (!supplierId || !expectedDate || !createdBy) {
        alert('기본 정보를 모두 입력해주세요.');
        e.preventDefault();
        return false;
      }

      // 발주 항목 유효성 검사
      const materialSelects = document.querySelectorAll("select[name$='.materialId']");
      let validItemCount = 0;

      materialSelects.forEach((select, index) => {
        if (select.value) {
          const row = select.closest('tr');
          const qty = row.querySelector("input[name$='.orderQuantity']").value;
          const price = row.querySelector("input[name$='.unitPrice']").value;

          if (qty && price && parseFloat(qty) > 0 && parseFloat(price) >= 0) {
            validItemCount++;
          }
        }
      });

      if (validItemCount === 0) {
        alert('최소 1개 이상의 유효한 발주 항목을 입력해주세요.');
        e.preventDefault();
        return false;
      }

      console.log('=== 폼 제출 진행 ===');
    });
  }
});


/* ---------------------------------------
 * [6] 자재 → 거래처 검색 모달 (Ajax 검색 & 선택 반영)
 * ------------------------------------- */

// 모달 열기
$('#btnSearchSupplier').on('click', function () {
  $('#supplierSearchModal').modal('show');
  $('#materialSearchInput').val('');
  $('#supplierSearchResult').empty();
});

// 자재명 입력 → 거래처 목록 Ajax 조회
$('#materialSearchInput').on('input', function () {
  const keyword = $(this).val().trim();
  if (keyword.length < 2) return;

  $.ajax({
    url: '/material/order/search-suppliers',
    method: 'GET',
    data: { keyword },
    success: function (data) {
      const tbody = $('#supplierSearchResult').empty();
      if (data.length === 0) {
        tbody.append(`<tr><td colspan="5">검색 결과가 없습니다.</td></tr>`);
        return;
      }

      data.forEach(row => {
        const tr = `
          <tr>
            <td>${row.materialName}</td>
            <td>${row.supplierName}</td>
            <td>${row.unitPrice}</td>
            <td>${row.warehouseCode || '-'}</td>
            <td>
              <button type="button" class="btn btn-sm btn-primary"
                      onclick="selectSupplier('${row.supplierId}', '${row.supplierName}')">선택</button>
            </td>
          </tr>
        `;
        tbody.append(tr);
      });
    }
  });
});

//선택 시 거래처 select에 반영 + 항목 자동 세팅
function selectSupplier(supplierId, supplierName) {
  const $select = $('#supplierSelect');

  // 거래처 select에 option이 없다면 추가
  if ($select.find(`option[value="${supplierId}"]`).length === 0) {
    $select.append(`<option value="${supplierId}">${supplierName}</option>`);
  }

  // select 값 설정 및 change 이벤트 트리거
  $select.val(supplierId).change();

  // === [신규 추가] 거래처의 자재 목록 조회 Ajax ===
  $.ajax({
    url: '/material/order/supplier-items',
    method: 'GET',
    data: {
        supplierId: supplierId,
        keyword: $('#materialSearchInput').val() // <- 자재명 검색어
      },
    success: function (items) {
      const tbody = $('#itemTable tbody');
      tbody.empty(); // 기존 항목 제거

      if (items.length === 0) {
        tbody.append(`<tr><td colspan="6">해당 거래처의 공급 품목이 없습니다.</td></tr>`);
        return;
      }

      // 항목 렌더링
      items.forEach((item, index) => {
        const row = `
          <tr>
            <td><input type="hidden" name="orderItems[${index}].materialId" value="${item.materialId}">
                <input type="text" class="form-control" value="${item.materialName}" readonly></td>
            <td><input type="number" name="orderItems[${index}].quantity" class="form-control" value="1" required></td>
            <td><input type="number" name="orderItems[${index}].unitPrice" class="form-control" value="${item.unitPrice}" readonly></td>
            <td><input type="number" name="orderItems[${index}].totalPrice" class="form-control" value="${item.unitPrice}" readonly></td>
            <td><input type="text" name="orderItems[${index}].warehouseCode" class="form-control" value="${item.warehouseCode}" readonly></td>
            <td><button type="button" class="btn btn-danger btn-sm">삭제</button></td>
          </tr>
        `;
        tbody.append(row);
      });
    },
    error: function () {
      alert('거래처 자재 목록 조회 중 오류가 발생했습니다.');
    }
  });

  // 모달 닫기
  $('#supplierSearchModal').modal('hide');
}
