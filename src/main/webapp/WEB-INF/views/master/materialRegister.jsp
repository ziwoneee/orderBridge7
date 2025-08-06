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

        <!-- 페이지 제목 -->
        <h3 class="font-weight-bold mb-4">
		  <c:choose>
		    <c:when test="${formType == 'edit'}">자재 정보 수정</c:when>
		    <c:otherwise>자재 신규 등록</c:otherwise>
		  </c:choose>
		</h3>

        <!-- 등록 폼 시작 -->
        <form action="/material/save" method="post">
          <div class="card">
            <div class="card-body">
              <div class="row">

				<!-- 자재ID -->
				<div class="col-md-6 mb-3">
				  <label>자재ID</label>
				  <input type="text" name="materialId" value="${material.materialId}" class="form-control" readonly />
				</div>				
				
                <!-- 자재명 -->
                <div class="col-md-6 mb-3">
                  <label>자재명 <span class="text-danger">*</span></label>
                  <input type="text" name="materialName" class="form-control" value="${material.materialName}" required>
                </div>

                <!-- 유형 -->
                <div class="col-md-6 mb-3">
                  <label>유형</label>
                  <select name="materialType" class="form-control">
                    <option value="생육" ${material.materialType == '생육' ? 'selected' : ''}>생육</option>
                    <option value="외주가공" ${material.materialType == '외주가공' ? 'selected' : ''}>외주가공</option>
                    <option value="조미료" ${material.materialType == '조미료' ? 'selected' : ''}>조미료</option>
                    <option value="액상조미료" ${material.materialType == '액상조미료' ? 'selected' : ''}>액상조미료</option>
                    <option value="채소류" ${material.materialType == '채소류' ? 'selected' : ''}>채소류</option>
                    <option value="향신료" ${material.materialType == '향신료' ? 'selected' : ''}>향신료</option>
                    <option value="포장재" ${material.materialType == '포장재' ? 'selected' : ''}>포장재</option>
                    <option value="기타" ${material.materialType == '기타' ? 'selected' : ''}>기타</option>
                  </select>
                </div>

                <!-- 단위 -->
                <div class="col-md-6 mb-3">
                  <label>단위</label>
                  <select name="unit" class="form-control">
                  	<option value="g" ${material.unit == 'g' ? 'selected' : ''}>g</option>
                  	<option value="kg" ${material.unit == 'kg' ? 'selected' : ''}>kg</option>
                  	<option value="ml" ${material.unit == 'ml' ? 'selected' : ''}>ml</option>
                  	<option value="L" ${material.unit == 'L' ? 'selected' : ''}>L</option>
                  	<option value="개" ${material.unit == '개' ? 'selected' : ''}>개</option>
                  </select>
                </div>

                <!-- 단가 -->
                <div class="col-md-6 mb-3">
                  <label>단가 <span class="text-danger">*</span></label>
                  <input type="number" name="unitPrice" class="form-control"
       					  value="${material.unitPrice}" required min="0">
                </div>

                <!-- 보관법 -->
                <div class="col-md-6 mb-3">
                  <label>보관법</label>
                  <select name="storageMethod" id="storageMethod" class="form-control" onchange="setWarehouseCode()">
                    <option value="냉동">냉동</option>
                    <option value="냉장">냉장</option>
                    <option value="상온">상온</option>
                  </select>
                </div>

                <!-- 보관창고 -->
                <div class="col-md-6 mb-3">
                  <label>보관창고</label>
                  <select name="warehouseCode" id="warehouseCode" class="form-control">
                    <option value="WH001" ${material.warehouseCode eq 'WH001' ? 'selected' : ''}>WH001</option>
                    <option value="WH002" ${material.warehouseCode eq 'WH002' ? 'selected' : ''}>WH002</option>
                    <option value="WH003" ${material.warehouseCode eq 'WH003' ? 'selected' : ''}>WH003</option>
                  </select>
                </div>

                <!-- LOT 관리 -->
                <div class="col-md-6 mb-3">
                  <label>LOT관리 여부</label>
                  <select name="lotFlag" class="form-control">
                    <option value="Y" ${material.lotFlag eq 'Y' ? 'selected' : ''}>Y</option>
                    <option value="N" ${material.lotFlag eq 'N' ? 'selected' : ''}>N</option>
                  </select>
                </div>

                <!-- 입고단위 -->
                <div class="col-md-12 mb-3">
                  <label>입고단위</label>
                  <input type="text" name="supplyUnit" value="${material.supplyUnit}" class="form-control" placeholder="예: 20kg 박스">
                </div>

              </div>

              <!-- 버튼 영역 -->
              <div class="text-right">
                <button type="submit" class="btn btn-primary">등록</button>
                <a href="/material/list" class="btn btn-secondary">취소</a>
              </div>

            </div>
          </div>
        </form>

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
function setWarehouseCode() {
  const storageMethod = document.getElementById('storageMethod').value;
  const warehouseCode = document.getElementById('warehouseCode');

  if (storageMethod === '냉동') {
    warehouseCode.value = 'WH001';
  } else if (storageMethod === '냉장') {
    warehouseCode.value = 'WH002';
  } else if (storageMethod === '상온') {
    warehouseCode.value = 'WH003';
  } else {
    warehouseCode.value = ''; // 선택 안 됨
  }
}
</script>