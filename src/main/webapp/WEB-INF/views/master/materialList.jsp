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
			  <h3 class="font-weight-bold">자재 정보</h3>
			</div>

		<!-- 제목 + 버튼 -->
	    <div class="col-md-6 text-right">
	      <button class="btn btn-primary" onclick="toggleForm()">신규 등록</button>
	    </div>
		
		<!-- 목록 테이블 -->
		<div class="table-responsive mt-4">
		  <table class="table table-bordered table-hover table-header-dark">
		       <thead>
		          <tr>
		            <th>자재ID</th>
		            <th>자재명</th>
		            <th>유형</th>
		            <th>단위</th>
		            <th>단가</th>
		            <th>보관법</th>
		            <th>보관창고</th>
		            <th>LOT관리</th>
		            <th>입고단위</th>
		            <th>수정</th>
		          </tr>
		        </thead>
		        <tbody>
		          <c:forEach var="material" items="${materialList}">
		            <tr>
		              <td>${material.materialId}</td>
		              <td>${material.materialName}</td>
		              <td>${material.materialType}</td>
		              <td>${material.unit}</td>
		              <td><fmt:formatNumber value="${material.unitPrice}" pattern="#,##0"/></td>
		              <td>${material.storageMethod}</td>
		              <td>${material.storageLocation}</td>
		              <td><c:if test="${material.lotFlag eq 'Y'}">Y</c:if><c:if test="${material.lotFlag ne 'Y'}">N</c:if></td>
		              <td>${material.supplyUnit}</td>
		              <td>
		                <button type="button" class="btn btn-sm btn-outline-secondary"
		                        onclick="fillForm('${material.materialId}','${material.materialName}','${material.materialType}',
		                                          '${material.unit}','${material.unitPrice}','${material.storageMethod}',
		                                          '${material.storageLocation}', '${material.lotFlag}','${material.supplyUnit}')">
		                  수정
		                </button>
		              </td>
		            </tr>
		          </c:forEach>
		        </tbody>
		      </table>
		    </div>
		  </div>

		<!-- 등록/수정 폼: 처음엔 숨김 -->
		<div class="card mt-4" id="formCard" style="display: none;">
		  <div class="card-body">
		    <h4 class="card-title">자재 등록 / 수정</h4>
		    <form action="/material/save" method="post" id="materialForm">
		      
		      <div class="form-group">
		        <label>자재ID</label>
		        <input type="text" name="materialId" id="materialId" class="form-control" readonly placeholder="자동생성됩니다.">
		      </div>
		      
		      <div class="form-group">
		        <label>자재명</label>
		        <input type="text" name="materialName" id="materialName" class="form-control" required>
		      </div>
		
		      <!-- 유형, 단위, 단가, 보관법, 창고 등 동일한 형식 반복 -->
		
		      <button type="submit" class="btn btn-primary mr-2">저장</button>
		      <button type="button" class="btn btn-light" onclick="resetForm()">초기화</button>
		    </form>
		  </div>
		</div>
		
		  <!-- 페이징 처리 시작 -->
			<div class="mt-4 d-flex justify-content-center">
			  <ul class="pagination">
			
			    <!-- 이전 버튼 -->
			    <c:if test="${pageMaker.prev}">
			      <li class="page-item">
			        <a class="page-link"
			           href="?page=${pageMaker.startPage - 1}&keyword=${cri.keyword}"
			           aria-label="Previous">
			          <span aria-hidden="true">&laquo;</span>
			        </a>
			      </li>
			    </c:if>
			
			    <!-- 페이지 번호 -->
			    <c:forEach var="i" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
			      <li class="page-item ${cri.page == i ? 'active' : ''}">
			        <a class="page-link"
			           href="?page=${i}&keyword=${cri.keyword}">${i}</a>
			      </li>
			    </c:forEach>
			
			    <!-- 다음 버튼 -->
			    <c:if test="${pageMaker.next}">
			      <li class="page-item">
			        <a class="page-link"
			           href="?page=${pageMaker.endPage + 1}&keyword=${cri.keyword}"
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

<!-- 수정 버튼 클릭 시, 기존 데이터 불러오기 -->
<script>

function fillForm(id, name, type, unit, price, storageMethod, storageLocation, lotFlag, supply) {
    // form에 값 세팅
    document.getElementById('materialId').value = id;
    document.getElementById('materialId').readOnly = true; // 수정시 ID 변경 못하게
    document.getElementById('materialName').value = name;
    document.getElementById('materialType').value = type;
    document.getElementById('unit').value = unit;
    document.getElementById('unitPrice').value = price;
    document.getElementById('storageMethod').value = storageMethod;
    document.getElementById('storageLocation').value = storageLocation;
    document.getElementById('lotFlag').value = lotFlag.trim();
    document.getElementById('supplyUnit').value = supply;

    // 탭 이동
    var formTab = new bootstrap.Tab(document.getElementById('form-tab'));
    formTab.show();
}



function resetForm() {
    document.getElementById('materialForm').reset();					// 전체 입력 초기화
    document.getElementById('materialId').value = '';					// ID 초기화
    document.getElementById('materialId').readOnly = true; 				// 항상 readonly 유지
}
</script>