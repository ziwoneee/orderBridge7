<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>



<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>      

  <div class="container-fluid page-body-wrapper">
  
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

    <div class="main-panel">
      <div class="content-wrapper">
       <div class="row">

          <!-- 제목 -->
          <div class="col-12 mb-4">
            <h3 class="font-weight-bold">고객사 정보</h3>
          </div>

          <!-- ✅ 검색창 -->
          <div class="d-flex justify-content-between align-items-center mb-2">
            <form action="/client/list" method="get" class="form-inline flex-wrap">
              <select name="sortColumn" class="form-control mr-2 mb-2">
                <option value="all" ${cri.sortColumn == 'all' ? 'selected' : ''}>전체</option>
                <option value="clientName" ${cri.sortColumn == 'clientName' ? 'selected' : ''}>고객사명</option>
                <option value="businessNumber" ${cri.sortColumn == 'businessNumber' ? 'selected' : ''}>사업자등록번호</option>
                <option value="ceoName" ${cri.sortColumn == 'ceoName' ? 'selected' : ''}>대표자명</option>
              </select>

              <div class="form-group d-flex flex-wrap">
                <input type="text" name="keyword" class="form-control mr-2 mb-2" style="min-width: 220px;"
                       value="${cri.keyword}"/>
                 <button type="submit" class="btn btn-primary me-2 mb-2" >
                      <i class="ti-search"></i> 검색
                    </button>
                
                
                <a href="/client/list" class="btn btn-light">
          <i class="ti-reload"></i> 초기화
        </a>
        
        <a href="/client/register" class="btn btn-success me-2">신규 등록</a>
              </div>
            </form>
          </div>

 <!-- 테이블 -->
<div id="table_content" class="table-responsive">
  <table class="table table-bordered text-center">
    <thead>
      <tr>
        <th>고객사코드</th>
        <!-- 고객사명 정렬 -->
        <th>
          <a href="?page=1&sortColumn=clientName&sortOrder=${cri.sortColumn eq 'clientname' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}">
            고객사명
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

        <th>사업자등록번호</th>
        <th>대표자명</th>
        <th>담당자 연락처</th>

        <!-- 등록일 정렬 -->
        <th>
          <a href="?page=1&sortColumn=createdAt&sortOrder=${cri.sortColumn eq 'createdat' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}">
            등록일자
            <c:choose>
              <c:when test="${cri.sortColumn eq 'createdat'}">
                <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
              </c:when>
              <c:otherwise>
                <span class="neutral-arrow">⇅</span>
              </c:otherwise>
            </c:choose>
          </a>
        </th>

        <!-- 상태 정렬 -->
        <th>
          <a href="?page=1&sortColumn=statusCode&sortOrder=${cri.sortColumn eq 'statuscode' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}">
            거래상태
            <c:choose>
              <c:when test="${cri.sortColumn eq 'statuscode'}">
                <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
              </c:when>
              <c:otherwise>
                <span class="neutral-arrow">⇅</span>
              </c:otherwise>
            </c:choose>
          </a>
        </th>

        <th>상세</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="client" items="${clientList}">
        <tr>
          <td>${client.clientId}</td>
          <td>${client.clientName}</td>
          <td>${client.businessNumber}</td>
          <td>${client.ceoName}</td>
          <td>${client.clientTel}</td>
          <td><fmt:formatDate value="${client.createdAt}" pattern="yyyy-MM-dd" /></td>
          <td>
            <span class="badge badge-${client.statusCode == 1 ? 'success' : 'secondary'}">
              ${client.statusCode == 1 ? '활성' : '비활성'}
            </span>
          </td>
          <td>
            <a href="/client/detail?clientId=${client.clientId}" class="btn btn-sm btn-outline-info">상세</a>
          </td>
        </tr>
      </c:forEach>

      <c:if test="${empty clientList}">
        <tr>
          <td colspan="8" class="text-danger font-weight-bold">조회된 고객사가 없습니다.</td>
        </tr>
      </c:if>
    </tbody>
  </table>
</div>

 
<!-- ✅ Bootstrap 페이징 스타일 -->
<!-- ✅ Bootstrap 중앙정렬 페이지네이션 (최적화 구조) -->
<div class="container mt-4">
  <nav class="d-flex justify-content-center">
    <ul class="pagination">

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

<style>
  .neutral-arrow {
    color: #ccc;
  }
</style>
 
