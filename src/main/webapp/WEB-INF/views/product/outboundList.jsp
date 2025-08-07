<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
    String today = sdf.format(new java.util.Date());
%>

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
			  <h3 class="font-weight-bold">출고 내역 리스트</h3>
			</div>

		    <!-- ✅ 검색 & 필터 -->
		    <div class="d-flex justify-content-between mb-3">
		        <form method="get" class="form-inline mb-4">
		         <select name="sortColumn" class="form-control mr-2">		               
		                <option value="all" ${cri.sortColumn eq 'all' ? 'selected' : ''}>전체</option>
		                <option value="product_name" ${cri.sortColumn eq 'product_name' ? 'selected' : ''}>제품명</option>
		                <option value="lot_no" ${cri.sortColumn eq 'lot_no' ? 'selected' : ''}>LOT번호</option>
		                <option value="client_name" ${cri.sortColumn eq 'client_name' ? 'selected' : ''}>담당자</option>
		         </select>
		            		            
		            <input type="text" name="keyword" value="${cri.keyword}" class="form-control mr-2" placeholder="제품명, ID, LOT, 거래처 검색">
		            <input type="date" id="startDate" name="startDate" value="${cri.startDate}" class="form-control mr-2" max="<%= today %>">
		           <span class="mx-1">~</span>
		            <input type="date" id ="endDate" name="endDate" value="${cri.endDate}" class="form-control mr-2" max="<%= today %>">
				           
		
		            <button type="submit" class="btn btn-primary me-2" >
                      <i class="ti-search"></i> 검색
                    </button>
		             <a href="/product/outbound/list" class="btn btn-light">
			          <i class="ti-reload"></i> 초기화
			        </a>
					        </form>
					    </div>
			
	    <c:if test="${not empty msg}">
	        <div class="alert alert-success text-center">${msg}</div>
	    </c:if>
	</div>
	    <!-- ✅ 테이블 -->
	     <div class="table-responsive mt-4">
	    <table id=outboundTable class="table table-bordered table-striped table-hover text-center">
	        <thead>
			  <tr>
			    <th>출고ID</th>
			    <th>제품ID</th>

    <!-- ✅ 제품명 정렬 -->
    <th>
      <a href="?page=1&sortColumn=productname&sortOrder=${cri.sortColumn eq 'productname' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}">
        제품명
        <c:choose>
          <c:when test="${cri.sortColumn eq 'productname'}">
            <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
          </c:when>
          <c:otherwise>
            <span class="neutral-arrow">⇅</span>
          </c:otherwise>
        </c:choose>
      </a>
    </th>

    <!-- ✅ LOT번호 정렬 -->
    <th>
      <a href="?page=1&sortColumn=lotno&sortOrder=${cri.sortColumn eq 'lotno' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}">
        LOT번호
        <c:choose>
          <c:when test="${cri.sortColumn eq 'lotno'}">
            <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
          </c:when>
          <c:otherwise>
            <span class="neutral-arrow">⇅</span>
          </c:otherwise>
        </c:choose>
      </a>
    </th>

    <th>출고수량</th>

    <!-- ✅ 출고일자 정렬 -->
    <th>
      <a href="?page=1&sortColumn=outbounddate&sortOrder=${cri.sortColumn eq 'outbounddate' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}">
        출고일자
        <c:choose>
          <c:when test="${cri.sortColumn eq 'outbounddate'}">
            <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
          </c:when>
          <c:otherwise>
            <span class="neutral-arrow">⇅</span>
          </c:otherwise>
        </c:choose>
      </a>
    </th>

    <th>출고유형</th>

    <!-- ✅ 거래처명 정렬 -->
    <th>
      <a href="?page=1&sortColumn=clientname&sortOrder=${cri.sortColumn eq 'clientname' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}">
        거래처명
        <c:choose>
          <c:when test="${cri.sortColumn eq 'clientname'}">
            <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
          </c:when>
          <c:otherwise>
            <span class="neutral-arrow">⇅</span>
          </c:otherwise>
        </c:choose>
      </a>
    </th>

    <!-- ✅ 담당자 정렬 -->
    <th>
      <a href="?page=1&sortColumn=manager&sortOrder=${cri.sortColumn eq 'manager' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}">
        담당자
        <c:choose>
          <c:when test="${cri.sortColumn eq 'manager'}">
            <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
          </c:when>
          <c:otherwise>
            <span class="neutral-arrow">⇅</span>
          </c:otherwise>
        </c:choose>
      </a>
    </th>
          <th>비고</th>
	            <th>상세내역</th>
	        </tr>
	        </thead>
	        <tbody>
	        <c:forEach var="vo" items="${outboundList}">
	            <tr>
	                <td>${vo.outboundId}</td>
	                <td>${vo.productId}</td>
	                <td>${vo.productName}</td>
	                <td>${vo.lotNo}</td>
	                <td>${vo.outboundQty}</td>
	                <td><fmt:formatDate value="${vo.outboundDate}" pattern="yyyy-MM-dd"/></td>
	                <td>${vo.outboundType}</td>
	                <td>${vo.clientName}</td>
	                <td>${vo.manager}</td>
	                <td>${vo.remark}</td>
	                
	                <td>
	    <!-- 출고 내역 리스트에 있는 상세보기 버튼 -->
