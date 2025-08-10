<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="modal fade" id="orderModal" tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
      <div class="modal-header" style="background:#1c355e;color:#fff;">
        <h4 class="modal-title">작업지시 선택</h4>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close" style="color:#fff;">
          <span>&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <div class="table-responsive">
          <table class="table table-bordered table-condensed">
            <thead>
              <tr>
                <th>지시번호</th>
                <th>제품</th>
                <th>라인</th>
                <th class="text-right">지시수량</th>
                <th>납기일</th>
                <th style="width:90px;"></th>
              </tr>
            </thead>
            <tbody id="orderBody">
              <!-- JS가 여기를 채웁니다 -->
              <tr><td colspan="6" class="text-center text-muted">불러오는 중...</td></tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">닫기</button>
      </div>
    </div>
  </div>
</div>

<script>
  // 컨텍스트 루트 (없으면 빈 문자열)
  window.ctx = window.ctx || '${pageContext.request.contextPath}';

  // 날짜를 YYYY-MM-DD로 안전 변환
  function toYmd(d) {
    if (!d) return '';
    if (typeof d === 'string') return d.slice(0, 10);
    if (typeof d === 'number') return new Date(d).toISOString().slice(0, 10);
    if (typeof d === 'object' && d.time) return new Date(d.time).toISOString().slice(0, 10);
    try { return new Date(d).toISOString().slice(0, 10); } catch(e){ return ''; }
  }

  // [A] “작업지시 불러오기” 버튼 → 모달 열기
  $(document).on('click', '#btnLoadOrder', function () {
    $('#orderModal').modal('show');
  });

  // [B] 모달이 열릴 때 Ajax로 대기 작업지시 목록 로드
  $('#orderModal').on('shown.bs.modal', function () {
    loadWaitingOrders();
  });

  function loadWaitingOrders() {
    var $tbody = $('#orderBody').empty().append(
      '<tr><td colspan="6" class="text-center text-muted">불러오는 중...</td></tr>'
    );

    $.getJSON(window.ctx + '/material/outbound/order-list')
      .done(function (list) {
        $tbody.empty();
        if (!list || list.length === 0) {
          $tbody.append('<tr><td colspan="6" class="text-center text-muted">대기 작업지시가 없습니다.</td></tr>');
          return;
        }

        list.forEach(function (wo) {
          var orderId = wo.orderId || '';
          var product = (wo.productName && wo.productName.length) ? wo.productName : (wo.productId || '');
          var lineId  = wo.lineId || '';
          var qty     = (wo.orderQty != null ? wo.orderQty : '');
          var ymd     = toYmd(wo.dueDate);

          var $tr = $('<tr>').attr('data-order-no', orderId)
            .append($('<td>').text(orderId))
            .append($('<td>').text(product))
            .append($('<td>').text(lineId))
            .append($('<td>').addClass('text-right').text(qty))
            .append($('<td>').text(ymd))
            .append(
              $('<td>').addClass('text-center').css('width','90px').append(
                $('<button>', {
                  type: 'button',
                  'data-order-no': orderId
                }).addClass('btn btn-primary btn-xs btn-select-wo').text('선택')
              )
            );

          $tbody.append($tr);
        });
      })
      .fail(function () {
        $tbody.empty().append('<tr><td colspan="6" class="text-center text-danger">목록을 불러오지 못했습니다.</td></tr>');
      });
  }

  // [C] 모달 안 “선택” 버튼 → register 페이지로 이동
  //   동적 요소이므로 document에 위임 바인딩 + 네임스페이스로 중복 방지
  $(document)
    .off('click.orderBridge', 'button.btn-select-wo')
    .on('click.orderBridge', 'button.btn-select-wo', function (e) {
      e.preventDefault();

      var orderNo = $(this).data('orderNo') || $(this).attr('data-order-no') || $(this).closest('tr').attr('data-order-no');
      if (!orderNo) { alert('지시번호를 찾을 수 없어요.'); return; }

      location.href = window.ctx + '/material/outbound/register?workOrderId=' + encodeURIComponent(orderNo);
    });
</script>
