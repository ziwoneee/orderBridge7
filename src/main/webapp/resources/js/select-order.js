/**
 * 확정 수주 선택 JavaScript
 * select-order.js
 */

$(document).ready(function () {
    // 전체 선택/해제 체크박스 추가 (테이블 헤더에)
    initSelectAllCheckbox();
    
    // 체크박스 변경 시 선택 정보 업데이트
    $(document).on('change', '.order-checkbox', updateSelectionInfo);
    
    // 병합 등록 버튼 클릭
    $('#mergeSelectBtn').on('click', processMergeOrders);
    
    // 검색 엔터키 처리
    $('input[name="keyword"]').on('keypress', function(e) {
        if (e.which === 13) {
            $(this).closest('form').submit();
        }
    });
    
    // ESC 키로 팝업 닫기
    $(document).on('keydown', function(e) {
        if (e.key === 'Escape') {
            if (confirm('선택을 취소하고 창을 닫으시겠습니까?')) {
                window.close();
            }
        }
    });
});

/**
 * 전체 선택 체크박스 초기화
 */
function initSelectAllCheckbox() {
    const headerHtml = `
        <th width="50">
            <input type="checkbox" id="selectAll" title="전체 선택">
        </th>
    `;
    
    // 헤더의 첫 번째 th가 "선택"이면 체크박스 추가
    const $firstTh = $('table thead tr th:first');
    if ($firstTh.text().trim() === '선택') {
        $firstTh.html(headerHtml);
    }
    
    // 전체 선택/해제 이벤트
    $(document).on('change', '#selectAll', function() {
        const isChecked = $(this).prop('checked');
        $('.order-checkbox').prop('checked', isChecked);
        updateSelectionInfo();
    });
}

/**
 * 선택된 항목 정보 업데이트
 */
function updateSelectionInfo() {
    const $selected = $('.order-checkbox:checked');
    const count = $selected.length;
    
    // 선택 정보 표시 영역이 없으면 생성
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
        // 선택된 제품 정보 확인
        const products = new Map();
        let totalQty = 0;
        
        $selected.each(function() {
            const productId = $(this).data('product-id');
            const productName = $(this).data('product-name');
            const qty = parseInt($(this).data('order-qty')) || 0;
            
            totalQty += qty;
            
            if (!products.has(productId)) {
                products.set(productId, { name: productName, count: 0, qty: 0 });
            }
            products.get(productId).count++;
            products.get(productId).qty += qty;
        });
        
        // 정보 업데이트
        $('#selectedCount').text(count);
        $('#totalQuantity').text(totalQty.toLocaleString());
        
        // 제품별 정보 표시
        if (products.size === 1) {
            const [productId, productInfo] = products.entries().next().value;
            $('#productInfo').html(`<strong>제품:</strong> ${productInfo.name}`);
            $('#selectionInfo').removeClass('alert-warning').addClass('alert-info');
        } else {
            $('#productInfo').html(`<span class="text-danger">⚠ 서로 다른 제품 ${products.size}종이 선택되었습니다</span>`);
            $('#selectionInfo').removeClass('alert-info').addClass('alert-warning');
        }
        
        $('#selectionInfo').slideDown();
        
        // 버튼 활성화/비활성화
        $('#mergeSelectBtn').prop('disabled', products.size !== 1);
        
    } else {
        $('#selectionInfo').slideUp();
        $('#mergeSelectBtn').prop('disabled', true);
    }
}

/**
 * 병합 수주 처리
 */
function processMergeOrders() {
    const $selected = $('.order-checkbox:checked');
    
    // 1. 선택 검증
    if ($selected.length === 0) {
        alert('선택된 수주가 없습니다.');
        return;
    }
    
    // 2. 동일 제품 검증
    const productValidation = validateSameProduct($selected);
    if (!productValidation.valid) {
        alert(productValidation.message);
        return;
    }
    
    // 3. 병합 데이터 수집
    const mergedData = collectMergedData($selected);
    if (!mergedData) {
        return;
    }
    
    // 4. 확인 메시지
    const confirmMsg = `
다음 내용으로 작업지시를 등록하시겠습니까?

▶ 제품: ${mergedData.productName}
▶ 병합 수주: ${mergedData.clOrderIds.length}건
▶ 총 수량: ${mergedData.orderQty.toLocaleString()}
▶ 납기일: ${mergedData.dueDate}
    `.trim();
    
    if (!confirm(confirmMsg)) {
        return;
    }
    
    // 5. URL 파라미터 구성
    const clOrderParams = mergedData.clOrderIds
        .map(id => `clOrderIds=${encodeURIComponent(id)}`)
        .join('&');

    const productParam = `productId=${encodeURIComponent(mergedData.productId)}`;
    const qtyParam = `orderQty=${mergedData.orderQty}`;
    const dateParam = `dueDate=${encodeURIComponent(mergedData.dueDate)}`;
    
    const fullUrl = `/workorder/register-popup?${clOrderParams}&${productParam}&${qtyParam}&${dateParam}`;

    // 6. 팝업 창 열기
    const popup = window.open(
        fullUrl,
        "registerPopup",
        "width=1000,height=800,scrollbars=yes"
    );

    // 7. (선택) 수주 선택 팝업 닫기
    if (popup) {
        window.close();
    } else {
        alert("팝업 차단이 되어있을 수 있습니다. 브라우저 설정을 확인해주세요.");
    }
}
/**
 * 동일 제품 검증
 */
