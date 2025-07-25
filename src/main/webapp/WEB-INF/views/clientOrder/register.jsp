<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script src="https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>


<div class="container-scroller">

  <%@ include file="/WEB-INF/views/main/top.jsp" %>      

  <div class="container-fluid page-body-wrapper">

    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

    <style>
        .cart-item {margin-bottom: 15px; padding: 15px; border: 1px solid #dee2e6; border-radius: 0.5rem;}
        #clientSuggestionList { position: absolute; z-index: 9999; width: 100%; background: #fff !important; border: 1px solid #ccc; max-height: 200px; overflow-y: auto; }
        #clientSuggestionList a { display: block; padding: 8px 12px; text-decoration: none; color: #000; font-size: 14px;}
        #clientSuggestionList a:hover { background: #f1f1f1;}
         #submitBtn:disabled {cursor: not-allowed;  opacity: 0.6;}
    </style>

    <div class="main-panel">
        <div class="content-wrapper" >
            <div class="row">                  
                <div class="col-md-12 grid-margin">
                    <div class="col-12 mb-4">
          <h3 class="font-weight-bold">신규 수주 등록</h3>
        </div>
                        
<div class="contentbody" style="width:1200px;">
 <!-- 거래처 정보 입력 -->
 <div class="card p-3 mb-4">
  <div class="row">
    <!-- 왼쪽: 거래처 정보 -->
    <div class="col-md-8" >
      <div class="form-group">
        <label>거래처명</label>
        <input type="text" class="form-control" id="clientNameInput" placeholder="거래처명을 입력하세요" autocomplete="off" >
        <div id="clientSuggestionList"></div>
      </div>
      <div class="form-group">
        <label>담당자</label>
        <input type="text" class="form-control" id="clientManager" name="clientManager">
      </div>
      <div class="form-group">
        <label>전화번호</label>
        <input type="text" class="form-control" id="clientPhone" name="clientPhone">
      </div>
      <div class="form-group">
  <label>주소</label>
  <div class="input-group">
    <input type="text" id="deliveryAddressView" name="deliveryAddressView" class="form-control" placeholder="주소를 입력하세요" readonly>
    <div class="input-group-append">
      <button type="button" class="btn btn-outline-secondary" onclick="execDaumPostcode()">주소 검색</button>
    </div>
  </div>
</div>

<!-- 상세주소 입력창 (처음에는 숨김) -->
<div class="form-group" id="detailAddressWrapper" style="display: none;">
  <label>상세주소</label>
  <input type="text" id="deliveryAddressDetail" class="form-control" placeholder="상세주소를 입력하세요" required>
</div>
      
    </div>
   
     <!-- 오른쪽: 납기 요청일 -->
    
  </div>
</div>
<!-- 주문 폼 -->
<form id="orderForm" method="post" action="${pageContext.request.contextPath}/clientorder/register">
<div class="col-md-4">
      <div class="form-group">
        <label>납기 요청일</label>
        <input type="date" name="clDeliveryDate" id="clDeliveryDate" class="form-control" required>
      </div>
       <div class="form-group">
  <label>요청 사항</label>
  <textarea class="form-control" id="detailMemo" name="clOrderMemo" rows="3" maxlength="10" placeholder="최대 10자까지 입력 가능" required></textarea>
<small id="memoHelp" class="form-text text-muted text-right d-block">
  <span id="memoCharCount">0</span>/10자 입력
</small>

</div>

    </div>
                            <input type="hidden" id="clientId" name="clientId">                            
							<input type="hidden" name="postCode" id="postCodeHidden">                            
                            <input type="hidden" name="deliveryAddress" id="deliveryAddress">
                            <input type="hidden" name="clOrderStatus" value="주문접수"/>
                            <!-- 관리자ID숨김 -->
                            <input type="hidden" name="adminId" value="${sessionScope.adminId}">
                            
                            
                            <div id="productCart1"></div>
                            <button type="button" class="btn btn-primary" onclick="addProductItem()">+ 제품 추가</button>
                            <button type="submit" class="btn btn-success" id="submitBtn">수주 등록</button>
                            <a href="${pageContext.request.contextPath}/clientorder/list" class="btn btn-secondary">목록</a>
                        </form>
                    </div>
                </div>
            </div>
        
       </div>
        <!-- content-wrapper 끝 -->
	  <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
     </div>
     <!-- 본문.jsp main-panel ends -->
  </div>   
  <!-- container-fluid page-body-wrapper 끝 -->
</div>
<!-- container-scroller 끝-->   

<!-- jQuery -->
<script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
<script>
let productList = [];
let productCount = 0;

// 최초 실행
$(document).ready(function () {
    // 1. 제품목록 AJAX 불러오기
    $.get("${pageContext.request.contextPath}/clientorder/products", function (products) {
        // 서버가 표준 JSON을 보내주므로, 받은 데이터를 그대로 사용합니다.
        // 불필요한 변환 로직을 모두 제거합니다.
        productList = products;

        // 정상적으로 데이터가 들어오는지 콘솔에서 확인
        console.log("서버에서 받은 제품 목록:", productList);

        // 첫 번째 제품 항목을 추가합니다.
        addProductItem();
        updateSubmitButtonState(); // 최초 버튼 상태
    });


    // 2. 거래처 자동완성 (이 부분은 그대로 둡니다)
    $('#clientNameInput').on('input', function () {
        const keyword = $(this).val().trim();
        const suggestionBox = $('#clientSuggestionList');
        if (keyword.length < 1) {
            suggestionBox.empty();
            return;
        }
        $.get("${pageContext.request.contextPath}/clientorder/clients", function (clients) {
            suggestionBox.empty();
            clients.forEach(client => {
                if (client.clientName && client.clientName.includes(keyword)) {
                    const item = $('<a href="#">'+client.clientName+'</a>');
                    item.on("click", function (e) {
                        e.preventDefault();
                        console.log(client);
                        $('#clientNameInput').val(client.clientName);
                        $('#clientId').val(client.clientId);
                        $('#clientPhone').val(client.clientTel);                       
                        $('#deliveryAddressView').val((client.address || '') + ' ' + (client.addressDetail || ''));
                        $('#deliveryAddress').val((client.address || '') + ' ' + (client.addressDetail || ''));
                        $('#postCodeHidden').val(client.postCode);
                        $('#clientManager').val(client.managerName);
                     
                        suggestionBox.empty();
                    });
                    suggestionBox.append(item);
                }
            });
        });
    });
});

//제품추가
function addProductItem() {
    const index = productCount++;
    let productOptions = "";
    for (let i = 0; i < productList.length; i++) {
        productOptions += '<option value="' + productList[i].productId + '">' + productList[i].productName + '</option>';
    }

    var itemHtml =
        '<div class="cart-item product-row" id="productItem-' + index + '">' + // ✅ 여기 수정
            '<div class="form-row">' +
                '<div class="form-group col-md-5">' +
                    '<label>제품</label>' +
                    '<select name="productId" class="form-control" required>' +
                        '<option value="">-- 제품 선택 --</option>' +
                        productOptions +
                    '</select>' +
                '</div>' +
                '<div class="form-group col-md-3">' +
                    '<label>단가</label>' +
                    '<input type="number" name="unitPrice" class="form-control" readonly>' +
                '</div>' +
                '<div class="form-group col-md-3">' +
                    '<label>수량</label>' +
                    '<input type="number" name="orderQty" class="form-control" required min="10" step="10">' +
                '</div>' +
                '<div class="form-group col-md-1 d-flex align-items-end">' +
                    '<button type="button" class="btn btn-danger btn-block" onclick="removeProductItem(this)">삭제</button>' + // ✅ this 전달
                '</div>' +
            '</div>' +
            '<div class="form-row">' +
                '<div class="form-group col-md-12">' +
                    '<input type="text" name="detailMemo" class="form-control" placeholder="상세 메모(선택)">' +
                '</div>' +
            '</div>' +
        '</div>';

    $("#productCart1").append(itemHtml);

   

 // 제품 선택 시 단가, 수량 자동 설정
    $("#productItem-" + index + " select[name='productId']").on("change", function () {
        const selectedProductId = $(this).val();
        const row = $(this).closest('.form-row');
        const unitPriceInput = row.find("input[name='unitPrice']");
        const orderQtyInput = row.find("input[name='orderQty']");

        // ✅ 중복 검사 정확하게 다시 구현
        let isDuplicate = false;
        $("select[name='productId']").each(function () {
            if ($(this).val() === selectedProductId && this !== row.find("select")[0]) {
                isDuplicate = true;
            }
        });

        if (isDuplicate) {
            alert("이미 선택한 제품입니다.");
            $(this).val(""); // 선택 해제
            unitPriceInput.val("");
            orderQtyInput.val("");
            updateProductOptions(); // 옵션 초기화
            return; // 이후 코드 실행 방지
        }

        const selectedProduct = productList.find(p => p.productId === selectedProductId);
        if (selectedProduct) {
            unitPriceInput.val(selectedProduct.unitPrice);
            orderQtyInput.val(10);
        } else {
            unitPriceInput.val("");
            orderQtyInput.val("");
        }

        updateProductOptions(); // 선택 후 옵션 재적용
        updateSubmitButtonState();
    });


}

// ✅ 삭제 함수
function removeProductItem(button) {
    const row = button.closest('.product-row');
    if (row) {
        row.remove();
        updateProductOptions(); // ✅ 삭제 후 옵션 업데이트
        updateSubmitButtonState();
    }
}



function updateProductOptions() {
    const selectedValues = [];

    // 현재 선택된 productId들을 배열로 수집
    $("select[name='productId']").each(function () {
        const val = $(this).val();
        if (val) selectedValues.push(val);
    });

    // 모든 select 요소 순회하면서 선택된 값 disable 처리
    $("select[name='productId']").each(function () {
        const currentSelect = this;
        $(this).find("option").each(function () {
            const optionValue = $(this).val();

            if (!optionValue) return; // '-- 제품 선택 --' 은 제외

            const isSelectedHere = (currentSelect.value === optionValue);
            if (!isSelectedHere && selectedValues.includes(optionValue)) {
                $(this).attr("disabled", true);
            } else {
                $(this).attr("disabled", false);
            }
        });
    });
}


</script>

<script>
// ✅ 등록 버튼 상태 업데이트 함수
function updateSubmitButtonState() {
  const productSelects = document.querySelectorAll("select[name='productId']");
  let hasValidProduct = false;

  productSelects.forEach(select => {
    if (select.value && select.value !== "") {
      hasValidProduct = true;
    }
  });

  document.getElementById("submitBtn").disabled = !hasValidProduct;
}
</script>

<script>
  // 대한민국 공휴일 배열 (yyyy-mm-dd 형식)
  const holidays = [ "2025-08-15", "2025-09-16" ];

  // 주말(토/일) 또는 공휴일인지 확인하는 함수
  function isHolidayOrWeekend(date) {
    const yyyyMMdd = date.toISOString().split('T')[0];
    const day = date.getDay();
    return (day === 0 || day === 6 || holidays.includes(yyyyMMdd));
  }

  // 오늘 이후 평일/공휴일 제외하고 3일 뒤의 날짜 계산
  function getValidMinDate(daysToAdd) {
    const date = new Date();
    let added = 0;

    while (added < daysToAdd) {
      date.setDate(date.getDate() + 1);
      if (!isHolidayOrWeekend(date)) {
        added++;
      }
    }

    return date.toISOString().split('T')[0];
  }

  document.addEventListener("DOMContentLoaded", function () {
    const input = document.getElementById("clDeliveryDate");
    const minDate = getValidMinDate(3); // 평일 기준 3일 뒤
    input.min = minDate;

    input.addEventListener("change", function () {
      const val = input.value;
      if (!val) return; // 값이 없을 경우 무시

      const date = new Date(val);
      if (isNaN(date.getTime())) return; // 잘못된 날짜 무시

      if (isHolidayOrWeekend(date)) {
        alert("주말 또는 공휴일은 선택할 수 없습니다.");
        input.value = "";
      }
    });
  });
</script>


<script>
  document.addEventListener("DOMContentLoaded", function () {
    const input = document.getElementById("detailMemo");
    const counter = document.getElementById("memoCharCount");

    input.addEventListener("input", function () {
      counter.textContent = input.value.length;
    });
  });
</script>

<script>
function execDaumPostcode() {
  new daum.Postcode({
    oncomplete: function(data) {
    	    	// 우편번호 설정
        document.getElementById("postCodeHidden").value = data.zonecode;
    	
      // 도로명주소 선택 시
      var fullRoadAddr = data.roadAddress;
      var extraAddr = '';

      if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
        extraAddr += data.bname;
      }
      if(data.buildingName !== '' && data.apartment === 'Y'){
        extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName);
      }
      if(extraAddr !== ''){
        fullRoadAddr += ' (' + extraAddr + ')';
      }

      // 주소 입력창에 넣기
      document.getElementById("deliveryAddressView").value = fullRoadAddr;
      document.getElementById("deliveryAddress").value = fullRoadAddr;
   
   // ✅ 상세주소 입력창 보이게 하기
      document.getElementById("detailAddressWrapper").style.display = 'block';
      document.getElementById("deliveryAddressDetail").value = '';
      
    }
  }).open();
}
</script>

<script>
document.addEventListener("DOMContentLoaded", function () {
  const viewField = document.getElementById('deliveryAddressView');
  const detailWrapper = document.getElementById('detailAddressWrapper');

  viewField.addEventListener('focus', function () {
    // 상세주소창 보여주기
    detailWrapper.style.display = 'block';
  });

  // 최종 주소 조합 (폼 제출 시)
  document.getElementById("orderForm").addEventListener("submit", function () {
	// 📍 주소 조합
	const main = viewField.value;
    const detail = document.getElementById('deliveryAddressDetail').value;
    document.getElementById('deliveryAddress').value = main + ' ' + detail;
  

 // submit 시점에서도 추가 검사 및 방어
    orderForm.addEventListener("submit", function (e) {
      const productSelects = document.querySelectorAll("select[name='productId']");
      let hasSelectedProduct = false;
      productSelects.forEach(select => {
        if (select.value !== "") {
          hasSelectedProduct = true;
        }
      });

      if (!hasSelectedProduct) {
        alert("최소 1개 이상의 제품을 선택해야 합니다.");
        e.preventDefault();
      }
    });
  
});
</script>