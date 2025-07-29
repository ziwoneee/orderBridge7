let itemIndex = 1;             // 항목 인덱스 (동적 필드 네이밍용)
let materialMap = {};          // 자재 정보 저장 (materialId → 자재정보)

/**
 * 항목 행 추가 함수
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
      <input type="number" name="orderItems[${itemIndex}].quantity" class="form-control" min="1" onchange="calculateTotal(this)" required>
    </td>
    <td>
      <input type="number" name="orderItems[${itemIndex}].unitPrice" class="form-control" min="0" step="0.01" onchange="calculateTotal(this)" required>
    </td>
    <td>
      <input type="number" class="form-control" readonly value="0">
      <input type="hidden" name="orderItems[${itemIndex}].totalPrice" value="0">
    </td>
    <td>
      <input type="text" name="orderItems[${itemIndex}].storageLocation" class="form-control" readonly>
    </td>
    <td>
      <button type="button" class="btn btn-sm btn-danger" onclick="removeRow(this)">삭제</button>
    </td>
  `;

  tbody.appendChild(row);
  itemIndex++;
}

/**
 * 행 삭제 함수
 */
function removeRow(btn) {
  btn.closest("tr").remove();
}

/**
 * 수량 또는 단가 변경 시 총금액 계산 - 수정된 버전
 */
function calculateTotal(input) {
  const row = input.closest("tr");
  const qtyInput = row.querySelector("input[name$='.quantity']");
  const priceInput = row.querySelector("input[name$='.unitPrice']");
  
  const qty = parseFloat(qtyInput.value) || 0;
  const price = parseFloat(priceInput.value) || 0;

  const visibleTotal = row.querySelector("input[type='number']:not([name])");
  const hiddenTotal = row.querySelector("input[type='hidden'][name$='.totalPrice']");

  const result = qty * price;

  if (visibleTotal) visibleTotal.value = result.toFixed(2);
  if (hiddenTotal) hiddenTotal.value = result.toFixed(2);
  
  console.log(`총금액 계산: ${qty} × ${price} = ${result}`); // 디버깅용
}

/**
 * 자재 select의 <option> 문자열 생성 함수
 */
function getMaterialOptions() {
  return Object.values(materialMap)
    .map(mat => `<option value="${mat.materialId}">${mat.materialName}</option>`)
    .join("");
}

/**
 * 거래처 선택 시 해당 자재 목록 불러오기 (materialMap에 저장 + option 반영)
 */
document.querySelector("select[name='order.supplierId']").addEventListener("change", function () {
  const supplierId = this.value;
  materialMap = {};  // 초기화

  // 기존 select 모두 초기화
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
        materialMap[item.materialId] = item;  // 저장
      });

      const optionHTML = getMaterialOptions();

      // 현재 있는 모든 항목 행에 옵션 삽입
      document.querySelectorAll("select[name$='.materialId']").forEach(select => {
        select.innerHTML += optionHTML;
      });
    })
    .catch(error => {
      console.error('거래처 자재 목록 조회 실패:', error);
      alert('거래처 자재 목록을 불러오는데 실패했습니다.');
    });
});

/**
 * 자재 선택 시 단가 + 입고창고 자동입력
 */
document.addEventListener("change", function (e) {
  if (e.target.matches("select[name$='.materialId']")) {
    const selectedId = e.target.value;
    const item = materialMap[selectedId];
    if (!item) return;

    const row = e.target.closest("tr");
    const unitPriceInput = row.querySelector("input[name$='.unitPrice']");
    const locationInput = row.querySelector("input[name$='.storageLocation']");

    if (unitPriceInput) unitPriceInput.value = item.unitPrice || '';
    if (locationInput) locationInput.value = item.storageLocation || '';

    calculateTotal(unitPriceInput); // 총금액도 갱신
  }
});

// ✅ 폼 제출 전 데이터 검증 및 디버깅 (새로 추가)
document.addEventListener('DOMContentLoaded', function() {
  const form = document.querySelector('form');
  if (form) {
    form.addEventListener('submit', function(e) {
      console.log('=== 폼 제출 데이터 검증 ===');
      
      // 기본 정보 체크
      const supplierId = document.querySelector('select[name="order.supplierId"]').value;
      const expectedDate = document.querySelector('input[name="order.expectedArrivedDate"]').value;
      const createdBy = document.querySelector('input[name="order.createdBy"]').value;
      
      console.log('거래처:', supplierId);
      console.log('납기일:', expectedDate);
      console.log('담당자:', createdBy);
      
      if (!supplierId || !expectedDate || !createdBy) {
        alert('기본 정보를 모두 입력해주세요.');
        e.preventDefault();
        return false;
      }
      
      // 발주 항목 체크
      const materialSelects = document.querySelectorAll("select[name$='.materialId']");
      let validItemCount = 0;
      
      materialSelects.forEach((select, index) => {
        if (select.value) {
          const row = select.closest('tr');
          const qty = row.querySelector("input[name$='.quantity']").value;
          const price = row.querySelector("input[name$='.unitPrice']").value;
          const total = row.querySelector("input[type='hidden'][name$='.totalPrice']").value;
          
          console.log(`항목 ${index + 1}:`, {
            materialId: select.value,
            quantity: qty,
            unitPrice: price,
            totalPrice: total
          });
          
          if (qty && price && parseFloat(qty) > 0 && parseFloat(price) >= 0) {
            validItemCount++;
          }
        }
      });
      
      console.log('유효한 항목 수:', validItemCount);
      
      if (validItemCount === 0) {
        alert('최소 1개 이상의 유효한 발주 항목을 입력해주세요.');
        e.preventDefault();
        return false;
      }
      
      console.log('=== 폼 제출 진행 ===');
    });
  }
});