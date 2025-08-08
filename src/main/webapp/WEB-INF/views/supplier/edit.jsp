<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script src="https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">

  <%@ include file="/WEB-INF/views/main/top.jsp" %>      

  <div class="container-fluid page-body-wrapper">

    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

      <!-- 본문 시작 -->
      <div class="main-panel">
        <div class="content-wrapper">
        
        	<!-- 제목 -->
			<div class="col-12 mb-4">
			  <h3 class="font-weight-bold">협력사 정보 수정</h3>
			</div>
    
      <form action="/supplier/edit" method="post">
      	<input type="hidden" name="supplierId" value="${supplierVO.supplierId}">
        
        <!-- 기본 정보 -->
        <div class="card-section">
          <h5 class="section-title">기본 정보</h5>
          <div class="row">
            <div class="col-md-6 mb-3">
              <label class="form-label required">거래처명</label>
              <input type="text" class="form-control" name="supplierName" required value="${supplierVO.supplierName}" readonly>
            </div>
            <div class="col-md-6 mb-3">
              <label class="form-label required">사업자등록번호</label>
                <div class="input-group">
			    	<input type="text" class="form-control" name="businessNumber" id="businessNumber" readonly
			    		   value="${supplierVO.businessNumber}">
			    	<div class="input-group-append">
			      		<button type="button" class="btn btn-outline-primary" id="checkBizBtn">중복확인</button>
			   		 </div>
			  	</div>
			  <small class="form-text text-muted text-danger">* 사업자등록번호는 수정할 수 없습니다.</small>
            </div>
            <div class="col-md-6 mb-3">
              <label>협력사 분류</label>
              <select name="supplierType" class="form-control">
                <option value="">선택</option>
                <option value="원자재" ${supplierVO.supplierType == '원자재' ? 'selected' : ''}>원자재</option>
                <option value="포장재" ${supplierVO.supplierType == '포장재' ? 'selected' : ''}>포장재</option>
                <option value="육류" ${supplierVO.supplierType == '육류' ? 'selected' : ''}>육류</option>
              </select>
            </div>
            <div class="col-md-6 mb-3">
              <label class="form-label required">대표자명</label>
              <input type="text" class="form-control" name="representativeName" required value="${supplierVO.representativeName}">
            </div>
            <div class="col-md-6 mb-3">
              <label>전화번호</label>
              <input type="text" class="form-control" name="phone" placeholder="ex) 010-1234-5678" value="${supplierVO.phone}">
            </div>
            <div class="col-md-6 mb-3">
              <label>이메일</label>
              <input type="email" class="form-control" name="email" value="${supplierVO.email}">
            </div>
          </div>
        </div>

        <!-- 주소 정보 -->
        <div class="card p-4 mb-4">
          <h5 class="section-title">주소 정보</h5>
          <div class="row"> 
  		  	 <div class="col-md-3 mb-2">
		    	<label>우편번호</label>
		   		<input type="text" class="form-control" id="postcode" name="zipcode" readonly value="${supplierVO.zipcode}">
		     </div>
		  	 <div class="col-md-2 mb-2 d-flex align-items-end">
		   	    <button type="button" id="findAddressBtn" class="btn btn-outline-primary w-100">주소검색</button>
		     </div>
		 	 <div class="col-md-4 mb-2">
		  	 	<label>주소</label>
		   	 	<input type="text" class="form-control" id="address" name="address" readonly value="${supplierVO.address}">
			 </div>
			 <div class="col-md-3 mb-2">
			 	<label>상세주소</label>
			    <input type="text" class="form-control" name="addressDetail" value="${supplierVO.addressDetail}">
			 </div>
		  </div>
        </div>

        <!-- 정산 정보 -->
        <div class="card-section">
          <h5 class="section-title">정산 정보</h5>
          <div class="row">
            <div class="col-md-6 mb-3">
			  <label>정산방식</label>
			  <select class="form-control" name="settlementMethod">
			    <option value="">선택</option>
			    <option value="계좌이체" ${supplierVO.settlementMethod == '계좌이체' ? 'selected' : ''}>계좌이체</option>
			    <option value="현금" ${supplierVO.settlementMethod == '현금' ? 'selected' : ''}>현금</option>
			    <option value="외상" ${supplierVO.settlementMethod == '외상' ? 'selected' : ''}>외상</option>
			    <option value="선지급" ${supplierVO.settlementMethod == '선지급' ? 'selected' : ''}>선지급</option>
			    <option value="기타" ${supplierVO.settlementMethod == '기타' ? 'selected' : ''}>기타</option>
			  </select>
			</div>
            <div class="col-md-6 mb-3">
              <label>예금주</label>
              <input type="text" class="form-control" name="accountHolder" value="${supplierVO.accountHolder}">
            </div>
            <div class="col-md-6 mb-3">
              <label>은행명</label>
              <input type="text" class="form-control" name="bankName" value="${supplierVO.bankName}">
            </div>
            <div class="col-md-6 mb-3">
              <label>계좌번호</label>
              <input type="text" class="form-control" name="accountNumber" value="${supplierVO.accountNumber}">
            </div>
          </div>
        </div>

        <!-- 담당자 정보 -->
        <div class="card-section">
          <h5 class="section-title">담당자 정보</h5>
          <div class="row">
            <div class="col-md-4 mb-3">
              <label class="form-label required">담당자 이름</label>
              <input type="text" class="form-control" name="contactName" required value="${supplierVO.contactName}">
            </div>
            <div class="col-md-4 mb-3">
              <label class="form-label required">담당자 연락처</label>
              <input type="text" class="form-control" name="contactPhone" required value="${supplierVO.contactPhone}">
            </div>
            <div class="col-md-4 mb-3">
              <label>담당자 이메일</label>
              <input type="email" class="form-control" name="contactEmail" value="${supplierVO.contactEmail}">
            </div>
            <div class="col-md-12 mb-3">
              <label>비고</label>
              <textarea class="form-control" rows="3" name="note">${supplierVO.note}</textarea>
            </div>
          </div>
        </div>
        
        <div class="col-md-4 mb-3">
           <label for="status">거래상태</label>
           <select class="form-control" name="status" id="status">
             <option value="활성" ${supplierVO.status == '활성' ? 'selected' : ''}>활성</option>
             <option value="비활성" ${supplierVO.status == '비활성' ? 'selected' : ''}>비활성</option>
           </select>
         </div>

        <!-- 버튼 -->
        <div class="text-right">
          <button type="submit" class="btn btn-primary mr-2">수정</button>
          <a href="/supplier/list" class="btn btn-secondary">목록</a>
        </div>
        
		<input type="hidden" id="errorMsg" value="${errorMsg}">
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

<script src="/resources/js/supplierRegister.js"></script>