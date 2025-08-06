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
			  <h3 class="font-weight-bold">고객사 상세 정보</h3>
			</div>
        
          <div class="col-md-12 grid-margin">
            <div class="row">
              <div class="contentbody"> 
                <!-- 본문내용 시작 -->
                <div class="row">
                  <!-- ✅ 기본 정보 -->
                  <div class="col-md-6 me-3"> 
                    <div class="card mb-4" style="width:600px; height:450px;">
                      <div class="card-header bg-primary text-white font-weight-bold">기본 정보</div>
                      <div class="card-body">
                        <table class="table table-bordered">
                          <tr><th class="bg-light w-25">고객사명</th><td>${client.clientName}</td></tr>
                          <tr><th class="bg-light">사업자등록번호</th><td>${client.businessNumber}</td></tr>
                          <tr><th class="bg-light">대표자명</th><td>${client.ceoName}</td></tr>
                          <tr><th class="bg-light">연락처</th><td>${client.clientTel}</td></tr>
                          <tr><th class="bg-light">업태</th><td>${client.businessType}</td></tr>
                          <tr><th class="bg-light">종목</th><td>${client.businessProduct}</td></tr>
                          <tr><th class="bg-light">고객 유형1</th><td>${client.clientType1}</td></tr>
							<tr><th class="bg-light">고객 유형2</th><td>${client.clientType2}</td></tr>
                                              
                          <tr><th class="bg-light">등록상태</th>
                            <td>
                              <span class="badge badge-${client.statusCode == 1 ? 'success' : 'secondary'}">
                                ${client.statusCode == 1 ? '활성' : '비활성'}
                              </span>
                            </td>
                          </tr>
                        </table>
                      </div>
                    </div>
                  </div>
                  <!-- ✅ 주소 & 담당자 정보 -->
                  <div class="card mb-4 ml-3" style="width:600px; height:450px;">
                    <div class="card-header bg-info text-white font-weight-bold">주소 & 담당자 정보</div>
                    <div class="card-body">
                      <table class="table table-bordered">
                        <tr><th class="bg-light w-25">우편번호</th><td>${client.postCode}</td></tr>
                        <tr><th class="bg-light">주소</th><td>${client.address}</td></tr>
                        <tr><th class="bg-light">상세주소</th><td>${client.addressDetail}</td></tr>
                        <tr><th class="bg-light">담당자명</th><td>${client.managerName}</td></tr>
                        <tr><th class="bg-light">부서</th><td>${client.managerDept}</td></tr>
                        <tr><th class="bg-light">휴대전화</th><td>${client.managerTel}</td></tr>
                        <tr><th class="bg-light">팩스번호</th><td>${client.faxNumber}</td></tr>
                        <tr><th class="bg-light">이메일</th><td>${client.managerEmail}</td></tr>
                      </table>
                    </div>
                  </div>
                   </div>
                  <!-- ✅ 버튼 영역 -->
                  <div class="text-right">
                    <a href="/client/list" class="btn btn-outline-secondary">목록으로</a>
                   <a href="/client/edit?clientId=${client.clientId}" class="btn btn-warning">수정</a>
<%--                     <a href="/client/delete?clientId=${client.clientId}" class="btn btn-danger" --%>
<!--                        onclick="return confirm('정말 삭제하시겠습니까?');">삭제</a> -->
                 
                </div>
              </div>
              <!-- 페이징하실거면 여기서 시작 -->
              <!--  본문내용 끝 -->    
            </div>
            <!-- 페이징 끝 -->
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