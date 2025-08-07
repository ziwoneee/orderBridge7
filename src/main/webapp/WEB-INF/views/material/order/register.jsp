<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>
  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

    <!-- 본문 시작 -->
    <div class="main-panel">
      <div class="content-wrapper">
        <h3 class="font-weight-bold mb-4">자재 발주 등록</h3>
        
        <c:if test="${not empty error}">
		  <div class="alert alert-danger mb-3">
		    ${error}
		  </div>
		</c:if>
        
        <form action="/material/order/register" method="post">

          <!-- 기본 정보 섹션 - orderDate 제거 -->
			<div class="card-section">
			  <h5 class="section-title">기본 정보</h5>
			  <div class="row">
			    <div class="col-md-4 mb-3">
			      <label>납기요청일 <span class="text-danger">*</span></label>
			      <input type="date" name="order.expectedArrivedDate" class="form-control" required>
			    </div>
			    
			    <div class="col-md-4 mb-3">
				  <label>거래처 <span class="text-danger">*</span></label>
				  <div class="input-group">
				    <select id="supplierSelect" name="order.supplierId" class="form-control" required>
				      <option value="">선택하세요</option>
				      <c:forEach var="supplier" items="${supplierList}">
				        <option value="${supplier.supplierId}">${supplier.supplierName}</option>
				      </c:forEach>
				    </select>
				    <div class="input-group-append">
				      <button type="button" id="btnSearchSupplier" class="btn btn-outline-secondary" title="자재로 거래처 찾기">
				        🔍
				      </button>
				    </div>
				  </div>
				</div>

			    <div class="col-md-4 mb-3">
			      <label>담당자 <span class="text-danger">*</span></label>
			      <input type="text" name="order.createdBy" class="form-control" placeholder="예: 홍길동" required>
			    </div>
			    <div class="col-md-12">
			      <label>비고</label>
			      <textarea name="order.note" class="form-control" rows="2" placeholder="발주 관련 특이사항을 입력하세요"></textarea>
			    </div>
			  </div>
			</div>

          <!-- 항목 정보 -->
          <div class="card-section">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <h5 class="section-title">발주 항목</h5>
              <button type="button" class="btn btn-sm btn btn-info" onclick="addItemRow()">+ 항목 추가</button>
            </div>
            
            <input type="hidden" name="order.orderDate" id="orderDate">
            <!-- 발주 상태 hidden으로 추가 -->
			<input type="hidden" name="order.orderStatus" value="요청">

            <table class="table table-bordered text-center" id="itemTable">
              <thead>
                <tr>
                  <th>자재명</th>
                  <th>수량</th>
                  <th>단가</th>
                  <th>총금액</th>
                  <th>입고창고</th>
                  <th>삭제</th>
                </tr>
              </thead>
              <!-- 항목 테이블의 첫 번째 행도 수정 -->
				<tbody>
				  <tr>
				    <td>
				      <select name="orderItems[0].materialId" class="form-control" required>
				        <option value="">거래처를 먼저 선택하세요</option>
				      </select>
				      
				    </td>
				    <td><input type="number" name="orderItems[0].orderQuantity" class="form-control" min="1" onchange="calculateTotal(this)" required></td>
				    <td><input type="number" name="orderItems[0].unitPrice" class="form-control" min="0" step="0.01" onchange="calculateTotal(this)" required></td>
				    <td>
				      <input type="number" class="form-control" value="0" readonly>
				      <input type="hidden" name="orderItems[0].totalPrice" value="0">
				    </td>
				    <td><input type="text" name="orderItems[0].warehouseCode" class="form-control" readonly></td>
				    <td><button type="button" class="btn btn-sm btn-danger" onclick="removeRow(this)">삭제</button></td>
				  </tr>
				</tbody>
            </table>
          </div>

          <!-- 버튼 -->
          <div class="text-right">
            <button type="submit" class="btn btn-primary">등록</button>
            <a href="/material/order/list" class="btn btn-outline-secondary">목록</a>
          </div>

        </form>
        
		<!-- 검색 모달 -->
		<div class="modal fade" id="supplierSearchModal" tabindex="-1" role="dialog" aria-labelledby="supplierSearchModalLabel" aria-hidden="true">
		  <div class="modal-dialog modal-lg" role="document">
		    <div class="modal-content">
		
		      <div class="modal-header">
		        <h5 class="modal-title" id="supplierSearchModalLabel">자재 기준 거래처 검색</h5>
		        <button type="button" class="close" data-dismiss="modal" aria-label="닫기">
		          <span aria-hidden="true">&times;</span>
		        </button>
		      </div>
		
		      <div class="modal-body">
		        <div class="form-inline mb-2">
		          <input type="text" id="materialSearchInput" class="form-control mr-2" placeholder="자재명 입력 (2글자 이상)">
		        </div>
		        <div class="table-responsive">
		          <table class="table table-bordered table-hover">
		            <thead class="thead-light">
		              <tr>
		                <th>자재명</th>
		                <th>공급 거래처</th>
		                <th>단가</th>
		                <th>입고창고</th>
		                <th>선택</th>
		              </tr>
		            </thead>
		            <tbody id="supplierSearchResult">
		              <tr>
		                <td colspan="5" class="text-center">검색어를 입력하세요.</td>
		              </tr>
		            </tbody>
		          </table>
		        </div>
		      </div>
		
		    </div>
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

<script src="${pageContext.request.contextPath}/resources/js/materialOrder.js"></script>