<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

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
			  <h3 class="font-weight-bold">자재 출고 관리</h3>
			</div>
			
		    <!-- 검색 필터 -->
		    <form method="get" class="form-inline mb-3">
		      <input type="text" name="keyword" class="form-control mr-2" placeholder="출고관리번호 or 품명">
		      <button type="submit" class="btn btn-primary">검색</button>
		    </form>
		
		    <!-- 출고 리스트 테이블 -->
		    <div id="table_content" class="table-responsive">
		      <table class="table table-bordered text-center">
		        <thead>
		          <tr>
		            <th>출고관리번호</th>
		            <th>작업지시번호</th>
		            <th>상세</th>
		            <th>납품자명</th>
		            <th>품명</th>
		            <th>주문수량</th>
		            <th>재고확인</th>
		            <th>작업지시일자</th>
		            <th>납기일자</th>
		            <th>진행현황</th>
		            <th>담당자</th>
		            <th>출고일자</th>
		            <th>출고처리</th>
		          </tr>
		        </thead>
		        <tbody>
		         <c:forEach var="item" items="${outList}">
				  <tr>
				    <td>${item.outboundId}</td>
				    <td>${item.workOrderNo}</td>
				    <td><button class="btn btn-sm btn-outline-secondary">상세</button></td>
				    <td>${item.supplierName}</td>
				    <td>${item.materialName}</td>
				    <td>${item.requiredQty}</td>
				    <td>${item.stockStatus} (재고 ${item.stockQty})</td>
				    <td><fmt:formatDate value="${item.workOrderDate}" pattern="yyyy-MM-dd" /></td>
				    <td><fmt:formatDate value="${item.dueDate}" pattern="yyyy-MM-dd" /></td>
				    <td>
				      <c:choose>
				        <c:when test="${item.status eq '완료'}">
				          <span class="badge badge-success">출고완료</span>
				        </c:when>
				        <c:otherwise>
				          <span class="badge badge-danger">미출고</span>
				        </c:otherwise>
				      </c:choose>
				    </td>
				    <td>${item.handledBy}</td>
				    <td>
				      <c:if test="${item.outboundDate != null}">
				        <fmt:formatDate value="${item.outboundDate}" pattern="yyyy-MM-dd" />
				      </c:if>
				    </td>
				    <td>
				      <c:if test="${item.status ne '완료'}">
				        <button class="btn btn-sm btn-outline-primary">출고처리</button>
				      </c:if>
				    </td>
				  </tr>
				</c:forEach>

		        </tbody>
		      </table>
		    </div>

          </div>
          
			<!-- 페이징 처리 시작 -->
			<div class="d-flex justify-content-center mt-4">
			 <nav>
			  <ul class="pagination justify-content-center mt-4">
			
			    <!-- 이전 버튼 -->
			    <c:if test="${pageMaker.cri.page>1}">
			      <li class="page-item">
			        <a class="page-link"href="?page=${pageMaker.startPage - 1}&perPageNum=${cri.perPageNum}&condition=${cri.condition}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">&laquo;</a>
			      </li>
			    </c:if>
			    
			    <!-- 페이지 번호 출력 -->
			    <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
			      <li class="page-item ${p == cri.page ? 'active' : ''}">
			        <a class="page-link"href="?page=${p}&perPageNum=${cri.perPageNum}&condition=${cri.condition}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">${p}</a>
			      </li>
			    </c:forEach>
			    
			    <!-- 다음 버튼 -->
			    <c:if test="${pageMaker.cri.page<pageMaker.endPage}">
			      <li class="page-item">
			        <a class="page-link"href="?page=${pageMaker.endPage + 1}&perPageNum=${cri.perPageNum}&condition=${cri.condition}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">&raquo;</a>
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



<!-- 출고 처리 JS 함수 -->
<script>
function processOut(outboundId) {
  if (confirm("출고처리 하시겠습니까?")) {
    location.href = '/material/out/process?outboundId=' + outboundId;
  }
}

function viewDetail(outboundId) {
  // 상세 모달 띄우기 Ajax 또는 location.href 사용
  location.href = '/material/out/detail?outboundId=' + outboundId;
}
</script>
