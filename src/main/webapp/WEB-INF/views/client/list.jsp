<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

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
                       value="${cri.keyword}" placeholder="고객사명, 사업자번호, 대표자명" />
                <button type="submit" class="btn btn-primary mb-2">검색</button>
                <a href="/client/register" class="btn btn-success mb-2">신규 등록</a>
              </div>
            </form>
          </div>

          <!-- ✅ 테이블 영역 -->
          <div class="table-responsive mt-4">
            <table id="clientTable" class="table table-bordered text-center">
              <thead>
                <tr>
                  <th>고객사명</th>
                  <th>사업자등록번호</th>
                  <th>대표자명</th>
                  <th>연락처</th>
                  <th>상태</th>
                  <th>상세</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="client" items="${clientList}">
                  <tr>
                    <td class="text-left pl-3">${client.clientName}</td>
                    <td>${client.businessNumber}</td>
                    <td>${client.ceoName}</td>
                    <td>${client.clientTel}</td>
                    <td>
                      <span class="badge badge-${client.statusCode == 1 ? 'success' : 'secondary'}">
                        ${client.statusCode == 1 ? '활성' : '비활성'}
                      </span>
                    </td>
                    <td>
                      <button type="button" class="btn btn-sm btn-outline-info"
                              onclick="location.href='/client/detail?clientId=${client.clientId}'">
                        상세보기
                      </button>
                    </td>
                  </tr>
                </c:forEach>
                <c:if test="${empty clientList}">
                  <tr>
                    <td colspan="6" class="text-danger font-weight-bold">조회된 고객사가 없습니다.</td>
                  </tr>
                </c:if>
              </tbody>
            </table>
          </div>
 </div>
          <!-- ✅ 페이징 영역 -->
         <!-- 페이지네이션 -->
<!-- ✅ Bootstrap 페이징 스타일 -->
<div class="d-flex justify-content-center mt-4">
<nav>
  <ul class="pagination justify-content-center mt-4">

    <c:if test="${pageMaker.prev}">
      <li class="page-item">
        <a class="page-link" href="?page=${pageMaker.startPage - 1}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">이전</a>
      </li>
    </c:if>

    <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
      <li class="page-item ${p == cri.page ? 'active' : ''}">
        <a class="page-link" href="?page=${p}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">${p}</a>
      </li>
    </c:forEach>

    <c:if test="${pageMaker.next}">
      <li class="page-item">
        <a class="page-link" href="?page=${pageMaker.endPage + 1}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">다음</a>
      </li>
    </c:if>

  </ul>
</nav>

</div>
<!-- 페이징 처리 끝 -->

      
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
  $('#clientTable').DataTable({
    paging: false,        // ❌ 페이징 비활성 (서버 페이징 사용)
    ordering: true,       // ✅ 정렬 가능
    searching: false,     // ❌ 검색창 비활성 (직접 구현)
    info: false,          // ❌ "n개 중 m개 표시 중" 비활성
    columnDefs: [
      { targets: [1, 2, 5], orderable: false }  // 정렬 제외 열 (사업자번호, 대표자명, 상세버튼)
    ]
  });
});
</script>
