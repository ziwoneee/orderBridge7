<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<% java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
    String today = sdf.format(new java.util.Date());%>

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
			  <h3 class="font-weight-bold">입고 내역 리스트</h3>
			</div>


    <!-- ✅ 검색 & 필터 -->
    <div class="d-flex justify-content-between mb-3">
    <form method="get" class="form-inline mb-4">
        <input type="text" name="keyword" value="${cri.keyword}" class="form-control mr-2" placeholder="제품명 ,ID 또는 LOT 검색">
	<input type="date" name="startDate" value="${cri.startDate}" class="form-control mr-2" max="<%= today %>">
	<input type="date" name="endDate" value="${cri.endDate}" class="form-control mr-2" max="<%= today %>">
        
               <select name="sortColumn" class="form-control mr-2">
            <option value="created_at" ${cri.sortColumn eq 'created_at' ? 'selected' : ''}>입고일자</option>
            <option value="product_id" ${cri.sortColumn eq 'product_id' ? 'selected' : ''}>제품ID</option>
        </select>
        <select name="sortOrder" class="form-control mr-2">
            <option value="desc" ${cri.sortOrder eq 'desc' ? 'selected' : ''}>내림차순</option>
            <option value="asc" ${cri.sortOrder eq 'asc' ? 'selected' : ''}>오름차순</option>
        </select>
        <button type="submit" class="btn btn-primary">조회</button>
    </form>
    
      <!-- 자동입고 버튼 -->
    <form method="post" action="${pageContext.request.contextPath}/product/inbound/saveFromProduction">
        <!-- Hidden 필드 전달 -->
        <input type="hidden" name="keyword" value="${cri.keyword}">
        <input type="hidden" name="startDate" value="${cri.startDate}">
        <input type="hidden" name="endDate" value="${cri.endDate}">
        <input type="hidden" name="sortColumn" value="${cri.sortColumn}">
        <input type="hidden" name="sortOrder" value="${cri.sortOrder}">
        <input type="hidden" name="page" value="${cri.page}">
        <button type="submit" class="btn btn-success">+ 자동입고 업데이트</button>
    </form>
</div>
   
    <c:if test="${not empty msg}">
    <div class="alert alert-success text-center">${msg}</div>
</c:if>
    

    <!-- ✅ 테이블 -->
    <table class="table table-bordered table-striped table-hover text-center">
        <thead class="thead-dark">
        <tr>
            <th>입고ID</th>
            <th>제품ID</th>
            <th>제품명</th>           
            <th>LOT번호</th>
            <th>수량</th>
            <th>입고일자</th>
            <th>입고유형</th>
            <th>담당자</th>
            <th>비고</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="vo" items="${inboundList}">
            <tr>
                <td>${vo.inboundId}</td>
                <td>${vo.productId}</td>
                <td>${vo.productName}</td>                
                <td>${vo.lotNo}</td>
                <td>${vo.inboundQty}</td>
                <td><fmt:formatDate value="${vo.createdAt}" pattern="yyyy-MM-dd"/></td>
                <td>${vo.inboundType}</td>
                <td>${vo.manager}</td>
                <td>${vo.remark}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <!-- ✅ 페이지네이션 -->
    <nav>
        <ul class="pagination justify-content-center">
            <c:if test="${pageMaker.prev}">
                <li class="page-item">
                    <a class="page-link" href="?page=${pageMaker.startPage - 1}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">이전</a>
                </li>
            </c:if>
            <c:forEach var="i" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
                <li class="page-item ${cri.page == i ? 'active' : ''}">
                    <a class="page-link" href="?page=${i}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">${i}</a>
                </li>
            </c:forEach>
            <c:if test="${pageMaker.next}">
                <li class="page-item">
                    <a class="page-link" href="?page=${pageMaker.endPage + 1}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">다음</a>
                </li>
            </c:if>
        </ul>
    </nav>
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