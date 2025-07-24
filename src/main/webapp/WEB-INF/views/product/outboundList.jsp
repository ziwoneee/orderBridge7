<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
		            <input type="text" name="keyword" value="${cri.keyword}" class="form-control mr-2" placeholder="제품명, ID, LOT, 거래처 검색">
		            <input type="date" name="startDate" value="${cri.startDate}" class="form-control mr-2" max="<%= today %>">
		            <input type="date" name="endDate" value="${cri.endDate}" class="form-control mr-2" max="<%= today %>">
		
		            <select name="sortColumn" class="form-control mr-2">
		                <option value="outbound_date" ${cri.sortColumn eq 'outbound_date' ? 'selected' : ''}>출고일자</option>
		                <option value="product_id" ${cri.sortColumn eq 'product_id' ? 'selected' : ''}>제품ID</option>
		            </select>
		            <select name="sortOrder" class="form-control mr-2">
		                <option value="desc" ${cri.sortOrder eq 'desc' ? 'selected' : ''}>내림차순</option>
		                <option value="asc" ${cri.sortOrder eq 'asc' ? 'selected' : ''}>오름차순</option>
		            </select>
		
		            <button type="submit" class="btn btn-primary">조회</button>
		        </form>
		    </div>
	<div row>
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
	            <th>제품명</th>
	            <th>LOT번호</th>
	            <th>출고수량</th>
	            <th>출고일자</th>
	            <th>출고유형</th>
	            <th>거래처명</th>
	            <th>담당자</th>
	            <th>비고</th>
	            <th>상세보기</th>
	            
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
	    <a href="${pageContext.request.contextPath}/outbound/detail?outboundId=${vo.outboundId}" class="btn btn-sm btn-info">상세보기</a>
	  </td>
	            </tr>
	        </c:forEach>
	        </tbody>
	    </table>
	
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

<!-- ✅ DataTables 초기화 (정렬만 사용, 페이징X) -->
<script>
$(document).ready(function () {
  $('#outboundTable').DataTable({
    paging: false,        // ❌ 페이징 비활성 (서버 페이징 사용)
    ordering: true,       // ✅ 정렬 가능
    searching: false,     // ❌ 검색창 비활성 (직접 구현)
    info: false,          // ❌ "n개 중 m개 표시 중" 비활성
    columnDefs: [
      { targets: [4,5,6,7,8,9,10], orderable: false }  
    ]
  });
});
</script> 