<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>
  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

    <div class="main-panel">
      <div class="content-wrapper">
        <h3 class="font-weight-bold mb-4">자재 출고 등록</h3>

        <!-- 선택된 작업지시서 정보 -->
        <div class="card mb-10">
		  <div class="card-body">
		    <div class="row">
		      <div class="col-sm-3">
		        <label for="workOrderIdView">작업지시번호</label>
		        <input type="text" class="form-control input-sm" id="workOrderIdView" readonly>
		      </div>
		      <div class="col-sm-3">
		        <label for="productId">제품ID</label>
		        <input type="text" class="form-control input-sm" id="productId" readonly>
		      </div>
		      <div class="col-sm-3">
		        <label for="lineId">라인ID</label>
		        <input type="text" class="form-control input-sm" id="lineId" readonly>
		      </div>
		      <div class="col-sm-3">
		        <label for="dueDateView">납기일</label>
		        <input type="text" class="form-control input-sm" id="dueDateView" readonly>
		      </div>
		    </div>
		    <div class="row" style="margin-top:8px;">
		      <div class="col-sm-3">
		        <label for="handledBy">담당자</label>
		        <input type="text" class="form-control input-sm" name="handledBy" id="handledBy" value="admin">
		      </div>
		      <div class="col-sm-9 text-right" style="margin-top:24px;">
		        <a href="/material/outbound/list" class="btn btn-default btn-sm">목록</a>
		      </div>
		    </div>
		  </div>
		</div>


        <!-- 출고 항목 (추후 작업지시서 기반 자재 불러오기 구현 예정) -->
		<form id="outboundForm" method="post" action="/material/outbound/register">
		  <%-- CSRF 쓰면 hidden 추가 --%>
		  <input type="hidden" name="workOrderId" id="workOrderIdHidden">
		  <input type="hidden" name="dueDate"     id="dueDateHidden">
		  <input type="hidden" name="productId"   id="productIdHidden">
		  <input type="hidden" name="lineId"      id="lineIdHidden">
		  
		  <div class="card">
		    <div class="card-header bg-light"><b>자재별 필요수량 & LOT 선택 (FEFO)</b></div>
		    <div class="card-body" style="padding:0;">
		      <table class="table table-bordered table-condensed" style="margin:0;">
		        <thead>
		          <tr>
		            <th style="width:200px;">자재</th>
		            <th class="text-center">필요수량</th>
		            <th>LOT 선택 (유통기한 빠른 순)</th>
		            <th class="text-center">예약수량(이번 WO)</th>
		            <th class="text-center">예상예약</th> 
		            <th class="text-center">선택합계</th>
		            <th class="text-center">부족</th>
		          </tr>
		        </thead>
		        <tbody id="materialLotBody"><!-- JS 로드 --></tbody>
		      </table>
		    </div>
		  </div>
		
		  <div class="text-right" style="margin-top:10px;">
		    <button type="button" id="btnCreateDraft" class="btn btn-warning btn-sm">부족분 발주</button>
		    <button type="submit" id="btnSubmit" class="btn btn-primary btn-sm" disabled>등록</button>
		    <a href="/material/outbound/list" class="btn btn-default btn-sm">취소</a>
		  </div>
		</form>


      </div>
      <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
    </div>
  </div>
</div>

<!-- 부족분 발주 미리보기 모달 -->
<div class="modal fade" id="draftModal" tabindex="-1" role="dialog">
  <div class="modal-dialog modal-xl" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">부족분 발주 초안</h5>
        <button type="button" class="close" data-dismiss="modal">&times;</button>
      </div>
      <div class="modal-body" id="draftBody">
        <!-- renderDraftModal() 에서 HTML 주입 -->
      </div>
      <div class="modal-footer">
        <button type="button" id="goRegister" class="btn btn-dark">발주 등록으로 이동</button>
      </div>
    </div>
  </div>
</div>


<!-- 작업지시서 선택 모달 -->
<%@ include file="/WEB-INF/views/material/out/orderModal.jsp" %>

<script>var ctx='${pageContext.request.contextPath}';</script>
<script src="${pageContext.request.contextPath}/resources/js/materialOutbound.js"></script>