<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">

  <%@ include file="/WEB-INF/views/main/top.jsp" %>      

  <div class="container-fluid page-body-wrapper">

    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>
    
    
    <script>
        function showInsertRow() {
            document.getElementById('insert-row').style.display = '';
            document.getElementById('insert-btn').style.display = 'none';
        }
        function cancelInsert() {
            document.getElementById('insert-row').style.display = 'none';
            document.getElementById('insert-btn').style.display = '';
        }
        function editRow(bomId) {
            document.querySelectorAll('.edit-row').forEach(function(tr) { tr.style.display = 'none'; });
            document.querySelectorAll('.display-row').forEach(function(tr) { tr.style.display = ''; });
            document.getElementById('display-' + bomId).style.display = 'none';
            document.getElementById('edit-' + bomId).style.display = '';
        }
        function cancelEdit(bomId) {
            document.getElementById('display-' + bomId).style.display = '';
            document.getElementById('edit-' + bomId).style.display = 'none';
        }
    </script>
    
    
<!-- 헤더, 사이드바,개인설정 끝 -->
  <div class="main-panel">
        <div class="content-wrapper">
            <div class="row">
            
               <!-- 제목 -->
			<div class="col-12 mb-4">
			  <h3 class="font-weight-bold">BOM 정보</h3>
			</div>
			
   <button id="insert-btn" type="button" class="btn btn-success mb-2"
    onclick="location.href='${pageContext.request.contextPath}/master/bom/insert'">신규등록</button>

   <div class="table-responsive">
		<table class="table table-hover text-center">
        <thead>
            <tr>
                <th>BOM ID</th>
                <th>제품ID</th>
                <th>제품명</th>
                <th>BOM명</th>
                <th>등록일</th>
                <th>상태</th>
                <th>비고</th>
                <th>관리</th>
            </tr>
        </thead>
        <tbody>
        

        <!-- BOM 목록 출력 및 인라인 수정 -->
        <c:forEach var="bom" items="${bomList}">
    <tr class="display-row"
        <c:if test="${bom.status eq 'INACTIVE'}"> style="color: #bbb; background: #f9f9f9; text-decoration:line-through;" </c:if>
        id="display-${bom.bomId}">
        <td>${bom.bomId}</td>
        <td>${bom.productId}</td>
        <td>${bom.productName}</td>
        <td>${bom.bomName}</td>
        <td><fmt:formatDate value="${bom.bomDate}" pattern="yyyy-MM-dd"/></td>
        <td>
  <c:choose>
    <c:when test="${bom.status eq 'ACTIVE'}">
      <span class="badge badge-success">활성</span>
    </c:when>
    <c:otherwise>
      <span class="badge badge-secondary">비활성</span>
    </c:otherwise>
  </c:choose>
</td>

        <td>${bom.bomEtc}</td>
        <td>                   
            <a href="${pageContext.request.contextPath}/master/bom/detail/${bom.bomId}" class="btn btn-sm btn-outline-info">상세</a>
        </td>
    </tr>
</c:forEach>

        </tbody>
    </table>
    </div>

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


<script>
function validateForm() {
    // 예시: 프론트 중복검사(실제는 서버에서 해야함)
    // 필요시 구현, 현재는 항상 true 반환
    return true;
}
</script>
