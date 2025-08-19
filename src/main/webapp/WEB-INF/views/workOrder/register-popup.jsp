<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title>작업지시 등록</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.1">

  <link rel="icon" href="${pageContext.request.contextPath}/favicon.ico" type="image/x-icon" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/vendors/css/vendor.bundle.base.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/vertical-layout-light/style.css">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@mdi/font@6.5.95/css/materialdesignicons.min.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/popup-style.css">
</head>
<body>

<div class="page-title">
  <i class="fas fa-plus-circle"></i> 작업지시 등록
</div>

<form id="workOrderForm" method="post" action="/workorder/register">
  <input type="hidden" name="dueDate" value="${dueDate}" />
  <input type="hidden" name="productId" id="productId" value="${productId}" />
  <input type="hidden" name="orderQty" id="orderQty" value="${requiredQty}" />
  <input type="hidden" name="status" value="WAITING">
  <c:forEach var="id" items="${clOrderIds}">
  <input type="hidden" name="clOrderIds" value="${id}" />
  </c:forEach>
  
  <!-- 수주번호들 -->
  <div id="clOrderHiddenInputs"></div>

  <!-- 기본 정보 표시 -->
  <div class="card mb-3">
    <div class="card-body">
      <div class="row">
        <div class="col-md-4">
		  <small class="text-muted">제품명</small>
		  <p class="font-weight-bold">${productName}</p>
		</div>
		<div class="col-md-4">
		  <small class="text-muted">총 생산수량</small>
		 <p class="font-weight-bold">${requiredQty}</p>
		</div>
		<div class="col-md-4">
		  <small class="text-muted">납기일</small>
		  <p class="font-weight-bold">${dueDate}</p>
		</div>
      </div>
      
      <!-- 병합 수주 목록 (2개 이상일 때만 표시) -->
      <div id="mergedOrdersSection" style="display: none;" class="mt-3">
        <hr>
        <a href="#mergedOrdersList" class="text-primary" data-toggle="collapse">
          <i class="fas fa-chevron-down"></i> 병합된 수주 목록 (<span id="mergedCount">0</span>건)
        </a>
        <div class="collapse mt-2" id="mergedOrdersList">
          <table class="table table-sm">
            <thead>
              <tr>
                <th>수주번호</th>
                <th>거래처</th>
                <th class="text-right">수량</th>
              </tr>
            </thead>
            <tbody id="mergedOrdersTableBody">
              <!-- 동적 생성 -->
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <!-- 작업지시 설정 -->
  <div class="card">
    <div class="card-body">
      <div class="row">
        <div class="col-md-6">
          <label>생산라인 <span class="text-danger">*</span></label>
          <select class="form-control" name="lineId" required>
            <c:forEach var="line" items="${lineList}">
              <option value="${line.lineId}">${line.lineName}</option>
            </c:forEach>
          </select>
        </div>
        <div class="col-md-6">
          <label>우선순위 <span class="text-danger">*</span></label>
          <select class="form-control" name="priority" required>
            <option value="HIGH">높음</option>
            <option value="NORMAL" selected>보통</option>
            <option value="LOW">낮음</option>
          </select>
        </div>
      </div>
      <div class="form-group mt-3">
        <label>특이사항</label>
        <textarea class="form-control" name="remarks" rows="2"></textarea>
      </div>
    </div>
  </div>

  <!-- BOM 자재 소요량 -->
  <div class="card mt-3">
    <div class="card-header bg-info text-white">
      <i class="fas fa-boxes"></i> 자재 소요량
    </div>
    <div class="card-body">
      <table class="table table-bordered">
        <thead>
          <tr>
            <th>자재코드</th>
            <th>자재명</th>
            <th>용도</th>
            <th class="text-center">10팩당</th>
            <th class="text-center">총 소요량</th>
            <th>단위</th>
          </tr>
        </thead>
        <tbody id="bomTableBody">
          <tr>
            <td colspan="6" class="text-center text-muted">불러오는 중...</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <!-- 버튼 -->
  <div class="mt-4 text-right">
    <button type="button" class="btn btn-secondary" onclick="window.close()">취소</button>
    <button type="submit" class="btn btn-primary">등록</button>
  </div>
