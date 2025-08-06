// materialOutbound.js

// 출고 처리
function processOut(outboundId) {
  if (confirm("출고처리 하시겠습니까?")) {
    location.href = '/material/out/process?outboundId=' + outboundId;
  }
}

// 상세 페이지 이동 (단순 링크 이동용)
function viewDetail(outboundId) {
  location.href = '/material/out/detail?outboundId=' + outboundId;
}

// Ajax 호출로 출고 상세 불러오기
function loadOutboundDetail(outboundId) {
  $.ajax({
    url: '/material/outbound/detail',
    method: 'GET',
    data: { outboundId: outboundId },
    success: function(response) {
      console.log("Ajax 응답 데이터:", response);
      openOutboundModal(response);
    },
    error: function() {
      alert('상세 정보를 불러오는 데 실패했습니다.');
    }
  });
}

// 상세 모달 열기 및 데이터 채우기
function openOutboundModal(data) {
  $('#workOrderNo').text(data.workOrderNo);
  $('#dueDate').text(formatDate(data.dueDate));
  $('#workOrderDate').text(formatDate(data.workOrderDate));
  $('#lineId').text(data.lineId);
  $('#outboundId').text(data.outboundId);
  $('#modalStatus').text(data.status);
  $('#outboundDate').text(formatDate(data.outboundDate));
  $('#handledBy').text(data.handledBy);

  $('#stockInfo').empty();
  data.materialList.forEach(function(item) {
    const stockStatus = item.stockQty >= item.requiredQty
      ? '<span class="badge badge-success">정상</span>'
      : '<span class="badge badge-danger">부족</span>';

    $('#stockInfo').append(`
      <tr>
        <td>${item.materialId}</td>
        <td>${item.materialName}</td>
        <td>${item.requiredQty}</td>
        <td>${item.stockQty}</td>
        <td>${stockStatus}</td>
      </tr>
    `);
  });

  $('#outboundDetailModal').modal('show');
}

// 날짜 포맷 변환 (yyyy-MM-dd)
function formatDate(timestamp) {
  if (!timestamp) return '--';
  const date = new Date(timestamp);
  if (isNaN(date.getTime())) return '--';
  const yyyy = date.getFullYear();
  const mm = ('0' + (date.getMonth() + 1)).slice(-2);
  const dd = ('0' + date.getDate()).slice(-2);
  return `${yyyy}-${mm}-${dd}`;
}

function processOutbound(outboundId, requiredQty, stockQty, btn) {
	  if (stockQty < requiredQty) {
	    alert("실재고가 부족하여 출고처리 불가능합니다.");
	    return;
	  }

	  if (!confirm("출고처리를 진행하시겠습니까?")) return;

	  $.ajax({
	    type: "POST",
	    url: "/material/outbound/process",
	    data: { outboundId: outboundId },
	    contentType: "application/x-www-form-urlencoded; charset=UTF-8",
	    success: function (result) {
	      alert("출고처리 완료되었습니다.");

	      // 버튼 비활성화 및 UI 변경
	      btn.disabled = true;
	      btn.classList.remove("btn-outline-primary");
	      btn.classList.add("btn-secondary");
	      btn.innerText = "출고완료";

	      // 상태 뱃지도 변경
	      const row = btn.closest("tr");
	      const statusCell = row.querySelector("td:nth-child(3)");
	      statusCell.innerHTML = '<span class="badge badge-success">출고완료</span>';
	    },
	    error: function (xhr, status, error) {
	      alert(xhr.responseText);
	      console.error(error);
	    }
	  });
	}
