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
	  <div class="card-header">발주 추천 초안</div>
	  <div class="card-body">
	    <div class="form-inline mb-2">
	      <input id="prodR" class="form-control mr-2" placeholder="productId 예: FG-001">
	      <input id="qtyR"  class="form-control mr-2" type="number" value="100" min="1">
	      <button id="btnR" class="btn btn-primary">조회</button>
	    </div>
	    <table class="table table-sm table-bordered">
	      <thead>
	        <tr>
	          <th>자재ID</th><th>자재명</th><th>순소요</th>
	          <th>공급사</th><th class="text-right">단가</th><th class="text-right">리드타임(일)</th>
	          <th class="text-right">계획수량</th><th>예상입고일</th><th class="text-right">총금액</th><th>상태</th>
	        </tr>
	      </thead>
	      <tbody id="tbodyR"></tbody>
	    </table>
	  </div>
	</div>
	
	<button id="btnCreatePO" class="btn btn-success">발주 초안 생성</button>
<script>
// 추천 조회 버튼
document.getElementById('btnR').addEventListener('click', function () {
  var p = document.getElementById('prodR').value.trim();
  var q = Number(document.getElementById('qtyR').value || 0);

  fetch('/mrp/recommend-po?productId=' + encodeURIComponent(p) + '&orderQty=' + q)
    .then(function (r) { return r.json(); })
    .then(function (d) {
      var tb = document.getElementById('tbodyR');
      tb.innerHTML = '';

      function text(v) { return (v === null || v === undefined || v === '') ? '-' : v; }
      function fmt(v)  { return Number(v || 0).toLocaleString('ko-KR'); }

      (d.items || []).forEach(function (x) {
        var tr = document.createElement('tr');
        tr.innerHTML =
          '<td>' + text(x.materialId) + '</td>' +
          '<td>' + text(x.materialName) + '</td>' +
          '<td class="text-right">' + fmt(x.net_req) + '</td>' +
          '<td>' + text(x.supplier_id) + '</td>' +
          '<td class="text-right">' + fmt(x.unit_price) + '</td>' +
          '<td class="text-right">' + text(x.lead_days) + '</td>' +
          '<td class="text-right"><strong>' + fmt(x.planned_qty) + '</strong></td>' +
          '<td>' + text(x.expected_inbound_date) + '</td>' +
          '<td class="text-right">' + fmt(x.total_cost) + '</td>' +
          '<td>' + text(x.plan_status) + '</td>';
        tb.appendChild(tr);
      });
    })
    .catch(function (err) {
      console.error(err);
      alert('추천 조회 중 오류가 발생했습니다.');
    });
});

// 발주 초안 생성 버튼
document.getElementById('btnCreatePO').addEventListener('click', function () {
  var rows  = Array.prototype.slice.call(document.querySelectorAll('#tbodyR tr'));

  function cellText(tr, sel) {
    var el = tr.querySelector(sel);
    return el ? el.textContent.trim() : '';
  }
  function cellNumber(tr, sel) {
    var el = tr.querySelector(sel);
    var raw = el ? el.textContent.replace(/,/g, '').trim() : '0';
    var n = Number(raw || '0');
    return isNaN(n) ? 0 : n;
  }

  // 테이블에서 데이터 추출
  var items = rows.map(function (tr) {
    return {
      materialId:          cellText(tr, 'td:nth-child(1)'),
      supplierId:          cellText(tr, 'td:nth-child(4)') || null,
      qty:                 cellNumber(tr, 'td:nth-child(7)'),
      unitPrice:           cellNumber(tr, 'td:nth-child(5)'),
      expectedInboundDate: cellText(tr, 'td:nth-child(8)') || null
    };
  }).filter(function (it) { return it.supplierId && it.qty > 0; });

  if (!items.length) {
    alert('생성할 아이템이 없습니다.');
    return;
  }

  fetch('/mrp/po/draft', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ items: items })
  })
  .then(function (r) { return r.json(); })
  .then(function (data) {
    var ids = (data.created || []).map(function (x) { return x.orderId; }).join(', ');
    alert('생성된 발주: ' + ids);
  })
  .catch(function (err) {
    console.error(err);
    alert('발주 생성 중 오류가 발생했습니다.');
  });
});
</script>
	
</body>
</html>