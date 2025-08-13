<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>

	<div class="card">
	  <div class="card-header">BOM 전개(총소요) 조회</div>
	  <div class="card-body">
	    <div class="form-inline mb-2">
	      <input id="prod" class="form-control mr-2" placeholder="productId 예: FG-001">
	      <input id="qty"  class="form-control mr-2" type="number" value="100" step="1" min="1">
	      <button id="btnGross" class="btn btn-primary">조회</button>
	    </div>
	    <table class="table table-sm table-bordered">
	      <thead>
	        <tr><th>자재ID</th><th>자재명</th><th>단위</th><th class="text-right">총소요</th></tr>
	      </thead>
	      <tbody id="grossBody"></tbody>
	    </table>
	  </div>
	</div>
	
</body>



</html>


<script>
document.getElementById('btnGross').addEventListener('click', async function () {
  const productId = document.getElementById('prod').value.trim();
  const orderQty  = Number(document.getElementById('qty').value || 0);

  const res  = await fetch('/mrp/gross/data?productId=' + encodeURIComponent(productId) + '&orderQty=' + orderQty);
  const data = await res.json();

  const tbody = document.getElementById('grossBody');
  tbody.innerHTML = '';

  // 값이 비었으면 '-' 로 표시
  function text(v) { return (v === null || v === undefined || v === '') ? '-' : v; }
  // 숫자 포맷팅
  function fmt(v)  { return Number(v || 0).toLocaleString('ko-KR'); }

  (data.items || []).forEach(function (r) {
    const tr = document.createElement('tr');
    tr.innerHTML =
      '<td>' + text(r.materialId)   + '</td>' +
      '<td>' + text(r.materialName) + '</td>' +
      '<td>' + text(r.unit)         + '</td>' +
      '<td class="text-right">' + fmt(r.gross_req) + '</td>';
    tbody.appendChild(tr);
  });
});
</script>