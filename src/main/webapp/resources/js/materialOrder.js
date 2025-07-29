// materialOrder.js

  let itemIndex = 1;

  function addItemRow() {
    const tbody = document.querySelector("#itemTable tbody");
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>
        <select name="items[\${itemIndex}].materialId" class="form-control">
          <option value="">선택</option>
          ${document.querySelector("select[name='items[0].materialId']").innerHTML}
        </select>
      </td>
      <td><input type="number" name="items[\${itemIndex}].quantity" class="form-control" onchange="calculateTotal(this)" required></td>
      <td><input type="number" name="items[\${itemIndex}].unitPrice" class="form-control" onchange="calculateTotal(this)" required></td>
      <td><input type="number" name="items[\${itemIndex}].totalPrice" class="form-control" readonly></td>
      <td><input type="text" name="items[\${itemIndex}].storageLocation" class="form-control"></td>
      <td><button type="button" class="btn btn-sm btn-danger" onclick="removeRow(this)">삭제</button></td>
    `;
    tbody.appendChild(row);
    itemIndex++;
  }

  function removeRow(btn) {
    btn.closest("tr").remove();
  }

  function calculateTotal(input) {
    const row = input.closest("tr");
    const qty = row.querySelector("input[name$='quantity']").value;
    const price = row.querySelector("input[name$='unitPrice']").value;
    const total = row.querySelector("input[name$='totalPrice']");
    total.value = qty && price ? qty * price : '';
  }

  
  /*JS: 거래처 선택 시 자재 목록 동적 로딩*/
  document.querySelector("select[name='supplierId']").addEventListener("change", function () {
	  const supplierId = this.value;

	  fetch(`/supplierItem/list?supplierId=${supplierId}`)
	    .then(res => res.json())
	    .then(data => {
	      const selects = document.querySelectorAll("select[name$='.materialId']");
	      selects.forEach(select => {
	        // 기존 옵션 삭제
	        select.innerHTML = '<option value="">선택</option>';
	        // 새로운 옵션 추가
	        data.forEach(item => {
	          const opt = document.createElement("option");
	          opt.value = item.materialId;
	          opt.textContent = item.materialName;
	          select.appendChild(opt);
	        });
	      });
	    });
	});
