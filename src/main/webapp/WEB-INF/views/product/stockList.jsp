<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>
<!-- Bootstrap Icons CDN -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">



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
              <select name="sortColumn" class="form-control mr-2">		               
		                <option value="all" ${cri.sortColumn eq 'all' ? 'selected' : ''}>전체</option>
		                <option value="product_name" ${cri.sortColumn eq 'product_name' ? 'selected' : ''}>제품명</option>
		                <option value="lot_no" ${cri.sortColumn eq 'lot_no' ? 'selected' : ''}>LOT번호</option>
		         </select>          
              
              
              
              <input type="hidden" name="mode" value="${param.mode}" />
              <input type="date" name="startDate" class="form-control mr-2" value="${param.startDate}">
              <span>~</span>
              <input type="date" name="endDate" class="form-control mx-2" value="${param.endDate}">
              <input type="text" name="keyword" class="form-control mx-2" placeholder="제품명, LOT 번호 검색" value="${param.keyword}">
              
              
              
              
               <button type="submit" class="btn btn-primary me-2"><i class="ti-search"></i>검색</button>
                <a href="/product/stocklist" class="btn btn-light">
          <i class="ti-reload"></i> 초기화
        </a>
           
           <div class="d-flex justify-content-end ">
			  <a href="${pageContext.request.contextPath}/product/stocklist"class="btn btn-success ml-5">
			    ⟳ 실시간 업데이트
			  </a>
			</div>
			
			 <!-- 제품별 재고현황 -->

<button type="button" class="btn btn-info" data-toggle="modal" data-target="#stockSummaryModal">
  재고 요약 보기
</button>
           
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
              <table id="stockTable" class="table table-hover">
                <thead>
                  <tr>
                    <th>제품명</th>
				    <th>LOT 번호</th>
				    <th>현재고</th>
				    <th>예약수량</th>
				    <th>가용수량</th>				    
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
                      <td><fmt:formatDate value="${item.regDate}" pattern="yyyy-MM-dd"/></td>
                      <td><fmt:formatDate value="${item.expireDate}" pattern="yyyy-MM-dd"/></td>
                      <td>
                        <c:choose>  
  <c:when test="${item.availableQty == 0}">
    <span class="badge badge-secondary">완료</span> 
  </c:when>   
  <c:otherwise>
    <span class="badge badge-success">정상</span>
  </c:otherwise>
</c:choose>



                      </td>
                      <td>
                       <button class="btn btn-sm btn-outline-info btn-sm open-lot-modal"
        data-productid="${item.productName}"  <%-- UI 표시용으로만 씀 --%>
        data-product="${item.productName}"
        data-lot="${item.lotNo}">
  상세
</button>

                      </td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
            
            
 </div> <!-- row -->
 
 
 <!-- 모달 영역 -->
<!-- LOT 상세 모달 -->
<div class="modal fade" id="lotHistoryModal" tabindex="-1" role="dialog">
  <div class="modal-dialog modal-lg" role="document">
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
  <table class="table table-bordered table-sm">
    <tbody>
      <tr>
        <th class="bg-light">입고량</th>
        <td><span id="modal-inboundQty">-</span></td>
        <th class="bg-light">총 출고량</th>
        <td><span id="modal-totalOutboundQty">-</span></td>
      </tr>
      <tr>
        <th class="bg-light">예약 수량</th>
        <td><span id="modal-reservedQty">-</span></td>
        <th class="bg-light">가용 수량</th>
        <td><span id="modal-availableQty">-</span></td>
      </tr>
      <tr>
        <th class="bg-light">유통기한</th>
        <td colspan="3"><span id="modal-expireDate">-</span></td>
      </tr>
    </tbody>
  </table>


</br>
        <!-- 하단 테이블 -->
        <table class="table table-bordered text-center">
         <colgroup>
		    <col style="width: 20%;">
		    <col style="width: 20%;">
		    <col style="width: 20%;">
		    <col style="width: 20%;">
		    <col style="width: 20%;">
		  </colgroup>
          <thead class="thead bg-primary">
            <tr>
      <th>처리일자</th>  
      <th>거래처</th>            
      <th>관리번호</th>
      <th>수량</th>
      <th>구분</th>
    </tr>
          </thead>
          <tbody id="lotHistoryTableBody"></tbody>
        </table>

        <div id="lotHistoryEmpty" class="alert alert-info text-center d-none">
          입출고 내역이 없습니다.
        </div>
    

      <div class="modal-footer">
        <button class="btn btn-secondary" data-dismiss="modal">닫기</button>
      </div>
    </div>
  </div>
</div>
   </div>
 </div>
 

 
