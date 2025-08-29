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
		        <input type="text" class="form-control input-sm" name="handledBy" id="handledBy" 
		               value="${handledBy != null ? handledBy : 'admin'}" readonly>
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
		  <input type="hidden" name="handledBy"   id="handledByHidden" value="${handledBy != null ? handledBy : 'admin'}">
		  
		  <div class="card">
		    <div class="card-header bg-light"><b>자재별 필요수량 & LOT 선택 (FEFO)</b></div>
		    <div class="card-body" style="padding:0;">
		    <div class="table-responsive">
		      <table class="table table-bordered table-condensed" style="margin:0;">
		        <thead>
		          <tr>
		            <th class="min-180">자재</th>
		            <th class="text-center">필요수량</th>
		            <th class="min-320">LOT 선택 (유통기한 빠른 순)</th>
		            <th class="text-center hidden-xs">예약수량(이번 WO)</th>
		            <th class="text-center hidden-xs">예상예약</th> 
		            <th class="text-center">선택합계</th>
		            <th class="text-center">부족</th>
		          </tr>
		        </thead>
		        <tbody id="materialLotBody"><!-- JS 로드 --></tbody>
		      </table>
		    </div>
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

<div class="modal fade" id="orderPreviewModal" tabindex="-1">
  <div class="modal-dialog modal-lg modal-dialog-scrollable">
    <div class="modal-content">
      <div class="modal-header" style="background-color: #1c355e; color: #ffffff;">
        <h5 class="modal-title">부족분 발주 미리보기</h5>
        <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
      </div>
      <div class="modal-body">
        <div id="opv-summary" class="mb-3 small text-muted"></div>
        <div id="opv-packs" class="mb-3"></div>
        <div id="opv-adjusted" class="mb-3"></div>
        <div id="opv-total" class="font-weight-bold"></div>
      </div>
      <div class="modal-footer">
        <button id="opv-cancel" type="button" class="btn btn-secondary" data-dismiss="modal">취소</button>
        <button id="opv-ok" type="button" class="btn btn-primary">확인</button>
      </div>
    </div>
  </div>
</div>



<!-- 작업지시서 선택 모달 -->
<%@ include file="/WEB-INF/views/material/out/orderModal.jsp" %>

<script>var ctx='${pageContext.request.contextPath}';</script>
<style>
  .min-180{min-width:180px;} .min-320{min-width:320px;}
  @media(max-width:767px){
    .table-condensed th, .table-condensed td{padding:6px 8px; font-size:12px; white-space:nowrap;}
    .lot-row .col-md-5, .lot-row .col-md-4, .lot-row .col-md-3{width:100%; float:none; margin-bottom:6px;}
    .btn-block-xs{display:block; width:100%;}
  }
</style>
<script src="${pageContext.request.contextPath}/resources/js/materialOutbound.js"></script>