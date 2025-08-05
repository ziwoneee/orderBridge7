/**
 * 작업지시 관리 JavaScript
 * workorder.js
 */

$(document).ready(function () {
    // 검색창 Enter 키 이벤트
    $('input[name="keyword"]').focus().on('keypress', function (e) {
        if (e.which === 13) {
            $(this).closest('form').submit();
        }
    });

    // ESC 키로 팝업 닫기 (팝업 환경에서만)
    $(document).on('keydown', function (e) {
        if (e.key === 'Escape') {
            if (window.opener) {
                window.close();
            }
        }
    });

    // 작업지시 등록 팝업 열기
    $('#openOrderPopupBtn').on('click', function (e) {
        e.preventDefault();
        openOrderSelectionPopup();
    });

    // 수정 폼 submit 처리
    $('#editForm').on('submit', function (e) {
        e.preventDefault();
        submitEditForm();
    });

    // 팝업에서 데이터 받기 위한 전역 함수 등록
    window.receiveOrderData = receiveOrderData;
});

/**
 * 수주 선택 팝업 열기
 */
function openOrderSelectionPopup() {
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

    const popup = window.open('/workorder/select-order', 'selectOrderPopup', features);
    
    if (!popup || popup.closed || typeof popup.closed === 'undefined') {
        alert('팝업이 차단되었습니다. 브라우저 설정에서 팝업을 허용해주세요.');
        return;
    }
    
    if (popup.focus) {
        popup.focus();
    }
    
    // 팝업 위치 재조정 (브라우저 호환성)
    setTimeout(function() {
        if (popup && !popup.closed) {
            popup.moveTo(left, top);
            popup.resizeTo(width, height);
            if (popup.focus) {
                popup.focus();
            }
        }
    }, 100);
}

/**
 * 작업지시 상세 모달 열기
 */
function openDetailModal(orderId) {
    if (!orderId) {
        alert('작업지시 번호가 없습니다.');
        return;
    }

    $.ajax({
        url: '/workorder/detail/' + orderId,
        type: 'GET',
        success: function(data) {
            populateDetailModal(data);
            loadBomData(data.productId, data.orderQty);
        },
        error: function(xhr, status, error) {
            console.error('작업지시 상세 정보 로드 실패:', error);
            alert("작업지시 상세 정보를 불러오는 데 실패했습니다.");
        }
    });
}

/**
 * 상세 모달에 데이터 채우기
 */
function populateDetailModal(data) {
    $('#modalOrderId').text(data.orderId || '');
    $('#modalProductName').text(data.productName || '');
    $('#modalOrderQty').text(data.orderQty ? Number(data.orderQty).toLocaleString() : '0');
    $('#modalLineName').text(data.lineName || '');
    $('#modalClientName').text(data.clientName || '');
    
    // 날짜 포맷팅
    $('#modalDueDate').text(formatDate(data.dueDate));
    $('#modalCreatedAt').text(formatDate(data.createdAt));
    
    // 우선순위 표시
    const priorityInfo = getPriorityInfo(data.priority);
    $('#modalPriority').html(`<span class="badge ${priorityInfo.class}">${priorityInfo.text}</span>`);
    
    // 상태 표시
    const statusInfo = getStatusInfo(data.status);
    $('#modalStatusBadge').html(`<span class="badge ${statusInfo.class}">${statusInfo.text}</span>`);
    $('#modalStatus').html(`<span class="badge ${statusInfo.class}">${statusInfo.text}</span>`);

    // 버튼 제어
    controlModalButtons(data.status);
}

/**
 * BOM 데이터 로드
 */
function loadBomData(productId, orderQty) {
    if (!productId || !orderQty) {
        renderBomTable([]);
        $('#detailModal').modal('show');
        return;
    }

    $.ajax({
        url: '/workorder/getBomByProduct',
        type: 'GET',
        data: {
            productId: productId,
            orderQty: orderQty
        },
        success: function(bomList) {
            renderBomTable(bomList);
            $('#detailModal').modal('show');
        },
        error: function(xhr, status, error) {
            console.error('BOM 데이터 로드 실패:', error);
            renderBomTable([]);
            $('#detailModal').modal('show');
        }
    });
}

/**
 * BOM 테이블 렌더링
 */
function renderBomTable(bomList) {
    const tbody = document.getElementById("bomTableBody");
    if (!tbody) return;
    
    tbody.innerHTML = "";

    if (!bomList || bomList.length === 0) {
        tbody.innerHTML = "<tr><td colspan='8' class='text-center text-muted py-3'>자재 정보가 없습니다.</td></tr>";
        return;
    }

    bomList.forEach(item => {
        const row = document.createElement("tr");
        
        // 재고 상태 결정
        const stockInfo = getStockInfo(item.stockQty || 0, item.totalQty || 0);
        
        row.innerHTML = `
            <td>${item.materialId || ''}</td>
            <td>${item.materialName || ''}</td>
            <td>${item.materialType || ''}</td>
            <td>${item.unit || ''}</td>
            <td class="text-end">${Number(item.qty || 0).toLocaleString()}</td>
            <td class="text-end">${Number(item.totalQty || 0).toLocaleString()}</td>
            <td class="text-end">${Number(item.stockQty || 0).toLocaleString()}</td>
            <td><span class="badge ${stockInfo.class}">${stockInfo.text}</span></td>
        `;
        tbody.appendChild(row);
    });
}

/**
 * 작업지시 수정
 */
