/**
 * 개선된 관리자 대시보드 JavaScript
 * @description: 가로 배치 라인 현황, 큰 글씨, 간소화된 UI
 * 변경 요약:
 *  - API URL을 window.DASHBOARD_CONFIG 기반으로 안전하게 설정
 *  - 완제품부족: fgShortage / fgShortageList 모두 인식 + 타일 숫자는 목록 길이로 보정
 *  - 원자재: rmShortage / rmShortageList, rmExhausted / rmExhaustedList 모두 인식
 *  - 유지보수용 주석/들여쓰기 정리
 */

(function () {
  'use strict';

  // ---------------------------------------------------------------------------
  // 전역 설정값
  // ---------------------------------------------------------------------------
  const CONFIG = {
    REFRESH_INTERVAL: 30000, // 30초마다 갱신
    // API URL: JSP에서 내려주는 window.DASHBOARD_CONFIG 우선 사용
    DASHBOARD_API:
      (window.DASHBOARD_CONFIG && window.DASHBOARD_CONFIG.API_URL) ||
      ((window.DASHBOARD_CONFIG && window.DASHBOARD_CONFIG.CONTEXT_PATH)
        ? window.DASHBOARD_CONFIG.CONTEXT_PATH + '/admin/dashboard/data'
        : '/admin/dashboard/data'),
    DATE_FORMAT: 'ko-KR',
    NUMBER_FORMAT: 'ko-KR'
  };

  // ---------------------------------------------------------------------------
  // 상태 관리
  // ---------------------------------------------------------------------------
  let isLoading = false;
  let refreshTimer = null;
  let retryCount = 0;
  const MAX_RETRY = 3;

  // ---------------------------------------------------------------------------
  // 초기화
  // ---------------------------------------------------------------------------
  /**
   * 대시보드 초기화
   * - 최초 데이터 로드
   * - 자동 갱신 시작
   * - 페이지 가시성/수동 새로고침 바인딩
   */
  function initDashboard() {
    console.log('✅ 개선된 대시보드 초기화 시작');
    loadDashboardData();
    startAutoRefresh();
    handleVisibilityChange();
    bindRefreshButton();
    console.log('✅ 개선된 대시보드 초기화 완료');
  }

  // ---------------------------------------------------------------------------
  // 데이터 로딩/에러 처리
  // ---------------------------------------------------------------------------
  /**
   * 대시보드 데이터 로딩
   * - 중복 로딩 방지
   * - fetch → JSON → 화면 바인딩
   */
  function loadDashboardData() {
    if (isLoading) {
      console.log('⏳ 이미 로딩 중입니다.');
      return;
    }
    isLoading = true;
    showLoadingState();

    fetch(CONFIG.DASHBOARD_API, { cache: 'no-store' })
      .then(handleResponse)
      .then(function (data) {
        console.log('📦 대시보드 데이터 수신:', data);
        retryCount = 0;
        bindDashboard(data);
        hideLoadingState();
      })
      .catch(function (error) {
        console.error('❌ 대시보드 데이터 로딩 실패:', error);
        handleLoadError(error);
      })
      .finally(function () {
        isLoading = false;
      });
  }

  /**
   * fetch 응답 처리
   * @param {Response} response
   * @returns {Promise<any>} JSON
   */
  function handleResponse(response) {
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }
    return response.json();
  }

  /**
   * 로딩 실패 처리(재시도 포함)
   * @param {Error} error
   */
  function handleLoadError(error) {
    retryCount++;
    if (retryCount <= MAX_RETRY) {
      console.log(`🔁 재시도 ${retryCount}/${MAX_RETRY} (5초 후)`);
      setTimeout(loadDashboardData, 5000);
    } else {
      console.error('⛔ 최대 재시도 횟수 초과');
      showErrorState('데이터를 불러올 수 없습니다. 페이지를 새로고침해 주세요.');
      stopAutoRefresh();
    }
  }

  // ---------------------------------------------------------------------------
  // 데이터 바인딩
  // ---------------------------------------------------------------------------
  /**
   * 메인 데이터 바인딩
   * @param {object} data 서버 응답 데이터
   */
  function bindDashboard(data) {
    try {
      updateMetrics(data); // 상단 메트릭 카드
      updateLineStatusHorizontal(data.lines || []); // 생산 라인
      updateTodayOrders(data.todayOrders || []); // 오늘 수주
      updateInventoryStatus(data); // 재고 현황(원자재/완제품)
      updateLastRefreshTime(); // 갱신 시간
      console.log('🎨 UI 업데이트 완료');
    } catch (error) {
      console.error('⚠️ UI 업데이트 중 오류:', error);
      showErrorState('화면 업데이트 중 오류가 발생했습니다.');
    }
  }

  /**
   * 상단 메트릭 업데이트
   * - 서로 다른 백엔드 키를 허용(유연한 매핑)
   * - 완제품부족 타일은 목록 길이를 기반으로 보정
   * @param {object} data
   */
  function updateMetrics(data) {
    // 완제품 부족 목록 키 유연 처리
    const fgList =
      data.fgShortageList ||
      data.fgShortage ||
      (data.finishedGoods && data.finishedGoods.shortageList) ||
      [];

    // 카운트가 명시되면 사용, 없으면 목록 길이로 보정
    const fgCount =
      data.fgShortageCount != null ? data.fgShortageCount : (Array.isArray(fgList) ? fgList.length : 0);

    const metrics = [
      { id: 'tile-today-req', value: data.todayOrdersRequested },
      { id: 'tile-today-conf', value: data.todayOrdersConfirmed },
      { id: 'wo-waiting', value: data.woWaiting },
      { id: 'wo-ready', value: data.woReady },
      { id: 'wo-inprogress', value: data.woInProgress },
      { id: 'tile-completed-today', value: data.woCompletedToday },
      { id: 'tile-prod-actual', value: data.productionActualToday },
      { id: 'rm-shortage', value: data.rmShortageCount },
      { id: 'rm-exhausted', value: data.rmExhaustedCount },
      { id: 'fg-shortage', value: fgCount } // ✅ 목록 길이 기반 보정
    ];

    metrics.forEach(function (metric) {
      setText(metric.id, metric.value);
    });
  }

  // ---------------------------------------------------------------------------
  // 생산 라인 렌더링
  // ---------------------------------------------------------------------------
  /**
   * 생산 라인 현황(가로 배치) 렌더링
   * @param {Array<object>} lines
   */
  /**
   * 생산 라인 현황(가로 배치) 렌더링
   * - 라인 3칸 고정 표시
   * - 라인 상태가 INACTIVE면 본문 대신 비활성 안내 노출
   * - 진행률은 안전 계산(없으면 producedQty/orderQty로 보충, 0~100 클램프)
   * @param {Array<object>} lines
   */
  function updateLineStatusHorizontal(lines) {
    const container = getElementById('cards-lines');
    if (!container) return;

    // 레이아웃 가드: row/flex 보장
    container.classList.add('row');
    container.style.display = 'flex';
    container.style.flexWrap = 'wrap';

    // 기존 placeholder 제거
    container.innerHTML = '';

    // 데이터 없을 때
    if (!Array.isArray(lines) || lines.length === 0) {
      const emptyCol = document.createElement('div');
      emptyCol.className = 'col-12 text-center text-muted py-4 h5';
      emptyCol.textContent = '운영 중인 라인이 없습니다.';
      container.appendChild(emptyCol);
      return;
    }

    // 3칸 고정(필요시 조정 가능)
    const maxLines = 3;
    const displayLines = lines.slice(0, maxLines);
    while (displayLines.length < maxLines) {
      displayLines.push({
        lineId: `line-${displayLines.length + 1}`,
        lineName: `${displayLines.length + 1}라인`,
        state: 'IDLE',
        isEmpty: true,
        producedQty: 0,
        progressRate: 0,
        readyCount: 0,
        waitingCount: 0
      });
    }

    // 진행률 보정 유틸(0~100)
    const clamp = (v) => Math.max(0, Math.min(100, Math.round(v)));
    const calcProgress = (line) => {
      // 1) 서버에서 준 progressRate 우선
      const pr = Number(line.progressRate);
      if (Number.isFinite(pr)) return clamp(pr);
      // 2) producedQty / orderQty 보충 계산
      const produced = Number(line.producedQty);
      const orderQty = Number(line.orderQty);
      if (Number.isFinite(produced) && Number.isFinite(orderQty) && orderQty > 0) {
        return clamp((produced / orderQty) * 100);
      }
      return 0;
    };

    displayLines.forEach((line, idx) => {
      const statusInfo = getLineStatusInfo(line.state);
      const isInactive = String(line.state || '').toUpperCase() === 'INACTIVE';
      const progress = isInactive ? 0 : calcProgress(line);

      // column wrapper
      const col = document.createElement('div');
      col.className = 'col-12 col-md-6 col-lg-4 mb-3 fade-in';

      // 카드
      const card = document.createElement('div');
      card.className = `line-card ${statusInfo.cardClass}`;

      // 카드 헤더
      const header = document.createElement('div');
      header.className = 'd-flex justify-content-between align-items-center mb-3';
      header.innerHTML =
        `<h5 class="mb-0 font-weight-bold text-dark">${esc(line.lineName || `${idx + 1}라인`)}</h5>
         <span class="badge ${statusInfo.badgeClass}">${statusInfo.text}</span>`;
      card.appendChild(header);

      // === 카드 본문 ===
      if (isInactive) {
        // 🔒 비활성 라인: 안내만 노출
        const idle = document.createElement('div');
        idle.className = 'text-center text-muted py-4';
        idle.innerHTML =
          `<i class="mdi mdi-power-plug-off-outline mdi-48px mb-3 d-block"></i>
           <h6 class="mb-0">라인이 비활성 상태입니다</h6>`;
        card.appendChild(idle);

      } else if (line.workOrderId && !line.isEmpty) {
        // 작업지시 존재: 상세 본문
        const body = document.createElement('div');
        body.innerHTML =
          `<div class="mb-3">
             <div class="row mb-2">
               <div class="col-6">
                 <small class="text-muted">작업지시</small><br>
                 <strong class="text-primary">${esc(line.workOrderId)}</strong>
               </div>
               <div class="col-6">
                 <small class="text-muted">납기</small><br>
                 <span class="text-dark">${formatDate(line.dueDate)}</span>
               </div>
             </div>

             <div class="mb-3">
               <small class="text-muted">제품: </small>
               <span class="font-weight-medium">${esc(line.productName || '-')}</span>
               ${line.orderQty ? ` (${formatNumber(line.orderQty)})` : ''}
             </div>

             <div class="progress mb-3" style="height:10px;">
               <div class="progress-bar ${statusInfo.progressClass}"
                    style="width:${progress}%"
                    title="${progress}% 완료"></div>
             </div>

             <div class="row text-center">
               <div class="col-4">
                 <small class="text-muted d-block">양품</small>
                 <strong class="text-success">${formatNumber(line.producedQty || 0)}</strong>
               </div>
               <div class="col-4">
                 <small class="text-muted d-block">진행률</small>
                 <strong class="${statusInfo.textClass}">${progress}%</strong>
               </div>
               <div class="col-4">
                 <small class="text-muted d-block">대기</small>
                 <strong class="text-warning">${(line.readyCount || 0) + (line.waitingCount || 0)}</strong>
               </div>
             </div>
           </div>`;
        card.appendChild(body);

      } else {
        // 작업 없음: 일반 안내
        const idle = document.createElement('div');
        idle.className = 'text-center text-muted py-4';
        idle.innerHTML =
          `<i class="mdi ${line.isEmpty ? 'mdi-information-outline' : 'mdi-pause-circle'} mdi-48px mb-3 d-block"></i>
           <h6 class="mb-0">${line.isEmpty ? '라인 정보가 없습니다' : '현재 생산 작업이 없습니다'}</h6>`;
        card.appendChild(idle);
      }

      col.appendChild(card);
      container.appendChild(col);
    });

    // 디버깅 로그(원할 때 1회 확인)
    console.log(
      'cards-lines display', getComputedStyle(container).display,
      'children', [...container.children].map((c) => c.className)
    );
  }

  // ---------------------------------------------------------------------------
  // 수주 테이블
  // ---------------------------------------------------------------------------
  /**
   * 오늘 수주 목록 업데이트
   * @param {Array<object>} orders
   */
  function updateTodayOrders(orders) {
    const tbody = getElementById('tbl-today-orders');
    if (!tbody) return;

    if (!Array.isArray(orders) || orders.length === 0) {
      tbody.innerHTML = createEmptyRow(5, '오늘 접수된 수주가 없습니다.');
      return;
    }

    const html = orders
      .map(function (order) {
        const statusInfo = getOrderStatusInfo(order.status);
        return (
          '<tr class="fade-in">' +
          '<td><span class="font-weight-bold text-primary h6">' +
          esc(order.clOrderId) +
          '</span></td>' +
          '<td class="h6">' +
          esc(order.clientName || '-') +
          '</td>' +
          '<td class="text-truncate" style="max-width: 300px;" title="' +
          esc(order.productNames || '') +
          '">' +
          '<span class="h6">' +
          esc(order.productNames || '품목 정보 없음') +
          '</span></td>' +
          '<td><span class="badge ' +
          statusInfo.badgeClass +
          ' h6">' +
          statusInfo.text +
          '</span></td>' +
          '<td class="text-muted h6">' +
          formatDateTime(order.createdAt) +
          '</td>' +
          '</tr>'
        );
      })
      .join('');

    tbody.innerHTML = html;
  }

  // ---------------------------------------------------------------------------
  // 재고 테이블 (원자재/완제품)
  // ---------------------------------------------------------------------------
  /**
   * 재고 현황 업데이트
   * - 원자재 부족/소진/완제품 부족 테이블 렌더링
   * @param {object} data
   */
  function updateInventoryStatus(data) {
    // 원자재 부족: 다양한 키 허용
    updateInventoryTable(
      'tbl-rm-shortage',
      data.rmShortage || data.rmShortageList || [],
      '부족한 원자재가 없습니다.'
    );

    // 원자재 소진: 다양한 키 허용
    updateInventoryTable(
      'tbl-rm-exhausted',
      data.rmExhausted || data.rmExhaustedList || [],
      '소진된 원자재가 없습니다.'
    );

    // 완제품 부족 목록: 다양한 키 허용
    const fgList =
      data.fgShortage ||
      data.fgShortageList ||
      (data.finishedGoods && data.finishedGoods.shortageList) ||
      [];
    updateProductTable('tbl-fg-shortage', fgList, '부족한 완제품이 없습니다.');
  }

  /**
   * 원자재 표 렌더링
   * @param {string} tableId 대상 tbody ID
   * @param {Array<object>} items 행 데이터
   * @param {string} emptyMessage 비어있을 때 메시지
   */
  function updateInventoryTable(tableId, items, emptyMessage) {
    const tbody = getElementById(tableId);
    if (!tbody) return;

    if (!Array.isArray(items) || items.length === 0) {
      tbody.innerHTML =
        '<tr><td colspan="5" class="text-center text-success py-4 h5">' +
        emptyMessage +
        '</td></tr>';
      return;
    }

    const html = items
      .map(function (item) {
        return (
          '<tr class="fade-in">' +
          '<td><code class="h6">' +
          esc(item.materialId) +
          '</code></td>' +
          '<td class="h6">' +
          esc(item.materialName || '-') +
          '</td>' +
          '<td class="text-end h6">' +
          formatNumber(item.quantity || 0) +
          '</td>' +
          '<td class="text-end h6">' +
          formatNumber(item.safetyStock || 0) +
          '</td>' +
          '<td class="h6">' +
          formatDate(item.nextExpireDate) +
          '</td>' +
          '</tr>'
        );
      })
      .join('');

    tbody.innerHTML = html;
  }

  /**
   * 완제품 표 렌더링
   * @param {string} tableId 대상 tbody ID
   * @param {Array<object>} items 행 데이터
   * @param {string} emptyMessage 비어있을 때 메시지
   */
  function updateProductTable(tableId, items, emptyMessage) {
    const tbody = getElementById(tableId);
    if (!tbody) return;

    if (!Array.isArray(items) || items.length === 0) {
      tbody.innerHTML =
        '<tr><td colspan="4" class="text-center text-success py-4 h5">' +
        emptyMessage +
        '</td></tr>';
      return;
    }

    const html = items
      .map(function (item) {
        return (
          '<tr class="fade-in">' +
          '<td><code class="h6">' +
          esc(item.productId) +
          '</code></td>' +
          '<td class="h6">' +
          esc(item.productName || '-') +
          '</td>' +
          '<td class="text-end text-danger font-weight-bold h5">' +
          formatNumber(item.availableQty || 0) +
          '</td>' +
          '<td class="text-end h6">' +
          formatNumber(item.reservedQty || 0) +
          '</td>' +
          '</tr>'
        );
      })
      .join('');

    tbody.innerHTML = html;
  }

  // ---------------------------------------------------------------------------
  // 자동 갱신/가시성/수동 새로고침
  // ---------------------------------------------------------------------------
  /**
   * 자동 갱신 시작
   */
  function startAutoRefresh() {
    if (refreshTimer) {
      clearInterval(refreshTimer);
    }
    refreshTimer = setInterval(function () {
      console.log(' 자동 갱신 실행');
      loadDashboardData();
    }, CONFIG.REFRESH_INTERVAL);
    console.log(`⏰ 자동 갱신 시작 (${CONFIG.REFRESH_INTERVAL / 1000}초 간격)`);
  }

  /**
   * 자동 갱신 중지
   */
  function stopAutoRefresh() {
    if (refreshTimer) {
      clearInterval(refreshTimer);
      refreshTimer = null;
      console.log(' 자동 갱신 중지');
    }
  }

  /**
   * 페이지 가시성 변경 처리 (백그라운드에서 폴링 중단)
   */
  function handleVisibilityChange() {
    document.addEventListener('visibilitychange', function () {
      if (document.hidden) {
        console.log(' 페이지 숨김 - 자동 갱신 중지');
        stopAutoRefresh();
      } else {
        console.log(' 페이지 표시 - 자동 갱신 재시작');
        loadDashboardData();
        startAutoRefresh();
      }
    });
  }

  /**
   * 수동 새로고침 버튼 바인딩
   */
  function bindRefreshButton() {
    const refreshBtn = getElementById('btn-refresh');
    if (refreshBtn) {
      refreshBtn.addEventListener('click', function () {
        console.log(' 수동 새로고침 요청');
        loadDashboardData();
      });
    }
  }

  // ---------------------------------------------------------------------------
  // 화면 공통 유틸 (로딩/시간/포맷/상태 텍스트 등)
  // ---------------------------------------------------------------------------
  function showLoadingState() {
    console.log(' 로딩 상태 표시');
  }

  function hideLoadingState() {
    console.log(' 로딩 상태 숨김');
  }

  function showErrorState(message) {
    console.error(' 에러 상태:', message);
  }

  /**
   * 마지막 업데이트 시간 표시
   */
  function updateLastRefreshTime() {
    const timeElement = getElementById('last-refresh-time');
    if (timeElement) {
      const now = new Date();
      timeElement.textContent =
        '마지막 업데이트: ' + now.toLocaleTimeString(CONFIG.DATE_FORMAT);
    }
  }

  // ---------------------------------------------------------------------------
  // 유틸리티
  // ---------------------------------------------------------------------------
  function getElementById(id) {
    return document.getElementById(id);
  }

  function setText(elementId, value) {
    const el = getElementById(elementId);
    if (el) {
      el.textContent = value != null ? formatNumber(value) : '-';
    }
  }

  function esc(str) {
    if (str == null) return '';
    return String(str).replace(/[&<>"']/g, function (m) {
      const escapeMap = {
        '&': '&amp;', '<': '&lt;', '>': '&gt;',
        '"': '&quot;', "'": '&#39;'
      };
      return escapeMap[m];
    });
  }

  function formatNumber(num) {
    return (num == null ? 0 : num).toLocaleString(CONFIG.NUMBER_FORMAT);
  }

  function formatDate(dateStr) {
    if (!dateStr) return '-';
    try {
      const date = new Date(dateStr);
      return isNaN(date) ? '-' : date.toLocaleDateString(CONFIG.DATE_FORMAT);
    } catch (e) {
      return '-';
    }
  }

  function formatDateTime(dateStr) {
    if (!dateStr) return '-';
    try {
      const date = new Date(dateStr);
      if (isNaN(date)) return '-';

      const today = new Date();
      const isToday = date.toDateString() === today.toDateString();

      if (isToday) {
        return date.toLocaleTimeString(CONFIG.DATE_FORMAT, {
          hour: '2-digit',
          minute: '2-digit'
        });
      } else {
        return date.toLocaleDateString(CONFIG.DATE_FORMAT, {
          month: 'short',
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        });
      }
    } catch (e) {
      return '-';
    }
  }

  /**
   * 수주 상태 → 표시 텍스트/뱃지 클래스
   */
  function getOrderStatusInfo(status) {
    switch (status) {
      case 'CONFIRMED':
        return { text: '확정', badgeClass: 'badge-success' };
      case 'REQUESTED':
      default:
        return { text: '접수', badgeClass: 'badge-primary' };
    }
  }

  /**
   * 라인 상태 → 배지/카드/프로그레스 클래스
   */
  function getLineStatusInfo(state) {
	  const status = (state || '').toUpperCase();
	  switch (status) {
	    case 'IN_PROGRESS':
	      return { text:'생산중',  badgeClass:'badge-success',  cardClass:'line-active',   progressClass:'bg-success',   textClass:'text-success' };
	    case 'READY':
	      return { text:'준비완료',badgeClass:'badge-info',     cardClass:'line-ready',    progressClass:'bg-info',      textClass:'text-info' };
	    case 'WAITING':
	      return { text:'대기중',  badgeClass:'badge-warning',  cardClass:'line-waiting',  progressClass:'bg-warning',   textClass:'text-warning' };
	    case 'INACTIVE': 
	      return { text:'비활성',  badgeClass:'badge-secondary',cardClass:'line-disabled', progressClass:'bg-secondary', textClass:'text-secondary' };
	    default:
	      return { text:'미생산',  badgeClass:'badge-secondary',cardClass:'line-idle',     progressClass:'bg-secondary', textClass:'text-secondary' };
	  }
	}

  function createEmptyRow(colspan, message) {
    return (
      '<tr><td colspan="' +
      colspan +
      '" class="text-center text-muted py-4 h5">' +
      message +
      '</td></tr>'
    );
  }

  function createEmptyMessage(message) {
    return '<div class="col-12 text-center text-muted py-4 h5">' + message + '</div>';
  }

  // ---------------------------------------------------------------------------
  // 초기 실행
  // ---------------------------------------------------------------------------
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initDashboard);
  } else {
    initDashboard();
  }

  // 디버깅용 전역 노출
  window.AdminDashboard = {
    refresh: loadDashboardData,
    startAutoRefresh: startAutoRefresh,
    stopAutoRefresh: stopAutoRefresh,
    config: CONFIG
  };
})();
