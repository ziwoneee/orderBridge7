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
			  <h3 class="font-weight-bold">고객사 목록</h3>
              <h6 class="font-weight-normal mb-0">고객사 목록 화면입니다. <span class="text-primary">강조쓰</span></h6>
			</div>
        
                <!-- 본문내용 시작 -->
                <!-- ✅ 검색창 -->
                <form action="/client/list" method="get" class="form-inline justify-content-between align-items-center mb-3">
                  <div class="form-group d-flex flex-wrap">
                    <input type="text" name="keyword" class="form-control mr-2 mb-2" style="min-width: 220px;"
                      value="${cri.keyword}" placeholder="고객사명, 대표자, 사업자번호" />

                    <select name="sortColumn" class="form-control mr-2 mb-2">
                      <option value="clientName" ${cri.sortColumn == 'clientName' ? 'selected' : ''}>고객사명</option>
                      <option value="businessNumber" ${cri.sortColumn == 'businessMumber' ? 'selected' : ''}>사업자등록번호</option>
                      <option value="regDate" ${cri.sortColumn == 'regDate' ? 'selected' : ''}>등록일</option>
                    </select>

                    <select name="sortOrder" class="form-control mr-2 mb-2">
                      <option value="asc" ${cri.sortOrder == 'asc' ? 'selected' : ''}>오름차순</option>
                      <option value="desc" ${cri.sortOrder == 'desc' ? 'selected' : ''}>내림차순</option>
                    </select>

                    <button type="submit" class="btn btn-outline-primary mr-2 mb-2">검색</button>
                    <a href="/client/register" class="btn btn-success mb-2">신규 등록</a>
                  </div>
                </form>
                
                <!-- ✅ 테이블 영역 -->
                <div id="table_content" style="width:1200px;">
                  <div class="table-responsive">
                    <table class="table table-hover table-bordered text-center align-middle" style="font-size: 14px;">
                      <thead class="thead-dark">
                        <tr>
                          <th style="width: 20%;">고객사명</th>
                          <th style="width: 20%;">사업자등록번호</th>
                          <th style="width: 15%;">대표자명</th>
                          <th style="width: 15%;">연락처</th>
                          <th style="width: 10%;">상태</th>
                          <th style="width: 10%;">상세</th>
                        </tr>
                      </thead>
                      <tbody>
                        <c:forEach var="client" items="${clientList}">
                          <tr>
                            <td class="text-left pl-3">${client.clientName}</td>
                            <td>${client.businessNumber}</td>
                            <td>${client.ceoName}</td>
                            <td>${client.clientTel}</td>
                            <td>
                              <span class="badge badge-${client.statusCode == 1 ? 'success' : 'secondary'}">
                                ${client.statusCode == 1 ? '활성' : '비활성'}
                              </span>
                            </td>
                            <td>
                              <button type="button" class="btn btn-sm btn-outline-info"
                                onclick="location.href='/client/detail?clientId=${client.clientId}'">
                                상세보기
                              </button>
                            </td>
                          </tr>
                        </c:forEach>
                        <c:if test="${empty clientList}">
                          <tr>
                            <td colspan="6" class="text-danger font-weight-bold">조회된 고객사가 없습니다.</td>
                          </tr>
                        </c:if>
                      </tbody>
                    </table>
                  </div>
                </div>
                
                <!--  본문내용 끝 -->    
         	 </div>   
                <!-- 페이징하실거면 여기서 시작 -->
                <div class="pagination">
                  <c:set var="pageCount" value="${totalCount / cri.perPageNum + (totalCount % cri.perPageNum > 0 ? 1 : 0)}" />
                  <c:forEach begin="1" end="${pageCount}" var="p">
                    <c:choose>
                      <c:when test="${p == cri.page}">
                        <strong>[${p}]</strong>
                      </c:when>
                      <c:otherwise>
                        <a href="/client/list?page=${p}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">${p}</a>
                      </c:otherwise>
                    </c:choose>
                  </c:forEach>
                </div>
              <!-- 페이징 끝 -->
        </div>
        <!-- content-wrapper 끝 -->
	  <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
     </div>
     <!-- 본문.jsp main-panel ends -->
  </div>   
  <!-- container-fluid page-body-wrapper 끝 -->
</div>
<!-- container-scroller 끝-->   
