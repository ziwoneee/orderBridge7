/**
 * 확정 수주 선택 JavaScript
 * select-order.js
 */

//행 강조 및 검색창 포커스 (보조 UI 효과)
$(document).ready(function () {
    // 선택된 행 강조
	$(document)
	  .off('change', '.order-checkbox')
	  .on('change', '.order-checkbox', function () {
	    // 행 강조 토글
	    $(this).closest('tr').toggleClass('selected', $(this).prop('checked'));
	    // 선택 정보 갱신
	    updateSelectionInfo();
	  });

    // 검색창 자동 포커스
    $('input[name="keyword"]').focus();
	});

	//미리 체크된 항목이 있으면 강조 상태 맞추기
	$('.order-checkbox:checked').each(function () {
	  $(this).closest('tr').addClass('selected');
	});


(function($){
  let opening = false;      //  팝업 중복열림 방지
  let popupRef = null;      //  같은 창 재사용

  $(document).ready(function () {
    initSelectAllCheckbox();

    $(document).off('change', '.order-checkbox').on('change', '.order-checkbox', updateSelectionInfo);

    // 🔑 기존 on('click', ...) 바인딩이 여러번 붙지 않도록 off 후 on
    $(document).off('click', '#mergeSelectBtn').on('click', '#mergeSelectBtn', function(e){
      e.preventDefault();
      processMergeOrders();
    });

    $('input[name="keyword"]').off('keypress').on('keypress', function(e) {
      if (e.which === 13) $(this).closest('form').submit();
    });

    $(document).off('keydown.selectOrderEsc').on('keydown.selectOrderEsc', function(e) {
      if (e.key === 'Escape') {
        if (confirm('선택을 취소하고 창을 닫으시겠습니까?')) {
          window.close();
        }
      }
    });
  });

  function initSelectAllCheckbox() {
    const headerHtml = `
      <th width="50">
        <input type="checkbox" id="selectAll" title="전체 선택">
      </th>
    `;
    const $firstTh = $('table thead tr th:first');
    if ($firstTh.text().trim() === '선택') $firstTh.html(headerHtml);

    $(document).off('change', '#selectAll').on('change', '#selectAll', function() {
      const isChecked = $(this).prop('checked');
      $('.order-checkbox').prop('checked', isChecked);
      updateSelectionInfo();
    });
  }

  function updateSelectionInfo() {
    const $selected = $('.order-checkbox:checked');
    const count = $selected.length;

    if ($('#selectionInfo').length === 0) {
      const infoHtml = `
        <div id="selectionInfo" class="alert alert-info mt-3" style="display: none;">
          <strong>선택된 수주:</strong> <span id="selectedCount">0</span>건 |
          <strong>총 수량:</strong> <span id="totalQuantity">0</span>
          <span id="productInfo" class="ml-3"></span>
        </div>
      `;
      $('.table-container').before(infoHtml);
    }

    if (count > 0) {
      const products = new Map();
      let totalQty = 0;

      $selected.each(function() {
        const productId = $(this).data('product-id');
        const productName = $(this).data('product-name');
        const qty = parseInt($(this).data('order-qty')) || 0;
        totalQty += qty;
        if (!products.has(productId)) products.set(productId, { name: productName, count: 0, qty: 0 });
        products.get(productId).count++;
        products.get(productId).qty += qty;
      });

      $('#selectedCount').text(count);
      $('#totalQuantity').text(totalQty.toLocaleString());

      if (products.size === 1) {
        const [_pid, info] = products.entries().next().value;
        $('#productInfo').html(`<strong>제품:</strong> ${info.name}`);
        $('#selectionInfo').removeClass('alert-warning').addClass('alert-info');
      } else {
        $('#productInfo').html(`<span class="text-danger">⚠ 서로 다른 제품 ${products.size}종이 선택되었습니다</span>`);
        $('#selectionInfo').removeClass('alert-info').addClass('alert-warning');
      }

      $('#selectionInfo').slideDown();
      $('#mergeSelectBtn').prop('disabled', products.size !== 1);
    } else {
      $('#selectionInfo').slideUp();
      $('#mergeSelectBtn').prop('disabled', true);
    }
  }

  function processMergeOrders() {
	    if (opening) return;  // ✅ 더블클릭 방지
	    opening = true;
	    const $selected = $('.order-checkbox:checked');
	    try {
	      if ($selected.length === 0) {
	        alert('선택된 수주가 없습니다.');
	        return;
	      }
	      const productValidation = validateSameProduct($selected);
	      if (!productValidation.valid) {
	        alert(productValidation.message);
	        return;
	      }
	      const mergedData = collectMergedData($selected);
	      if (!mergedData) return;
	      const confirmMsg = `
	다음 내용으로 작업지시를 등록하시겠습니까?
	▶ 제품: ${mergedData.productName}
	▶ 병합 수주: ${mergedData.clOrderIds.length}건
	▶ 총 수량: ${mergedData.orderQty.toLocaleString()}
	▶ 납기일: ${mergedData.dueDate}
	      `.trim();
	      if (!confirm(confirmMsg)) return;
	      
	      // ✅ URL 생성 방법 수정
	      const baseUrl = getContextPath() + '/workorder/register-popup';
	      const params = new URLSearchParams();
	      
	      // Spring이 List<String>으로 받을 수 있도록 각각 추가
	      mergedData.clOrderIds.forEach(id => params.append('clOrderIds', id));
	      params.append('productId', mergedData.productId);
	      params.append('orderQty', mergedData.orderQty);
	      params.append('dueDate', mergedData.dueDate);
	      const fullUrl = `${baseUrl}?${params.toString()}`;
	      
	      console.log('팝업 URL:', fullUrl); // 디버깅용
	      
	      // ✅ 첫 번째 팝업과 동일한 크기와 위치 계산 방식
	      const width = 1200;
	      const height = 650;
	      
	      // 듀얼 모니터 환경을 고려한 화면 중앙 계산
	      const screenLeft = window.screenLeft !== undefined ? window.screenLeft : window.screenX;
	      const screenTop = window.screenTop !== undefined ? window.screenTop : window.screenY;
	      
	      const innerWidth = window.innerWidth ? window.innerWidth : 
	          document.documentElement.clientWidth ? document.documentElement.clientWidth : screen.width;
	      const innerHeight = window.innerHeight ? window.innerHeight : 
	          document.documentElement.clientHeight ? document.documentElement.clientHeight : screen.height;
	      
	      const left = Math.round(screenLeft + (innerWidth / 2) - (width / 2));
	      const top = Math.round(screenTop + (innerHeight / 2) - (height / 2));
	      
	      const features = `width=${width},height=${height},left=${left},top=${top},resizable=yes,scrollbars=yes,status=no,menubar=no,toolbar=no,location=no`;
	      
	      if (!popupRef || popupRef.closed) {
	        popupRef = window.open(fullUrl, 'workorder_register_popup', features);
	      } else {
	        popupRef.location.href = fullUrl;
	        popupRef.focus();
	      }
	      
	      if (!popupRef || popupRef.closed || typeof popupRef.closed === 'undefined') {
	        alert('팝업이 차단되었습니다. 브라우저 설정에서 팝업을 허용해주세요.');
	        return;
	      }
	      
	      if (popupRef.focus) {
	        popupRef.focus();
	      }
	      
	      // 팝업 위치 재조정 (브라우저 호환성)
	      setTimeout(function() {
	        if (popupRef && !popupRef.closed) {
	          popupRef.moveTo(left, top);
	          popupRef.resizeTo(width, height);
	          if (popupRef.focus) {
	            popupRef.focus();
	          }
	        }
	      }, 100);
	      
	      // 필요하면 현재 선택창 닫기
	      if (popupRef) window.close();
	      else alert('팝업 차단이 되어 있을 수 있습니다. 브라우저 설정을 확인해주세요.');
	    } finally {
	      setTimeout(() => { opening = false; }, 400);
	    }
	  }
  

  // ✅ 컨텍스트 경로 가져오는 함수 수정
  function getContextPath() {
    // 1순위: 전역 변수
    if (window.CONTEXT_PATH !== undefined) {
      return window.CONTEXT_PATH;
    }
    
    // 2순위: 현재 페이지 경로에서 추출
    const path = window.location.pathname;
    
    // 루트 컨텍스트인 경우 (예: /workorder/select-order)
    // 첫 번째 세그먼트가 애플리케이션 경로가 아닌 컨트롤러 경로인지 확인
    if (path.startsWith('/workorder/')) {
      return ''; // 루트 컨텍스트
    }
    
    // 실제 컨텍스트가 있는 경우 (예: /myapp/workorder/select-order)
    const segments = path.split('/');
    if (segments.length > 3 && segments[1] && segments[2] === 'workorder') {
      return '/' + segments[1];
    }
    
    // 기본값: 빈 문자열 (루트 컨텍스트)
    return '';
  }

  function validateSameProduct($selected) {
    const products = new Set();
    const names = new Set();
    $selected.each(function() {
      products.add($(this).data('product-id'));
      names.add($(this).data('product-name'));
    });
    if (products.size === 0) return { valid: false, message: '제품 정보를 확인할 수 없습니다.' };
    if (products.size > 1) {
      return { valid: false, message: `서로 다른 제품이 선택되었습니다.\n선택된 제품: ${Array.from(names).join(', ')}` };
    }
    return { valid: true };
  }

  function collectMergedData($selected) {
	  const clOrderIds = [];
	  const mergedOrders = []; // ✅ [{clOrderId, orderQty}] 로 쌓는다
	  let totalRequired = 0;
	  let earliestDueDate = null;
	  let productId = null;
	  let productName = null;
	  let hasError = false;

	  $selected.each(function() {
	    const $cb = $(this);
	    const clOrderId   = $cb.data('cl-order-id');
	    const requiredQty = parseInt($cb.data('required-qty')) || 0;   // ✅
	    const rawDueDate  = $cb.data('due-date');

	    if (!productId) {
	      productId   = $cb.data('product-id');
	      productName = $cb.data('product-name');
	    }
	    if (!clOrderId) { alert('수주번호가 없는 항목이 있습니다.'); hasError = true; return false; }

	    const dueDate = parseDueDate(rawDueDate);
	    if (!dueDate) { alert(`수주 [${clOrderId}]의 납기일이 유효하지 않습니다: ${rawDueDate || '없음'}`); hasError = true; return false; }

	    const clientName = $cb.data('client-name'); // ✅ 거래처명 가져오기
	    
	    totalRequired += requiredQty;
	    clOrderIds.push(clOrderId);
	    mergedOrders.push({ 
	      clOrderId, 
	      orderQty: requiredQty,
	      clientName: clientName, // ✅ 거래처명 추가
	      dueDate: rawDueDate     // ✅ 납기일도 추가
	    });

	    if (!earliestDueDate || dueDate < earliestDueDate) earliestDueDate = dueDate;
	  });

	  if (hasError) return null;
	  if (clOrderIds.length === 0) { alert('선택된 수주가 없습니다.'); return null; }
	  if (!earliestDueDate) { alert('납기일 정보를 확인할 수 없습니다.'); return null; }
	  if (totalRequired <= 0) { alert('생산필요 수량이 0입니다.'); return null; }

	  // ✅ 상세(객체 배열)는 sessionStorage에 저장해 팝업으로 전달
	  try {
	    sessionStorage.setItem('WO_MERGED_ORDERS', JSON.stringify(mergedOrders));
	  } catch (e) { console.warn('sessionStorage 실패', e); }

	  return {
	    clOrderIds,                 // URL 파라미터용
	    productId,
	    productName,
	    orderQty: totalRequired,    // 합계
	    dueDate: formatDate(earliestDueDate),
	    mergedCount: clOrderIds.length
	  };
	}
  
  function parseDueDate(rawDate) {
    if (!rawDate) return null;
    if (rawDate instanceof Date) return rawDate;
    if (typeof rawDate === 'string' && /^\d{4}-\d{2}-\d{2}/.test(rawDate)) return new Date(rawDate + 'T00:00:00');
    const d = new Date(rawDate);
    return isNaN(d.getTime()) ? null : d;
  }

  function formatDate(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  // 디버깅용 전역
  window.debugOrderSelection = function() {
    console.log('=== 선택된 수주 데이터 ===');
    $('.order-checkbox:checked').each(function() {
      const $cb = $(this);
      console.log({
        clOrderId: $cb.data('cl-order-id'),
        productId: $cb.data('product-id'),
        productName: $cb.data('product-name'),
        orderQty: $cb.data('order-qty'),
        requiredQty: $cb.data('required-qty'),
        dueDate: $cb.data('due-date')
      });
    });
  };

})(jQuery);