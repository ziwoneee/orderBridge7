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
        <div class="row">
          <!-- 왼쪽: 입고완료건 목록 (다중선택) -->
          <div class="col-md-6">
            <h6>입고완료건 목록</h6>
            <div class="table-responsive">
              <table class="table table-bordered table-condensed" style="margin-bottom:10px;">
                <thead>
                  <tr>
                    <th style="width:36px" class="text-center">
                      <input type="checkbox" id="inb-check-all">
                    </th>
                    <th class="text-center">입고ID</th>
                    <th class="text-center">발주ID</th>
                    <th class="text-center">입고일</th>
                    <th class="text-center">사용상태</th>
                  </tr>
                </thead>
                <tbody id="inboundPickerBody">
                  <tr><td colspan="5" class="text-center text-muted">불러오는 중...</td></tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 오른쪽: 선택한 입고건들의 가용 자재 집계 -->
          <div class="col-md-6">
            <h6>가용 자재 목록 <small class="text-muted">(선택한 입고건 합산)</small></h6>
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

        <!-- 선택 요약 -->
        <div id="selectedInboundInfo" class="alert alert-info" style="display:none; margin-top:15px;">
          <strong>선택된 입고건:</strong> <span id="selectedInboundId">-</span>
          | <strong>작업지시:</strong> <span id="selectedWorkOrderId">미확인</span>
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
// 컨텍스트 루트
window.ctx = window.ctx || '${pageContext.request.contextPath}';

// 날짜 포맷
function toYmd(d){
  if(!d) return '';
  if(typeof d==='string') return d.slice(0,10);
  if(typeof d==='number') return new Date(d).toISOString().slice(0,10);
  if(d && d.time) return new Date(d.time).toISOString().slice(0,10);
  try{ return new Date(d).toISOString().slice(0,10);}catch(e){ return ''; }
}

// 공용 메시지 행
function infoRowHTML(icon, cls, msg, colspan){
  return '<tr>'
       +   '<td colspan="' + (colspan||5) + '" class="text-center ' + cls + ' py-4">'
       +     '<i class="' + icon + '" style="font-size:24px;"></i>'
       +     '<p class="mt-2 mb-0">' + msg + '</p>'
       +   '</td>'
       + '</tr>';
}
function showLoading($tb, msg, colspan){ $tb.html(infoRowHTML('ti-reload','text-muted', msg||'불러오는 중...', colspan)); }
function showEmpty($tb, msg, colspan){ $tb.html(infoRowHTML('ti-info-alt','text-muted', msg||'데이터가 없습니다.', colspan)); }
function showError($tb, msg, colspan){ $tb.html(infoRowHTML('ti-alert','text-danger', msg||'불러오기 실패', colspan)); }

// 상태 배지
function getUsageStatusBadge(s){
  if(s==='AVAILABLE') return '<span class="badge badge-success">사용가능</span>';
  if(s==='PARTIALLY_USED') return '<span class="badge badge-warning">부분사용</span>';
  if(s==='FULLY_USED') return '<span class="badge badge-secondary">완전사용</span>';
  return '<span class="badge badge-light">미확인</span>';
}

/* -------- 전역 선택 상태 -------- */
const pickedInboundIds = new Set(); // 선택된 입고ID들
let currentWorkOrderId = null;      // 같은 WO만 허용
let inboundPickerReqToken = 0;

/* -------- 모달 오픈 -------- */
$(document).on('click', '#btnPickInbound', function(){
  const $left = $('#inboundPickerBody');
  const $right = $('#availableMaterialsBody');

  // 완전 초기화
  pickedInboundIds.clear();
  currentWorkOrderId = null;
  $('#inb-check-all').prop('checked', false);
  $('#btnConfirmInbound').prop('disabled', true);
  $('#selectedInboundInfo').hide();
  $('#selectedInboundId').text('-');
  $('#selectedWorkOrderId').text('미확인');
  $('#totalAvailableMaterials').text(0);

  showLoading($left, '불러오는 중...', 5);
  showEmpty($right, '입고건을 선택하세요', 5);

  $('#inboundPickerModal').modal('show');
});

$('#inboundPickerModal')
  .on('shown.bs.modal', loadCompletedInbounds)
  .on('hidden.bs.modal', function(){ inboundPickerReqToken++; });

/* -------- 왼쪽 목록 로드 -------- */
function loadCompletedInbounds(){
  const $tb = $('#inboundPickerBody');
  const myToken = ++inboundPickerReqToken;
  showLoading($tb, '불러오는 중...', 5);

  $.getJSON(ctx + '/material/outbound/inbounds', { status:'입고완료', processed:'N' })
    .done(function(list){
      if(myToken !== inboundPickerReqToken || !$('#inboundPickerModal').is(':visible')) return;

      if(!list || list.length===0){
        showEmpty($tb, '입고완료 건이 없습니다.', 5);
        return;
      }

      const rows = list.map(function(row){
        const inboundId  = row.inboundId || '';
        const orderId    = row.orderId   || '-';
        const inboundYmd = toYmd(row.inboundDate);
        const usage      = row.usageStatus || 'AVAILABLE';
        const workOrderId= row.workOrderId || row.woId || ''; // 백엔드 키 호환

        return ''+
        '<tr class="inbound-row" data-inbound-id="'+inboundId+'" data-work-order-id="'+workOrderId+'">'+
          '<td class="text-center"><input type="checkbox" class="inb-pick" value="'+inboundId+'"></td>'+
          '<td class="text-center">'+inboundId+'</td>'+
          '<td class="text-center">'+orderId+'</td>'+
          '<td class="text-center">'+inboundYmd+'</td>'+
          '<td class="text-center">'+getUsageStatusBadge(usage)+'</td>'+
        '</tr>';
      }).join('');

      $tb.html(rows);
    })
    .fail(function(){ showError($tb, '목록을 불러오지 못했습니다.', 5); });
}

