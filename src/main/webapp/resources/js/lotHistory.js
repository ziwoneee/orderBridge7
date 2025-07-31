$(document).on('click', '.open-lot-modal', function () {
  const productId = $(this).data('productid');     // ✅ productId 추출
  const productName = $(this).data('product');
  const lotNo = $(this).data('lot');

  // 모달 타이틀 출력
  $('#modal-product-name-title').text(productName);
  $('#modal-lot-no-title').text(lotNo);

  // LOT 상세 Ajax 조회
  openLotHistoryModal(productId, lotNo);  // ✅ productId 함께 전달
});


function openLotHistoryModal(productId, lotNo) {
	  $.ajax({
	    url: '/product/transaction',
	    method: 'GET',
	    data: { product: productId, lot: lotNo },  // ✅ Controller 매핑 파라미터 이름과 일치
	    success: function (response) {
	      console.log("✅ 서버 응답:", response);

	      $('#modal-lot-no').text(lotNo);
	      $('#modal-product-name').text(productId); // 필요시 이름 출력

	      // 입출고 수치 출력
	      $('#modal-inboundQty').text(response.inboundQty || 0);
	      $('#modal-totalOutboundQty').text(response.totalOutboundQty || 0);
	      $('#modal-reservedQty').text(response.reservedQty || 0);
	      $('#modal-availableQty').text(response.availableQty || 0);
	      $('#modal-expireDate').text(response.expireDate ? formatDate(response.expireDate) : '-');

	      // 테이블 렌더링
	      const history = response;
	      $('#lotHistoryTableBody').empty();

	      if (!history || history.length === 0) {
	        $('#lotHistoryEmpty').removeClass('d-none');
	        return;
	      }

	      $('#lotHistoryEmpty').addClass('d-none');
	      history.forEach(entry => {
	        const row = `
	          <tr>
	            <td>${formatDate(entry.regDate)}</td>
	            <td>${entry.type}</td>
	            <td>${entry.qty}</td>
	            <td>${entry.clientName || '-'}</td>
	          </tr>`;
	        $('#lotHistoryTableBody').append(row);
	      });

	      $('#lotHistoryModal').modal('show');
	    },
	    error: function () {
	      alert("LOT 상세 정보를 불러오는데 실패했습니다.");
	    }
	  });
	}
