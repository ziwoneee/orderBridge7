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
			
			    <button type="submit" class="btn btn-primary me-2"><i class="ti-search"></i> 검색</button>
			    <a href="/supplier/list" class="btn btn-light"><i class="ti-reload"></i> 초기화 </a>
			    
			  </form>
			
			  <!-- 오른쪽: 신규 등록 버튼 -->
			  <a href="/supplier/register" class="btn btn-success mb-2">신규 등록</a>
			</div>



			
		<!-- 테이블 -->
		<div id="table_content" class="table-responsive">
		<table class="table table-bordered text-center">
			<thead>
				<tr>
				<th>
				  <a href="?page=1&sortColumn=supplier_id&sortOrder=${cri.sortColumn eq 'supplier_id' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&condition=${cri.condition}&keyword=${fn:escapeXml(cri.keyword)}">
				    협력사코드
					<c:choose>
				      <c:when test="${cri.sortColumn eq 'supplier_id'}">
					      <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
					  </c:when>
				      <c:otherwise>
				        <span class="neutral-arrow">⇅</span>  <%-- 기본 회색 아이콘 --%>
				      </c:otherwise>
				    </c:choose>
				  </a>
				</th>
				<th>
				  <a href="?page=1&sortColumn=supplier_name&sortOrder=${cri.sortColumn eq 'supplier_name' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&condition=${cri.condition}&keyword=${fn:escapeXml(cri.keyword)}">
				    협력사명
					<c:choose>
				      <c:when test="${cri.sortColumn eq 'supplier_name'}">
				        <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
				      </c:when>
				      <c:otherwise>
				        <span class="neutral-arrow">⇅</span>  <%-- 기본 회색 아이콘 --%>
				      </c:otherwise>
				    </c:choose>
				  </a>
				</th>
				<th>사업자등록번호</th>
				<th>업태/종목</th>
				<th>대표자명</th>
				<th>담당자 연락처</th>
				<th>
				  <a href="?page=1&sortColumn=created_at&sortOrder=${cri.sortColumn eq 'created_at' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&condition=${cri.condition}&keyword=${fn:escapeXml(cri.keyword)}">
				    등록일자
					<c:choose>
				      <c:when test="${cri.sortColumn eq 'created_at'}">
				        <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
				      </c:when>
				      <c:otherwise>
				        <span class="neutral-arrow">⇅</span>  <%-- 기본 회색 아이콘 --%>
				      </c:otherwise>
				    </c:choose>
				  </a>
				</th>
				<th>거래상태</th>
				<th>상세</th>
	         </tr>
		</thead>
		
			    <tbody>
			      <c:forEach var="supplier" items="${supplierList}">
			        <tr
						class="display-row ${supplier.status eq '비활성' ? 'inactive-row' : ''}"
						id="display-${supplier.supplierId}">

			          <td>${supplier.supplierId}</td>
			          <td>${supplier.supplierName}</td>
			          <td>${supplier.businessNumber}</td>
			          <td>${supplier.supplierType}</td>
			          <td>${supplier.representativeName}</td>
			          <td>${supplier.contactPhone}</td>
			          <td><fmt:formatDate value="${supplier.createdAt}" pattern="yyyy-MM-dd" /></td>
			          <td>
			            <c:choose>
			              <c:when test="${supplier.status eq '활성'}">
			                <span class="badge badge-success">활성</span>
			              </c:when>
			              <c:otherwise>
			                <span class="badge badge-secondary">비활성</span>
			              </c:otherwise>
			            </c:choose>
			          </td>
			          <td>
			            <a href="/supplierItem/items?supplierId=${supplier.supplierId}" class="btn btn-sm btn-outline-primary">품목</a>
  						<a href="/supplier/view?supplierId=${supplier.supplierId}" class="btn btn-sm btn-outline-info">상세</a>
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