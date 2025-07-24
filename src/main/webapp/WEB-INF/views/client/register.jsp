<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script src="https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<style>
  h5.section-title {
    border-left: 5px solid #003366;
    padding-left: 10px;
    font-weight: 600;
    color: #003366;
    margin-bottom: 1rem;
  }

  label {
    font-weight: 500;
  }

  .card-section {
    border: 1px solid #dee2e6;
    padding: 2rem;
    border-radius: 0.5rem;
    background-color: #ffffff;
    margin-bottom: 1.5rem;
  }

  .form-control:focus {
    border-color: #003366;
    box-shadow: 0 0 0 0.1rem rgba(0, 51, 102, 0.25);
  }

  .form-label.required::after {
    content: " *";
    color: red;
    margin-left: 2px;
  }
</style>

<div class="container-scroller">

  <%@ include file="/WEB-INF/views/main/top.jsp" %>
  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

    <div class="main-panel">
      <div class="content-wrapper">

        <div class="col-12 mb-4">
          <h3 class="font-weight-bold">고객사 신규 등록</h3>
        </div>

        <!-- ✅ 에러/성공 메시지 -->
        <c:if test="${not empty error}">
          <div class="alert alert-danger">${error}</div>
        </c:if>
        <c:if test="${not empty msg}">
          <div class="alert alert-success">${msg}</div>
        </c:if>

        <form action="/client/register" method="post" onsubmit="return validateForm();">

          <!-- 기본 정보 -->
          <div class="card-section">
            <h5 class="section-title">기본 정보</h5>
            <div class="row">
              <div class="col-md-6 mb-3">
                <label class="form-label required">고객사명</label>
                <input type="text" name="clientName" class="form-control" required value="${client.clientName}">
              </div>
              <div class="col-md-6 mb-3">
                <label class="form-label required">사업자등록번호</label>
                <input type="text" name="businessNumber" class="form-control" required placeholder="ex) 123-45-67890">
              </div>
              <div class="col-md-6 mb-3">
                <label>대표자명</label>
                <input type="text" name="ceoName" class="form-control" value="${client.ceoName}">
              </div>
              <div class="col-md-6 mb-3">
                <label>연락처</label>
                <input type="text" name="clientTel" class="form-control" value="${client.clientTel}">
              </div>
              <div class="col-md-6 mb-3">
                <label>업태</label>
                <input type="text" name="businessType" class="form-control" value="${client.businessType}">
              </div>
              <div class="col-md-6 mb-3">
                <label>종목</label>
                <input type="text" name="businessProduct" class="form-control" value="${client.businessProduct}">
              </div>
            </div>
          </div>

          <!-- 주소 및 담당자 정보 -->
          <div class="card-section">
            <h5 class="section-title">주소 및 담당자 정보</h5>
            <div class="row">
              <div class="col-md-3 mb-3">
                <label>우편번호</label>
                <input type="text" id="postcode" name="postCode" class="form-control" readonly value="${client.postCode}">
              </div>
              <div class="col-md-2 mb-3 d-flex align-items-end">
                <button type="button" class="btn btn-outline-primary w-100" onclick="execDaumPostcode()">주소검색</button>
              </div>
              <div class="col-md-4 mb-3">
                <label>주소</label>
                <input type="text" id="address" name="address" class="form-control" readonly value="${client.address}">
              </div>
              <div class="col-md-3 mb-3">
                <label>상세주소</label>
                <input type="text" id="addressDetail" name="addressDetail" class="form-control" value="${client.addressDetail}">
              </div>
              <div class="col-md-4 mb-3">
                <label class="form-label required">담당자명</label>
                <input type="text" name="managerName" class="form-control" required value="${client.managerName}">
              </div>
              <div class="col-md-4 mb-3">
                <label>부서</label>
                <input type="text" name="managerDept" class="form-control" value="${client.managerDept}">
              </div>
              <div class="col-md-4 mb-3">
                <label>휴대전화</label>
                <input type="text" name="managerTel" class="form-control" value="${client.managerTel}">
              </div>
              <div class="col-md-6 mb-3">
                <label>팩스</label>
                <input type="text" name="faxNumber" class="form-control" value="${client.faxNumber}">
              </div>
              <div class="col-md-6 mb-3">
                <label>이메일</label>
                <input type="email" name="managerEmail" class="form-control" value="${client.managerEmail}">
              </div>
            </div>
          </div>

          <!-- 상태 및 버튼 -->
          <div class="form-group">
            <label>등록 상태</label>
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

      </div>
      <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
    </div>
  </div>
</div>

<script>
  function execDaumPostcode() {
    new daum.Postcode({
      oncomplete: function(data) {
        document.getElementById("postcode").value = data.zonecode;
        document.getElementById("address").value = data.roadAddress || data.jibunAddress;
        document.getElementById("addressDetail").focus();
      }
    }).open();
  }

  function validateForm() {
    const bizNum = document.querySelector("input[name='businessNumber']").value;
    const regex = /^\d{3}-\d{2}-\d{5}$/;
    if (!regex.test(bizNum)) {
      alert("사업자등록번호는 반드시 '123-45-67890' 형식이어야 합니다.");
      return false;
    }
    return true;
  }
</script>
