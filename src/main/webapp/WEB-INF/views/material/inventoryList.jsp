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
			  <h3 class="font-weight-bold">자재 재고 현황</h3>
			</div>
			
			<!-- 검색 필터 -->
		  <form method="get" action="/material/inventory/list" class="form-inline mb-3">
		    <!-- 상품유형 -->
		    <select name="materialType" class="form-control mr-3" onchange="this.form.submit();">
			    <option value="">자재유형</option>
			    <option value="생육" ${param.materialType eq '생육' ? 'selected' : ''}>생육</option>
			    <option value="채소류" ${param.materialType eq '채소류' ? 'selected' : ''}>채소류</option>
			    <option value="향신료" ${param.materialType eq '향신료' ? 'selected' : ''}>향신료</option>
			    <option value="조미료" ${param.materialType eq '조미료' ? 'selected' : ''}>조미료</option>
			    <option value="액상조미료" ${param.materialType eq '액상조미료' ? 'selected' : ''}>액상조미료</option>
			    <option value="외주가공" ${param.materialType eq '외주가공' ? 'selected' : ''}>외주가공</option>
			    <option value="포장재" ${param.materialType eq '포장재' ? 'selected' : ''}>포장재</option>
			    <option value="기타" ${param.materialType eq '기타' ? 'selected' : ''}>기타</option>
		    </select>
			<!-- 품번 -->
		    <label class="sr-only">자재코드</label>
		    <input type="text" name="materialId" class="form-control mr-2" placeholder="예: RM-0001" value="${param.materialId}">
		
		    <!-- 품명 -->
		    <label class="sr-only">자재명</label>
		    <input type="text" name="materialName" class="form-control mr-2" placeholder="자재명" value="${param.materialName}">
		    
		    <button type="submit" class="btn btn-primary">조회</button>
		  </form>
          
          
    <!-- 재고 목록 테이블 -->      
	<div id="table_content" class="table-responsive">
	<table class="table table-bordered text-center">
		<thead>
			<tr>
		        <th>자재코드</th>
		        <th>자재명</th>
		        <th>유형</th>
		        <th>현재고</th>
		        <th>안전재고</th>
		        <th>상태</th>
		        <th>단위</th>
		        <th>유통기한</th>
		        <th>최근입출고일</th>
		        <th>보관창고</th>
		        <th>실사량</th>
		        <th>LOT 번호 상세</th>
	        </tr>
		</thead>
		
		<tbody>
			 <c:forEach var="inv" items="${inventoryList}">
		        <tr>
		          <td>${inv.materialId}</td>
		          <td>${inv.materialName}</td>
		          <td>${inv.materialType}</td>
		          <td>${inv.quantity}</td>
		          <td>${inv.safetyStock}</td>
		          <td>
					  <c:choose>
					    <c:when test="${inv.inventoryStatus eq '정상'}">
					      <span class="badge badge-success">정상</span>
					    </c:when>
					    <c:when test="${inv.inventoryStatus eq '부족'}">
					      <span class="badge badge-warning">부족</span>
					    </c:when>
					    <c:otherwise>
					      <span class="badge badge-danger">${inv.inventoryStatus}</span>
					    </c:otherwise>
					  </c:choose>
				  </td>
		          <td>${inv.unit}</td>
				  <td>
		          	  <fmt:formatDate value="${inv.expirationDate}" pattern="yyyy-MM-dd" />
					  <c:if test="${inv.expirationDate.time - now.time le 3 * 24 * 60 * 60 * 1000}">
					    <span class="badge badge-danger ml-1">임박</span>
					  </c:if>
	          	  </td>
		          <td><fmt:formatDate value="${inv.lastMovementDate}" pattern="yyyy-MM-dd" /></td>
		          <td>${inv.storageLocation}</td>
		          <td>${inv.actualQuantity}</td>
		          <!-- LOT 상세 버튼 -->
		          <td>
		            <button class="btn btn-outline-info btn-sm">LOT</button>
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