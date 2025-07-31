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
                              

          <!-- 검색 영역 -->
          <div class="col-12 mb-3">
            <form id="stockForm" action="${pageContext.request.contextPath}/product/stocklist" method="get" class="form-inline">
              <input type="hidden" name="mode" value="${param.mode}" />
              <input type="date" name="startDate" class="form-control mr-2" value="${param.startDate}">
              <span>~</span>
              <input type="date" name="endDate" class="form-control mx-2" value="${param.endDate}">
              <input type="text" name="keyword" class="form-control mx-2" placeholder="제품명, LOT 번호 검색" value="${param.keyword}">
                           <button type="button" class="btn btn-primary" onclick="viewAll()">조 회</button>
           <div class="d-flex justify-content-end ">
			  <a href="${pageContext.request.contextPath}/product/stocklist"class="btn btn-success ml-5">
			    ⟳ 실시간 업데이트
			  </a>
			</div>
           
            </form>            
          </div>
          
      
<!-- ✅ 재고 상태 필터 탭 -->
<div class="col-12 mb-2">
  <ul class="nav nav-tabs nav-underline-custom">
    <li class="nav-item">
      <a class="nav-link ${empty param.status || param.status == 'all' ? 'active' : ''}"
         href="${pageContext.request.contextPath}/product/stocklist?status=all&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}">
        전체
      </a>
    </li>
    <li class="nav-item">
      <a class="nav-link ${param.status == '정상' ? 'active' : ''}"
         href="${pageContext.request.contextPath}/product/stocklist?status=정상&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}">
        정상
      </a>
    </li>    
    <li class="nav-item">
      <a class="nav-link ${param.status == '부족' ? 'active' : ''}"
         href="${pageContext.request.contextPath}/product/stocklist?status=부족&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}">
        부족
      </a>
    </li>
    <li class="nav-item">
      <a class="nav-link ${param.status == '완료' ? 'active' : ''}"
         href="${pageContext.request.contextPath}/product/stocklist?status=완료&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}">
        완료
      </a>
    </li>
  </ul>
</div>


          <!-- 재고 테이블 -->
          <div class="col-12">
            <div class="table-responsive">
              <table id="stockTable" class="table table-bordered table-hover">
                <thead>
                  <tr>
                    <th>제품명</th>
				    <th>LOT 번호</th>
				    <th>현재고</th>
				    <th>예약수량</th>
				    <th>가용수량</th>
				    <th>안전재고</th>
				    <th>생산일자</th>
				    <th>유통기한</th>            
				    <th>재고상태</th>
				    <th>상세내역</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="item" items="${stockList}">
                    <tr>
                      <td>${item.productName}</td>
				      <td>${item.lotNo}</td>
				      <td>${item.stockQty}</td>
				      <td>${item.reservedQty}</td>
				      <td>${item.availableQty}</td>
				      <td>${item.safeQty}</td>
                      <td><fmt:formatDate value="${item.regDate}" pattern="yyyy-MM-dd"/></td>
                      <td><fmt:formatDate value="${item.expireDate}" pattern="yyyy-MM-dd"/></td>
                      <td>
                        <c:choose>  
  <c:when test="${item.availableQty == 0}">
    <span class="badge badge-secondary">완료</span>
  </c:when>  
  <c:when test="${item.availableQty > 0 && item.availableQty lt item.safeQty}">
    <span class="badge badge-danger">부족</span>
  </c:when> 
  <c:otherwise>
    <span class="badge badge-success">정상</span>
  </c:otherwise>
</c:choose>



                      </td>
                      <td>
                        <button class="btn btn-outline-secondary btn-sm"
                                data-toggle="modal"
                                data-target="#lotHistoryModal"
                                data-product="${item.productName}"
                                data-lot="${item.lotNo}">
                          내역확인
                        </button>
                      </td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>

            <!-- 다운로드 버튼 -->
            <div class="d-flex justify-content-end mt-3">
              <a href="#" class="btn btn-warning">+ 목록 다운로드</a>
            </div>
          </div>
 </div> <!-- row -->
           <!-- ✅ 페이징 영역 -->
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

       
        <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
      </div> <!-- content-wrapper -->
    </div> <!-- main-panel -->
  </div> <!-- container-fluid -->
</div> <!-- container-scroller -->