function editWorkOrder() {
    const orderId = $('#modalOrderId').text();
    const lineName = $('#modalLineName').text();
    const orderQty = $('#modalOrderQty').text().replace(/,/g, '');
    const priorityText = $('#modalPriority').find('.badge').text();
    
    // 우선순위 텍스트를 값으로 변환
    const priorityValue = getPriorityValue(priorityText);

    // 수정 모달에 값 설정
    $('#edit_orderId').val(orderId);
    $('#edit_lineId').val(lineName);
    $('#edit_orderQty').val(orderQty);
    $('#edit_priority').val(priorityValue);

    // 모달 전환
    $('#detailModal').modal('hide');
    setTimeout(() => {
        $('#editModal').modal('show');
    }, 300);
}

/**
 * 작업지시 삭제
 */
function deleteWorkOrder() {
    const orderId = $('#modalOrderId').text();
    
    if (!orderId) {
        alert('작업지시 번호가 없습니다.');
        return;
    }
    
    if (!confirm('정말로 이 작업지시를 삭제하시겠습니까?\n삭제된 데이터는 복구할 수 없습니다.')) {
        return;
    }
    
    $.ajax({
        url: '/workorder/delete/' + orderId,
        type: 'DELETE',
        success: function(response) {
            alert('작업지시가 삭제되었습니다.');
            $('#detailModal').modal('hide');
            location.reload();
        },
        error: function(xhr, status, error) {
            console.error('삭제 실패:', error);
            alert('작업지시 삭제에 실패했습니다.');
        }
    });
}

/**
 * 수정 폼 제출
 */
function submitEditForm() {
    const formData = $('#editForm').serialize();
    const orderQty = $('#edit_orderQty').val();
    
    // 유효성 검사
    if (!orderQty || orderQty <= 0) {
        alert('지시 수량은 1 이상이어야 합니다.');
        $('#edit_orderQty').focus();
        return;
    }

    $.ajax({
        url: '/workorder/update',
        method: 'POST',
        data: formData,
        success: function (response) {
            alert('작업지시가 수정되었습니다.');
            $('#editModal').modal('hide');
            location.reload();
        },
        error: function (xhr, status, error) {
            console.error('수정 실패:', error);
            let errorMsg = '작업지시 수정에 실패했습니다.';
            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMsg += '\n오류: ' + xhr.responseJSON.message;
            }
            alert(errorMsg);
        }
    });
}

/**
 * 팝업에서 선택한 수주 데이터 처리
 */
function receiveOrderData(orderData) {
    if (!orderData) {
        alert('선택된 수주 데이터가 없습니다.');
        return;
    }

    $.ajax({
        url: '/workorder/create',
        method: 'POST',
        data: {
            clOrderId: orderData.clOrderId,
            productId: orderData.productId,
            lineId: orderData.lineId,
            orderQty: orderData.orderQty,
            priority: orderData.priority || 'NORMAL',
            dueDate: orderData.dueDate
        },
        success: function(response) {
            alert('작업지시가 등록되었습니다.');
            location.reload();
        },
        error: function(xhr, status, error) {
            console.error('작업지시 등록 실패:', error);
            let errorMsg = '작업지시 등록에 실패했습니다.';
            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMsg += '\n오류: ' + xhr.responseJSON.message;
            }
            alert(errorMsg);
        }
    });
}

/**
 * 모달 버튼 제어
 */
function controlModalButtons(status) {
    const isWaiting = (status === 'WAITING');
    $('#btnEdit').prop('disabled', !isWaiting);
    $('#btnDelete').prop('disabled', !isWaiting);
}

/**
 * 날짜 포맷팅 함수
 */
function formatDate(dateString) {
    if (!dateString) return '';
    
    try {
        // 다양한 날짜 형식 처리
        let date;
        
        if (dateString.includes('T')) {
            // ISO 형식: 2024-01-15T09:30:00
            date = new Date(dateString);
        } else if (dateString.includes('-')) {
            // 날짜만: 2024-01-15
            date = new Date(dateString + 'T00:00:00');
        } else {
            // 기타 형식
            date = new Date(dateString);
        }
        
        if (isNaN(date.getTime())) {
            return dateString; // 파싱 실패시 원본 반환
        }
        
        // YYYY-MM-DD 형식으로 반환
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        
        return `${year}-${month}-${day}`;
    } catch (error) {
        console.error('날짜 포맷팅 오류:', error);
        return dateString;
    }
}

/**
 * 우선순위 정보 반환
 */
function getPriorityInfo(priority) {
    const priorityMap = {
        'EMERGENCY': { text: '긴급', class: 'badge-danger' },
        'HIGH': { text: '높음', class: 'badge-warning' },
        'NORMAL': { text: '보통', class: 'badge-info' },
        'LOW': { text: '낮음', class: 'badge-secondary' }
    };
    
    return priorityMap[priority] || { text: priority || '', class: 'badge-light' };
}

/**
 * 상태 정보 반환
 */
function getStatusInfo(status) {
    const statusMap = {
        'WAITING': { text: '대기', class: 'badge-secondary' },
        'IN_PROGRESS': { text: '생산중', class: 'badge-warning' },
        'COMPLETED': { text: '생산완료', class: 'badge-success' }
    };
    
    return statusMap[status] || { text: status || '', class: 'badge-light' };
}

/**
 * 재고 상태 정보 반환
 */
function getStockInfo(stockQty, totalQty) {
    if (stockQty >= totalQty) {
        return { text: '충분', class: 'badge-success' };
    } else if (stockQty > 0) {
        return { text: '부족', class: 'badge-warning' };
    } else {
        return { text: '없음', class: 'badge-danger' };
    }
}

/**
 * 우선순위 텍스트를 값으로 변환
 */
function getPriorityValue(priorityText) {
    const textToValue = {
        '긴급': 'EMERGENCY',
        '높음': 'HIGH',
        '보통': 'NORMAL',
        '낮음': 'LOW'
    };
    
    return textToValue[priorityText] || 'NORMAL';
}