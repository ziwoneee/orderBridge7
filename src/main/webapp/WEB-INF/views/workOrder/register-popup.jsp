<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>

<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/popup-style.css">

<head>
  <title>작업지시 등록</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.1">
  <!-- 🎯 핵심 CSS만 선별해서 사용 -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/vendors/css/vendor.bundle.base.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/vertical-layout-light/style.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/orderBridge.css">
  
  <!-- 아이콘용 CDN -->
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@mdi/font@6.5.95/css/materialdesignicons.min.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
  
  <!-- jQuery -->
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>

<body>
  <div class="page-title">
    <i class="fas fa-plus-circle"></i>
    작업지시 등록
  </div>


      <!-- 작업지시 등록 폼 -->
<form id="workOrderForm" method="post" action="/workorder/register">
  <div class="container p-3">

    <!-- 좌우 레이아웃 (반응형 2단) -->
    <div class="row">
      <!-- 작업지시 정보 (좌측) -->
      <div class="col-md-6 mb-3">
        <div class="card border-primary h-100">
          <div class="card-header bg-primary text-white">
            <i class="fas fa-cogs"></i> 작업지시 정보
          </div>
          <div class="card-body">
            <!-- 생산라인 -->
            <div class="form-group">
              <label>생산라인 <span class="text-danger">*</span></label>
              <select class="form-control" name="lineId" required>
                <option value="">선택하세요</option>
                <option value="L-01">L-01</option>
                <option value="L-02">L-02</option>
                <option value="L-03">L-03</option>
              </select>
            </div>

            <!-- 우선순위 -->
            <div class="form-group">
              <label>우선순위 <span class="text-danger">*</span></label>
              <select class="form-control" name="priority" required>
                <option value="HIGH">높음</option>
                <option value="NORMAL" selected>보통</option>
                <option value="LOW">낮음</option>
              </select>
            </div>

            <!-- 지시수량 -->
            <div class="form-group">
              <label>지시 수량 <span class="text-danger">*</span></label>
              <input type="number" name="orderQty" class="form-control" value="${requiredQty}" min="1" max="999999" required>
              <small class="form-text text-muted">
                권장 생산수량: <strong class="text-primary">${requiredQty}개</strong>
              </small>
            </div>

            <!-- 특이사항 -->
            <div class="form-group">
              <label>특이사항</label>
              <textarea class="form-control" name="remarks" rows="3" placeholder="작업 시 주의사항이나 특이사항 입력"></textarea>
            </div>
          </div>
        </div>
      </div>

      <!-- 수주 정보 (우측) -->
      <div class="col-md-6 mb-3">
        <div class="card border-primary h-100 d-flex flex-column justify-content-between">
          <div>
            <div class="card-header bg-primary text-white">
              <i class="fas fa-file-contract"></i> 수주 정보
            </div>
            <div class="card-body">
              <div class="form-group">
                <label>수주번호</label>
                <input type="text" class="form-control" value="${clOrderId}" readonly>
                <input type="hidden" name="clOrderId" value="${clOrderId}">
              </div>
              <div class="form-group">
                <label>제품명</label>
                <input type="text" class="form-control" value="${productName}" readonly>
                <input type="hidden" name="productId" value="${productId}">
              </div>
              <div class="form-group">
                <label>거래처</label>
                <input type="text" class="form-control" value="${clientName}" readonly>
              </div>
              <div class="form-group">
                <label>납기일</label>
                <input type="text" class="form-control" value="<fmt:formatDate value='${dueDate}' pattern='yyyy-MM-dd' />" readonly>
                <input type="hidden" name="dueDate" value="<fmt:formatDate value='${dueDate}' pattern='yyyy-MM-dd' />">
              </div>
              
              <input type="hidden" name="status" value="WAITING">
              
            </div>
          </div>
          

          <!-- 버튼 영역 -->
          <div class="card-footer bg-transparent border-0 d-flex justify-content-between">
            <button type="button" class="btn btn-light btn-lg" onclick="window.close()">
              <i class="fas fa-times"></i> 취소
            </button>
            <button type="submit" class="btn btn-primary btn-lg">
              <i class="fas fa-save"></i> 작업지시 등록
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</form>