<!-- 모달 영역 -->
<!-- LOT 상세 모달 -->
<div class="modal fade" id="lotHistoryModal" tabindex="-1">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">

      <!-- 제목 -->
      <div class="modal-header bg-primary text-white">
        <h5 class="modal-title">
          <span id="modal-product-name"></span>
          (<span id="modal-lot-no"></span>) 입출고 상세
        </h5>
        <button type="button" class="close text-white" data-dismiss="modal">&times;</button>
      </div>

      <!-- 상단 요약 -->
      <div class="modal-body">
        <div class="row mb-3">
          <div class="col-md-6"><strong>입고량:</strong> <span id="modal-inboundQty"></span></div>
          <div class="col-md-6"><strong>총 출고량:</strong> <span id="modal-totalOutboundQty"></span></div>
          <div class="col-md-6"><strong>예약 수량:</strong> <span id="modal-reservedQty"></span></div>
          <div class="col-md-6"><strong>가용 수량:</strong> <span id="modal-availableQty"></span></div>
          <div class="col-md-6"><strong>유통기한:</strong> <span id="modal-expireDate"></span></div>
        </div>

        <!-- 하단 테이블 -->
        <table class="table table-bordered text-center">
          <thead class="thead-dark">
            <tr>
              <th>처리일자</th>
              <th>구분</th>
              <th>수량</th>
              <th>거래처</th>
            </tr>
          </thead>
          <tbody id="lotHistoryTableBody"></tbody>
        </table>

        <div id="lotHistoryEmpty" class="alert alert-info text-center d-none">
          입출고 내역이 없습니다.
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-secondary" data-dismiss="modal">닫기</button>
      </div>
    </div>
  </div>
</div>




<script>
  // 시작 날짜 선택 시 → 종료 날짜 최소값 변경
  document.querySelector('input[name="startDate"]').addEventListener('change', function () {
    const startDate = this.value;
    const endDateInput = document.querySelector('input[name="endDate"]');

    if (startDate) {
      endDateInput.min = startDate;

      // 현재 선택된 endDate가 startDate보다 이전이면 초기화
      if (endDateInput.value && endDateInput.value < startDate) {
        endDateInput.value = '';
      }
    }
  });
</script>


<style>
/* 언더라인 탭 스타일 - 상단 라인 */
.nav-underline-custom {
    border-bottom: 1px solid #dee2e6;
    margin-bottom: 0;
}

.nav-underline-custom .nav-link {
    border: none;
    border-top: 3px solid transparent;
    color: #6c757d;
    padding: 0.75rem 1.5rem;
    font-weight: 500;
    background: none;
}

.nav-underline-custom .nav-link.active {
    color: #1C355E;
    border-top-color: #1C355E;
    background: none;
    font-weight: 700;
}

.nav-underline-custom .nav-link:hover {
    color: #1C355E;
    border-top-color: rgba(28, 53, 94, 0.5);
    background: none;
}

/* 배지 스타일 */
.nav-link .badge {
    font-size: 0.75rem;
    font-weight: 500;
}

/* 체크박스 스타일 */
.highlight-checkbox {
    width: 18px;
    height: 18px;
    accent-color: #28a745;
    cursor: pointer;
}

.highlight-checkbox:hover {
    box-shadow: 0 0 5px #28a745;
    transform: scale(1.1);
    transition: all 0.2s ease;
}

/* 테이블 호버 효과 */
.table-hover tbody tr:hover {
    background-color: rgba(28, 53, 94, 0.05);
}

/* 정렬 링크 스타일 */
.table thead th a:hover {
    color: #f8f9fa !important;
    text-decoration: underline !important;
}

/* 페이지네이션 호버 효과 */
.page-link:hover {
    background-color: rgba(28, 53, 94, 0.1);
    border-color: #1C355E;
    color: #1C355E;
}

/* 버튼 호버 효과 */
.btn-primary:hover {
    background-color: #152a4a !important;
    border-color: #152a4a !important;
}

/* 탭 콘텐츠 부드러운 전환 */
.tab-content {
    margin-top: 20px;
}
 .neutral-arrow {
    color: #ccc;
    font-size: 12px;
    margin-left: 4px;
  }
</style>

<script src="${pageContext.request.contextPath}/resources/js/lotHistory.js"></script>

