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
			  <h3 class="font-weight-bold">수주상세 정보</h3>
              <h6 class="font-weight-normal mb-0">수주상세 정보 화면입니다. <span class="text-primary">강조쓰</span></h6>
			</div>
          
          <div class="col-md-12 grid-margin">
            <div class="row">
              <div class="contentbody"> 

    <!-- 수주 마스터 정보 -->
      <div id="table_content"  style="width:1200px;">
    <table class="table table-bordered mb-4" style="table-layout: fixed; width: 100%;">
    <colgroup>
    <col style="width:20%">
    <col style="width:30%">
    <col style="width:20%">
    <col style="width:30%">
  </colgroup>
        <tbody>
            <tr>
                <th class="table-active" style="width: 150px;">수주번호</th>
                <td>${order.clOrderNum}</td>
                <th class="table-active">거래처명</th>
                <td>${order.clientName}</td>
            </tr>
            
            <tr>
  <th class="table-active">담당자</th>    <td>${order.managerName}</td>
  <th class="table-active">전화번호</th>  <td>${order.managerTel}</td>
</tr>
<tr>
  <th class="table-active">배송주소</th>  <td>${order.deliveryAddress}</td>
  <th class="table-active">우편번호</th>  <td>${order.postCode}</td>
</tr>
            
            <tr>
                <th class="table-active">수주일자</th>
                <td><fmt:formatDate value="${order.clOrderDate}" pattern="yyyy-MM-dd"/></td>
                <th class="table-active">납기요청일</th>
                <td><fmt:formatDate value="${order.clDeliveryDate}" pattern="yyyy-MM-dd"/></td>
            </tr>
            <tr>
    <th class="table-active">수주상태</th>
    <td>
      <c:choose>
        <c:when test="${order.clOrderStatus == 'REQUESTED'}">
          <span style="color: #dc3545; font-weight: bold;">주문접수</span>
        </c:when>
        <c:when test="${order.clOrderStatus == 'CONFIRMED'}">
          <span style="color: #007bff; font-weight: bold;">확정</span>
        </c:when>
        <c:when test="${order.clOrderStatus == 'SHIPPED'}">
          <span style="color: #28a745; font-weight: bold;">출하</span>
        </c:when>
        <c:when test="${order.clOrderStatus == 'CANCELLED'}">
          <span style="color: #6c757d; font-weight: bold;">취소</span>
        </c:when>
        <c:otherwise>
          <span style="color: #6c757d;">알 수 없음</span>
        </c:otherwise>
      </c:choose>
    </td>
    <th class="table-active">메모</th>
    <td>${order.clOrderMemo}</td>
</tr>

        </tbody>
    </table>
 

    <!-- 수주 상세내역(제품별) -->
    <h4 class="mb-3">수주 상세 내역</h4>
    <table class="table table-striped table-bordered">
        <thead class="thead-light">
            <tr>
                <th class="text-center">No</th>
                <th class="text-center">제품명</th>
                <th class="text-center">수량</th>
                <th class="text-center">단가</th>
                <th class="text-center">합계</th>
                <th class="text-center">비고</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="detail" items="${detailList}" varStatus="vs">
                <tr>
                    <td class="text-center">${vs.index + 1}</td>
                    <td class="text-center">${detail.productName}</td>
                    <td class="text-center">${detail.orderQty}</td>
                    <td class="text-center">
                        <fmt:formatNumber value="${detail.unitPrice}" pattern="#,##0"/>
                    </td>
                    <td class="text-center">
                        <fmt:formatNumber value="${detail.orderQty * detail.unitPrice}" pattern="#,##0"/>
                    </td>
                    <td class="text-center">${detail.detailMemo}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
   </div>
    <div class="mt-4">
        <a href="${pageContext.request.contextPath}/clientorder/list" class="btn btn-secondary">목록으로</a>
    </div>
</div>
<!--  본문내용 끝 -->    
           
              <!-- 페이징 끝 -->
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