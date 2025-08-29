/**
 * 작업지시 관리 JavaScript
 * workorder.js
 */

// 부모창: 팝업이 postMessage로 알려줄 때 새로고침
window.addEventListener('message', function(ev) {
  try {
    if (ev.origin !== window.location.origin) return; // 보안
    if (ev.data && ev.data.type === 'WORK_ORDER_CREATED') {
      location.reload(); // ✅ 리스트 즉시 새로고침
    }
  } catch (e) {
    console.warn('postMessage 처리 중 오류', e);
  }
});

// 부모창: localStorage 이벤트로도 새로고침 (팝업이 닫힌 후에도 동작)
window.addEventListener('storage', function(e) {
  if (e.key === 'WORK_ORDER_REFRESH') {
    location.reload(); // ✅ 리스트 즉시 새로고침
  }
});

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

    // 팝업에서 데이터 받기 위한 전역 함수 등록
    window.receiveOrderData = receiveOrderData;
    
    // 모달이 닫힐 때 내용 초기화
    $('#detailModal').on('hidden.bs.modal', function () {
        $(this).find('.modal-content').empty();
    });

    // ============================
    // ✅ IN_PROGRESS 드롭다운 차단
    // (상태 변경 select가 있는 경우 방어)
    // ============================
    $('.js-status-select option[value="IN_PROGRESS"]').remove();

    console.log('작업지시 관리 JavaScript 로드 완료');
});

// ========================================================================
// 작업지시 상세 및 수정 관련
// ========================================================================

/**
 * 작업지시 상세 모달 열기
 */
function openDetailModal(orderId) {
    if (!orderId) {
        alert('작업지시 번호가 없습니다.');
        return;
    }

    $.ajax({
        url: '/workorder/detail-modal',
        type: 'GET',
        data: { orderId: orderId },
        success: function(data) {
            // 모달 내용 채우기
            $('#detailModal .modal-content').html(data);
            // 모달 열기
            $('#detailModal').modal('show');
        },
        error: function(xhr, status, error) {
            console.error('작업지시 상세 정보 로드 실패:', error);
            alert("작업지시 상세 정보를 불러오는 데 실패했습니다.");
        }
    });
}

/**
 * 편집 모드 활성화
 */
function enableEditMode() {
    // 모든 보기 모드 요소 숨기기
    $('.view-mode').hide();
    $('.view-mode-buttons').hide();
    
    // 모든 편집 모드 요소 보이기
    $('.edit-mode').show();
    $('.edit-mode-buttons').show();
    
    // 모달 타이틀 변경
    $('.modal-title').text('작업지시 수정');
}

/**
 * 편집 모드 취소
 */
function cancelEditMode() {
    // 모든 편집 모드 요소 숨기기
    $('.edit-mode').hide();
    $('.edit-mode-buttons').hide();
    
    // 모든 보기 모드 요소 보이기
    $('.view-mode').show();
    $('.view-mode-buttons').show();
    
    // 모달 타이틀 원복
    $('.modal-title').text('작업지시 상세');
    
    // 변경사항 되돌리기 (원래 값으로)
    const workOrderData = $('#workOrderData');
    $('#lineSelect').val(workOrderData.data('line-id'));
    $('#remarksTextarea').val(workOrderData.data('remarks'));
}

/**
 * 변경사항 저장
 */
function saveChanges() {
    // data 속성에서 값 가져오기
    const workOrderData = $('#workOrderData');
    const orderId = workOrderData.data('order-id');
    
    // 현재 입력값들
    const lineId = $('#lineSelect').val();
    const remarks = $('#remarksTextarea').val();
    const priority = $('#prioritySelect').val(); // ← 우선순위 전송
    
    // 유효성 검사
    if (!lineId) {
        alert('라인을 선택해주세요.');
        return;
    }
    
    if (!priority) {
        alert('우선순위를 선택해주세요.');
        return;
    }
    
    // Ajax로 수정 요청
    $.ajax({
        url: '/workorder/edit',
        method: 'POST',
        data: {
            orderId: orderId,
            priority: priority,
            lineId: lineId,
            remarks: remarks
        },
        success: function(response) {
            if (response === 'success') {
                alert('작업지시가 성공적으로 수정되었습니다.');
                
                // 화면 업데이트 (페이지 새로고침 없이)
                $('#lineDisplay').text(lineId);
                $('#remarksDisplay').text(remarks || '-');
                
                // data 속성 업데이트
                workOrderData.data('line-id', lineId);
                workOrderData.data('remarks', remarks);
                workOrderData.data('priority', priority);
                
                // 편집 모드 종료
                cancelEditMode();
                
                // 페이지 새로고침으로 목록도 업데이트
                setTimeout(function() {
                    location.reload();
                }, 1000);
            }
        },
        error: function(xhr, status, error) {
            console.error('수정 실패:', error);
            let errorMsg = '작업지시 수정에 실패했습니다.';
            if (xhr.responseText) {
                errorMsg = xhr.responseText;
            }
            alert(errorMsg);
        }
    });
}