<button class="btn btn-sm btn-outline-info"
        data-toggle="modal"
        data-target="#outboundDetailModal"
        data-id="${vo.outboundId}">
  상세
</button>

	  </td>
	            </tr>
	        </c:forEach>
	        </tbody>
	    </table>
	    
	   
<!-- 출고 상세 모달 -->
<div class="modal fade" id="outboundDetailModal" tabindex="-1" role="dialog">
  <div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">

      <!-- 타이틀 영역 -->
  <div class="modal-header bg-primary text-white">
        <h5 class="modal-title">
    <span id="detail-productName-title"></span>
    <span id="detail-outboundId-title"></span>
    - 출고 상세정보
  </h5>
  <button type="button" class="close text-white" data-dismiss="modal">
    <span>&times;</span>
  </button>
</div>


      <!-- 내용 영역 -->
      <div class="modal-body">
        <table class="table table-bordered table-sm text-center align-middle">
          <colgroup>
            <col style="width: 15%;">
            <col style="width: 35%;">
            <col style="width: 15%;">
            <col style="width: 35%;">
          </colgroup>
           <tbody>
            <tr>
              <th class="bg-light">출고ID</th><td id="detail-outboundId"></td>
              <th class="bg-light">제품명</th><td id="detail-productName"></td>
            </tr>
            <tr>
              <th class="bg-light">LOT 번호</th><td id="detail-lotNo"></td>
              <th class="bg-light">출고수량</th><td id="detail-outboundQty"></td>
            </tr>
            <tr>
              <th class="bg-light">출고일자</th><td id="detail-outboundDate"></td>
              <th class="bg-light">거래처명</th><td id="detail-clientName"></td>
            </tr>
            <tr>
              <th class="bg-light">송장번호</th><td id="detail-trackingNumber"></td>
              <th class="bg-light">담당자</th><td id="detail-manager"></td>
            </tr>
            <tr>
              <th class="bg-light">비고</th>
              <td colspan="3" id="detail-remark"></td>
            </tr>
          </tbody>
        </table>

        <div class="alert alert-info text-center mt-5">
         <span id="detail-clientName-title"></span> 출고의 상세 내역입니다.
        </div> 
      </div>

      <!-- 푸터 -->
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal">닫기</button>
      </div>

    </div>
  </div>
</div>


	    
	
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
 		  </div>
        <!-- content-wrapper 끝 -->
	  <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
	</div>
     </div>
     <!-- 본문.jsp main-panel ends -->
  </div>   
  <!-- container-fluid page-body-wrapper 끝 -->
</div>
<!-- container-scroller 끝-->  

<style>
  .neutral-arrow {
    color: #ccc;
  }
</style>
 
 <script>
  const startDateInput = document.getElementById("startDate");
  const endDateInput = document.getElementById("endDate");

  startDateInput.addEventListener("change", function () {
    const selectedStart = this.value;
    if (selectedStart) {
      endDateInput.min = selectedStart;

      // 현재 endDate가 startDate보다 이전이면 초기화
      if (endDateInput.value && endDateInput.value < selectedStart) {
        endDateInput.value = '';
      }
    }
  });

  // 페이지 진입 시에도 초기 min 세팅 (새로고침 대비)
  window.addEventListener("DOMContentLoaded", function () {
    if (startDateInput.value) {
      endDateInput.min = startDateInput.value;
    }
  });
</script>

 <script src="/resources/js/outboundDetail.js"></script>
 