</form>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>

<script>
  const mergedOrders = JSON.parse('${clOrderIdsJson}');
  console.log(" 병합된 수주:", mergedOrders);
</script>
<script>
function loadBom(productId, orderQty) {
  $.ajax({
    url: '/workorder/getBomByProduct',
    type: 'GET',
    data: {
      productId: productId,
      orderQty: orderQty
    },
    success: function(bomList) {
      const tbody = $('#bomTableBody');
      tbody.empty();

      if (!bomList || bomList.length === 0) {
        tbody.html('<tr><td colspan="6" class="text-center text-muted">BOM 정보 없음</td></tr>');
        return;
      }

      let hiddenMaterials = [];
      const packs = orderQty / 10;

      for (let i = 0; i < bomList.length; i++) {
        const item = bomList[i];
        const total = item.qty * packs;

        hiddenMaterials.push({
          materialId: item.materialId,
          requiredQty: total
        });

        const row = '<tr>' +
          '<td>' + item.materialId + '</td>' +
          '<td>' + item.materialName + '</td>' +
          '<td>' + item.materialType + '</td>' +
          '<td class="text-center">' + item.qty + '</td>' +
          '<td class="text-center font-weight-bold">' + total.toFixed(1) + '</td>' +
          '<td>' + item.unit + '</td>' +
          '</tr>';
        tbody.append(row);
      }

      window.materialList = hiddenMaterials;
    },
    error: function() {
      $('#bomTableBody').html('<tr><td colspan="6" class="text-center text-danger">로딩 실패</td></tr>');
    }
  });
}

$(document).ready(function () {
  // 병합 수주 접기/펼치기 아이콘 처리
  $('#mergedOrdersList').on('show.bs.collapse', function () {
    $('a[href="#mergedOrdersList"] i')
      .removeClass('fa-chevron-down')
      .addClass('fa-chevron-up');
  });
  $('#mergedOrdersList').on('hide.bs.collapse', function () {
    $('a[href="#mergedOrdersList"] i')
      .removeClass('fa-chevron-up')
      .addClass('fa-chevron-down');
  });

  // BOM 로딩
  const productId = $('input[name="productId"]').val();
  const orderQty = parseInt($('#orderQty').val());

  if (productId && orderQty) {
    loadBom(productId, orderQty);
  }

  // 중복 제출 방지 + 등록 처리
  let isSubmitting = false;

  $('#workOrderForm').on('submit', function (e) {
    e.preventDefault();

    if (isSubmitting) {
      console.warn("이미 제출 중입니다.");
      return;
    }
    isSubmitting = true;

    const data = {
      productId: productId,
      orderQty: orderQty,
      dueDate: $('input[name="dueDate"]').val(),
      lineId: $('select[name="lineId"]').val(),
      priority: $('select[name="priority"]').val(),
      remarks: $('textarea[name="remarks"]').val(),
      status: "WAITING",
      materialList: window.materialList || [],
      mergedOrders: JSON.parse('${clOrderIdsJson}')
    };

    $('button[type="submit"]').prop('disabled', true).text("등록 중...");

    $.ajax({
      url: '/workorder/register',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(data),
      success: function (res) {
        if (res.success) {
          alert(" 등록 성공!");
          window.close();
        } else {
          alert(" 등록 실패: " + res.message);
          isSubmitting = false;
          $('button[type="submit"]').prop('disabled', false).text("등록");
        }
      },
      error: function (err) {
        alert(" 서버 오류 발생");
        console.error(err);
        isSubmitting = false;
        $('button[type="submit"]').prop('disabled', false).text("등록");
      }
    });
  });
});
</script>


</body>
</html>