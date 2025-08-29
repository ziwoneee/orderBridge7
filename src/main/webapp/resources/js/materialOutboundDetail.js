/* ---------- 출고 상세 조회 (모달) ---------- */
window.loadOutboundDetail = function(outboundId) {
	// 작은 헬퍼들 (의존성 없이 내부에서만 사용)
	function esc(s){ const div=document.createElement('div'); div.textContent=(s==null?'':s); return div.innerHTML; }
	function pad(n){ return String(n).padStart(2,'0'); }

	// 'YYYY-MM-DD' 또는 'YYYY-MM-DD HH:mm'로 표시
	// 시간이 00:00이면 날짜만
	function fmtTS(v){
	  if (!v) return '-';
	  if (typeof v === 'string') {
	    const m = v.match(/^(\d{4}-\d{2}-\d{2})(?:[ T](\d{2}):(\d{2})(?::\d{2})?)?/);
	    if (m) {
	      const [, d, hh, mm] = m;
	      if (!hh) return d;
	      return (hh === '00' && mm === '00') ? d : `${d} ${hh}:${mm}`;
	    }
	    return v.slice(0,19).replace('T',' ');
	  }
	  const t = (v && typeof v === 'object' && 'time' in v) ? Number(v.time) : Number(v);
	  if (!Number.isFinite(t)) return '-';
	  const d = new Date(t);
	  const Y = d.getFullYear(), M = pad(d.getMonth()+1), D = pad(d.getDate());
	  const H = pad(d.getHours()), m = pad(d.getMinutes());
	  return (H === '00' && m === '00') ? `${Y}-${M}-${D}` : `${Y}-${M}-${D} ${H}:${m}`;
	}

	// 날짜만 필요할 때
	function fmtYmd(v){
	  if (!v) return '-';
	  if (typeof v === 'string') {
	    const m = v.match(/^(\d{4}-\d{2}-\d{2})/);
	    if (m) return m[1];
	  }
	  const t = (v && typeof v === 'object' && 'time' in v) ? Number(v.time) : Number(v);
	  if (!Number.isFinite(t)) return '-';
	  const d = new Date(t);
	  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`;
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
      +   '<th>출고일자</th><td>'+esc(fmtYmd(_issuedAt))+'</td>'
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
