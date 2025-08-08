// 날짜만 포맷 (YYYY-MM-DD)
function formatDate(dateString) {
  if (!dateString) return '-';
  const date = new Date(dateString);
  if (isNaN(date)) return dateString;

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

// 시간만 포맷 (HH:MM:SS)
function formatTime(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString);
  if (isNaN(date)) return '';
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  return `${hours}:${minutes}:${seconds}`;
}



$(document).on('click', '.open-lot-modal', function () {
  const productId = $(this).data('productid');     // 화면 출력용으로 유지
  const productName = $(this).data('product');
  const lotNo = $(this).data('lot');

  console.log("🚀 productId:", productId);
  console.log("🚀 lotNo:", lotNo);

  if (!lotNo) {
    alert("LOT 번호가 없습니다.");
    return;
  }

  // 모달 타이틀 출력
  $('#modal-product-name-title').text(productName);
  $('#modal-lot-no-title').text(lotNo);

  // LOT 상세 Ajax 조회 (productId 제거)
  openLotHistoryModal(lotNo, productId);  // 전달 순서 바꿈 (lotNo만 요청에 사용)
});


function openLotHistoryModal(lotNo, productId) {
  $.ajax({
    url: '/product/transaction',
    method: 'GET',
    data: { lot: lotNo },  // ✅ product 파라미터 제거
    success: function (response) {
      console.log("✅ 서버 응답:", response);
      console.log("✅ 유통기한 확인:", response.expireDate);


      $('#modal-lot-no').text(lotNo);
      $('#modal-product-name').text(productId || '-'); // 화면 표시용

      // 입출고 수치 출력 (response가 객체가 아니라 리스트일 경우 제거 가능)
      $('#modal-inboundQty').text(response.inboundQty || 0);
      $('#modal-totalOutboundQty').text(response.totalOutboundQty || 0);
      $('#modal-reservedQty').text(response.reservedQty || 0);
      $('#modal-availableQty').text(response.availableQty || 0);
      $('#modal-expireDate').text(response.expireDate ? formatDate(response.expireDate) : '-');

      // 테이블 렌더링
      const history = response.history || [];

      $('#lotHistoryTableBody').empty();

   // ✅ 데이터가 없으면 안내 문구 출력
      if (!history || history.length === 0) {
    	  $('#lotHistoryEmpty').removeClass('d-none');  // 안내 문구 보이기
    	  $('#lotHistoryModal').modal('show');
    	  return;
    	}


      // ✅ 데이터가 있을 경우 안내 문구 숨기고 테이블 출력
      $('#lotHistoryEmpty').addClass('d-none');
      // 테이블에 입출고 이력 추가
      history.forEach(entry => {
    	  // 예약/예약취소는 무시
    	  if (entry.type === 'RESERVE' || entry.type === '예약' ||
    	      entry.type === 'CANCEL_RESERVE' || entry.type === '예약취소') {
    	    return;
    	  }

    	  let badgeHtml = '';
    	  switch (entry.type) {
    	    case 'INBOUND':
    	    case '입고':
    	      badgeHtml = '<span class="badge badge-success">입고</span>';
    	      break;
    	    case 'OUTBOUND':
    	    case '출고':
    	      badgeHtml = '<span class="badge badge-danger">출고</span>';
    	      break;
    	    case 'CANCEL':
    	    case '취소':
    	      badgeHtml = '<span class="badge badge-warning">취소</span>';
    	      break;
    	    default:
    	      badgeHtml = entry.type;
    	  }

    	  // ✅ 구분(type)에 따라 표 중간 열(수주번호 칸)에 다른 번호 표시
    	  let idText = '-';
    	  if (entry.type === 'OUTBOUND' || entry.type === '출고') {
    	    idText = entry.outboundId || '-';
    	  } else if (entry.type === 'INBOUND' || entry.type === '입고') {
    	    idText = entry.inboundId || '-';
    	  } else if (entry.type === 'CANCEL' || entry.type === '취소') {
    	    idText = entry.clOrderId || '-';
    	  }

    	  const row = `
    	    <tr>
    	      <td>
    	        ${formatDate(entry.regDate)}
    	        <i class="bi bi-clock text-muted mx-1"></i>
    	        <span class="text-muted" style="font-size:0.9em;">${formatTime(entry.regDate)}</span>
    	      </td>
    	      <td>${entry.clientName || '-'}</td>
    	      <td>${idText}</td>        
    	      <td>${entry.qty}</td>
    	      <td>${badgeHtml}</td>
    	    </tr>
    	  `;
    	  $('#lotHistoryTableBody').append(row);
    	});


      $('#lotHistoryModal').modal('show');
    },
    
    error: function () {
      alert("LOT 상세 정보를 불러오는데 실패했습니다.");
    }
  });
}
