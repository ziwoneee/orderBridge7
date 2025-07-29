<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title>작업지시 등록</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.1">
  <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.6.0/css/bootstrap.min.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  
</head>
<body>
  <div class="main-container">
    <!-- 헤더 -->
    <div class="form-header">
      <i class="fas fa-clipboard-check"></i>
      작업지시 등록
    </div>

    <!-- 폼 컨테이너 -->
    <div class="form-container">
      <form method="post" action="/workorder/register" id="registerForm">
        <input type="hidden" name="clOrderId" value="${order.clOrderId}">
        <input type="hidden" name="productId" value="${order.productId}">

        <!-- 수주 정보 섹션 (읽기 전용) -->
        <div class="readonly-section">
          <h6 class="section-title">
            <i class="fas fa-info-circle"></i>
            수주 정보
          </h6>
          
          <div class="form-row-custom">
            <div class="form-group">
              <label>
                <i class="fas fa-hashtag"></i>
                작업지시번호
              </label>
              <input type="text" name="orderId" class="form-control" 
                     value="${generatedOrderId}" readonly>
            </div>
            
            <div class="form-group">
              <label>
                <i class="fas fa-box"></i>
                제품명
              </label>
              <input type="text" class="form-control" 
                     value="${order.productName}" readonly>
            </div>
          </div>

          <div class="form-row-custom">
            <div class="form-group">
              <label>
                <i class="fas fa-calendar-alt"></i>
                납기일
              </label>
              <input type="text" class="form-control" 
                     value="<fmt:formatDate value='${order.dueDate}' pattern='yyyy-MM-dd'/>" readonly>
            </div>
            
            <div class="form-group">
              <label>
                <i class="fas fa-industry"></i>
                생산라인
              </label>
              <input type="text" class="form-control" 
                     value="${order.lineId}" readonly>
            </div>
          </div>
        </div>

        <!-- 작업지시 정보 섹션 -->
        <div class="input-section">
          <h6 class="section-title">
            <i class="fas fa-cogs"></i>
            작업지시 정보
          </h6>

          <div class="form-row-custom">
            <div class="form-group">
              <label class="required">
                <i class="fas fa-list-ol"></i>
                작업 수량
                <i class="fas fa-question-circle info-icon" 
                   data-toggle="tooltip" 
                   title="생산할 제품의 수량을 입력하세요"></i>
              </label>
              <input type="number" 
                     name="orderQty" 
                     class="form-control" 
                     min="1" 
                     value="${not empty param.orderQty ? param.orderQty : order.orderQty}"
                     required
                     oninput="validateQuantity(this)">
              <div class="invalid-feedback">
                올바른 수량을 입력해주세요 (1 이상)
              </div>
            </div>

            <div class="form-group">
              <label class="required">
                <i class="fas fa-flag"></i>
                우선순위
                <i class="fas fa-question-circle info-icon" 
                   data-toggle="tooltip" 
                   title="작업의 우선순위를 선택하세요"></i>
              </label>
              <select name="priority" class="form-control priority-select" required>
                <option value="LOW" ${param.priority == 'LOW' ? 'selected' : ''}>
                  🔵 낮음 - 여유있는 작업
                </option>
                <option value="NORMAL" ${param.priority == 'NORMAL' || empty param.priority ? 'selected' : ''}>
                  🟢 보통 - 일반적인 작업
                </option>
                <option value="HIGH" ${param.priority == 'HIGH' ? 'selected' : ''}>
                  🟡 높음 - 우선 처리 필요
                </option>
                <option value="EMERGENCY" ${param.priority == 'EMERGENCY' ? 'selected' : ''}>
                  🔴 긴급 - 즉시 처리 필요
                </option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label>
              <i class="fas fa-sticky-note"></i>
              작업 지시사항
              <i class="fas fa-question-circle info-icon" 
                 data-toggle="tooltip" 
                 title="작업자에게 전달할 특별한 지시사항이 있으면 입력하세요"></i>
            </label>
            <textarea name="memo" 
                      class="form-control" 
                      rows="4" 
                      placeholder="작업자에게 전달할 특별한 지시사항이나 주의사항을 입력하세요...&#10;예: 품질 검사 강화, 특별 포장 요구사항 등"
                      maxlength="500"></textarea>
            <small class="text-muted">
              <span id="memoCount">0</span>/500자
            </small>
          </div>
        </div>

        <!-- 버튼 그룹 -->
        <div class="btn-group-custom">
          <button type="button" class="btn btn-cancel" onclick="cancelForm()">
            <i class="fas fa-times"></i> 취소
          </button>
          <button type="button" class="btn btn-register" onclick="submitForm()">
            <i class="fas fa-check"></i> 등록
          </button>
        </div>
        </form>
      </div>
    </div>


  <script>
    $(document).ready(function() {
      // 툴팁 초기화
      $('[data-toggle="tooltip"]').tooltip();
      
      // 첫 번째 입력 필드에 포커스
      $('input[name="orderQty"]').focus().select();
      
      // 메모 글자수 카운터
      $('textarea[name="memo"]').on('input', function() {
        const count = $(this).val().length;
        $('#memoCount').text(count);
        
        if (count > 450) {
          $('#memoCount').addClass('text-warning');
        } else if (count > 480) {
          $('#memoCount').addClass('text-danger').removeClass('text-warning');
        } else {
          $('#memoCount').removeClass('text-warning text-danger');
        }
      });
      
      // 초기 글자수 설정
      $('textarea[name="memo"]').trigger('input');
    });

    // 수량 검증 함수
    function validateQuantity(input) {
      const value = parseInt(input.value);
      const min = parseInt(input.getAttribute('min'));
      
      if (isNaN(value) || value < min) {
        input.classList.add('is-invalid');
        input.classList.remove('is-valid');
        return false;
      } else {
        input.classList.add('is-valid');
        input.classList.remove('is-invalid');
        return true;
      }
    }

    // 폼 제출 함수
    function submitForm() {
      const form = document.getElementById('registerForm');
      const orderQtyInput = form.querySelector('input[name="orderQty"]');
      const prioritySelect = form.querySelector('select[name="priority"]');
      
      // 입력값 검증
      if (!validateQuantity(orderQtyInput)) {
        alert('올바른 작업 수량을 입력해주세요.');
        orderQtyInput.focus();
        return;
      }
      
      if (!prioritySelect.value) {
        alert('우선순위를 선택해주세요.');
        prioritySelect.focus();
        return;
      }
      
      // 확인 대화상자
      const orderQty = orderQtyInput.value;
      const priority = prioritySelect.options[prioritySelect.selectedIndex].text;
      
      if (confirm(`작업지시를 등록하시겠습니까?\n\n작업 수량: ${parseInt(orderQty).toLocaleString()}개\n우선순위: ${priority}`)) {
        // 버튼 로딩 상태
        const btn = event.target;
        const originalText = btn.innerHTML;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 등록 중...';
        btn.classList.add('btn-loading');
        
        // 일반적인 form submit 사용
        try {
          form.submit();
        } catch (error) {
          console.error('폼 제출 오류:', error);
          alert('작업지시 등록 중 오류가 발생했습니다. 다시 시도해주세요.');
          // 버튼 상태 복원
          btn.innerHTML = originalText;
          btn.classList.remove('btn-loading');
        }
      }
    }

    // 취소 함수
    function cancelForm() {
      if (confirm('작성 중인 내용이 있습니다. 정말 취소하시겠습니까?')) {
        window.close();
      }
    }

    // ESC 키로 창 닫기
    $(document).on('keydown', function(e) {
      if (e.key === 'Escape') {
        cancelForm();
      }
    });

    // Enter 키로 등록
    $(document).on('keydown', function(e) {
      if (e.ctrlKey && e.key === 'Enter') {
        submitForm();
      }
    });

    // 브라우저 닫기 이벤트 처리
    window.addEventListener('beforeunload', function(e) {
      // 입력된 내용이 있는지 확인
      const hasInput = document.querySelector('textarea[name="memo"]').value.trim() !== '';
      if (hasInput) {
        e.preventDefault();
        e.returnValue = '';
      }
    });
  </script>
  
  <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.6.0/js/bootstrap.bundle.min.js"></script>
</body>
</html>