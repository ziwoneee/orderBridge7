<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
  
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">

  <%@ include file="/WEB-INF/views/main/top.jsp" %>      

  <div class="container-fluid page-body-wrapper">

    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>
    
    <!-- 본문 시작 -->
      <div class="main-panel">
        <div class="content-wrapper">
          <div class="row">
          
          <!-- 제목 -->
			<div class="col-12 mb-4">
			  <h3 class="font-weight-bold">실시간 재고 조회</h3>
			</div>
			


    		<!-- [1] 검색 필터 -->
    		   <div class="d-flex justify-content-between mb-3">
			<form id="stockForm" action="${pageContext.request.contextPath}/product/stocklist" method="get" class="form-inline mb-4">
				<input type="hidden" name="mode" value="${param.mode}" />
			  <input type="date" name="startDate" class="form-control mr-2" value="${param.startDate}">
			  <span>~</span>
			  <input type="date" name="endDate" class="form-control mx-2" value="${param.endDate}">
			<!--   <button type="submit" class="btn btn-primary">조회</button> -->
			 
 			<hr>
			<!-- ✅ 버튼은 form 밖에 두어도 됨 -->
			 <div class="mb-1">
			 <input type="text" name="keyword" class="form-control mr-2" placeholder="제품명, LOT 번호 검색" value="${param.keyword}">
			  <button type="button" class="btn btn-success" onclick="viewStock()">현재고 내역 조회</button>
			  <button type="button" class="btn btn-primary" onclick="viewAll()">전체 내역 조회</button>
			</div>
			</form>
			</div>

    <!-- [2] 재고 테이블 -->  
     <div class="table-responsive mt-4">
    <table id = "stockTable" class="table table-bordered table-hover">
        <thead>
        <tr>
            <th>제품명</th>
            <th>LOT 번호</th>
            <th>현재고</th>
            <th>안전재고</th>
            <th>생산일자</th>
            <th>유통기한</th>            
            <th>재고상태</th>
            <th>상세내역</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="item" items="${stockList}">
            <tr class="highlight-row">
                <td>${item.productName}</td>
                <td>${item.lotNo}</td>
                <td>${item.stockQty}</td>
                <td>${item.safeQty}</td>
                <td><fmt:formatDate value="${item.regDate}" pattern="yyyy-MM-dd"/></td>
                <td><fmt:formatDate value="${item.expireDate}" pattern="yyyy-MM-dd"/></td>
                <td>
                   <c:choose>
    <c:when test="${item.stockQty == 0}">
        <span class="status-complete">완료</span>
    </c:when>
    <c:when test="${item.stockQty lt item.safeQty}">
        <span class="status-low">부족</span>
    </c:when>
    <c:otherwise>
        <span class="status-normal">정상</span>
    </c:otherwise>
</c:choose>

                </td>
                <td>
                   <button class="btn btn-outline-secondary btn-sm"
        data-toggle="modal"
        data-target="#detailModal"
        data-product="${item.productName}"
        data-lot="${item.lotNo}">
    내역확인
</button>

                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <div class="d-flex justify-content-end mt-3">
  <a href="#" class="btn btn-warning ml-3">+ 목록다운로드</a>
</div>

</div>
</div>
    <!-- [3] 상세내역 모달 -->
<div class="modal fade" id="detailModal" tabindex="-1" role="dialog" aria-labelledby="detailModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">
                    <span id="modalProductName"></span> (<span id="modalLotNo"></span>) 입출고 내역
                </h5>
                <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
            </div>
            <div class="modal-body">
                <!-- ✅ Ajax로 동적 데이터 삽입될 영역 -->
                <table class="table table-sm table-bordered">
                    <thead>
                        <tr>
                            <th>입력일</th>
                            <th>구분</th>
                            <th>수량</th>
                            <th>비고</th>
                        </tr>
                    </thead>
                    <tbody id="modalTransactionBody">
                        <!-- JavaScript에서 Ajax로 삽입 -->
                    </tbody>
                </table>
                <div class="alert alert-info mt-3">
                    제품의 실시간 입고/출고 내역을 확인할 수 있습니다.
                </div>
            </div>
        </div>
    </div>
