/**
 * date-range-sync.js
 * 
 * 기능: 시작일(startDate)이 바뀌면 종료일(endDate)의 최소값(min)을 자동으로 설정하고,
 *       종료일이 시작일보다 이전이면 종료일 값을 초기화합니다.
 * 
 * 기본 셀렉터:
 *   startSelector: input[name="startDate"]
 *   endSelector  : input[name="endDate"]
 * 
 * 사용 방법:
 *   1) 그냥 script로 포함하면 DOMContentLoaded 시 자동으로 기본 셀렉터를 기준으로 동작합니다.
 *   2) 다른 셀렉터를 쓰고 싶다면:
 *        DateRangeSync.setupDateRange({ startSelector: '#from', endSelector: '#to' });
 */
(function (global) {
  'use strict';

  function getInput(selector) {
    try {
      return document.querySelector(selector);
    } catch (e) {
      return null;
    }
  }

  function isDateLikeInput(el) {
    if (!el) return false;
    const t = (el.getAttribute('type') || '').toLowerCase();
    return t === 'date' || t === 'datetime-local' || t === 'text'; // text는 직접 타이핑하는 케이스 대비
  }

  // 문자열 비교(YYYY-MM-DD) 또는 Date 비교 지원
  function isAfterOrEqual(a, b, type) {
    if (!a || !b) return false;
    if (type === 'date') {
      // YYYY-MM-DD 포맷은 문자열 비교가 안전
      return a >= b;
    }
    // datetime-local 또는 기타: Date 비교
    const da = new Date(a);
    const db = new Date(b);
    if (isNaN(da.getTime()) || isNaN(db.getTime())) return a >= b; // fallback 문자열
    return da.getTime() >= db.getTime();
  }

  function setupDateRange(opts) {
    const options = Object.assign({
      startSelector: 'input[name="startDate"]',
      endSelector: 'input[name="endDate"]'
    }, opts || {});

    const startInput = getInput(options.startSelector);
    const endInput   = getInput(options.endSelector);

    if (!startInput || !endInput) {
      // 필요한 요소가 없으면 아무 것도 하지 않음
      return;
    }

    // 초기 min 동기화 (페이지가 값이 채워진 상태에서 열렸을 수 있음)
    if (startInput.value) {
      endInput.min = startInput.value;
      // 이미 선택된 종료일이 유효하지 않다면 초기화
      const type = (endInput.getAttribute('type') || '').toLowerCase();
      const cmpType = type === 'date' ? 'date' : 'datetime';
      if (endInput.value && !isAfterOrEqual(endInput.value, startInput.value, cmpType)) {
        endInput.value = '';
      }
    } else {
      // 시작일이 비어있으면 제한 해제
      endInput.removeAttribute('min');
    }

    // 시작 날짜 변경 시: 종료 min 업데이트 & 유효성 체크
    startInput.addEventListener('change', function () {
      const startVal = startInput.value;
      if (startVal) {
        endInput.min = startVal;
        const type = (endInput.getAttribute('type') || '').toLowerCase();
        const cmpType = type === 'date' ? 'date' : 'datetime';
        if (endInput.value && !isAfterOrEqual(endInput.value, startVal, cmpType)) {
          endInput.value = '';
        }
      } else {
        endInput.removeAttribute('min');
      }
    });

    // 종료 날짜 직접 변경 시도 시: 시작일보다 이전이면 초기화
    endInput.addEventListener('change', function () {
      const startVal = startInput.value;
      const endVal = endInput.value;
      if (!startVal || !endVal) return;

      const type = (endInput.getAttribute('type') || '').toLowerCase();
      const cmpType = type === 'date' ? 'date' : 'datetime';
      if (!isAfterOrEqual(endVal, startVal, cmpType)) {
        endInput.value = '';
      }
    });
  }

  // 자동 초기화: DOMContentLoaded 후 기본 셀렉터로 1회 설정
  function autoInit() {
    try {
      setupDateRange();
    } catch (e) {
      // 콘솔에만 조용히 로깅
      if (typeof console !== 'undefined' && console.warn) {
        console.warn('[date-range-sync] auto init failed:', e);
      }
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', autoInit);
  } else {
    // 이미 로드된 경우 즉시 실행
    autoInit();
  }

  // 전역 노출(선택 사용)
  global.DateRangeSync = {
    setupDateRange: setupDateRange
  };

})(window);

/**
 * production-result-modal.js (merged)
 * 
 * 기능: 입고목록 등에서 .open-result-modal 버튼 클릭 시 생산 실적 상세 모달을 Ajax로 채우고 표시합니다.
 * 의존: (선택) jQuery, Bootstrap 5 (bootstrap.Modal). jQuery가 없으면 fetch/native 이벤트로 동작.
 * 
 * 요구되는 DOM:
 *   - 상세 버튼: <button class="open-result-modal" data-resultid="..." data-lot="...">
 *   - 모달:   #resultDetailModal (Bootstrap Modal)
 *   - 필드들: #rd-resultId, #rd-orderId, #rd-productName, #rd-lineName, #rd-workerName,
 *             #rd-orderQty, #rd-actualQty, #rd-defectQty, #rd-achievement,
 *             #rd-startedAt, #rd-endedAt, #rd-lotNo, #rd-createdAt,
 *             #rd-statusBadge, #rd-defect-tbody, #rd-editBtn
 */
(function (global) {
	  'use strict';

	  function $(id){ return document.getElementById(id); }
	  function escHtml(s){ return String(s || '').replace(/</g,'&lt;').replace(/>/g,'&gt;'); }
	  function fmtNumber(n){ var x = Number(n || 0); return isNaN(x) ? '' : x.toLocaleString(); }

	  function formatDateTime(val) {
	    if (!val) return '';
	    try {
	      var dt = new Date(val);
	      if (isNaN(dt.getTime())) return String(val);
	      return dt.getFullYear() + '-' +
	        String(dt.getMonth() + 1).padStart(2, '0') + '-' +
	        String(dt.getDate()).padStart(2, '0') + ' ' +
	        String(dt.getHours()).padStart(2, '0') + ':' +
	        String(dt.getMinutes()).padStart(2, '0') + ':' +
	        String(dt.getSeconds()).padStart(2, '0');
	    } catch (e) {
	      return String(val);
	    }
	  }

	  function fillResultModal(d) {
	    const setText = (id, val) => { const el = $(id); if (el) el.innerText = val || '-'; };

	    setText('rd-resultId', d.resultId);
	    setText('rd-orderId', d.orderId);
	    setText('rd-productName', d.productName);
	    setText('rd-lineName', d.lineName);
	    setText('rd-workerName', d.workerName);

	    let badge = '대기';
	    if (d.status === 'IN_PROGRESS') badge = '생산중';
	    else if (d.status === 'DONE') badge = '완료';
	    setText('rd-statusBadge', badge);

	    const orderQty = Number(d.orderQty || 0);
	    const actualQty = Number(d.actualQty || 0);
	    const defectQty = Number(d.defectQty || 0);
	    const achievement = orderQty > 0 ? ((actualQty / orderQty) * 100).toFixed(1) + '%' : '-';

	    setText('rd-orderQty', fmtNumber(orderQty));
	    setText('rd-actualQty', fmtNumber(actualQty));
	    setText('rd-defectQty', fmtNumber(defectQty));
	    setText('rd-achievement', achievement);

	    setText('rd-startedAt', formatDateTime(d.startedAt));
	    setText('rd-endedAt', formatDateTime(d.endedAt));
	    setText('rd-lotNo', d.lotNo);
	    setText('rd-createdAt', formatDateTime(d.createdAt));

	    // 불량 리스트
	    const tbody = $('rd-defect-tbody');
	    if (tbody) {
	      tbody.innerHTML = '';
	      if (d.defectList && d.defectList.length) {
	        d.defectList.forEach(def => {
	          const tr = document.createElement('tr');
	          tr.innerHTML = `
	            <td>${escHtml(def.defectType)}</td>
	            <td>${fmtNumber(def.quantity)}</td>
	            <td>${escHtml(def.note)}</td>
	          `;
	          tbody.appendChild(tr);
	        });
	      } else {
	        tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted">불량 기록 없음</td></tr>';
	      }
	    }
	  }

	  function fetchDetail(resultId, lotNo) {
	    const url = `/product/inbound/detail?resultId=${encodeURIComponent(resultId)}&lotNo=${encodeURIComponent(lotNo)}`;
	    return fetch(url, { method: 'GET', headers: { 'Accept': 'application/json' } })
	      .then(res => {
	        if (!res.ok) throw new Error('Failed to fetch');
	        return res.json();
	      });
	  }

	  function openResultDetailModal({ resultId, lotNo }) {
	    const modal = document.querySelector('#resultDetailModal');
	    if (modal) {
	      if (window.bootstrap?.Modal) {
	        new bootstrap.Modal(modal).show();
	      } else if (window.jQuery) {
	        window.jQuery(modal).modal('show');
	      }
	    }

	    fetchDetail(resultId, lotNo)
	      .then(fillResultModal)
	      .catch(() => {
	        const tbody = $('rd-defect-tbody');
	        if (tbody) tbody.innerHTML = '<tr><td colspan="3" class="text-center text-danger">데이터를 불러오지 못했습니다.</td></tr>';
	      });
	  }

	  // 버튼 이벤트 바인딩
	  document.addEventListener('click', function (e) {
	    const btn = e.target.closest('.open-result-modal');
	    if (btn) {
	      const resultId = btn.dataset.resultid;
	      const lotNo = btn.dataset.lot;
	      openResultDetailModal({ resultId, lotNo });
	    }
	  });

	  // expose
	  global.ProductionResultModal = {
	    open: openResultDetailModal
	  };
	})(window);
