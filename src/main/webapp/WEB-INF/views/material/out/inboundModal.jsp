<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- 입고완료건 선택 모달 -->
<div class="modal fade" id="inboundPickerModal" tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog modal-xl" role="document" style="max-width:1400px;">
    <div class="modal-content">
      <div class="modal-header" style="background:#1c355e;color:#fff;">
        <h4 class="modal-title">입고완료건 선택</h4>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close" style="color:#fff;"><span>&times;</span></button>
      </div>
      <div class="modal-body" style="padding:15px;">
        <!-- 입고완료건 목록 -->
        <div class="row">
          <div class="col-md-6">
            <h6>입고완료건 목록</h6>
            <div class="table-responsive">
              <table class="table table-bordered table-condensed" style="margin-bottom:10px;">
                <thead>
                  <tr>
                    <th class="text-center">입고ID</th>
                    <th class="text-center">발주ID</th>
                    <th class="text-center">입고일</th>
                    <th class="text-center">사용상태</th>
                    <th style="width:90px;"></th>
                  </tr>
                </thead>
                <tbody id="inboundPickerBody">
                  <tr><td colspan="5" class="text-center text-muted">불러오는 중...</td></tr>
                </tbody>
              </table>
            </div>
          </div>
          
          <!-- 선택된 입고건의 가용 자재 목록 -->
          <div class="col-md-6">
            <h6>가용 자재 목록 <small class="text-muted">(입고건 선택 시 표시)</small></h6>
            <div class="table-responsive">
              <table class="table table-bordered table-condensed" style="margin-bottom:10px;">
                <thead>
                  <tr>
                    <th>자재명</th>
                    <th>LOT번호</th>
                    <th class="text-center">가용수량</th>
                    <th class="text-center">필요수량</th>
                    <th class="text-center">유통기한</th>
                  </tr>
                </thead>
                <tbody id="availableMaterialsBody">
                  <tr><td colspan="5" class="text-center text-muted">입고건을 선택하세요</td></tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
        
        <!-- 선택된 입고건 정보 -->
        <div id="selectedInboundInfo" class="alert alert-info" style="display:none; margin-top:15px;">
          <strong>선택된 입고건:</strong> <span id="selectedInboundId"></span> 
          | <strong>작업지시:</strong> <span id="selectedWorkOrderId"></span>
          | <strong>총 가용자재:</strong> <span id="totalAvailableMaterials">0</span>종
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" id="btnConfirmInbound" class="btn btn-primary" disabled>출고등록 진행</button>
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
  return n || 5;
}
function infoRowHTML(icon, cls, msg, colspan){
  colspan = colspan || inboundCols();
  return '<tr>'
       +   '<td colspan="' + colspan + '" class="text-center ' + cls + ' py-4">'
       +     '<i class="' + icon + '" style="font-size:24px;"></i>'
       +     '<p class="mt-2 mb-0">' + msg + '</p>'
       +   '</td>'
       + '</tr>';
}
function showLoading($tb, msg, colspan){ $tb.html(infoRowHTML('ti-reload','text-muted', msg || '불러오는 중...', colspan)); }
function showEmpty($tb, msg, colspan){ $tb.html(infoRowHTML('ti-info-alt','text-muted', msg || '데이터가 없습니다.', colspan)); }
function showError($tb, msg, colspan){ $tb.html(infoRowHTML('ti-alert','text-danger', msg || '데이터를 불러오지 못했습니다.', colspan)); }

// 사용상태 배지 생성
function getUsageStatusBadge(status) {
  switch(status) {
    case 'AVAILABLE': return '<span class="badge badge-success">사용가능</span>';
    case 'PARTIALLY_USED': return '<span class="badge badge-warning">부분사용</span>';
    case 'FULLY_USED': return '<span class="badge badge-secondary">완전사용</span>';
    default: return '<span class="badge badge-light">미확인</span>';
  }
}

/* ---------- 전역 변수 ---------- */
var selectedInboundId = null;
var selectedWorkOrderId = null;
var inboundPickerReqToken = 0; // 응답 경합 방지

/* ---------- [1] 버튼 → 모달 오픈 ---------- */
$(document).on('click', '#btnPickInbound', function(){
  var $tb = $('#inboundPickerBody');
  var $matTb = $('#availableMaterialsBody');
  
  showLoading($tb, '불러오는 중...', 5);
  showEmpty($matTb, '입고건을 선택하세요', 5);
  
  // 선택 초기화
  selectedInboundId = null;
  selectedWorkOrderId = null;
  $('#selectedInboundInfo').hide();
  $('#btnConfirmInbound').prop('disabled', true);
  
  $('#inboundPickerModal').modal('show');
});

