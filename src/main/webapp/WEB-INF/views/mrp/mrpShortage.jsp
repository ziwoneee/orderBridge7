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
	  <div class="card-header">부족분 리스트</div>
	  <div class="card-body">
	    <div class="form-inline mb-2">
	      <input id="prodS" class="form-control mr-2" placeholder="productId 예: FG-001">
	      <input id="qtyS"  class="form-control mr-2" type="number" value="100" min="1">
	      <button id="btnS" class="btn btn-primary">조회</button>
	    </div>
	    <table class="table table-sm table-bordered">
	      <thead>
	        <tr>
	          <th>자재ID</th><th>자재명</th><th>단위</th>
	          <th class="text-right">총소요</th><th class="text-right">재고</th>
	          <th class="text-right">예약</th><th class="text-right">가용</th>
	          <th class="text-right">순소요</th>
	        </tr>
	      </thead>
	      <tbody id="tbodyS"></tbody>
	    </table>
	  </div>
	</div>

</body>

<script>
document.getElementById('btnS').addEventListener('click', function () {
  var p = document.getElementById('prodS').value.trim();
  var q = Number(document.getElementById('qtyS').value || 0);

  fetch('/mrp/shortage?productId=' + encodeURIComponent(p) + '&orderQty=' + q)
    .then(function (res) { return res.json(); })
    .then(function (d) {
      var tb = document.getElementById('tbodyS');
      tb.innerHTML = '';

      function text(v) { return (v === null || v === undefined || v === '') ? '-' : v; }
      function fmt(v)  { return Number(v || 0).toLocaleString('ko-KR'); }

      (d.items || []).forEach(function (x) {
        var tr = document.createElement('tr');
        tr.innerHTML =
          '<td>' + text(x.materialId)   + '</td>' +
          '<td>' + text(x.materialName) + '</td>' +
          '<td>' + text(x.unit)         + '</td>' +
          '<td class="text-right">' + fmt(x.gross_req)  + '</td>' +
          '<td class="text-right">' + fmt(x.on_hand)    + '</td>' +
          '<td class="text-right">' + fmt(x.reserved)   + '</td>' +
          '<td class="text-right">' + fmt(x.available)  + '</td>' +
          '<td class="text-right"><strong>' + fmt(x.net_req) + '</strong></td>';
        tb.appendChild(tr);
      });
    })
    .catch(function (err) {
      console.error(err);
      alert('조회 중 오류가 발생했습니다.');
    });
});
</script>


</html>