<!-- ✅ 제품 재고 요약 모달 -->
<div class="modal fade" id="stockSummaryModal" tabindex="-1" role="dialog" aria-labelledby="stockSummaryModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg" role="document"> 
    <div class="modal-content">  
      <div class="modal-header bg-primary text-white">
        <h5 class="modal-title fs-4" id="stockSummaryModalLabel">제품별 재고 요약</h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="닫기">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body fs-5"> <!-- ✅ 전체 본문 글자 크기 증가 -->

        <!-- ✅ 카드 리스트 시작 (가운데 정렬 적용됨) -->
        <div class="d-flex flex-wrap justify-content-center gap-3">
          <c:forEach var="item" items="${summaryList}">
            <div class="card border rounded-4 shadow-sm mr-3 mb-3" style="width: 500px;">
              <div class="card-body d-flex justify-content-between align-items-center px-4 py-3">
                <div>
                  <h4 class="fw-bold fs-4">${item.productName}</h4> 
                  <h6 class="mb-1">예약: ${item.reservedQty}</h6>
                  <h6 class="mb-0">가용: ${item.availableQty}</h6>
                </div>
                <div>
                  <c:choose>
                    <c:when test="${item.availableQty >= 50}">
                      <span class="badge badge-success px-3 py-2 fs-6">정상</span>
                    </c:when>
                    <c:otherwise>
                      <span class="badge badge-danger px-3 py-2 fs-6">부족</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>
            </div>
          </c:forEach>
        </div>
        <!-- ✅ 카드 리스트 끝 -->

      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal">닫기</button>
      </div>
    </div>
  </div>
</div>


 
           <!-- ✅ 페이징 영역 -->
<!-- ✅ 페이징 영역 (필터 유지 + 안전 링크) -->
<div class="d-flex justify-content-center mt-4">
  <nav>
    <ul class="pagination justify-content-center mt-4">

      <!-- ◀ Prev -->
      <c:if test="${pageMaker.cri.page > 1}">
        <c:url var="prevUrl" value="/product/stocklist">
          <c:param name="page" value="${pageMaker.startPage - 1}" />
          <c:param name="perPageNum" value="${pageMaker.cri.perPageNum}" />
          <c:param name="status" value="${param.status}" />
          <c:param name="mode" value="${param.mode}" />
          <c:param name="keyword" value="${param.keyword}" />
          <c:param name="startDate" value="${param.startDate}" />
          <c:param name="endDate" value="${param.endDate}" />
        </c:url>
        <li class="page-item">
          <a class="page-link" href="${prevUrl}">&laquo;</a>
        </li>
      </c:if>

      <!-- 숫자 페이지 -->
      <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
        <c:url var="pageUrl" value="/product/stocklist">
          <c:param name="page" value="${p}" />
          <c:param name="perPageNum" value="${pageMaker.cri.perPageNum}" />
          <c:param name="status" value="${param.status}" />
          <c:param name="mode" value="${param.mode}" />
          <c:param name="keyword" value="${param.keyword}" />
          <c:param name="startDate" value="${param.startDate}" />
          <c:param name="endDate" value="${param.endDate}" />
        </c:url>
        <li class="page-item ${p == pageMaker.cri.page ? 'active' : ''}">
          <a class="page-link" href="${pageUrl}">${p}</a>
        </li>
      </c:forEach>

      <!-- Next ▶ -->
      <c:if test="${pageMaker.cri.page < pageMaker.endPage}">
        <c:url var="nextUrl" value="/product/stocklist">
          <c:param name="page" value="${pageMaker.cri.page + 1}" />
          <c:param name="perPageNum" value="${pageMaker.cri.perPageNum}" />
          <c:param name="status" value="${param.status}" />
          <c:param name="mode" value="${param.mode}" />
          <c:param name="keyword" value="${param.keyword}" />
          <c:param name="startDate" value="${param.startDate}" />
          <c:param name="endDate" value="${param.endDate}" />
        </c:url>
        <li class="page-item">
          <a class="page-link" href="${nextUrl}">&raquo;</a>
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





<script>
//시작 날짜 선택 시 → 종료 날짜 최소값 변경
document.querySelector('input[name="startDate"]').addEventListener('change', function () {
  const startDate = this.value;
  const endDateInput = document.querySelector('input[name="endDate"]');

  if (startDate) {
    // 종료일의 최소값을 시작일과 같은 날짜로 설정 (같은 날짜 허용)
    endDateInput.min = startDate;

    // 현재 선택된 endDate가 startDate보다 이전이면 시작일과 동일하게 설정
    if (endDateInput.value && endDateInput.value < startDate) {
      endDateInput.value = startDate; // 같은 날짜로 자동 설정
    }
  }
});

</script>



<script src="${pageContext.request.contextPath}/resources/js/lotHistory.js"></script>