<script>
$(document).ready(function() {
  
  // 폼 제출 처리
  $('#workOrderForm').on('submit', function(e) {
    e.preventDefault();
    
    // 입력 검증
    if (!validateForm()) {
      return false;
    }
    
    // 버튼 로딩 상태
    const submitBtn = $('.register-btn');
    const originalText = submitBtn.html();
    submitBtn.html('<span class="spinner-border spinner-border-sm mr-2"></span>등록 중...').prop('disabled', true);
    
    // 폼 데이터 수집
    const formData = $(this).serialize();
    
    $.ajax({
      url: '/workorder/register',
      type: 'POST',
      data: formData,
      dataType: 'json',
      success: function(response) {
        if (response.success) {
          alert('작업지시가 성공적으로 등록되었습니다.\n작업지시번호: ' + response.orderId);
          
          // 부모 창 새로고침 (목록 업데이트)
          if (window.opener) {
            window.opener.location.reload();
          }
          
          // 팝업 닫기
          window.close();
        } else {
          alert('작업지시 등록에 실패했습니다: ' + response.message);
          submitBtn.html(originalText).prop('disabled', false);
        }
      },
      error: function(xhr, status, error) {
        console.error('등록 실패:', error);
        alert('작업지시 등록에 실패했습니다. 다시 시도해주세요.');
        submitBtn.html(originalText).prop('disabled', false);
      }
    });
  });
  
  // 폼 검증 함수
  function validateForm() {
    let isValid = true;
    
    // 생산라인 검증
    if (!$('select[name="lineId"]').val()) {
      showError('select[name="lineId"]', '생산라인을 선택해주세요.');
      isValid = false;
    }
    
    // 우선순위 검증
    if (!$('select[name="priority"]').val()) {
      showError('select[name="priority"]', '우선순위를 선택해주세요.');
      isValid = false;
    }
    
    // 수량 검증
    const qty = parseInt($('input[name="orderQty"]').val());
    if (!qty || qty < 1) {
      showError('input[name="orderQty"]', '1개 이상 입력해주세요.');
      isValid = false;
    }
    
    return isValid;
  }
  
  // 에러 표시 함수
  function showError(selector, message) {
    const field = $(selector);
    field.addClass('is-invalid');
    field.siblings('.invalid-feedback').remove();
    field.after(`<div class="invalid-feedback">${message}</div>`);
  }
  
  // 지시수량 입력 검증
  $('.quantity-input').on('input', function() {
    const value = parseInt($(this).val());
    const recommended = parseInt('${param.requiredQty}');
    
    $(this).removeClass('is-invalid is-valid');
    $(this).siblings('.invalid-feedback').remove();
    
    if (value > recommended * 2) {
      $(this).addClass('is-invalid');
      $(this).after('<div class="invalid-feedback">권장 수량의 2배를 초과할 수 없습니다.</div>');
    } else if (value < 1) {
      $(this).addClass('is-invalid');
      $(this).after('<div class="invalid-feedback">1개 이상 입력해주세요.</div>');
    } else {
      $(this).addClass('is-valid');
    }
  });
  
  // 우선순위 선택 시 색상 변경
  $('.priority-select').on('change', function() {
    const priority = $(this).val();
    $(this).removeClass('priority-high priority-normal priority-low is-invalid');
    $(this).siblings('.invalid-feedback').remove();
    
    if (priority) {
      $(this).addClass('priority-' + priority.toLowerCase());
    }
  });
  
  // 생산라인 선택 시 검증 해제
  $('.line-select').on('change', function() {
    if ($(this).val()) {
      $(this).removeClass('is-invalid');
      $(this).siblings('.invalid-feedback').remove();
    }
  });
  
  // ESC 키로 팝업 닫기
  $(document).on('keydown', function(e) {
    if (e.key === 'Escape') {
      if (confirm('작업지시 등록을 취소하시겠습니까?')) {
        window.close();
      }
    }
  });
});
</script>

<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.6.0/js/bootstrap.bundle.min.js"></script>
</body>
</html>