function validateSameProduct($selected) {
    const products = new Set();
    const productNames = new Set();
    
    $selected.each(function() {
        const productId = $(this).data('product-id');
        const productName = $(this).data('product-name');
        
        if (productId) {
            products.add(productId);
            productNames.add(productName);
        }
    });
    
    if (products.size === 0) {
        return { valid: false, message: '제품 정보를 확인할 수 없습니다.' };
    }
    
    if (products.size > 1) {
        const names = Array.from(productNames).join(', ');
        return { 
            valid: false, 
            message: `서로 다른 제품이 선택되었습니다.\n선택된 제품: ${names}\n\n동일한 제품만 선택해주세요.` 
        };
    }
    
    return { valid: true };
}

/**
 * 병합 데이터 수집
 */
function collectMergedData($selected) {
    const orderList = [];
    let totalQty = 0;
    let earliestDueDate = null;
    let productId = null;
    let productName = null;
    
    // 데이터 수집
    let hasError = false;
    
    $selected.each(function() {
        const $cb = $(this);
        const clOrderId = $cb.data('cl-order-id');
        const orderQty = parseInt($cb.data('order-qty')) || 0;
        const rawDueDate = $cb.data('due-date');
        
        // 제품 정보 (첫 번째 항목에서만)
        if (!productId) {
            productId = $cb.data('product-id');
            productName = $cb.data('product-name');
        }
        
        console.log(`수주: ${clOrderId}, 수량: ${orderQty}, 납기일: ${rawDueDate}`);
        
        // 필수 데이터 검증
        if (!clOrderId) {
            alert('수주번호가 없는 항목이 있습니다.');
            hasError = true;
            return false; // each 중단
        }
        
        // 날짜 검증 및 파싱
        const dueDate = parseDueDate(rawDueDate);
        if (!dueDate) {
            alert(`수주 [${clOrderId}]의 납기일이 유효하지 않습니다: ${rawDueDate || '없음'}`);
            hasError = true;
            return false;
        }
        
        // 데이터 누적
        totalQty += orderQty;
        orderList.push(clOrderId);
        
        // 가장 빠른 납기일 계산
        if (!earliestDueDate || dueDate < earliestDueDate) {
            earliestDueDate = dueDate;
        }
    });
    
    if (hasError) {
        return null;
    }
    
    // 최종 검증
    if (orderList.length === 0) {
        alert('선택된 수주가 없습니다.');
        return null;
    }
    
    if (!earliestDueDate) {
        alert('납기일 정보를 확인할 수 없습니다.');
        return null;
    }
    
    // 반환 데이터
    return {
        clOrderIds: orderList,
        productId: productId,
        productName: productName,
        orderQty: totalQty,
        dueDate: formatDate(earliestDueDate),
        mergedCount: orderList.length
    };
}

/**
 * 납기일 파싱
 */
function parseDueDate(rawDate) {
    if (!rawDate) return null;
    
    try {
        // 다양한 날짜 형식 처리
        let date;
        
        // 이미 Date 객체인 경우
        if (rawDate instanceof Date) {
            date = rawDate;
        }
        // ISO 형식 (2025-01-10)
        else if (typeof rawDate === 'string' && /^\d{4}-\d{2}-\d{2}/.test(rawDate)) {
            date = new Date(rawDate + 'T00:00:00');
        }
        // 기타 문자열
        else {
            date = new Date(rawDate);
        }
        
        // 유효성 검증
        if (isNaN(date.getTime())) {
            console.error('Invalid date:', rawDate);
            return null;
        }
        
        return date;
        
    } catch (error) {
        console.error('Date parsing error:', error, rawDate);
        return null;
    }
}

/**
 * 날짜 포맷팅 (YYYY-MM-DD)
 */
function formatDate(date) {
    if (!date || !(date instanceof Date)) return '';
    
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    
    return `${year}-${month}-${day}`;
}

/**
 * 디버깅용 - 선택된 데이터 콘솔 출력
 */

function debugSelectedData() {
    console.log('=== 선택된 수주 데이터 ===');
    $('.order-checkbox:checked').each(function() {
        const $cb = $(this);
        console.log({
            clOrderId: $cb.data('cl-order-id'),
            productId: $cb.data('product-id'),
            productName: $cb.data('product-name'),
            orderQty: $cb.data('order-qty'),
            dueDate: $cb.data('due-date')
        });
    });
}

// 전역 함수로 등록 (디버깅용)
window.debugOrderSelection = debugSelectedData;