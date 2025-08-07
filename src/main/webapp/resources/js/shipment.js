// ✅ 전체 선택
function toggleAll(source) {
  const checkboxes = document.querySelectorAll("input[name='clOrderIds']");
  checkboxes.forEach(cb => {
    if (!cb.disabled) cb.checked = source.checked;
  });
}

// ✅ 탭에 따라 검색 영역 표시 제어
document.addEventListener('DOMContentLoaded', function () {
  const currentTab = new URLSearchParams(window.location.search).get('tab') || 'pending';
  const searchArea = document.getElementById('searchArea');

  if (searchArea) {
    searchArea.style.display = (currentTab === 'completed' || currentTab === 'reservation') ? 'block' : 'none';
  }
});

// ✅ 수주건 예약
function reserveStock(clOrderId) {
  if (confirm("이 수주건의 재고를 예약하시겠습니까?")) {
    location.href = "/shipment/reserve?clOrderId=" + clOrderId;
  }
}

// ✅ 예약 및 예약 해지
function toggleReservation(clOrderId, isReserved) {
  const action = isReserved ? "예약을 해지" : "재고를 예약";
  const url = isReserved
    ? "/shipment/unreserve?clOrderId=" + clOrderId
    : "/shipment/reserve?clOrderId=" + clOrderId;

  if (confirm("이 수주건의 " + action + "하시겠습니까?")) {
    location.href = url;
  }
}

// ✅ 출하 처리 전 확인
function confirmShipment() {
  const checked = document.querySelectorAll("input[name='clOrderIds']:checked");
  if (checked.length === 0) {
    alert("출하할 수주건을 선택해주세요.");
    return false;
  }

  return confirm("선택한 수주건을 출하 처리하시겠습니까?");
}

// ✅ 예약 상세 모달 열기
$(document).on('click', '.btn-reservation-detail', function () {
  const lotNo = $(this).data('lot');
  const clOrderId = $(this).data('order');

  $.ajax({
    url: '/shipment/reservation/detail',
    method: 'GET',
    data: {
      lotNo: lotNo,
      clOrderId: clOrderId
    },
    success: function (data) {
      $('#modal-lot-no').text(data.lotNo);
      $('#modal-product-name').text(data.productName);
      $('#modal-cl-order-id').text(data.clOrderId);
      $('#modal-client-name').text(data.clientName);
      $('#modal-reserved-qty').text(data.reservedQty);
      $('#modal-reserved-at').text(data.reservedAtFormatted || data.reservedAt);
      $('#modal-expire-date').text(data.expireDate);
      $('#modal-current-stock').text(data.currentStock);
      $('#modal-delivery-date').text(data.deliveryDate);

      $('#reservationDetailModal').modal('show');
    },
    error: function () {
      alert('예약 상세 정보를 불러오는 데 실패했습니다.');
    }
  });
});
