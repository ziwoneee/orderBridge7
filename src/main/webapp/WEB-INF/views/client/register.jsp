<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">

  <%@ include file="/WEB-INF/views/main/top.jsp" %>      

  <div class="container-fluid page-body-wrapper">

    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>
      
    <!-- 본문시작 -->
    <div class="main-panel">
      <div class="content-wrapper" style="height:800px;">
      
        <!-- ✅ 에러/성공 메시지 표시 위치 -->
        <c:if test="${not empty error}">
          <div class="alert alert-danger" role="alert">
            ${error}
          </div>
        </c:if>
        <c:if test="${not empty msg}">
          <div class="alert alert-success" role="alert">
            ${msg}
          </div>
        </c:if>
        <div class="row">
        
       		<!-- 제목 -->
			<div class="col-12 mb-4">
			  <h3 class="font-weight-bold">신규 고객사 등록</h3>
			</div>
              
              <div class="contentbody"> 
                <!--  본문 내용 시작 -->
                <!-- 등록 폼 -->
                <form action="/client/register" method="post" style="width:90%; height:800px;" onsubmit="return validateForm();">
                  <div class="row">

                    <!-- 기본 정보 -->
                    <div class="col-md-6 mb-4">
                      <h5 class="text-primary font-weight-bold mb-3">[기본 정보]</h5>
                      <div class="form-row">
                        <div class="form-group col-md-6">
                          <label>고객사명 <span class="text-danger">*</span></label>
                          <input type="text" name="clientName" class="form-control" required value="${client.clientName}">
                        </div>
                        <div class="form-group col-md-6">
                          <label>사업자등록번호 <span class="text-danger">*</span></label>
                          <input type="text" name="businessNumber" class="form-control" required>
                        </div>
                      </div>
                      <div class="form-row">
                        <div class="form-group col-md-6">
                          <label>대표자명</label>
                          <input type="text" name="ceoName" class="form-control" value="${client.ceoName}">
                        </div>
                        <div class="form-group col-md-6">
                          <label>연락처</label>
                          <input type="text" name="clientTel" class="form-control" value="${client.clientTel}">
                        </div>
                      </div>
                      <div class="form-row">
                        <div class="form-group col-md-6">
                          <label>업태</label>
                          <input type="text" name="businessType" class="form-control" value="${client.businessType}">
                        </div>
                        <div class="form-group col-md-6">
                          <label>종목</label>
                          <input type="text" name="businessProduct" class="form-control" value="${client.businessProduct}">
                        </div>
                      </div>
                    </div>

                    <!-- 주소 & 담당자 -->
                    <div class="col-md-6 mb-4">
                      <h5 class="text-primary font-weight-bold mb-3">[주소 & 담당자]</h5>
                      <!-- 우편번호 검색 -->
                      <div class="form-group">
                        <label>우편번호</label>
                        <div class="input-group">
                          <input type="text" id="postcode" name="postCode" class="form-control" readonly value="${client.postCode}">
                          <div class="input-group-append">
                            <button type="button" class="btn btn-secondary" onclick="execDaumPostcode()">검색</button>
                          </div>
                        </div>
                      </div>
                      <!-- 주소 -->
                      <div class="form-group">
                        <label>주소</label>
                        <input type="text" id="address" name="address" class="form-control" readonly value="${client.address}">
                      </div>
                      <!-- 상세주소 -->
                      <div class="form-group">
                        <label>상세주소</label>
                        <input type="text" id="addressDetail" name="addressDetail" class="form-control" value="${client.addressDetail}">
                      </div>
                      <div class="form-row">
                        <div class="form-group col-md-6">
                          <label>담당자명 <span class="text-danger">*</span></label>
                          <input type="text" name="managerName" class="form-control" required value="${client.managerName}">
                        </div>
                        <div class="form-group col-md-6">
                          <label>부서</label>
                          <input type="text" name="managerDept" class="form-control" value="${client.managerDept}">
                        </div>
                      </div>
                      <div class="form-row">
                        <div class="form-group col-md-6">
                          <label>휴대전화</label>
                          <input type="text" name="managerTel" class="form-control" value="${client.managerTel}">
                        </div>
                        <div class="form-group col-md-6">
                          <label>팩스</label>
                          <input type="text" name="faxNumber" class="form-control" value="${client.faxNumber}">
                        </div>
                      </div>
                      <div class="form-group">
                        <label>이메일</label>
                        <input type="email" name="managerEmail" class="form-control" value="${client.managerEmail}">
                      </div>
                    </div>               
                  </div>
                  <!-- 상태 및 버튼 -->
                  <div class="form-group">
                    <label>등록상태</label>
                    <select name="statusCode" class="form-control w-25">
                      <option value="1" selected>활성</option>
                      <option value="0">비활성</option>
                    </select>
                  </div>
                  <div class="text-right mt-4">
                    <a href="/client/list" class="btn btn-outline-secondary">목록으로</a>
                    <button type="submit" class="btn btn-primary">등록하기</button>
                  </div>
                </form>
                <!--  본문내용 끝 -->    
              </div>
              <!-- 페이징하실거면 여기서 시작 -->
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


<script>
  function execDaumPostcode() {
    new daum.Postcode({
      oncomplete: function(data) {
        // 주소 선택 시 값 설정
        document.getElementById("postcode").value = data.zonecode; // 우편번호
        document.getElementById("address").value = data.roadAddress || data.jibunAddress; // 도로명 or 지번 주소
        document.getElementById("addressDetail").focus(); // 상세주소로 커서 이동
      }
    }).open();
  }
</script>

<script>
function validateForm() {
  const bizNum = document.querySelector("input[name='businessNumber']").value;
  const regex = /^\d{3}-\d{2}-\d{5}$/;

  if (!regex.test(bizNum)) {
    alert("사업자등록번호는 반드시 '123-45-67890' 형식이어야 합니다.");
    return false; // 제출 중단
  }

  return true; // 통과 시 제출 진행
}
</script>