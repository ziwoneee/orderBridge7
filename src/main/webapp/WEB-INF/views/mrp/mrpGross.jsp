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

<script>
document.getElementById('btnGross').addEventListener('click', async () => {
  const productId = document.getElementById('prod').value.trim();
  const orderQty  = document.getElementById('qty').value || 0;

  const res  = await fetch(`/mrp/gross?productId=${encodeURIComponent(productId)}&orderQty=${orderQty}`);
  const data = await res.json();

  const tbody = document.getElementById('grossBody');
  tbody.innerHTML = '';
  (data.items || []).forEach(r => {
    const tr = document.createElement('tr');
    tr.innerHTML =
      `<td>${r.materialId}</td>
       <td>${r.materialName ?? '-'}</td>
       <td>${r.unit ?? '-'}</td>
       <td class="text-right">${Number(r.gross_req).toLocaleString('ko-KR')}</td>`;
    tbody.appendChild(tr);
  });
});
</script>

</html>
