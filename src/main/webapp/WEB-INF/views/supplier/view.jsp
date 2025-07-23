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
			  <h3 class="font-weight-bold">협력사 정보</h3>
			</div>
                  
	                
	<!--  본문 내용 시작 -->
	<div class="contentbody"> 

  		<h4>${supplierVO.supplierName} 상세정보</h4>

		<!-- 탭 버튼 -->
		<ul class="nav nav-tabs" id="myTab" role="tablist">
		  <li class="nav-item">
		    <a class="nav-link active" id="basic-tab" data-toggle="tab" href="#tab-basic" role="tab">기본정보</a>
		  </li>
		  <li class="nav-item">
		    <a class="nav-link" id="settlement-tab" data-toggle="tab" href="#tab-settlement" role="tab">정산정보</a>
		  </li>
		  <li class="nav-item">
		    <a class="nav-link" id="items-tab" data-toggle="tab" href="#tab-items" role="tab">공급 품목 관리</a>
		  </li>
		</ul>

		<!-- 탭 콘텐츠 -->
		<div class="tab-content" id="myTabContent">
		  
		  <!-- 기본정보 탭 -->
		  <div class="tab-pane fade show active" id="tab-basic" role="tabpanel">
		    <p><span class="info-label">거래처명:</span> <span class="info-value">${supplierVO.supplierName}</span></p>
		    <p><span class="info-label">사업자등록번호:</span> <span class="info-value">${supplierVO.businessNumber}</span></p>
		    <p><span class="info-label">대표자명:</span> <span class="info-value">${supplierVO.representativeName}</span></p>
		    <p><span class="info-label">주소:</span> <span class="info-value">${supplierVO.address} (${supplierVO.zipcode})</span></p>
		    <p><span class="info-label">전화번호:</span> <span class="info-value">${supplierVO.phone}</span></p>
		  </div>
		
		  <!-- 정산정보 탭 -->
		  <div class="tab-pane fade" id="tab-settlement" role="tabpanel">
		    <p><span class="info-label">정산방식:</span> <span class="info-value">${supplierVO.settlementMethod}</span></p>
		    <p><span class="info-label">은행명:</span> <span class="info-value">${supplierVO.bankName}</span></p>
		    <p><span class="info-label">계좌번호:</span> <span class="info-value">${supplierVO.accountNumber}</span></p>
		    <p><span class="info-label">예금주:</span> <span class="info-value">${supplierVO.accountHolder}</span></p>
		  </div>
		
		  <!-- 공급 품목 관리 탭 -->
		  <div class="tab-pane fade" id="tab-items" role="tabpanel">
		    <p>공급 품목 탭입니다. (추후 구현)</p>
		  </div>
		
		</div>
	</div>
	<!--  본문내용 끝 -->

            </div> <!-- class="row" -->
            
          
        </div>
        <!-- content-wrapper 끝 -->
	  <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
     </div>
     <!-- 본문.jsp main-panel ends -->
  </div>   
  <!-- container-fluid page-body-wrapper 끝 -->
</div>
<!-- container-scroller 끝-->   