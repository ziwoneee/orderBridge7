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



//[작업지시서 불러오기] 버튼 클릭 시 모달 열기
function openOrderModal() {
  $('#orderModal').modal('show');
  loadOrderList(); // 목록 불러오기
}

// 작업지시서 목록 Ajax로 불러오기
function loadOrderList() {
  $.ajax({
    url: "/material/outbound/order-list",
    method: "GET",
    success: function(data) {
      const tbody = $("#orderTableBody").empty();

      if (data.length === 0) {
        tbody.append("<tr><td colspan='6' class='text-center'>대기 중인 작업지시서가 없습니다.</td></tr>");
        return;
      }

      data.forEach(order => {
        const row = `
          <tr>
            <td>${order.orderId}</td>
            <td>${order.productId}</td>
            <td>${order.lineId}</td>
            <td>${order.orderQty}</td>
            <td>${order.remarks || '-'}</td>
            <td>
              <button class="btn btn-sm btn-outline-primary" onclick="selectOrder('${order.orderId}', '${order.productId}', '${order.lineId}', ${order.orderQty}, '${order.remarks || ''}')">
                선택
              </button>
            </td>
          </tr>
        `;
        tbody.append(row);
      });
    },
    error: function() {
      alert("작업지시서를 불러오지 못했습니다.");
    }
  });
}

// 작업지시서 선택 시 등록 폼에 채우기
function selectOrder(orderId, productId, lineId, qty, remarks) {
  $("#selectedOrderId").val(orderId);
  $("#selectedProductId").val(productId);
  $("#selectedLineId").val(lineId);
  $("#selectedQty").val(qty);
  $("#selectedRemarks").val(remarks);
  $("#orderModal").modal('hide');
}