</div>
    
   <!-- ✅ 페이징 영역 -->
         <!-- 페이지네이션 -->
<!-- ✅ Bootstrap 페이징 스타일 -->
<div class="d-flex justify-content-center mt-4">
<nav>
  <ul class="pagination justify-content-center mt-4">

    <c:if test="${pageMaker.cri.page>1}">
      <li class="page-item">
        <a class="page-link" href="?page=${pageMaker.startPage - 1}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">&laquo;</a>
      </li>
    </c:if>

    <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
      <li class="page-item ${p == cri.page ? 'active' : ''}">
        <a class="page-link" href="?page=${p}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">${p}</a>
      </li>
    </c:forEach>

    <c:if test="${pageMaker.cri.page<pageMaker.endPage}">
      <li class="page-item">
        <a class="page-link" href="?page=${pageMaker.cri.page + 1}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">&raquo;</a>
      </li>
    </c:if>

  </ul>
  
</nav>

</div>
<!-- 페이징 처리 끝 -->
	
	 	</div>
        <!-- content-wrapper 끝 -->
	  <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
     </div>
     <!-- 본문.jsp main-panel ends -->
  </div>   
  <!-- container-fluid page-body-wrapper 끝 -->
</div>
<!-- container-scroller 끝-->   


<!-- ✅ DataTables JS -->
<script src="https://cdn.datatables.net/1.10.24/js/jquery.dataTables.min.js"></script>

<!-- ✅ 모달 관련 JS도 이 아래에 유지하세요 -->
<script>
    $('#detailModal').on('show.bs.modal', function (event) {
        const button = $(event.relatedTarget);
        const product = button.data('product');
        const lot = button.data('lot');
        $('#modalProductName').text(product);
        $('#modalLotNo').text(lot);

        // Ajax로 내역 불러오기
        $.getJSON("/product/transaction", { product: product, lot: lot }, function(data) {
            let html = "";
            if (data.length === 0) {
                html = "<tr><td colspan='4'>내역이 없습니다.</td></tr>";
            } else {
                data.forEach(row => {
                    html += `<tr>
                        <td>${row.reg_date}</td>
                        <td>${row.type}</td>
                        <td>${row.qty}</td>
                        <td>${row.memo || ''}</td>
                    </tr>`;
                });
            }
            $('#modalTransactionBody').html(html);
        });
    });
</script>

<script>
    $(document).ready(function () {
        $('#stockTable').DataTable({
            paging: false,          // 기존 페이징과 충돌 방지
            info: false,            // 하단 정보 제거
            ordering: true,         // 정렬 가능
            searching: false,       // ✅ 검색창 제거
            language: {
                "emptyTable": "데이터가 없습니다",
                "zeroRecords": "일치하는 결과가 없습니다",
                "infoEmpty": "표시할 항목이 없습니다",
                "loadingRecords": "로딩 중...",
                "processing": "처리 중...",
                "paginate": {
                    "first": "처음",
                    "last": "마지막",
                    "next": "다음",
                    "previous": "이전"
                }
            },
            columnDefs: [
                { targets: [6], orderable: false }  // 상세내역 버튼 정렬 제외
            ]
        });
    });
</script>

<script>
  function viewStock() {
    const form = document.getElementById('stockForm');
    if (!form) return alert("폼이 없습니다.");

    let modeInput = document.querySelector('#stockForm input[name="mode"]');
    if (!modeInput) {
      modeInput = document.createElement('input');
      modeInput.type = 'hidden';
      modeInput.name = 'mode';
      form.appendChild(modeInput);
    }
    modeInput.value = 'stock';  // 현재고 모드
    form.submit();
  }

  function viewAll() {
    const form = document.getElementById('stockForm');
    if (!form) return alert("폼이 없습니다.");

    let modeInput = document.querySelector('#stockForm input[name="mode"]');
    if (!modeInput) {
      modeInput = document.createElement('input');
      modeInput.type = 'hidden';
      modeInput.name = 'mode';
      form.appendChild(modeInput);
    }
    modeInput.value = 'all';  // 전체 보기 모드
    form.submit();
  }
</script>