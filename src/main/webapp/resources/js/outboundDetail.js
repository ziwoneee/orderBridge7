// ✅ 날짜 포맷 함수
function formatDate(timestamp) {
  const date = new Date(timestamp);
  const yyyy = date.getFullYear();
  const mm = ('0' + (date.getMonth() + 1)).slice(-2);
  const dd = ('0' + date.getDate()).slice(-2);
  return `${yyyy}-${mm}-${dd}`;
}

$(document).ready(function () {
  // 모달 열기 버튼 클릭 시
  $('#outboundTable').on('click', 'button[data-toggle="modal"]', function () {
    const outboundId = $(this).data('id');

    $.ajax({
      url: '/product/outbound/detail',
      method: 'GET',
      data: { outboundId: outboundId },
      success: function (data) {
        // ✅ 기본 필드
        $('#detail-outboundId').text(data.outboundId || '');
        $('#detail-productName').text(data.productName || '');
        $('#detail-lotNo').text(data.lotNo || '');
        $('#detail-outboundQty').text(data.outboundQty || '');

        // ✅ 날짜 포맷 적용
        const formattedDate = data.outboundDate ? formatDate(data.outboundDate) : '';
        $('#detail-outboundDate').text(formattedDate);

        $('#detail-clientName').text(data.clientName || '');
        $('#detail-manager').text(data.manager || '');
        $('#detail-remark').text(data.remark || '');

        // ✅ 제목용 제품명, 출하ID 삽입
        $('#detail-productName-title').text(data.productName || '');
        $('#detail-outboundId-title').text(data.outboundId || '');
        $('#detail-clientName-title').text(data.clientName || '');

        // ✅ 송장번호 표시
        $('#detail-trackingNumber').text(data.trackingNumber || '');

        // ✅ 모달 열기
        $('#outboundDetailModal').modal('show');
      },
      error: function () {
        alert('상세정보를 불러오는데 실패했습니다.');
      }
    });
  });
});
