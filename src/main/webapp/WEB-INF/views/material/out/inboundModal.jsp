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
// 컨텍스트 루트 (JSP EL 사용 OK)
window.ctx = window.ctx || '${pageContext.request.contextPath}';

// yyyy-MM-dd
function toYmd(d){
  if(!d) return '';
  if(typeof d==='string') return d.slice(0,10);
  if(typeof d==='number') return new Date(d).toISOString().slice(0,10);
  if(d && d.time) return new Date(d.time).toISOString().slice(0,10);
  try{ return new Date(d).toISOString().slice(0,10);}catch(e){ return ''; }
}

/* ---------- 공통 UI 헬퍼 (문자열 연결 사용) ---------- */
function inboundCols(){
  var n = $('#inboundPickerModal thead th').length;
  return n || 4;
}
function infoRowHTML(icon, cls, msg){
  return '<tr>'
       +   '<td colspan="' + inboundCols() + '" class="text-center ' + cls + ' py-4">'
       +     '<i class="' + icon + '" style="font-size:24px;"></i>'
       +     '<p class="mt-2 mb-0">' + msg + '</p>'
       +   '</td>'
       + '</tr>';
}
function showLoading($tb, msg){ $tb.html(infoRowHTML('ti-reload','text-muted', msg || '불러오는 중...')); }
function showEmpty($tb, msg){ $tb.html(infoRowHTML('ti-info-alt','text-muted', msg || '입고완료 건이 없습니다.')); }
function showError($tb, msg){ $tb.html(infoRowHTML('ti-alert','text-danger', msg || '목록을 불러오지 못했습니다. 잠시 후 다시 시도하세요.')); }

/* ---------- [1] 버튼 → 모달 오픈 ---------- */
$(document).on('click', '#btnPickInbound', function(){
  var $tb = $('#inboundPickerBody');
  showLoading($tb);
  $('#inboundPickerModal').modal('show');
});

/* ---------- [2] 모달 열릴 때 목록 로드 ---------- */
var inboundPickerReqToken = 0; // 응답 경합 방지

$('#inboundPickerModal')
  .on('shown.bs.modal', function(){
    loadCompletedInbounds();
  })
  .on('hidden.bs.modal', function(){
    inboundPickerReqToken++; // 모달 닫히면 이후 응답 무시
  });

function loadCompletedInbounds(){
  var $tb = $('#inboundPickerBody');
  var myToken = ++inboundPickerReqToken;
  showLoading($tb);

  $.getJSON(ctx + '/material/outbound/inbounds', { status:'입고완료', processed:'N' })
    .done(function(list){
      if(myToken !== inboundPickerReqToken || !$('#inboundPickerModal').is(':visible')) return;

      if(!list || list.length === 0){
        showEmpty($tb);
        return;
      }

      var rows = '';
      for(var i=0;i<list.length;i++){
        var row = list[i];
        var inboundId  = row.inboundId || '';
        var orderId    = row.orderId   || '-';
        var inboundYmd = toYmd(row.inboundDate);

        rows += '<tr data-inbound-id="' + inboundId + '">'
             +    '<td class="text-center">' + inboundId   + '</td>'
             +    '<td class="text-center">' + orderId     + '</td>'
             +    '<td class="text-center">' + inboundYmd  + '</td>'
             +    '<td class="text-center" style="width:90px;">'
             +      '<button type="button" class="btn btn-primary btn-xs pick-inbound" data-id="' + inboundId + '">선택</button>'
             +    '</td>'
             +  '</tr>';
      }
      $tb.html(rows);
    })
    .fail(function(xhr){
	   	console.warn('inbound list load failed', xhr);
	   	showEmpty($tb, '입고완료 건이 없습니다.');
    });
}

/* ---------- [3] 선택 → register로 이동 (inbound→workOrder 매핑) ---------- */
$(document)
  .off('click.pickInbound', '.pick-inbound')
  .on('click.pickInbound', '.pick-inbound', function(e){
    e.preventDefault();
    var inboundId = $(this).data('id');
    if(!inboundId) return;

    $.get(ctx + '/material/outbound/resolve-workorder', { inboundId: inboundId })
      .done(function(res){
        if(res && res.workOrderId){
          location.href = ctx + '/material/outbound/register?workOrderId='
                          + encodeURIComponent(res.workOrderId)
                          + '&inboundId=' + encodeURIComponent(inboundId);
        }else{
          location.href = ctx + '/material/outbound/register?inboundId=' + encodeURIComponent(inboundId);
        }
      })
      .fail(function(){
        location.href = ctx + '/material/outbound/register?inboundId=' + encodeURIComponent(inboundId);
      });
  });
</script>
