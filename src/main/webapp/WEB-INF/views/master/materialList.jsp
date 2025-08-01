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
			
			<!-- [1] 검색 조건 줄 -->
			<div class="d-flex flex-wrap align-items-center justify-content-between mb-2">
			
			  <form method="get" action="/material/list" class="form-inline flex-wrap">
			    <input type="text" name="keyword" class="form-control mr-2 mb-2" placeholder="자재명 검색" value="${cri.keyword}" />
			
			    <select name="materialType" class="form-control mr-2 mb-2">
			      <option value="">전체 유형</option>
			      <option value="생육" ${cri.materialType == '생육' ? 'selected' : ''}>생육</option>
			      <option value="채소류" ${cri.materialType == '채소류' ? 'selected' : ''}>채소류</option>
			    </select>
			
			    <select name="storageMethod" class="form-control mr-2 mb-2">
			      <option value="">전체 보관법</option>
			      <option value="냉장" ${cri.storageMethod == '냉장' ? 'selected' : ''}>냉장</option>
			      <option value="냉동" ${cri.storageMethod == '냉동' ? 'selected' : ''}>냉동</option>
			      <option value="상온" ${cri.storageMethod == '상온' ? 'selected' : ''}>상온</option>
			    </select>
			
			    <select name="lotFlag" class="form-control mr-2 mb-2">
			      <option value="">LOT관리 여부</option>
			      <option value="Y" ${cri.lotFlag == 'Y' ? 'selected' : ''}>Y</option>
			      <option value="N" ${cri.lotFlag == 'N' ? 'selected' : ''}>N</option>
			    </select>
			
			    <button type="submit" class="btn btn-primary mr-2 mb-2">검색</button>
			    <a href="/material/list" class="btn btn-light mb-2"><i class="ti-reload"></i> 초기화</a>
			  </form>
			
			</div>
			
			<!-- [2] 신규 등록 버튼 한 줄 아래 정렬 -->
			<div class="text-right mb-3">
			  <a href="/material/register" class="btn btn-success">신규 등록</a>
			</div>


		    
		<!-- 목록 테이블 -->
		<div class="table-responsive">
		  <table class="table table-hover">
		       <thead>
		          <tr>
		            <th>
				      <a href="?page=1&keyword=${cri.keyword}&sortColumn=material_id&sortOrder=${cri.sortColumn == 'material_id' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}"
				         class="text-white text-decoration-none">
				        자재ID
				        <c:choose>
				          <c:when test="${cri.sortColumn == 'material_id'}">
				            <i class="ti-arrow-${cri.sortOrder == 'asc' ? 'up' : 'down'}"></i>
				          </c:when>
				          <c:otherwise>⇅</c:otherwise>
				        </c:choose>
				      </a>
		            </th>
		            
		            <th>
				      <a href="?page=1&keyword=${cri.keyword}&sortColumn=material_name&sortOrder=${cri.sortColumn == 'material_name' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}"
				         class="text-white text-decoration-none">
				        자재명
				        <c:choose>
				          <c:when test="${cri.sortColumn == 'material_name'}">
				            <i class="ti-arrow-${cri.sortOrder == 'asc' ? 'up' : 'down'}"></i>
				          </c:when>
				          <c:otherwise>⇅</c:otherwise>
				        </c:choose>
				      </a>
		            </th>
		            <th>
				      <a href="?page=1&keyword=${cri.keyword}&sortColumn=material_type&sortOrder=${cri.sortColumn == 'material_type' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}"
				         class="text-white text-decoration-none">
				        유형
				        <c:choose>
				          <c:when test="${cri.sortColumn == 'material_type'}">
				            <i class="ti-arrow-${cri.sortOrder == 'asc' ? 'up' : 'down'}"></i>
				          </c:when>
				          <c:otherwise>⇅</c:otherwise>
				        </c:choose>
				      </a>
				    </th>
		            <th>단위</th>
		             <th>
				      <a href="?page=1&keyword=${cri.keyword}&sortColumn=unit_price&sortOrder=${cri.sortColumn == 'unit_price' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}"
				         class="text-white text-decoration-none">
				        단가
				        <c:choose>
				          <c:when test="${cri.sortColumn == 'unit_price'}">
				            <i class="ti-arrow-${cri.sortOrder == 'asc' ? 'up' : 'down'}"></i>
				          </c:when>
				          <c:otherwise>⇅</c:otherwise>
				        </c:choose>
				      </a>
				    </th>
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
		              <td>${material.warehouseCode}</td>
		              <td><c:if test="${material.lotFlag eq 'Y'}">Y</c:if><c:if test="${material.lotFlag ne 'Y'}">N</c:if></td>
		              <td>${material.supplyUnit}</td>
		              <td>
		                <button type="button" class="btn btn-sm btn-outline-secondary"
		                       onclick="location.href='/material/edit?materialId=${material.materialId}'">
		                  수정
		                </button>
		              </td>
		            </tr>
		          </c:forEach>
		        </tbody>
		      </table>
		    </div>
		  </div>
		
		  <!-- 페이징 처리 시작 -->
			<div class="d-flex justify-content-center mt-4">
			 <nav>
			  <ul class="pagination justify-content-center mt-4">
			
			    <!-- 이전 버튼 -->
			    <c:if test="${pageMaker.cri.page > 1}">
			      <li class="page-item">
			        <a class="page-link"
			           href="?page=${pageMaker.startPage - 1}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}"
			           aria-label="Previous">
			          <span aria-hidden="true">&laquo;</span>
			        </a>
			      </li>
			    </c:if>
			
			    <!-- 페이지 번호 -->
			    <c:forEach var="i" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
			      <li class="page-item ${cri.page == i ? 'active' : ''}">
			        <a class="page-link"
			           href="?page=${i}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">${i}</a>
			      </li>
			    </c:forEach>
			
			    <!-- 다음 버튼 -->
			    <c:if test="${pageMaker.cri.page < pageMaker.endPage}">
			      <li class="page-item">
			        <a class="page-link"
			           href="?page=${pageMaker.endPage + 1}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}"
			           aria-label="Next">
			          <span aria-hidden="true">&raquo;</span>
			        </a>
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

<script>
  function fillForm(materialId, materialName, materialType, unit, unitPrice, storageMethod, warehouseCode, lotFlag, supplyUnit) {
    // 폼 보이기
    document.getElementById("formCard").style.display = "block";

    // 값 채우기
    document.getElementById("materialId").value = materialId;
    document.getElementById("materialName").value = materialName;
    
    // 셀렉트 박스 설정
    setSelectedValue("materialType", materialType);
    setSelectedValue("unit", unit);
    document.getElementById("unitPrice").value = unitPrice;
    setSelectedValue("storageMethod", storageMethod);
    setSelectedValue("warehouseCode", warehouseCode);
    setSelectedValue("lotFlag", lotFlag);
    document.getElementById("supplyUnit").value = supplyUnit;
  }

  function setSelectedValue(selectId, value) {
    const select = document.getElementById(selectId);
    if (!select) return;
    for (let i = 0; i < select.options.length; i++) {
      if (select.options[i].value === value) {
        select.selectedIndex = i;
        break;
      }
    }
  }

  function resetForm() {
    document.getElementById("materialForm").reset();
    document.getElementById("formCard").style.display = "none";
  }
</script>