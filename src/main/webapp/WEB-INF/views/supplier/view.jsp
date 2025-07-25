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
			  <h3 class="font-weight-bold">${supplierVO.supplierName} 상세 정보</h3>
			</div>
                  
	                
	<!--  본문 내용 시작 -->
	 <!-- 상세 정보 카드 -->
          <div class="row w-100">
            <!-- 좌측: 기본 정보 -->
            <div class="col-md-6">
              <div class="card">
                <div class="card-header bg-primary text-white">기본 정보</div>
                <div class="card-body">
                  <table class="table">
                    <tr><th>거래처명</th><td>${supplierVO.supplierName}</td></tr>
                    <tr><th>사업자등록번호</th><td>${supplierVO.businessNumber}</td></tr>
                    <tr><th>대표자명</th><td>${supplierVO.representativeName}</td></tr>
                    <tr><th>전화번호</th><td>${supplierVO.phone}</td></tr>
                    <tr><th>업태 / 종목</th><td>${supplierVO.supplierType}</td></tr>
                    <tr><th>등록상태</th>
                      <td>
			            <c:choose>
						  <c:when test="${supplierVO.status eq '활성'}">
						    <span class="badge bg-success">활성</span>
						  </c:when>
						  <c:otherwise>
						    <span class="badge bg-secondary">비활성</span>
						  </c:otherwise>
						</c:choose>
			          </td>
                    </tr>
                  </table>
                </div>
              </div>
            </div>

            <!-- 우측: 주소 + 정산정보 -->
            <div class="col-md-6">
              <div class="card mb-4">
                <div class="card-header bg-info text-white">주소 및 담당자 정보</div>
                <div class="card-body">
                  <table class="table">
                    <tr><th>우편번호</th><td>${supplierVO.zipcode}</td></tr>
                    <tr><th>주소</th><td>${supplierVO.address}</td></tr>
                    <tr><th>상세주소</th><td>${supplierVO.addressDetail}</td></tr>
                    <tr><th>담당자명</th><td>${supplierVO.contactName}</td></tr>
                    <tr><th>담당자 연락처</th><td>${supplierVO.contactPhone}</td></tr>
                    <tr><th>담당자 이메일</th><td>${supplierVO.contactEmail}</td></tr>
                  </table>
                </div>
              </div>

              <div class="card">
                <div class="card-header bg-warning text-white">정산 정보</div>
                <div class="card-body">
                  <table class="table">
                    <tr><th>정산방식</th><td>${supplierVO.settlementMethod}</td></tr>
                    <tr><th>은행명</th><td>${supplierVO.bankName}</td></tr>
                    <tr><th>계좌번호</th><td>${supplierVO.accountNumber}</td></tr>
                    <tr><th>예금주</th><td>${supplierVO.accountHolder}</td></tr>
                  </table>
                </div>
              </div>
            </div>
          </div>

          <!-- 버튼 -->
          <div class="col-12 mt-4 text-center">
            <a href="list" class="btn btn-outline-secondary">목록</a>
            <a href="edit?supplierId=${supplierVO.supplierId}" class="btn btn-primary text-white">수정</a>
            <button class="btn btn-danger">삭제</button>
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