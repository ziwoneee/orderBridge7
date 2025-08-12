/* ---------- 출고 상세 조회 (모달) ---------- */
window.loadOutboundDetail = function(outboundId) {
  // 작은 헬퍼들 (의존성 없이 내부에서만 사용)
  function esc(s){ return $('<div>').text(s == null ? '' : s).html(); }
  function fmtTS(v){
    if(!v) return '-';
    if(typeof v==='string') return v.replace('T',' ').slice(0,19);
    if(v.time) return new Date(v.time).toISOString().replace('T',' ').slice(0,19);
    try{ return new Date(v).toISOString().replace('T',' ').slice(0,19);}catch(e){ return '-'; }
  }
  function fmtYmd(v){
    if(!v) return '-';
    if(typeof v==='string') return v.slice(0,10);
    if(v.time) return new Date(v.time).toISOString().slice(0,10);
    try{ return new Date(v).toISOString().slice(0,10);}catch(e){ return '-'; }
  }
  function badgeStatus(st){
    st = String(st || 'DRAFT').toUpperCase();
    if (st === 'COMPLETED' || st === 'ISSUED') return '<span class="badge badge-success">완료</span>';
    if (st === 'CANCELLED' || st === 'CANCELED') return '<span class="badge badge-secondary">취소</span>';
    if (st === 'PARTIAL') return '<span class="badge badge-info">부분</span>';
    return '<span class="badge badge-warning">임시</span>';
  }

  $('#outboundDetailBody').html('<div class="p-4 text-center text-muted">불러오는 중...</div>');
  $('#outboundDetailModal').modal('show');

  $.ajax({
    url: ctx + '/material/outbound/detail',
    method: 'GET',
    data: { outboundId: outboundId },
    dataType: 'json'
  })
  .done(function(data) {
    // 백엔드 포맷 방어적 보정
    var head  = (data && (data.header || data.outbound || data)) || {};
    var items = (data && (data.items || data.detailItems)) || [];

    // 키명 유연 대응
    var _outboundId = head.outboundId || head.id || '';
    var _workOrderId= head.workOrderId || head.woId || '';
    var _issuedAt   = head.issuedAt || head.outboundDate || head.createdAt;
    var _userName   = head.userName || head.handledBy || head.createdBy || '-';
    var _status     = head.status || 'DRAFT';
    var _note       = head.note || head.remark || '';

    // 상단 요약 테이블(출고번호/작업지시/출고일시/담당자/상태/비고)
    var topHTML =
      '<table class="table table-bordered table-sm mb-3"><tbody>'
      + '<tr>'
      +   '<th style="width:15%">출고번호</th><td style="width:35%">'+esc(_outboundId)+'</td>'
      +   '<th style="width:15%">작업지시</th><td style="width:35%">'+esc(_workOrderId)+'</td>'
      + '</tr>'
      + '<tr>'
      +   '<th>출고일시</th><td>'+esc(fmtTS(_issuedAt))+'</td>'
      +   '<th>담당자</th><td>'+esc(_userName)+'</td>'
      + '</tr>'
      + '<tr>'
      +   '<th>상태</th><td>'+badgeStatus(_status)+'</td>'
      +   '<th>비고</th><td>'+(_note ? esc(_note) : '-')+'</td>'
      + '</tr>'
      + '</tbody></table>';

    // 상세 라인 테이블(자재ID/품명/LOT/출고수량/창고/유통기한/비고)
    var bodyRows = (items.length ? items.map(function(it){
      return '<tr>'
        + '<td>'+esc(it.materialId||'')+'</td>'
        + '<td>'+esc(it.materialName||'')+'</td>'
        + '<td>'+esc(it.lotNo||'')+'</td>'
        + '<td class="text-right">'+(Number(it.quantity||0))+'</td>'
        + '<td>'+esc(it.warehouseCode||'')+'</td>'
        + '<td>'+fmtYmd(it.expirationDate)+'</td>'
        + '<td>'+esc(it.remark||it.note||'')+'</td>'
        + '</tr>';
    }).join('') : '<tr><td colspan="7" class="text-center text-muted">항목이 없습니다.</td></tr>');

    var listHTML =
      '<div class="table-responsive">'
      + '<table class="table table-bordered table-sm text-center">'
      +   '<thead class="thead-light">'
      +     '<tr>'
      +       '<th>자재ID</th>'
      +       '<th>품명</th>'
      +       '<th>LOT</th>'
      +       '<th>출고수량</th>'
      +       '<th>창고</th>'
      +       '<th>유통기한</th>'
      +       '<th>비고</th>'
      +     '</tr>'
      +   '</thead>'
      +   '<tbody>'+ bodyRows +'</tbody>'
      + '</table>'
      + '</div>';

    $('#outboundDetailBody').html(topHTML + listHTML);
  })
  .fail(function(xhr) {
    console.error('상세 정보 로드 실패:', xhr);
    $('#outboundDetailBody').html('<div class="p-4 text-danger">상세 정보를 불러올 수 없습니다. '+(xhr.status||'')+'</div>');
  });
};
