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
		            <input type="date" name="startDate" value="${cri.startDate}" class="form-control mr-2" max="<%= today %>">
		            <input type="date" name="endDate" value="${cri.endDate}" class="form-control mr-2" max="<%= today %>">
				           
		
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
	            <th>
  <a href="?page=1&sortColumn=productName&sortOrder=${cri.sortColumn eq 'productName' and cri.sortOrder eq 'ASC' ? 'DESC' : 'ASC'}&keyword=${fn:escapeXml(cri.keyword)}">
    제품명
    <c:if test="${cri.sortColumn eq 'productName'}">
      ${cri.sortOrder eq 'ASC' ? '▲' : '▼'}
    </c:if>
  </a>
</th>
	            
<th>
  <a href="?page=1&sortColumn=lotNo&sortOrder=${cri.sortColumn eq 'lotNo' and cri.sortOrder eq 'ASC' ? 'DESC' : 'ASC'}&keyword=${fn:escapeXml(cri.keyword)}">
    LOT번호
    <c:if test="${cri.sortColumn eq 'lotNo'}">
      ${cri.sortOrder eq 'ASC' ? '▲' : '▼'}
    </c:if>
  </a>
</th>

	            <th>출고수량</th>
	            <th>
  <a href="?page=1&sortColumn=outboundDate&sortOrder=${cri.sortColumn eq 'outboundDate' and cri.sortOrder eq 'ASC' ? 'DESC' : 'ASC'}&keyword=${fn:escapeXml(cri.keyword)}">
    출고일자
    <c:if test="${cri.sortColumn eq 'outboundDate'}">
      ${cri.sortOrder eq 'ASC' ? '▲' : '▼'}
    </c:if>
  </a>
</th>
	            <th>출고유형</th>
	            <th>
  <a href="?page=1&sortColumn=clientName&sortOrder=${cri.sortColumn eq 'clientName' and cri.sortOrder eq 'ASC' ? 'DESC' : 'ASC'}&keyword=${fn:escapeXml(cri.keyword)}">
    거래처명
    <c:if test="${cri.sortColumn eq 'clientName'}">
      ${cri.sortOrder eq 'ASC' ? '▲' : '▼'}
    </c:if>
  </a>
</th>
	            <th>
  <a href="?page=1&sortColumn=manager&sortOrder=${cri.sortColumn eq 'manager' and cri.sortOrder eq 'ASC' ? 'DESC' : 'ASC'}&keyword=${fn:escapeXml(cri.keyword)}">
    담당자
    <c:if test="${cri.sortColumn eq 'manager'}">
      ${cri.sortOrder eq 'ASC' ? '▲' : '▼'}
    </c:if>
  </a>
</th>
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
