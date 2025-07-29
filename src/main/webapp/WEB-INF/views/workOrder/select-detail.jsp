<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<div class="detail-table-container">
  <table class="detail-table table table-bordered">
    <thead>
      <tr>
        <th style="width: 35%;">
          <i class="fas fa-box header-icon"></i>제품명
        </th>
        <th style="width: 15%;">
          <i class="fas fa-list-ol header-icon"></i>수주 수량
        </th>
        <th style="width: 15%;">
          <i class="fas fa-edit header-icon"></i>지시 수량
        </th>
        <th style="width: 20%;">
          <i class="fas fa-flag header-icon"></i>우선순위
        </th>
        <th style="width: 15%;">
          <i class="fas fa-plus-circle header-icon"></i>작업지시
        </th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="item" items="${detailList}" varStatus="status">
        <tr data-product-id="${item.productId}">
          <td class="product-name">${item.productName}</td>
          <td>
            <div class="order-quantity">
              <fmt:formatNumber value="${item.orderQty}" pattern="#,##0"/>
            </div>
          </td>
          <td>
            <input type="number" 
                   class="form-control quantity-input" 
                   min="1" 
                   max="${item.orderQty}"
                   name="orderQty-${item.productId}" 
                   value="${item.orderQty}" 
                   data-original="${item.orderQty}"
                   oninput="validateQuantity(this)"
                   title="1 ~ ${item.orderQty} 범위로 입력하세요">
          </td>
          <td>
            <select class="form-control priority-select" 
                    name="priority-${item.productId}"
                    title="작업 우선순위를 선택하세요">
              <option value="LOW">🔵 낮음</option>
              <option value="NORMAL" selected>🟢 보통</option>
              <option value="HIGH">🟡 높음</option>
              <option value="EMERGENCY">🔴 긴급</option>
            </select>
          </td>
          <td>
            <button class="btn register-btn" 
                    onclick="openRegisterPopup('${item.clOrderId}', '${item.productId}')"
                    title="작업지시 등록 팝업 열기">
              <i class="fas fa-plus"></i> 등록
            </button>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty detailList}">
        <tr>
          <td colspan="5" class="text-muted py-4">
            <i class="fas fa-info-circle"></i>
            해당 수주에 등록된 제품이 없습니다.
          </td>
        </tr>
      </c:if>
    </tbody>
  </table>
</div>

<script>
  // 수량 입력 검증 함수
  function validateQuantity(input) {
    const value = parseInt(input.value);
    const max = parseInt(input.getAttribute('max'));
    const min = parseInt(input.getAttribute('min'));
    
    // 입력값 검증
    if (isNaN(value) || value < min || value > max) {
      input.classList.add('is-invalid');
      input.classList.remove('is-valid');
    } else {
      input.classList.add('is-valid');
      input.classList.remove('is-invalid');
    }
  }

  // 모든 수량 입력 필드에 이벤트 리스너 추가
  $(document).ready(function() {
    $('.quantity-input').each(function() {
      validateQuantity(this);
    });
  });

  function openRegisterPopup(clOrderId, productId) {
    // 현재 행에서 사용자가 입력한 값들 가져오기
    const currentRow = $(`tr[data-product-id="${productId}"]`);
    const orderQtyInput = currentRow.find(`input[name="orderQty-${productId}"]`);
    const prioritySelect = currentRow.find(`select[name="priority-${productId}"]`);
    
    // 입력값 검증
    if (orderQtyInput.hasClass('is-invalid')) {
      alert('올바른 지시 수량을 입력해주세요.');
      orderQtyInput.focus();
      return;
    }
    
    const orderQty = orderQtyInput.val();
    const priority = prioritySelect.val();
    
    // 버튼 로딩 상태
    const btn = event.target.closest('button');
    const originalText = btn.innerHTML;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 로딩...';
    btn.classList.add('btn-loading');
    
    // 팝업 크기 계산 (화면 크기에 따라 조절)
    const width = Math.min(900, screen.width * 0.8);
    const height = Math.min(750, screen.height * 0.8);
    const left = (screen.width - width) / 2;
    const top = (screen.height - height) / 2;
    
    // URL 파라미터 구성
    const params = new URLSearchParams({
      clOrderId: clOrderId,
      productId: productId,
      orderQty: orderQty,
      priority: priority
    });
    
    const url = `/workorder/register-popup?${params}`;
    
    try {
      // 팝업 창 열기
      const popup = window.open(
        url, 
        'registerPopup', 
        `width=${width},height=${height},left=${left},top=${top},resizable=yes,scrollbars=yes`
      );
      
      // 팝업이 차단되었는지 확인
      if (!popup || popup.closed || typeof popup.closed == 'undefined') {
        alert('팝업이 차단되었습니다. 팝업 차단을 해제하고 다시 시도해주세요.');
      } else {
        // 팝업에 포커스
        popup.focus();
        
        // 팝업이 닫힐 때까지 모니터링
        const checkClosed = setInterval(function() {
          if (popup.closed) {
            clearInterval(checkClosed);
            // 부모 창 새로고침 (선택사항)
            // window.location.reload();
          }
        }, 1000);
      }
    } catch (error) {
      console.error('팝업 열기 실패:', error);
      alert('팝업을 열 수 없습니다. 브라우저 설정을 확인해주세요.');
    } finally {
      // 버튼 상태 복원
      setTimeout(() => {
        btn.innerHTML = originalText;
        btn.classList.remove('btn-loading');
      }, 1000);
    }
  }

  // 엔터키로 다음 필드로 이동
  $('.quantity-input').on('keypress', function(e) {
    if (e.which === 13) {
      const currentRow = $(this).closest('tr');
      const nextRow = currentRow.next('tr');
      if (nextRow.length > 0) {
        nextRow.find('.quantity-input').focus().select();
      }
    }
  });

  // 우선순위 변경시 색상 반영
  $('.priority-select').on('change', function() {
    const value = $(this).val();
    $(this).removeClass('priority-emergency priority-high priority-normal priority-low');
    $(this).addClass('priority-' + value.toLowerCase());
  });

  // 초기 우선순위 색상 설정
  $('.priority-select').each(function() {
    $(this).trigger('change');
  });
</script>