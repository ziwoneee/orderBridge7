<%@ page contentType="text/html; charset=UTF-8" %>
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
      <div class="row">
      
 <!-- 제목 -->
          <div class="col-12 mb-4">
            <h3 class="font-weight-bold">고객사 정보 수정</h3>
          </div>

          <form action="/client/update" method="post" >
            <input type="hidden" name="clientId" value="${client.clientId}"/>
           
           
           
              <!-- 기본 정보 -->
              <div class="row">
               <div class="row">
                <div class="col-12">
                <div class="card" >
                  <div class="card-header bg-primary text-white font-weight-bold">기본 정보</div>
                  <div class="card-body">
                    <div class="form-group">
						  <label>고객사명</label>
						  <input type="text" name="clientName" class="form-control" value="${client.clientName}" readonly />
						</div>
						
						<div class="form-group">
						  <label>사업자등록번호</label>
						  <input type="text" name="businessNumber" class="form-control" value="${client.businessNumber}" placeholder="000-00-00000" readonly />
						</div>

                    <div class="form-group">
                      <label>대표자명</label>
                      <input type="text" name="ceoName" class="form-control" required value="${client.ceoName}" />
                    </div>
                    <div class="form-group">
                      <label>연락처</label>
                      <input type="text" name="clientTel" class="form-control" required value="${client.clientTel}" placeholder="010-0000-0000" oninput="formatPhoneNumber(this)" />
                    </div>
                    <div class="form-group">
                      <label>업태</label>
                      <input type="text" name="businessType" class="form-control" value="${client.businessType}" />
                    </div>
                    <div class="form-group">
                      <label>종목</label>
                      <input type="text" name="businessProduct" class="form-control" value="${client.businessProduct}" />
                    </div>
                    
                    <div class="form-group">
  <label>고객 유형1</label>
  <input type="text" name="clientType1" class="form-control" value="${client.clientType1}" />
</div>
<div class="form-group">
  <label>고객 유형2</label>
  <input type="text" name="clientType2" class="form-control" value="${client.clientType2}" />
</div>
                    
                  </div>
                </div>
              </div>

              <!-- 주소 및 담당자 -->
            <div class="row">
                <div class="col-12">
                 <div class="card " >
                  <div class="card-header bg-info text-white font-weight-bold">주소 & 담당자 정보</div>
                  <div class="card-body">
                    <div class="form-group">
                      <label>우편번호</label>
                      <div class="input-group">
                        <input type="text" name="postCode" class="form-control" value="${client.postCode}" readonly id="postcode" />
                        <div class="input-group-append">
                          <button type="button" class="btn btn-secondary" onclick="execDaumPostcode()">검색</button>
                        </div>
                      </div>
                    </div>
                    <div class="form-group">
                      <label>주소</label>
                      <input type="text" name="address" class="form-control" value="${client.address}" readonly id="address" />
                    </div>
                    <div class="form-group">
                      <label>상세주소</label>
                      <input type="text" name="addressDetail" class="form-control" value="${client.addressDetail}" />
                    </div>
                    <div class="form-group">
                      <label>담당자명</label>
                      <input type="text" name="managerName" class="form-control" value="${client.managerName}" />
                    </div>
                    <div class="form-group">
                      <label>부서</label>
                      <input type="text" name="managerDept" class="form-control" value="${client.managerDept}" />
                    </div>
                    <div class="form-group">
                      <label>휴대전화</label>
                      <input type="text" name="managerTel" class="form-control" value="${client.managerTel}" placeholder="010-0000-0000" oninput="formatPhoneNumber(this)" />
                    </div>
                    <div class="form-group">
                      <label>팩스번호</label>
                      <input type="text" name="faxNumber" class="form-control" value="${client.faxNumber}" />
                    </div>
                    <div class="form-group">
                      <label>이메일</label>
                      <input type="email" name="managerEmail" class="form-control" value="${client.managerEmail}" />
                    </div>
                    
                    <div class="form-group">
					  <label>등록상태</label>
					  <select name="statusCode" class="form-control" required>
					    <option value="1" ${client.statusCode == 1 ? 'selected' : ''}>활성</option>
					    <option value="0" ${client.statusCode == 0 ? 'selected' : ''}>비활성</option>
					  </select>
					</div>
					                    
                  </div>
                </div>
              </div>
              </div>
           
            <!-- 버튼 영역 -->
            <div class="text-right mb-4">
              <button type="submit" class="btn btn-primary">저장</button>
              <a href="/client/detail?clientId=${client.clientId}" class="btn btn-secondary">취소</a>
            </div>
          </form>
        </div>
      </div>
       </div>
       
      <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
    </div>
  </div>
</div>

<!-- 하이픈 자동 입력 스크립트 -->
<script>
function formatPhoneNumber(input) {
  input.value = input.value
    .replace(/[^0-9]/g, '')
    .replace(/(\d{3})(\d{3,4})(\d{4})/, '$1-$2-$3')
    .substr(0, 13);
}
function formatBizNumber(input) {
  input.value = input.value
    .replace(/[^0-9]/g, '')
    .replace(/(\d{3})(\d{2})(\d{5})/, '$1-$2-$3')
    .substr(0, 12);
}

// 다음 주소 검색
function execDaumPostcode() {
  new daum.Postcode({
    oncomplete: function(data) {
      document.getElementById('postcode').value = data.zonecode;
      document.getElementById('address').value = data.roadAddress;
    }
  }).open();
}
</script>
