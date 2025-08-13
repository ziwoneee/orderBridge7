<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

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
    
    <select name="sortColumn" class="form-control mr-2">
            <option value="all" ${cri.sortColumn eq 'all' ? 'selected' : ''}>전체</option>
            <option value="product_id" ${cri.sortColumn eq 'product_id' ? 'selected' : ''}>제품ID</option>
            <option value="product_name" ${cri.sortColumn eq 'product_name' ? 'selected' : ''}>제품명</option>
            <option value="lot_no" ${cri.sortColumn eq 'lot_no' ? 'selected' : ''}>LOT번호</option>
        </select>
        <input type="text" name="keyword" value="${cri.keyword}" class="form-control mr-2" placeholder="제품명 ,ID 또는 LOT 검색">
	<input type="date" name="startDate" value="${cri.startDate}" class="form-control mr-2" max="<%= today %>">
	<span class="mx-1">~</span>
	<input type="date" name="endDate" value="${cri.endDate}" class="form-control mr-2" max="<%= today %>">
        
               
        
       <button type="submit" class="btn btn-primary me-2" >
                      <i class="ti-search"></i> 검색
                    </button>
        <a href="/product/inbound/list" class="btn btn-light">
          <i class="ti-reload"></i> 초기화
        </a>
    </form>
    </div>
    
      <!-- 자동입고 버튼 -->
    <form method="post" action="${pageContext.request.contextPath}/product/inbound/saveFromProduction">
        <!-- Hidden 필드 전달 -->
        <input type="hidden" name="keyword" value="${cri.keyword}">
        <input type="hidden" name="startDate" value="${cri.startDate}">
        <input type="hidden" name="endDate" value="${cri.endDate}">
        <input type="hidden" name="sortColumn" value="${cri.sortColumn}">
        <input type="hidden" name="sortOrder" value="${cri.sortOrder}">
        <input type="hidden" name="page" value="${cri.page}">
        
        <button type="submit" class="btn btn-success ml-5">+ 자동입고 업데이트</button>
    </form>

   <div row>
    <c:if test="${not empty msg}">
    <div class="alert alert-warning text-center">${msg}</div>
</c:if>
    </div>

    <!-- ✅ 테이블 -->
     <div class="table-responsive mt-4">
    <table id="inboundTable" class="table table-hover text-center">
       <thead>
<tr>
  <th>입고ID</th>
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


  <th>LOT번호</th>
  <th>수량</th>

  <!-- ✅ 입고일자 정렬 -->
<th>
  <a href="?page=1&sortColumn=createdat&sortOrder=${cri.sortColumn eq 'createdat' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}&keyword=${fn:escapeXml(cri.keyword)}">
    입고일자
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


  <th>입고유형</th>

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
  <th>상세내역</th> <!-- 출고내역과 동일하게 상세내역 컬럼 추가 -->
</tr>
</thead>

        <tbody>
        <c:forEach var="vo" items="${inboundList}">
            <tr>
                <td>${vo.inboundId}</td>
                <td>${vo.productId}</td>
                <td>${vo.productName}</td>                
                <td>${vo.lotNo}</td>
                <td><fmt:formatNumber value="${vo.inboundQty}" pattern="#,###"/></td>
                <td><fmt:formatDate value="${vo.createdAt}" pattern="yyyy-MM-dd"/></td>
                <td>${vo.inboundType}</td>
                <td>${vo.manager}</td>
                <td>${vo.remark}</td>
                <td>
                    <!-- 입고 상세보기 버튼 (출고내역과 동일한 스타일) -->
                  <button type="button"
        class="btn btn-sm btn-outline-info open-result-modal"
        data-resultid="${vo.resultId}"
        data-lot="${vo.lotNo}">
  상세
</button>



                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>


      <!--입고 상세 모달 영역 -->
<div class="modal fade" id="resultDetailModal" tabindex="-1" aria-hidden="true">
 <div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">

     <div class="modal-header bg-primary text-white">
        <h5 class="modal-title">생산 실적 상세</h5>
               <button type="button" class="close text-white" data-dismiss="modal">&times;</button>

      </div>
<div class="modal-body">

  <!-- ✅ 기본 정보 -->
  <table class="table table-bordered table-sm mb-4 align-middle text-center">
    <thead class="table-primary">
      <tr>
        <th colspan="8" class="text-start ps-3">기본 정보</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <th class="bg-light">실적번호</th>
        <td id="rd-resultId"></td>
        <th class="bg-light">작업지시번호</th>
        <td id="rd-orderId"></td>
        <th class="bg-light">제품명</th>
        <td id="rd-productName"></td>
        <th class="bg-light">라인명</th>
        <td id="rd-lineName"></td>
      </tr>
      <tr>
        <th class="bg-light">작업자명</th>
        <td id="rd-workerName"></td>
        <th class="bg-light">상태</th>
        <td colspan="1">
          <span id="rd-statusBadge" class="badge bg-secondary"></span>
        </td>
        <th class="bg-light">LOT번호</th>
        <td id="rd-lotNo"></td>
        <th class="bg-light">등록일시</th>
        <td id="rd-createdAt"></td>
      </tr>
    </tbody>
  </table>

  <!-- ✅ 수량 정보 -->
  <table class="table table-bordered table-sm mb-4 align-middle text-center">
    <thead class="table-primary">
      <tr>
        <th colspan="8" class="text-start ps-3">수량 정보</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <th class="bg-light">계획수량</th>
        <td id="rd-orderQty"></td>
        <th class="bg-light">생산수량</th>
        <td id="rd-actualQty"></td>
        <th class="bg-light">불량수량</th>
        <td id="rd-defectQty"></td>
        <th class="bg-light">달성률</th>
        <td id="rd-achievement"></td>
      </tr>
    </tbody>
  </table>

  <!-- ✅ 작업 시간 -->
  <table class="table table-bordered table-sm mb-1 align-middle text-center">
    <thead class="table-primary">
      <tr>
        <th colspan="4" class="text-start ps-3">작업 시간</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <th class="bg-light">작업 시작시간</th>
        <td id="rd-startedAt"></td>
        <th class="bg-light">작업 종료시간</th>
        <td id="rd-endedAt"></td>
      </tr>
    </tbody>
  </table>

</div>

<!-- ✅ 모달 닫기 버튼 -->
<div class="modal-footer">
  <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
</div>


    </div>
  </div>
</div>


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

<style>
  .neutral-arrow {
    color: #ccc;
  }
</style>

<script src="<c:url value='/resources/js/date-range-sync.js'/>"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

