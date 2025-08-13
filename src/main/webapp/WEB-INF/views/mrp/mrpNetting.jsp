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
	  <div class="card-header">MRP Netting (가용/순소요)</div>
	  <div class="card-body">
	    <div class="form-inline mb-2">
	      <input id="prod" class="form-control mr-2" placeholder="productId 예: FG-001">
	      <input id="qty" class="form-control mr-2" type="number" value="100" step="1" min="1">
	      <button id="btnNet" class="btn btn-primary">조회</button>
	    </div>
	    <table class="table table-sm table-bordered">
	      <thead>
	        <tr>
	          <th>자재ID</th><th>자재명</th><th>단위</th>
	          <th class="text-right">총소요</th>
	          <th class="text-right">재고</th>
	          <th class="text-right">예약</th>
	          <th class="text-right">가용</th>
	          <th class="text-right">순소요</th>
	        </tr>
	      </thead>
	      <tbody id="netBody"></tbody>
	    </table>
	  </div>
	</div>

</body>

<script>
document.getElementById('btnNet').addEventListener('click', async () => {
  const productId = document.getElementById('prod').value.trim();
  const orderQty  = document.getElementById('qty').value || 0;

  const res  = await fetch(`/mrp/netting?productId=${encodeURIComponent(productId)}&orderQty=${orderQty}`);
  const data = await res.json();

  const tbody = document.getElementById('netBody');
  tbody.innerHTML = '';
  (data.items || []).forEach(r => {
    const fmt = v => Number(v ?? 0).toLocaleString('ko-KR');
    const tr = document.createElement('tr');
    tr.innerHTML =
      `<td>${r.materialId}</td>
       <td>${r.materialName ?? '-'}</td>
       <td>${r.unit ?? '-'}</td>
       <td class="text-right">${fmt(r.gross_req)}</td>
       <td class="text-right">${fmt(r.on_hand)}</td>
       <td class="text-right">${fmt(r.reserved)}</td>
       <td class="text-right">${fmt(r.available)}</td>
       <td class="text-right"><strong>${fmt(r.net_req)}</strong></td>`;
    tbody.appendChild(tr);
  });
});
</script>

</html>