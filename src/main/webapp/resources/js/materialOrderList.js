// 공통 유틸
window.formatYMD = d => !d ? '-' : new Date(d).toISOString().slice(0,10);
window.calcRowTotal = (row) => {
  const qty = +((row.querySelector("input[name$='.orderQuantity']")||{}).value||0);
  const unit= +((row.querySelector("input[name$='.unitPrice']")||{}).value||0);
  return Math.round(qty*unit);
};


/**
 * 발주 초안에서 요청
 */ 
$(document).on('click', '.btnSubmitOrder', function(){
  const orderId = $(this).data('id');
  if (!confirm('이 발주 초안을 "요청" 상태로 전환할까요?')) return;

  $.post({
	url: ctx + '/material/order/submit',
    data: { orderId },
    success: function(res){
      alert(res.message || '발주요청 완료');
      // 화면 갱신: 상태 뱃지/텍스트 교체 + 버튼 제거
      const $tr = $('.btnSubmitOrder[data-id="'+orderId+'"]').closest('tr');
      $tr.find('td').eq(6).html('<span class="badge badge-warning">요청</span>'); // 7번째 컬럼이 상태일 때
      $tr.find('td').eq(7).html('<a href="${pageContext.request.contextPath}/material/order/view?orderId='+orderId+'" class="btn btn-outline-secondary btn-sm">상세</a>');
    },
    error: function(xhr){
      const msg = (xhr.responseJSON && xhr.responseJSON.message) ? xhr.responseJSON.message : '처리 중 오류';
      alert('실패: ' + msg);
    }
  });
});