/* -------- 체크박스 동기화 -------- */
$(document).on('change', '#inb-check-all', function(){
  const checked = this.checked;
  $('#inboundPickerBody .inb-pick').each(function(){
    if (!this.disabled) this.checked = checked;
  });
  syncPickedFromUI();
});

$(document).on('change', '.inb-pick', syncPickedFromUI);

function syncPickedFromUI(){
  pickedInboundIds.clear();

  // 기준 WO 세팅/검증
  $('#inboundPickerBody .inb-pick').each(function(){
    const $tr = $(this).closest('tr');
    const wo  = $tr.data('work-order-id') || null;

    if (this.checked && !currentWorkOrderId) currentWorkOrderId = wo || null;

    if (currentWorkOrderId && wo && wo !== currentWorkOrderId) {
      this.checked = false;
      this.disabled = true;
      $tr.addClass('table-warning');
      return;
    } else {
      this.disabled = false;
      $tr.removeClass('table-warning');
    }

    if (this.checked) pickedInboundIds.add(this.value);
  });

  // 전체선택 체크 상태 보정
  const all  = $('#inboundPickerBody .inb-pick:not(:disabled)').length;
  const on   = $('#inboundPickerBody .inb-pick:not(:disabled):checked').length;
  $('#inb-check-all').prop('checked', all>0 && all===on);

  renderPickedInfoAndMaterials();
}

function renderPickedInfoAndMaterials(){
  const ids = Array.from(pickedInboundIds);
  $('#selectedInboundInfo').show();
  $('#selectedInboundId').text(ids.join(', ') || '-');
  $('#selectedWorkOrderId').text(currentWorkOrderId || '미확인');

  const canGo = ids.length>0 && !!currentWorkOrderId;
  $('#btnConfirmInbound').prop('disabled', !canGo);

  if (!canGo) {
    $('#availableMaterialsBody').html(infoRowHTML('ti-info-alt','text-muted','입고건을 선택하세요',5));
    $('#totalAvailableMaterials').text(0);
    return;
  }
  loadAndRenderAggregatedMaterials(ids);
}

/* -------- 오른쪽: 선택건 합산 로드 -------- */
async function loadAndRenderAggregatedMaterials(inboundIds){
  const $matTb = $('#availableMaterialsBody');
  showLoading($matTb, '가용 자재 집계중...', 5);

  try {
    // 배치 API가 없으면 개별 호출로 대체
    const lists = await Promise.all(
      inboundIds.map(id => $.getJSON(ctx + '/material/outbound/available-materials', { inboundId:id }))
    );
    const mats = lists.flat();
    if (!mats || mats.length===0){
      showEmpty($matTb, '가용한 자재가 없습니다.', 5);
      $('#totalAvailableMaterials').text(0);
      return;
    }

    // materialId+lotNo 기준 합산
    const map = new Map();
    mats.forEach(m => {
      const key = (m.materialId||'') + '|' + (m.lotNo||'');
      const prev = map.get(key) || {
        materialId: m.materialId,
        materialName: m.materialName,
        lotNo: m.lotNo || '',
        availableQty: 0,
        requiredQty: 0,
        expirationDate: m.expirationDate
      };
      prev.availableQty += Number(m.availableQty||0);
      prev.requiredQty  += Number(m.requiredQty||0);
      if (!prev.expirationDate && m.expirationDate) prev.expirationDate = m.expirationDate;
      map.set(key, prev);
    });

    const rows = Array.from(map.values())
      .sort((a,b)=> new Date(a.expirationDate||'9999-12-31') - new Date(b.expirationDate||'9999-12-31'))
      .map(m => (
        '<tr>'
        + '<td>'+(m.materialName||'')+'<br><small class="text-muted">'+(m.materialId||'')+'</small></td>'
        + '<td class="text-center"><span class="badge badge-light">'+(m.lotNo||'')+'</span></td>'
        + '<td class="text-center"><strong>'+(m.availableQty||0)+'</strong></td>'
        + '<td class="text-center">'+(m.requiredQty||0)+'</td>'
        + '<td class="text-center">'+(toYmd(m.expirationDate)||'-')+'</td>'
        + '</tr>'
      )).join('');

    $matTb.html(rows);
    $('#totalAvailableMaterials').text(map.size);
  } catch (e) {
    console.error(e);
    showError($matTb, '가용 자재 집계 실패', 5);
    $('#totalAvailableMaterials').text(0);
  }
}

/* -------- 진행 버튼: 다중 입고건으로 register 이동 -------- */
$(document).on('click', '#btnConfirmInbound', function(){
  const ids = Array.from(pickedInboundIds);
  if (!(ids.length>0 && currentWorkOrderId)) {
    alert('같은 작업지시의 입고건을 하나 이상 선택하세요.');
    return;
  }

  // (선택) 사용상태 업데이트 – 대량이면 서버에서 일괄 처리 권장
  // updateInboundUsageStatus(ids[0]);

  // register로 이동 (백엔드에서 inboundIds 처리 필요)
  location.href = ctx + '/material/outbound/register'
    + '?workOrderId=' + encodeURIComponent(currentWorkOrderId)
    + '&inboundIds=' + encodeURIComponent(ids.join(','));
});

// 선택사항: 상태 업데이트
function updateInboundUsageStatus(inboundId){
  $.post(ctx + '/material/outbound/update-inbound-status', { inboundId })
   .done(function(res){ console.log('입고건 상태 업데이트 완료', res); })
   .fail(function(){ console.warn('입고건 상태 업데이트 실패(무시)'); });
}
</script>