/**
 * 작업지시 삭제 확인
 */
function confirmDelete(orderId) {
    if (!orderId) {
        alert('작업지시 번호가 없습니다.');
        return;
    }
    
    if (confirm('정말로 이 작업지시를 삭제하시겠습니까?\n작업지시번호: ' + orderId + '\n삭제된 데이터는 복구할 수 없습니다.')) {
        deleteWorkOrder(orderId);
    }
}

/**
 * 작업지시 삭제 처리
 */
function deleteWorkOrder(orderId) {
    console.log('삭제 요청 - orderId:', orderId);
    
    if (!orderId) {
        alert('작업지시 번호가 없습니다.');
        return;
    }
    
    $.ajax({
        url: '/workorder/delete/' + orderId,
        type: 'DELETE',
        success: function(response) {
            if (response.success) {
                alert(response.message || '작업지시가 삭제되었습니다.');
                $('#detailModal').modal('hide');
                location.reload();
            } else {
                alert(response.message || '삭제에 실패했습니다.');
            }
        },
        error: function(xhr, status, error) {
            console.error('삭제 실패:', error);
            let errorMsg = '작업지시 삭제에 실패했습니다.';
            if (xhr.responseText) {
                errorMsg += '\n' + xhr.responseText;
            }
            alert(errorMsg);
        }
    });
}

// ========================================================================
// ✅ 생산 시작 (READY → IN_PROGRESS)
// 상세 모달에서 생산 시작 (READY -> IN_PROGRESS)
// ========================================================================
$(document).on('click', '.js-start-production', function (e) {
  e.preventDefault();
  e.stopPropagation();

  const orderId = $(this).data('order-id');
  if (!orderId) return alert('작업지시 번호가 없습니다.');

  if (!confirm('이 작업지시를 생산 시작(IN_PROGRESS)으로 전환할까요?')) return;

  $.ajax({
    url: '/workorder/start/' + encodeURIComponent(orderId), // ★ 전용 URL
    method: 'POST',
    success: function (res) {
      if (res && res.success) {
        alert('생산을 시작했습니다.');
        $('#detailModal').modal('hide');
        setTimeout(() => location.reload(), 150);
      } else {
        alert(res && res.message ? res.message : '처리 실패');
      }
    },
    error: function () {
      alert('서버 오류가 발생했습니다.');
    }
  });
});


// ========================================================================
// 작업지시 등록 관련
// ========================================================================

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
 * 팝업에서 선택한 수주 데이터 처리
 */
function receiveOrderData(orderData) {
    if (!orderData || !orderData.clOrderIds || orderData.clOrderIds.length === 0) {
        alert("유효하지 않은 수주 정보입니다.");
        return;
    }

    console.log("받은 병합 수주 정보:", orderData);

    // 수량, 납기일 입력
    $('#orderQty').val(orderData.orderQty);
    $('#dueDate').val(orderData.dueDate);

    // 숨겨진 수주번호들 추가
    $('#workOrderForm').find('input[name="clOrderIds"]').remove(); // 기존 제거
    orderData.clOrderIds.forEach(clOrderId => {
        $('#workOrderForm').append(`<input type="hidden" name="clOrderIds" value="${clOrderId}">`);
    });

    // BOM 불러오기
    loadBom(orderData.productId, orderData.orderQty);
}

// ========================================================================
// 유틸리티 함수
// ========================================================================

/**
 * 날짜 포맷팅 함수
 */
function formatDate(dateString) {
    if (!dateString) return '';
    
    try {
        let date;
        
        if (dateString.includes('T')) {
            date = new Date(dateString);
        } else if (dateString.includes('-')) {
            date = new Date(dateString + 'T00:00:00');
        } else {
            date = new Date(dateString);
        }
        
        if (isNaN(date.getTime())) {
            return dateString;
        }
        
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

function loadBom(productId, orderQty) {
    $.ajax({
        url: '/workorder/getBomByProduct',
        type: 'GET',
        data: { 
            productId: productId, 
            orderQty: orderQty 
        },
        success: function(bomList) {
            var tbody = $('#bomTableBody');
            tbody.empty();
            
            if (!bomList || bomList.length === 0) {
                tbody.html('<tr><td colspan="6" class="text-center text-muted">BOM 정보 없음</td></tr>');
                return;
            }
            
            var packs = orderQty / 10;
            
            for (var i = 0; i < bomList.length; i++) {
                var item = bomList[i];
                var total = item.qty * packs;
                var row = '<tr>' +
                            '<td>' + item.materialId + '</td>' +
                            '<td>' + item.materialName + '</td>' +
                            '<td>' + item.materialType + '</td>' +
                            '<td class="text-center">' + item.qty + '</td>' +
                            '<td class="text-center font-weight-bold">' + total.toFixed(1) + '</td>' +
                            '<td>' + item.unit + '</td>' +
                          '</tr>';
                tbody.append(row);
            }
        },
        error: function() {
            $('#bomTableBody').html('<tr><td colspan="6" class="text-center text-danger">로딩 실패</td></tr>');
        }
    });
}