/* ---------- [2] 모달 열릴 때 목록 로드 ---------- */
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
  showLoading($tb, '불러오는 중...', 5);

  $.getJSON(ctx + '/material/outbound/inbounds', { status:'입고완료', processed:'N' })
    .done(function(list){
      if(myToken !== inboundPickerReqToken || !$('#inboundPickerModal').is(':visible')) return;

      if(!list || list.length === 0){
        showEmpty($tb, '입고완료 건이 없습니다.', 5);
        return;
      }

      var rows = '';
      for(var i=0;i<list.length;i++){
        var row = list[i];
        var inboundId  = row.inboundId || '';
        var orderId    = row.orderId   || '-';
        var inboundYmd = toYmd(row.inboundDate);
        var usageStatus = row.usageStatus || 'AVAILABLE';

        rows += '<tr data-inbound-id="' + inboundId + '" class="inbound-row">'
             +    '<td class="text-center">' + inboundId   + '</td>'
             +    '<td class="text-center">' + orderId     + '</td>'
             +    '<td class="text-center">' + inboundYmd  + '</td>'
             +    '<td class="text-center">' + getUsageStatusBadge(usageStatus) + '</td>'
             +    '<td class="text-center" style="width:90px;">'
             +      '<button type="button" class="btn btn-primary btn-xs select-inbound" data-id="' + inboundId + '">선택</button>'
             +    '</td>'
             +  '</tr>';
      }
      $tb.html(rows);
    })
    .fail(function(xhr){
      console.warn('inbound list load failed', xhr);
      showError($tb, '목록을 불러오지 못했습니다.', 5);
    });
}

/* ---------- [3] 입고건 선택 시 가용 자재 로드 ---------- */
$(document)
  .off('click.selectInbound', '.select-inbound')
  .on('click.selectInbound', '.select-inbound', function(e){
    e.preventDefault();
    var inboundId = $(this).data('id');
    if(!inboundId) return;
    
    // 선택 효과
    $('.inbound-row').removeClass('table-primary');
    $(this).closest('tr').addClass('table-primary');
    
    selectedInboundId = inboundId;
    loadAvailableMaterials(inboundId);
  });

function loadAvailableMaterials(inboundId) {
  var $matTb = $('#availableMaterialsBody');
  showLoading($matTb, '가용 자재 로딩중...', 5);
  
  $.getJSON(ctx + '/material/outbound/available-materials', { inboundId: inboundId })
    .done(function(materials){
      if(!materials || materials.length === 0){
        showEmpty($matTb, '가용한 자재가 없습니다.', 5);
        updateSelectedInfo(inboundId, null, 0);
        return;
      }

      var rows = '';
      var workOrderId = null;
      
      for(var i=0; i<materials.length; i++){
        var mat = materials[i];
        var materialName = mat.materialName || '';
        var lotNo = mat.lotNo || '';
        var availableQty = mat.availableQty || 0;
        var requiredQty = mat.requiredQty || 0;
        var expirationDate = toYmd(mat.expirationDate);
        
        // 첫 번째 자재에서 workOrderId 추출
        if(i === 0 && mat.workOrderId) {
          workOrderId = mat.workOrderId;
        }

        rows += '<tr>'
             +    '<td>' + materialName + '</td>'
             +    '<td class="text-center"><span class="badge badge-light">' + lotNo + '</span></td>'
             +    '<td class="text-center"><strong>' + availableQty + '</strong></td>'
             +    '<td class="text-center">' + requiredQty + '</td>'
             +    '<td class="text-center">' + expirationDate + '</td>'
             +  '</tr>';
      }
      $matTb.html(rows);
      
      updateSelectedInfo(inboundId, workOrderId, materials.length);
    })
    .fail(function(xhr){
      console.error('available materials load failed', xhr);
      showError($matTb, '가용 자재를 불러오지 못했습니다.', 5);
      updateSelectedInfo(inboundId, null, 0);
    });
}

function updateSelectedInfo(inboundId, workOrderId, materialCount) {
  selectedInboundId = inboundId;
  selectedWorkOrderId = workOrderId;
  
  $('#selectedInboundId').text(inboundId);
  $('#selectedWorkOrderId').text(workOrderId || '미확인');
  $('#totalAvailableMaterials').text(materialCount);
  
  $('#selectedInboundInfo').show();
  $('#btnConfirmInbound').prop('disabled', !workOrderId);
}

/* ---------- [4] 출고등록 진행 ---------- */
$(document).on('click', '#btnConfirmInbound', function(){
  if(!selectedInboundId || !selectedWorkOrderId) {
    alert('입고건과 작업지시를 확인할 수 없습니다.');
    return;
  }
  
  // 입고건 사용상태 업데이트 (선택사항)
  updateInboundUsageStatus(selectedInboundId);
  
  // register 페이지로 이동
  location.href = ctx + '/material/outbound/register?workOrderId='
                + encodeURIComponent(selectedWorkOrderId)
                + '&inboundId=' + encodeURIComponent(selectedInboundId);
});

// 입고건 사용상태 업데이트 (백그라운드)
function updateInboundUsageStatus(inboundId) {
  $.post(ctx + '/material/outbound/update-inbound-status', { inboundId: inboundId })
    .done(function(result){
      console.log('입고건 사용상태 업데이트 완료:', result);
    })
    .fail(function(){
      console.warn('입고건 사용상태 업데이트 실패 (무시)');
    });
}

/* ---------- [5] 기존 호환성 유지 (legacy 선택 방식) ---------- */
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