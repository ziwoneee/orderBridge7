<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>작업지시 등록</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.1">
    
    <!-- CSS 로드 -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/vendors/css/vendor.bundle.base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/vertical-layout-light/style.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@mdi/font@6.5.95/css/materialdesignicons.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/popup-style.css">
    
    <!-- jQuery -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>

<body>
    <!-- 페이지 제목 -->
    <div class="page-title">
        <i class="fas fa-plus-circle"></i>
        작업지시 등록
    </div>

    <!-- 작업지시 등록 폼 -->
    <form id="workOrderForm" method="post" action="/workorder/register">
        <div class="container-fluid">
            <div class="row">
                
                <!-- 수주 정보 (좌측) -->
                <div class="col-md-6 mb-3">
                    <div class="card border-primary">
                        <div class="card-header bg-primary text-white">
                            <i class="fas fa-file-contract"></i> 수주 정보
                        </div>
                        <div class="card-body">
                            <div class="row">
                                <div class="col-md-6">
                                    <div class="form-group">
                                        <label>수주번호</label>
                                        <input type="text" class="form-control" value="${clOrderId}" readonly>
                                        <input type="hidden" name="clOrderId" value="${clOrderId}">
                                    </div>
                                    <div class="form-group">
                                        <label>거래처</label>
                                        <input type="text" class="form-control" value="${clientName}" readonly>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="form-group">
                                        <label>제품명</label>
                                        <input type="text" class="form-control" value="${productName}" readonly>
                                        <input type="hidden" name="productId" value="${productId}">
                                    </div>
                                    <div class="form-group">
                                        <label>납기일</label>
                                        <input type="text" class="form-control" value="<fmt:formatDate value='${dueDate}' pattern='yyyy-MM-dd' />" readonly>
                                        <input type="hidden" name="dueDate" value="<fmt:formatDate value='${dueDate}' pattern='yyyy-MM-dd' />">
                                    </div>
                                </div>
                            </div>
                            <input type="hidden" name="status" value="WAITING">
                        </div>
                    </div>
                </div>

                <!-- 작업지시 정보 (우측) -->
                <div class="col-md-6 mb-3">
                    <div class="card border-primary">
                        <div class="card-header bg-primary text-white">
                            <i class="fas fa-cogs"></i> 작업지시 정보
                        </div>
                        <div class="card-body">
                            <div class="row">
                                <div class="col-md-6">
                                    <div class="form-group">
                                        <label>생산라인 <span class="text-danger">*</span></label>
                                        <select class="form-control line-select" name="lineId" required>
                                            <option value="">선택하세요</option>
                                            <option value="L-01">L-01</option>
                                            <option value="L-02">L-02</option>
                                            <option value="L-03">L-03</option>
                                        </select>
                                    </div>
                                    <div class="form-group">
                                        <label>지시 수량 <span class="text-danger">*</span></label>
                                        <input type="number" name="orderQty" class="form-control quantity-input" value="${requiredQty}" min="1" max="999999" required>
                                        <small class="form-text text-muted">권장 수량: <strong class="text-primary">${requiredQty}개</strong></small>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="form-group">
                                        <label>우선순위 <span class="text-danger">*</span></label>
                                        <select class="form-control priority-select" name="priority" required>
                                            <option value="HIGH">높음</option>
                                            <option value="NORMAL" selected>보통</option>
                                            <option value="LOW">낮음</option>
                                        </select>
                                    </div>
                                    <div class="form-group">
                                        <label>특이사항</label>
                                        <textarea class="form-control" name="remarks" rows="2" placeholder="특이사항 입력"></textarea>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- BOM 정보 -->
            <div class="row">
                <div class="col-12 mb-3">
                    <div class="card border-info">
                        <div class="card-header bg-info text-white">
                            <i class="fas fa-list-alt"></i> BOM 정보
                        </div>
                        <div class="card-body">
                            <table class="table table-bordered bom-table">
                                <thead class="thead-light">
                                    <tr>
                                        <th width="15%">자재 코드</th>
                                        <th width="25%">자재명</th>
                                        <th width="15%">자재 용도</th>
                                        <th width="15%">1팩당 소요량</th>
                                        <th width="20%">총 소요량</th>
                                        <th width="10%">단위</th>
                                    </tr>
                                </thead>
                                <tbody id="bomTableBody">
                                    <tr>
                                        <td colspan="6" class="text-center text-muted">BOM 정보를 불러오는 중...</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 버튼 영역 -->
            <div class="row">
                <div class="col-12">
                    <div class="d-flex justify-content-between">
                        <button type="button" class="btn btn-light" onclick="window.close()">
                            취소
                        </button>
                        <button type="submit" class="btn btn-primary register-btn">
                            등록
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </form>

    <script>
    $(document).ready(function() {
        
        // === 초기화 ===
        initializePage();
        
        // === 이벤트 바인딩 ===
        bindEvents();
        
        /**
         * 페이지 초기화
         */
        function initializePage() {
            loadBomData();
        }
        
        /**
         * 이벤트 바인딩
         */
        function bindEvents() {
            // 지시수량 변경 이벤트
            $('.quantity-input').on('input', handleQuantityChange);
            
            // 생산라인/우선순위 선택 이벤트
            $('.line-select, .priority-select').on('change', handleSelectChange);
            
            // 폼 제출 이벤트
            $('#workOrderForm').on('submit', handleFormSubmit);
            
            // ESC 키 이벤트
            $(document).on('keydown', handleKeyDown);
        }
        
        /**
         * 수량 변경 처리
         */
        function handleQuantityChange() {
            const value = parseInt($(this).val());
            const recommended = parseInt('${requiredQty}');
            
            clearValidation($(this));
            
            if (value > recommended * 2) {
                showValidationError($(this), '권장 수량의 2배를 초과할 수 없습니다.');
            } else if (value < 1) {
                showValidationError($(this), '1개 이상 입력해주세요.');
            } else {
                showValidationSuccess($(this));
                calculateBom();
            }
        }
        
        /**
         * 선택 필드 변경 처리
         */
        function handleSelectChange() {
            if ($(this).val()) {
                clearValidation($(this));
            }
        }
        
        /**
         * 폼 제출 처리
         */
        function handleFormSubmit(e) {
            e.preventDefault();
            
            if (!validateForm()) {
                return false;
            }
            
            submitWorkOrder();
        }
        
        /**
         * 키보드 이벤트 처리
         */
        function handleKeyDown(e) {
            if (e.key === 'Escape') {
                if (confirm('작업지시 등록을 취소하시겠습니까?')) {
                    window.close();
                }
            }
        }
        
        /**
         * BOM 데이터 로드
         */
        function loadBomData() {
            const productId = '${productId}';
            const orderQty = parseInt($('.quantity-input').val()) || 0;

            $.ajax({
                url: '/workorder/getBomByProduct',
                type: 'GET',
                data: { 
                    productId: productId,
                    orderQty: orderQty
                },
                dataType: 'json',
                success: function(bomData) {
                    renderBomTable(bomData);
                    calculateBom();
                },
                error: function(xhr, status, error) {
                    showBomError('BOM 정보를 불러올 수 없습니다.');
                }
            });
        }
        
        /**
         * BOM 테이블 렌더링
         */
        function renderBomTable(bomData) {
            const tbody = $('#bomTableBody');
            tbody.empty();

            if (!bomData || bomData.length === 0) {
                tbody.html('<tr><td colspan="6" class="text-center text-muted">등록된 BOM 정보가 없습니다.</td></tr>');
                return;
            }

            bomData.forEach(function(item) {
                const row = `
                    <tr>
                        <td>\${item["materialId"]}</td>
                        <td>\${item["materialName"]}</td>
                        <td>\${item["materialType"]}</td>
                        <td class="text-right">\${item["qty"]}</td>
                        <td class="text-right">\${item["totalQty"]}</td>
                        <td>\${item["unit"]}</td>
                    </tr>
                `;
                tbody.append(row);
            });
        }
        
        /**
         * BOM 총량 계산
         */
        function calculateBom() {
            const orderQty = parseInt($('.quantity-input').val()) || 0;

            $('#bomTableBody tr').each(function() {
                const unitQty = parseFloat($(this).find('td:nth-child(4)').text()) || 0;
                const totalQty = unitQty * orderQty;
                const displayQty = Number.isInteger(totalQty) ? totalQty.toLocaleString() : totalQty.toFixed(1);
                $(this).find('td:nth-child(5)').text(displayQty);
            });
        }
        
        /**
         * 작업지시 등록 요청
         */
        function submitWorkOrder() {
            const submitBtn = $('.register-btn');
            const originalText = submitBtn.html();
            
            // 버튼 로딩 상태
            submitBtn.html('<span class="spinner-border spinner-border-sm mr-2"></span>등록 중...').prop('disabled', true);
            
            $.ajax({
                url: '/workorder/register',
                type: 'POST',
                data: $('#workOrderForm').serialize(),
                dataType: 'json',
                success: function(response) {
                    handleSubmitSuccess(response);
                },
                error: function(xhr, status, error) {
                    handleSubmitError();
                    submitBtn.html(originalText).prop('disabled', false);
                }
            });
        }
        
        /**
         * 등록 성공 처리
         */
        function handleSubmitSuccess(response) {
            if (response.success) {
                alert('작업지시가 성공적으로 등록되었습니다.\n작업지시번호: ' + response.orderId);
                
                if (window.opener) {
                    window.opener.location.reload();
                }
                
                window.close();
            } else {
                alert('작업지시 등록에 실패했습니다: ' + response.message);
                $('.register-btn').html('등록').prop('disabled', false);
            }
        }
        
        /**
         * 등록 실패 처리
         */
        function handleSubmitError() {
            alert('작업지시 등록에 실패했습니다. 다시 시도해주세요.');
        }
        
        /**
         * 폼 검증
         */
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
        
        /**
         * 검증 상태 초기화
         */
        function clearValidation(field) {
            field.removeClass('is-invalid is-valid');
            field.siblings('.invalid-feedback').remove();
        }
        
        /**
         * 검증 성공 표시
         */
        function showValidationSuccess(field) {
            field.addClass('is-valid');
        }
        
        /**
         * 검증 오류 표시
         */
        function showValidationError(field, message) {
            field.addClass('is-invalid');
            field.after(`<div class="invalid-feedback">${message}</div>`);
        }
        
        /**
         * 에러 표시
         */
        function showError(selector, message) {
            const field = $(selector);
            field.addClass('is-invalid');
            field.siblings('.invalid-feedback').remove();
            field.after(`<div class="invalid-feedback">${message}</div>`);
        }
        
        /**
         * BOM 에러 표시
         */
        function showBomError(message) {
            $('#bomTableBody').html(`<tr><td colspan="6" class="text-center text-muted">${message}</td></tr>`);
        }
    });
    </script>

    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.6.0/js/bootstrap.bundle.min.js"></script>
</body>
</html>