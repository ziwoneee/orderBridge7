<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>
    <%
    // 오늘 날짜를 yyyy-MM-dd로 request에 today로 저장
    java.time.LocalDate today = java.time.LocalDate.now();
    String todayStr = today.toString();
    request.setAttribute("today", todayStr);
%>
    
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
			  <h3 class="font-weight-bold">수주 목록</h3>
			</div>    

            
                <!-- 본문내용 시작 -->
                <!-- ✅ 검색창 -->
  	<div class="d-flex justify-content-between align-items-center mb-2">
		
        <form method="get" class="form-inline flex-wrap">
 <!-- 수주일자 기간 조회 추가 -->
     <!-- 수주일자 기간 조회 -->
        <label for="startDate" class="mr-1">수주일자</label>
        <input type="date" id="startDate" name="startDate"
            value="${empty cri.startDate ? '' : cri.startDate}"
            max="${today}" class="form-control mr-2">
        <span class="mx-1">~</span>
        <input type="date" id="endDate" name="endDate"
            value="${empty cri.endDate ? '' : cri.endDate}"
            max="${today}" class="form-control mr-2">

<!-- 거래처/제품명 검색 -->
 <input type="text" name="keyword" value="${cri.keyword}" class="form-control mr-2" placeholder="거래처/제품명 검색">
 
      <select name="status" class="form-control mr-2">
    <option value="">전체상태</option>
    <option value="REQUESTED"
      <%
          if ("REQUESTED".equals(request.getParameter("status"))) {
              out.print("selected");
          }
      %>
    >주문접수</option>
    <option value="CONFIRMED"
      <%
          if ("CONFIRMED".equals(request.getParameter("status"))) {
              out.print("selected");
          }
      %>
    >확정</option>
    <option value="SHIPPED"
      <%
          if ("SHIPPED".equals(request.getParameter("status"))) {
              out.print("selected");
          }
      %>
    >출하</option>
    <option value="CANCELLED"
      <%
          if ("CANCELLED".equals(request.getParameter("status"))) {
              out.print("selected");
          }
      %>
    >취소</option>
</select>

        <button type="submit" class="btn btn-primary mr-2">검색</button>
        
         
         </form>
         
         </div>
        
<!-- 구분선 -->
<hr class="my-3">
   
<div class="d-flex justify-content-between align-items-center mb-2"> 
  
  
       <!-- 상태 일괄 변경 폼 -->      
<form id="bulkStatusForm" method="post" action="${pageContext.request.contextPath}/clientorder/updateStatus">
  <div class="form-row align-items-center">
    <div class="col-auto">
      <select id="bulkStatus" name="newStatus" class="form-control">
        <option value="">상태 선택</option>
        <option value="REQUESTED">주문접수</option>
        <option value="CONFIRMED">확정</option>
        <option value="SHIPPED">출하</option>
        <option value="CANCELLED">취소</option>
      </select>
    </div>
    <div class="col-auto">
      <button type="button" class="btn btn-warning" onclick="submitBulkStatus()">수주상태변경</button>
    </div>
  </div>
  <!-- 숨겨진 input 영역 (자바스크립트로 선택된 ID 전달) -->
  <input type="hidden" name="orderIds" id="bulkOrderIds">
</form>
    
  <!-- 목록 다운로드 버튼 -->
  <div class="col-auto">
 <a href="${pageContext.request.contextPath}/clientorder/export" class="btn btn-success">목록 다운로드</a>
    </div>
         </div>  
  </div>
      
    
  	<div class="table-responsive mt-4">
     <table id="clorderTable" class="table table-bordered text-center">
        <thead>
        <tr>
            <th><input type="checkbox" id="selectAll"></th>
            <th>수주번호</th>
            <th>거래처명</th>            
            <th>수주일자</th>
            <th>납기요청일</th>
            <th>수주상태</th>
            <th>메모</th>
            <th>상세</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="order" items="${orderList}">
            <tr>
                <td><input type="checkbox" name="orderChk" value="${order.clOrderId}"></td>
                <td>${order.clOrderNum}</td>
                <td>${order.clientName}</td>                
                <td><fmt:formatDate value="${order.clOrderDate}" pattern="yyyy-MM-dd"/></td>
                <td><fmt:formatDate value="${order.clDeliveryDate}" pattern="yyyy-MM-dd"/></td>
                <td>
                  <c:choose>
                    <c:when test="${order.clOrderStatus == 'REQUESTED'}">
                      <span style="color: #dc3545; font-weight: bold;">주문접수</span>
                    </c:when>
                    <c:when test="${order.clOrderStatus == 'CONFIRMED'}">
                      <span style="color: #007bff; font-weight: bold;">확정</span>
                    </c:when>
                    <c:when test="${order.clOrderStatus == 'SHIPPED'}">
                      <span style="color: #28a745; font-weight: bold;">출하</span>
                    </c:when>
                    <c:when test="${order.clOrderStatus == 'CANCELLED'}">
                      <span style="color: #6c757d;">취소</span>
                    </c:when>
                    <c:otherwise>
                      <span style="color: #6c757d;">알 수 없음</span>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td>${order.clOrderMemo}</td>
                <td>
                  <a href="${pageContext.request.contextPath}/clientorder/detail?clOrderId=${order.clOrderId}" class="btn btn-outline-secondary btn-sm">상세</a>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    
  
    <div class = "text-right mb-3">
      <a href="${pageContext.request.contextPath}/clientorder/register" class="btn btn-primary">+ 신규 수주 등록</a>   
      </div> </div>
      
     <!-- ✅ 페이징 영역 -->
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

<!-- 전체선택 JS -->
<script>
    document.getElementById('selectAll').onclick = function() {
        var checkboxes = document.getElementsByName('orderChk');
        for (var checkbox of checkboxes) {
            checkbox.checked = this.checked;
        }
    }
</script>

<script>
function submitBulkStatus() {
    // 선택된 체크박스 수집
    var checked = document.querySelectorAll('input[name="orderChk"]:checked');
    if (checked.length === 0) {
        alert('상태를 변경할 수주를 선택하세요.');
        return;
    }
    // 상태 선택 여부
    var newStatus = document.getElementById('bulkStatus').value;
    if (!newStatus) {
        alert('변경할 수주상태를 선택하세요.');
        return;
    }
    // ID 리스트 만들기 (콤마 구분)
    var ids = Array.from(checked).map(c => c.value).join(',');
    document.getElementById('bulkOrderIds').value = ids;

    // 폼 전송
    document.getElementById('bulkStatusForm').submit();
}
</script>
<!-- ✅ DataTables JS -->
<script src="https://cdn.datatables.net/1.10.24/js/jquery.dataTables.min.js"></script>

<!-- ✅ DataTables 초기화 (정렬만 사용, 페이징X) -->
<script>
$(document).ready(function () {
  $('#clorderTable').DataTable({
    paging: false,        // ❌ 페이징 비활성 (서버 페이징 사용)
    ordering: true,       // ✅ 정렬 가능
    searching: false,     // ❌ 검색창 비활성 (직접 구현)
    info: false,          // ❌ "n개 중 m개 표시 중" 비활성
    columnDefs: [
      { targets: [0,5,6,7], orderable: false }  // 정렬 제외 열 (사업자번호, 대표자명, 상세버튼)
    ]
  });
});
</script>


