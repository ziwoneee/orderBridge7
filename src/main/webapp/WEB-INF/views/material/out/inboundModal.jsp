<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

			<!-- 입고완료건 선택 모달 -->
			<div class="modal fade" id="inboundPickerModal" tabindex="-1" role="dialog" aria-hidden="true">
			  <div class="modal-dialog modal-lg" role="document" style="max-width:1000px;">
			    <div class="modal-content">
			      <div class="modal-header" style="background:#1c355e;color:#fff;">
			        <h4 class="modal-title">입고완료건 선택</h4>
			        <button type="button" class="close" data-dismiss="modal" aria-label="Close" style="color:#fff;"><span>&times;</span></button>
			      </div>
			      <div class="modal-body" style="padding:15px;">
			        <div class="table-responsive">
			          <table class="table table-bordered table-condensed" style="margin-bottom:0;">
			            <thead>
			              <tr>
			                <th class="text-center">입고ID</th>
			                <th class="text-center">발주ID</th>
			                <th class="text-center">입고일</th>
			                <th style="width:90px;"></th>
			              </tr>
			            </thead>
			            <tbody id="inboundPickerBody">
			              <tr><td colspan="4" class="text-center text-muted">불러오는 중...</td></tr>
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
//컨텍스트 루트
window.ctx = window.ctx || '${pageContext.request.contextPath}';

// yyyy-MM-dd
function toYmd(d){ if(!d) return ''; if(typeof d==='string') return d.slice(0,10);
  if(typeof d==='number') return new Date(d).toISOString().slice(0,10);
  if(d && d.time) return new Date(d.time).toISOString().slice(0,10);
  try{ return new Date(d).toISOString().slice(0,10);}catch(e){ return ''; }
}

/* [1] 버튼 → 모달 오픈 */
$(document).on('click', '#btnPickInbound', function(){
  $('#inboundPickerBody').html('<tr><td colspan="4" class="text-center text-muted">불러오는 중...</td></tr>');
  $('#inboundPickerModal').modal('show');
});

/* [2] 모달 열릴 때 목록 로드 */
$('#inboundPickerModal').on('shown.bs.modal', function(){
  loadCompletedInbounds();
});

function loadCompletedInbounds(){
  var $tb = $('#inboundPickerBody').empty().append(
    '<tr><td colspan="4" class="text-center text-muted">불러오는 중...</td></tr>'
  );

  // 서버에서: 입고완료 + 미처리 리스트 반환
  $.getJSON(ctx + '/material/outbound/inbounds', { status:'입고완료', processed:'N' })
    .done(function(list){
      $tb.empty();
      if(!list || list.length===0){
        $tb.append('<tr><td colspan="4" class="text-center text-muted">표시할 입고완료 건이 없습니다.</td></tr>');
        return;
      }
      list.forEach(function(row){
        var tr = $('<tr>').attr('data-inbound-id', row.inboundId)
          .append($('<td class="text-center">').text(row.inboundId || ''))
          .append($('<td class="text-center">').text(row.orderId || '-'))
          .append($('<td class="text-center">').text(toYmd(row.inboundDate)))
          .append($('<td class="text-center" style="width:90px;">').append(
            $('<button>',{type:'button','data-id':row.inboundId})
              .addClass('btn btn-primary btn-xs pick-inbound').text('선택')
          ));
        $tb.append(tr);
      });
    })
    .fail(function(xhr){
      $tb.empty().append('<tr><td colspan="4" class="text-center text-danger">목록을 불러오지 못했습니다.</td></tr>');
      console.error(xhr);
    });
}

/* [3] 선택 → register로 이동 (inbound→workOrder 매핑 시도) */
$(document)
  .off('click.pickInbound', '.pick-inbound')
  .on('click.pickInbound', '.pick-inbound', function(e){
    e.preventDefault();
    var inboundId = $(this).data('id');
    if(!inboundId) return;

    $.get(ctx + '/material/outbound/resolve-workorder', { inboundId: inboundId })
      .done(function(res){
        if(res && res.workOrderId){
          location.href = ctx + '/material/outbound/register?workOrderId=' + encodeURIComponent(res.workOrderId)
                          + '&inboundId=' + encodeURIComponent(inboundId);
        }else{
          location.href = ctx + '/material/outbound/register?inboundId=' + encodeURIComponent(inboundId);
        }
      })
      .fail(function(){
        // 매핑 못 찾아도 inboundId만 들고 가서 화면에서 WO 선택하게
        location.href = ctx + '/material/outbound/register?inboundId=' + encodeURIComponent(inboundId);
      });
  });
</script>
