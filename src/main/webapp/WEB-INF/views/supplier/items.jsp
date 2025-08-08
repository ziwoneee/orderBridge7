<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<%
  String sid = (request.getAttribute("supplierId") != null) 
               ? (String)request.getAttribute("supplierId") 
               : "";
%>


<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>      
  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

    <!-- 본문 시작 -->
    <div class="main-panel">
      <div class="content-wrapper">
        <div class="row">
          <div class="col-12 mb-4">
            <h3 class="font-weight-bold">${supplier.supplierName} 공급 품목 목록</h3>
            <p class="text-muted">${supplier.supplierName} 협력사의 공급 가능한 자재 목록을 확인하세요.</p>
          </div>
          
          <!-- 등록 버튼 -->
          <div class="col-12 mb-3 text-right">
            <button class="btn btn-success mb-2" id="btnAddItem">공급 품목 등록</button>
          </div>
                    
          <!-- 공급 품목 테이블 -->
          <div class="col-12">
                <div class="table-responsive">
                  <table class="table table-hover" id="itemTable">
                    <thead style="background-color: #1c355e; color: white;">
                      <tr>
                        <th>자재명</th>
                        <th>유형</th>
                        <th>단가</th>
                        <th>단위</th>
                        <th>공급 가능</th>
                        <th>비고</th>
                        <th>작업</th>
                      </tr>
                    </thead>
                    <tbody>
                      <!-- JavaScript로 동적 로딩 -->
                    </tbody>
                  </table>
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

<!-- 모달 -->
<div class="modal fade" id="itemModal" tabindex="-1" role="dialog">
  <div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
      <form id="itemForm">
        <div class="modal-header">
          <h5 class="modal-title">공급 품목 등록</h5>
          <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
        </div>
        <div class="modal-body">
          <input type="hidden" name="supplierId" value="<%= sid %>">
          <div class="form-group">
            <label>자재 ID</label>
            <input type="text" name="materialId" class="form-control">
          </div>
          <div class="form-group">
            <label>단가</label>
            <input type="number" name="unitPrice" class="form-control">
          </div>
          <div class="form-group">
            <label>단위</label>
            <input type="text" name="unit" class="form-control">
          </div>
          <div class="form-group">
            <label>공급 가능</label>
            <select name="supplyAvailable" class="form-control">
              <option value="Y">Y</option>
              <option value="N">N</option>
            </select>
          </div>
          <div class="form-group">
            <label>비고</label>
            <textarea name="note" class="form-control"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">닫기</button>
          <button type="submit" class="btn btn-primary">저장</button>
        </div>
      </form>
    </div>
  </div>
</div>

<script src="${pageContext.request.contextPath}/resources/js/supplierItem.js"></script>