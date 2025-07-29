<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title>확정 수주 선택</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.1">
  <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.6.0/css/bootstrap.min.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>

</head>
<body>
  <div class="page-title">
    <i class="fas fa-clipboard-list"></i>
    확정 수주 선택
  </div>

  <!-- 검색 영역 -->
  <div class="search-container">
    <form method="get" action="/workorder/select-order-popup" class="form-inline">
      <div class="input-group flex-fill">
        <div class="input-group-prepend">
          <span class="input-group-text bg-white border-0">
            <i class="fas fa-search text-muted"></i>
          </span>
        </div>
        <input type="text" name="keyword" class="form-control border-0" 
               placeholder="수주번호를 입력하세요" value="${cri.keyword}">
        <div class="input-group-append ml-3">
          <button type="submit" class="btn">
            <i class="fas fa-search"></i> 검색
          </button>
        </div>
      </div>
    </form>
  </div>

  <!-- 수주 목록 테이블 -->
  <div class="table-container">
    <div class="table-responsive">
      <table class="table">
        <thead>
          <tr>
            <th style="width: 50px;"><i class="fas fa-angle-right"></i></th>
            <th style="width: 200px;">수주번호</th>
            <th style="width: 180px;">거래처</th>
            <th>제품명</th>
            <th style="width: 130px;">수주일</th>
            <th style="width: 130px;">납기일</th>
            <th style="width: 100px;">수량</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="order" items="${orderList}">
            <tr class="order-row" data-order-id="${order.clOrderId}" 
                tabindex="0" onkeypress="if(event.key==='Enter') $(this).click()">
              <td><i class="fas fa-chevron-right"></i></td>
              <td class="order-id">${order.clOrderId}</td>
              <td class="client-name">${order.clientName}</td>
              <td>${order.productName}</td>
              <td class="date-text">
                <fmt:formatDate value="${order.clOrderDate}" pattern="yyyy-MM-dd"/>
              </td>
              <td class="date-text">
                <fmt:formatDate value="${order.clDeliveryDate}" pattern="yyyy-MM-dd"/>
              </td>
              <td class="quantity-text">
                <fmt:formatNumber value="${order.orderQty}" pattern="#,##0"/>
              </td>
            </tr>
            <tr id="detail-${order.clOrderId}" class="order-detail" style="display: none;">
              <td colspan="7">로딩 중...</td>
            </tr>
          </c:forEach>
          <c:if test="${empty orderList}">
            <tr>
              <td colspan="7" class="empty-message">
                <i class="fas fa-inbox fa-2x mb-3 d-block"></i>
                확정된 수주가 없습니다.
              </td>
            </tr>
          </c:if>
        </tbody>
      </table>
    </div>
  </div>

  <div class="text-right">
    <button class="btn btn-close-custom" onclick="window.close()">
      <i class="fas fa-times"></i> 닫기
    </button>
  </div>

  <script>
    $(document).ready(function() {
      // 검색 입력창에 포커스
      $('input[name="keyword"]').focus();
      
      // Enter 키로 검색
      $('input[name="keyword"]').on('keypress', function(e) {
        if (e.which === 13) {
          $(this).closest('form').submit();
        }
      });
    });

    $('.order-row').on('click', function () {
      const orderId = $(this).data('order-id');
      const detailRow = $('#detail-' + orderId);
      
      // 다른 모든 상세 행 숨김
      $('.order-detail').not(detailRow).hide();
      
      // 이미 열려있으면 닫기
      if (detailRow.is(':visible')) {
        detailRow.hide();
        return;
      }
      
      // 로딩 상태 표시
      detailRow.show().html(`
        <td colspan="7">
          <div class="loading-content">
            <div class="spinner-border-custom"></div>
            상세 정보를 불러오는 중...
          </div>
        </td>
      `);
      
      // Ajax 요청으로 상세 정보 로딩
      $.get('/workorder/select-detail?clOrderId=' + orderId)
        .done(function (html) {
          detailRow.html('<td colspan="7" class="p-0">' + html + '</td>');
        })
        .fail(function () {
          detailRow.html(`
            <td colspan="7" class="error-message">
              <i class="fas fa-exclamation-triangle"></i>
              상세 정보를 불러오는데 실패했습니다. 다시 시도해주세요.
            </td>
          `);
        });
    });

    // ESC 키로 창 닫기
    $(document).on('keydown', function(e) {
      if (e.key === 'Escape') {
        window.close();
      }
    });
  </script>
  
  <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.6.0/js/bootstrap.bundle.min.js"></script>
</body>
</html>