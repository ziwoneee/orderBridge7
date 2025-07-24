<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
    
    <%
    // 오늘 날짜를 yyyy-MM-dd로 request에 today로 저장
    java.time.LocalDate today = java.time.LocalDate.now();
    String todayStr = today.toString();
    request.setAttribute("today", todayStr);
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
			  <h3 class="font-weight-bold">수주 목록</h3>
              <h6 class="font-weight-normal mb-0">수주 목록화면입니다. <span class="text-primary">강조쓰</span></h6>
			</div>

             <div class="contentbody"> 
                <!-- 본문내용 시작 -->
                <!-- ✅ 검색창 -->
    <div class="d-flex justify-content-center mb-3">
    <form method="get" class="form-inline mb-3">

   

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


   
<div class="d-flex justify-content-end mb-1"> 
    
 
<div class="d-flex justify-content-end ">   
  
  
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
 <a href="${pageContext.request.contextPath}/clientorder/export" class="btn btn-success">목록 다운로드</a>
    
         </div>  
  </div>
      
      <div class="text-center">
     <div id="table_content" >
   <div class="table-responsive">
    <table class="table table-bordered table-hover">
        <thead class="thead-dark">
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
    </div>
    </div>
    <div class = "text-right mb-3">
      <a href="${pageContext.request.contextPath}/clientorder/register" class="btn btn-primary">+ 신규 수주 등록</a>   
      </div> 
      
    <!-- 페이지네이션 -->
    <nav class="d-flex justify-content-center">
      <ul class="pagination">
        <c:if test="${pageMaker.prev}">
          <li class="page-item">
            <a class="page-link"
               href="?page=${pageMaker.startPage-1}&keyword=${cri.keyword}&status=${cri.status}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">이전</a>
          </li>
        </c:if>
        <c:forEach begin="${pageMaker.startPage}" end="${pageMaker.endPage}" var="num">
          <li class="page-item ${cri.page == num ? 'active' : ''}">
            <a class="page-link"
               href="?page=${num}&keyword=${cri.keyword}&status=${cri.status}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">${num}</a>
          </li>
        </c:forEach>
        <c:if test="${pageMaker.next}">
          <li class="page-item">
            <a class="page-link"
               href="?page=${pageMaker.endPage+1}&keyword=${cri.keyword}&status=${cri.status}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">다음</a>
          </li>
        </c:if>
      </ul>
    </nav>

    


</div>
  


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

 <!--  본문내용 끝 -->    
              </div>
              <!-- 페이징 끝 -->
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