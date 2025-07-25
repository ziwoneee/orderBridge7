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
			  <h3 class="font-weight-bold">협력사 정보</h3>
			</div>
			
			<!-- 검색바 + 신규등록 버튼 정렬 -->
			<div class="d-flex justify-content-between align-items-center mb-2">
			  <!-- 왼쪽: 검색 폼 -->
			  <form method="get" action="/supplier/list" class="form-inline flex-wrap">
			    <select name="condition" class="form-control mr-2 mb-2">
			      <option value="all" ${condition eq 'all' ? 'selected' : ''}>전체</option>
			      <option value="supplier_name" ${condition eq 'supplier_name' ? 'selected' : ''}>협력사명</option>
			      <option value="business_number" ${condition eq 'business_number' ? 'selected' : ''}>사업자등록번호</option>
			      <option value="representative_name" ${condition eq 'representative_name' ? 'selected' : ''}>대표자명</option>
			    </select>
			
			    <input type="text" name="keyword" value="${keyword}" class="form-control mr-2 mb-2" placeholder="검색어 입력" style="min-width: 200px;" />
			
			    <button type="submit" class="btn btn-primary mb-2">검색</button>
			  </form>
			
			  <!-- 오른쪽: 신규 등록 버튼 -->
			  <a href="/supplier/register" class="btn btn-success mb-2">신규 등록</a>
			</div>



			
			<!-- 테이블 -->
			<div class="table-responsive mt-4">
			  <table id="supplierTable" class="table table-bordered text-center">
			    <thead>
			      <tr>
			      	<th>협력사코드</th>
			        <th>협력사명</th>
			        <th style="min-width: 130px;">사업자등록번호</th>
			        <th>업태/종목</th>
			        <th>대표자명</th>
			        <th>담당자 연락처</th>
			        <th>거래상태</th>
			        <th>등록일자</th>
			        <th>상세</th>
			      </tr>
			    </thead>
			    <tbody>
			      <c:forEach var="supplier" items="${supplierList}">
			        <tr>
			          <td>${supplier.supplierId}</td>
			          <td>${supplier.supplierName}</td>
			          <td>${supplier.businessNumber}</td>
			          <td>${supplier.supplierType}</td>
			          <td>${supplier.representativeName}</td>
			          <td>${supplier.contactPhone}</td>
			          <td>
			            <c:choose>
			              <c:when test="${supplier.status eq '활성'}">
			                <span class="badge bg-success">활성</span>
			              </c:when>
			              <c:otherwise>
			                <span class="badge bg-secondary">비활성</span>
			              </c:otherwise>
			            </c:choose>
			          </td>
			          <td><fmt:formatDate value="${supplier.createdAt}" pattern="yyyy-MM-dd" /></td>
			          <td>
			            <a href="/supplier/view?supplierId=${supplier.supplierId}" class="btn btn-sm btn-outline-secondary">상세</a>
			          </td>
			        </tr>
			      </c:forEach>
			    </tbody>
			  </table>
			</div>
			

			
			<!-- 메시지 알림 -->
			<c:if test="${not empty msg}">
			  <script>alert("${msg}");</script>
			</c:if>
			
          </div>
          
			<!-- 페이징 처리 시작 -->
			<div class="mt-4 d-flex justify-content-center">
			  <ul class="pagination">
			
			    <!-- 이전 버튼 -->
			    <c:if test="${pageMaker.prev}">
			      <li class="page-item">
			        <a class="page-link"
			           href="?page=${pageMaker.startPage - 1}
			                  &condition=${cri.condition}
			                  &keyword=${cri.keyword}
			                  &sortColumn=${cri.sortColumn}
			                  &sortOrder=${cri.sortOrder}"
			           aria-label="Previous">
			          <span aria-hidden="true">&laquo;</span>
			        </a>
			      </li>
			    </c:if>
			
			    <!-- 페이지 번호 출력 -->
			    <c:forEach var="i" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
			      <li class="page-item ${cri.page == i ? 'active' : ''}">
			        <a class="page-link"
			           href="?page=${i}
			                  &condition=${cri.condition}
			                  &keyword=${cri.keyword}
			                  &sortColumn=${cri.sortColumn}
			                  &sortOrder=${cri.sortOrder}">${i}</a>
			      </li>
			    </c:forEach>
			
			    <!-- 다음 버튼 -->
			    <c:if test="${pageMaker.next}">
			      <li class="page-item">
			        <a class="page-link"
			           href="?page=${pageMaker.endPage + 1}
			                  &condition=${cri.condition}
			                  &keyword=${cri.keyword}
			                  &sortColumn=${cri.sortColumn}
			                  &sortOrder=${cri.sortOrder}"
			           aria-label="Next">
			          <span aria-hidden="true">&raquo;</span>
			        </a>
			      </li>
			    </c:if>
			
			  </ul>
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

<!-- ✅ DataTables 초기화 (정렬만 사용, 페이징X) -->
<script>
$(document).ready(function () {
  $('#supplierTable').DataTable({
    paging: false,        // ❌ 페이징 비활성 (서버 페이징 사용)
    ordering: true,       // ✅ 정렬 가능
    searching: false,     // ❌ 검색창 비활성 (직접 구현)
    info: false,          // ❌ "n개 중 m개 표시 중" 비활성
    columnDefs: [
      { targets: [4, 5, 6, 7, 8], orderable: false }  // 정렬 제외 열 ([5, 8, 9])
    ]
  });
});
</script>